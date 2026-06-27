package com.ticketsystem.opensearch;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * OpenSearch bağlantı yapılandırması.
 * Indexleme işlemleri OpenSearch REST API üzerinden RestTemplate ile yapılır;
 * opensearch-java SDK kullanılmaz — gereksiz AWS SDK transitive bağımlılıklarından kaçınmak için.
 *
 * SimpleClientHttpRequestFactory: blocking Java SE HTTP — NIO gerektirmez.
 * Spring Boot 3.2+ RestTemplateBuilder artık JdkClientHttpRequestFactory (NIO tabanlı)
 * kullanabileceğinden burada açıkça SimpleClientHttpRequestFactory belirlenir.
 */
@Configuration
public class OpenSearchConfig {

    @Bean
    public RestTemplate openSearchRestTemplate() {
        return new RestTemplate(new SimpleClientHttpRequestFactory());
    }
}
