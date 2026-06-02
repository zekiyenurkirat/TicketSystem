package com.ticketsystem.entity.enums;

/** Atama isteğinin yaşam döngüsü durumu. */
public enum AssignmentRequestStatus {

    /** İstek oluşturuldu, manager henüz işlem yapmadı. */
    PENDING,

    /** Manager isteği onayladı; ticket ilgili agent'a atandı. */
    APPROVED,

    /** Manager isteği reddetti; ticket'a dokunulmadı. */
    REJECTED
}
