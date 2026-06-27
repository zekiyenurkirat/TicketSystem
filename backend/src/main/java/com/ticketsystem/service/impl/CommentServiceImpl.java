package com.ticketsystem.service.impl;

import com.ticketsystem.core.exception.BusinessRuleException;
import com.ticketsystem.dto.request.AddCommentRequest;
import com.ticketsystem.entity.Comment;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.CommentType;
import com.ticketsystem.entity.enums.Role;
import com.ticketsystem.repository.CommentRepository;
import com.ticketsystem.service.CommentService;
import com.ticketsystem.service.TicketService;
import com.ticketsystem.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** {@link CommentService} arayüzünün varsayılan uygulaması. */
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TicketService ticketService;
    private final UserService userService;

    public CommentServiceImpl(CommentRepository commentRepository,
                              TicketService ticketService,
                              UserService userService) {
        this.commentRepository = commentRepository;
        this.ticketService = ticketService;
        this.userService = userService;
    }

    @Override
    @Transactional
    public Comment addComment(AddCommentRequest request) {
        Ticket ticket = ticketService.getTicketById(request.getTicketId());
        User author = userService.getUserById(request.getAuthorId());

        if (!author.isActive()) {
            throw new BusinessRuleException("Pasif kullanıcı yorum ekleyemez. id: " + request.getAuthorId());
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BusinessRuleException("Yorum içeriği boş olamaz.");
        }

        if (request.getType() == null) {
            throw new BusinessRuleException("Yorum tipi boş olamaz.");
        }

        Role authorRole = author.getRole();
        if (authorRole != Role.CUSTOMER && authorRole != Role.AGENT && authorRole != Role.MANAGER) {
            throw new BusinessRuleException("Tanımlanamayan rol: " + authorRole);
        }
        if (authorRole == Role.CUSTOMER && request.getType() == CommentType.INTERNAL) {
            throw new BusinessRuleException("CUSTOMER rolündeki kullanıcı INTERNAL yorum ekleyemez.");
        }
        if (authorRole == Role.AGENT) {
            if (ticket.getAssignedTo() == null
                    || !ticket.getAssignedTo().getId().equals(author.getId())) {
                throw new BusinessRuleException("Yalnızca size atanmış taleplere yorum ekleyebilirsiniz.");
            }
        }

        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setContent(request.getContent());
        comment.setType(request.getType());

        return commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> getCommentsForTicket(Long ticketId, Long requesterId) {
        User currentUser = getCurrentUser();
        Ticket ticket = ticketService.getTicketById(ticketId);

        if (currentUser.getRole() == Role.CUSTOMER) {
            return commentRepository.findByTicketAndType(ticket, CommentType.EXTERNAL)
                    .stream()
                    .sorted(Comparator.comparing(Comment::getCreatedAt))
                    .collect(Collectors.toList());
        } else if (currentUser.getRole() == Role.AGENT || currentUser.getRole() == Role.MANAGER) {
            return commentRepository.findByTicketOrderByCreatedAtAsc(ticket);
        } else {
            throw new BusinessRuleException("Tanımlanamayan rol: " + currentUser.getRole());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> getCommentsByAuthor(Long authorId) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == Role.CUSTOMER
                && !currentUser.getId().equals(authorId)) {
            throw new AccessDeniedException("Yalnızca kendi yorumlarınızı listeleyebilirsiniz.");
        }
        User author = userService.getUserById(authorId);
        return commentRepository.findByAuthor(author);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByEmail(email);
    }
}
