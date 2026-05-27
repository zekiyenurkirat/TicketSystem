package com.ticketsystem.service.impl;

import com.ticketsystem.entity.Comment;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.enums.CommentType;
import com.ticketsystem.entity.enums.Role;
import com.ticketsystem.repository.CommentRepository;
import com.ticketsystem.service.CommentService;
import com.ticketsystem.service.TicketService;
import com.ticketsystem.service.UserService;
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
    public Comment addComment(Long ticketId, Long authorId, String content, CommentType type) {
        Ticket ticket = ticketService.getTicketById(ticketId);
        User author = userService.getUserById(authorId);

        if (!author.isActive()) {
            throw new RuntimeException("Pasif kullanıcı yorum ekleyemez. id: " + authorId);
        }

        if (content == null || content.isBlank()) {
            throw new RuntimeException("Yorum içeriği boş olamaz.");
        }

        if (type == null) {
            throw new RuntimeException("Yorum tipi boş olamaz.");
        }

        Role authorRole = author.getRole();
        if (authorRole != Role.CUSTOMER && authorRole != Role.AGENT && authorRole != Role.MANAGER) {
            throw new RuntimeException("Tanımlanamayan rol: " + authorRole);
        }
        if (authorRole == Role.CUSTOMER && type == CommentType.INTERNAL) {
            throw new RuntimeException("CUSTOMER rolündeki kullanıcı INTERNAL yorum ekleyemez.");
        }

        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setContent(content);
        comment.setType(type);

        return commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> getCommentsForTicket(Long ticketId, Long requesterId) {
        Ticket ticket = ticketService.getTicketById(ticketId);
        User requester = userService.getUserById(requesterId);

        Role requesterRole = requester.getRole();

        if (requesterRole == Role.CUSTOMER) {
            return commentRepository.findByTicketAndType(ticket, CommentType.EXTERNAL)
                    .stream()
                    .sorted(Comparator.comparing(Comment::getCreatedAt))
                    .collect(Collectors.toList());
        } else if (requesterRole == Role.AGENT || requesterRole == Role.MANAGER) {
            return commentRepository.findByTicketOrderByCreatedAtAsc(ticket);
        } else {
            throw new RuntimeException("Tanımlanamayan rol: " + requesterRole);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> getCommentsByAuthor(Long authorId) {
        User author = userService.getUserById(authorId);
        return commentRepository.findByAuthor(author);
    }
}
