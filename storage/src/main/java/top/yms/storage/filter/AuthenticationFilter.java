package top.yms.storage.filter;

import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import top.yms.storage.cache.RequestContextCache;
import top.yms.storage.constants.StorageConstants;
import top.yms.storage.entity.RequestLog;
import top.yms.storage.repo.RequestLogRepository;
import top.yms.storage.service.StorageValidateService;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Component
public class AuthenticationFilter implements WebFilter {

    private final static Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Resource
    private StorageValidateService storageValidateService;

    @Resource
    private RequestLogRepository requestLogRepository;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst(StorageConstants.REQ_TOKEN);
        String sysId = exchange.getRequest().getHeaders().getFirst(StorageConstants.SYS_ID);
        log.info("token={}, sysId={}", token, sysId);
        String reqId = exchange.getRequest().getHeaders().getFirst(StorageConstants.REQ_ID);
        StorageRequestContext src = new StorageRequestContext();
        src.setReqId(reqId);
        src.setSysId(sysId);
        src.setReqTime(LocalDateTime.now());
        //put val
        RequestContextCache.put(reqId, src);
        ValidateInfo validateInfo = ValidateInfo.packToken(token, sysId);
        return storageValidateService.validate(validateInfo).flatMap(validateRsp -> {
            if (!validateRsp) {
                // token 无效，直接拒绝
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED); // 401
                return exchange.getResponse().setComplete(); // 结束请求，不继续
            }

            //save to log
            // 获取请求路径
            String path = exchange.getRequest().getPath().value();
            RequestLog requestLog = new RequestLog();
            requestLog.setReqPath(path);
            requestLog.setReqId(reqId);
            requestLog.setReqSysId(sysId);
            requestLog.setCreateTime(LocalDateTime.now());
            return requestLogRepository.save(requestLog);
        }).then(chain.filter(exchange));

    }

}
