package top.yms.storage.client.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.yms.storage.client.StorageClient;
import top.yms.storage.client.impl.FileClient;
import top.yms.storage.client.impl.FileClient2;
import top.yms.storage.client.impl.StorageClientImpl;

@Configuration
@EnableConfigurationProperties(StorageClientProperties.class)
public class StorageClientAutoConfiguration {

    private final static Logger log = LoggerFactory.getLogger(StorageClientAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public StorageClient storageClient(StorageClientProperties props) {
        log.info("storage sdk config={}", props);
//        FileClient fileClient = new FileClient(props.getBaseUrl());
        FileClient fileClient = new FileClient(props.getBaseUrl(), props.getToken(), props.getSysId());
        return new StorageClientImpl(fileClient);
    }
}
