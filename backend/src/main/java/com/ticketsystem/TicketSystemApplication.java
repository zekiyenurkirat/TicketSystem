package com.ticketsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TicketSystem uygulamasının başlangıç noktası.
 *
 * @SpringBootApplication anotasyonu üç anotasyonu bir arada barındırır:
 * - @Configuration       → Bu sınıfın bir konfigürasyon kaynağı olduğunu belirtir.
 * - @EnableAutoConfiguration → Spring Boot'un bağımlılıklara göre otomatik ayar yapmasını sağlar.
 * - @ComponentScan       → com.ticketsystem paketi altındaki tüm bileşenleri tarar.
 */
@SpringBootApplication
public class TicketSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketSystemApplication.class, args);
    }

}
