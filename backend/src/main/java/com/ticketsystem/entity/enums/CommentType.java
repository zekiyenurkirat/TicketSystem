package com.ticketsystem.entity.enums;

/**
 * Ticket yorumlarının görünürlük türünü tanımlar.
 *
 * INTERNAL : Yalnızca AGENT ve MANAGER tarafından görülebilir.
 * EXTERNAL : Tüm taraflarca görülebilir; CUSTOMER dahil.
 */
public enum CommentType {
    INTERNAL,
    EXTERNAL
}
