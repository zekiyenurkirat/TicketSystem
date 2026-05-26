package com.ticketsystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Spring Boot uygulama context'inin hatasız yüklendiğini doğrular.
 * Bu test, Faz 1'de iskeleton'ın doğru kurulduğunu kanıtlar.
 */
@SpringBootTest
class TicketSystemApplicationTests {

    @Test
    void contextLoads() {
        // Spring context başarıyla yüklenirse bu test geçer.
        // Entity, Service, Controller geldikçe bu testlerin yanına
        // birim testler (unit test) eklenecektir.
    }

}
