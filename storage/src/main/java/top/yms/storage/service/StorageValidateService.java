package top.yms.storage.service;

import reactor.core.publisher.Mono;
import top.yms.storage.filter.ValidateInfo;

public interface StorageValidateService {
    /**
     * validate
     * @param validateInfo
     * @return true-pass, else false
     */
    Mono<Boolean> validate(ValidateInfo validateInfo);

}
