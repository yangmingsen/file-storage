package top.yms.storage.service.impl;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.yms.storage.filter.AuthenticationFilter;
import top.yms.storage.filter.ValidateInfo;
import top.yms.storage.repo.AuthorizedSystemRepository;
import top.yms.storage.service.StorageValidateService;

import javax.annotation.Resource;

@Service
public class StorageValidateImpl implements StorageValidateService {

    @Resource
    private AuthorizedSystemRepository authorizedSystemRepository;

    @Override
    public Mono<Boolean> validate(ValidateInfo validateInfo) {
        return authorizedSystemRepository.findBySystemId(validateInfo.getSysId()).flatMap(as -> {
            String authToken = as.getAuthToken();
            boolean res = false;
            if (validateInfo.getToken().equals(authToken)) {
                res = true;
            }
            return Mono.just(res);
        }).defaultIfEmpty(Boolean.FALSE);
    }
}
