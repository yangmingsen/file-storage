package top.yms.storage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.yms.storage.component.IdWorker;

@Configuration
public class BeanConfig {
    @Bean
    public IdWorker idWorker() {
        return new IdWorker(0,2);
    }
}
