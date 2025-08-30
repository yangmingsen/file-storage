package top.yms.storage.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import top.yms.storage.constants.StorageConstants;

import java.time.LocalDateTime;

public class StorageRequestContextHolder {

    private final static Logger log = LoggerFactory.getLogger(StorageRequestContextHolder.class);

    private static final String CONTEXT_KEY = StorageConstants.REQUEST_CONTEXT;

    public static Context with(StorageRequestContext ctx) {
        return Context.of(CONTEXT_KEY, ctx);
    }

    public static Mono<StorageRequestContext> current() {
        return Mono.subscriberContext()
                .map(ctx -> {
                    StorageRequestContext o = ctx.get(StorageConstants.REQUEST_CONTEXT);
                    log.info("o StorageRequestContext={}", o);
                    return o;
                });
    }

    public static Mono<String> reqId() {
        return current().map(StorageRequestContext::getReqId);
    }

    public static Mono<String> sysId() {
        return current().map(StorageRequestContext::getSysId);
    }

    public static Mono<LocalDateTime> reqTime() {
        return current().map(StorageRequestContext::getReqTime);
    }
}
