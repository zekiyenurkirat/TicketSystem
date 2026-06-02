package com.ticketsystem.opensearch;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * OpenSearch bağlantı yapılandırması.
 * Indexleme işlemleri OpenSearch REST API üzerinden RestTemplate ile yapılır;
 * opensearch-java SDK kullanılmaz — gereksiz AWS SDK transitive bağımlılıklarından kaçınmak için.
 */
@Configuration
public class OpenSearchConfig {

    @Bean
    public RestTemplate openSearchRestTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
