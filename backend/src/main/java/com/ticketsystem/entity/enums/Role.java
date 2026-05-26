package com.ticketsystem.entity.enums;

/**
 * Sistemdeki kullanıcı rollerini tanımlar.
 * Her kullanıcının yalnızca bir rolü olabilir.
 *
 * CUSTOMER : Destek talebi oluşturan son kullanıcı.
 * AGENT    : Talepleri işleyen destek personeli.
 * MANAGER  : Raporları izleyen ve sistemi yöneten yönetici.
 */
public enum Role {
    CUSTOMER,
    AGENT,
    MANAGER
}
