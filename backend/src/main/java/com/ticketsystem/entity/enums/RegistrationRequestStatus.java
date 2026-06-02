package com.ticketsystem.entity.enums;

/** Kayıt talebinin yaşam döngüsü durumu. */
public enum RegistrationRequestStatus {

    /** Talep oluşturuldu, manager henüz işlem yapmadı. */
    PENDING,

    /** Manager talebi onayladı; kullanıcı hesabı aktif olarak oluşturuldu. */
    APPROVED,

    /** Manager talebi reddetti; kullanıcı hesabı oluşturulmadı. */
    REJECTED
}
