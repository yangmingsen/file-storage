package top.yms.storage.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yms.storage.filter.StorageRequestContext;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestContextCache {

    private static final Logger log = LoggerFactory.getLogger(RequestContextCache.class);

    private static final Map<String, CacheData> srcCache = new ConcurrentHashMap<>();

    private static long expireTime = 60;

    private static int cacheSize() {
        return srcCache.size();
    }

    public static long getExpireTime() {
        return expireTime;
    }

    private static class CacheData {
        private StorageRequestContext src;
        private LocalDateTime expireTime;

        public CacheData(StorageRequestContext src, LocalDateTime expireTime) {
            this.src = src;
            this.expireTime = expireTime;
        }

        public StorageRequestContext getSrc() {
            return src;
        }

        public void setSrc(StorageRequestContext src) {
            this.src = src;
        }

        public LocalDateTime getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(LocalDateTime expireTime) {
            this.expireTime = expireTime;
        }

        /**
         * 过期 true, 未过期 false
         * @return
         */
        public boolean isExpire() {
            LocalDateTime now = LocalDateTime.now();
            if (!expireTime.isAfter(now)) {
                //过期
                return true;
            }
            return false;
        }
    }

    private static StorageRequestContext doPut(String key, StorageRequestContext value) {
        LocalDateTime futureTime = LocalDateTime.now().plusSeconds(getExpireTime());
        CacheData cacheData = new CacheData(value, futureTime);
        CacheData cd = srcCache.put(key, cacheData);
        if (cd != null) {
            return cd.getSrc();
        }
        return null;
    }

    private static StorageRequestContext doGet(String key) {
        CacheData cd = srcCache.get(key);
        if (cd == null) {
            return null;
        }
        if (cd.isExpire()) {
            return null;
        }
        return cd.getSrc();
    }

    public static StorageRequestContext put(String key, StorageRequestContext value) {
        return doPut(key, value);
    }

    public static StorageRequestContext clearAfterGet(String key) {
        StorageRequestContext requestContext = doGet(key);
        if (requestContext != null) {
            srcCache.remove(key);
        }
        return requestContext;
    }

    private static void doRemove(String key) {
        srcCache.remove(key);
    }

    public static StorageRequestContext get(String key) {
        return doGet(key);
    }

    public static void clearCache() {
        log.info("start cache clear......");
        while (true) {
            try {
                if (cacheSize() > 0) {
                    for (String key : srcCache.keySet()) {
                        CacheData cd = srcCache.get(key);
                        if (cd.isExpire()) {
                            log.info("remove expire key={}", key);
                            doRemove(key);
                        }
                    }
                }
                Thread.sleep(1000*60);
            } catch (Exception e) {
                log.error("clearCache error", e);
            }
        }
    }

    static {
        Thread clearTask = new Thread(RequestContextCache::clearCache);
        clearTask.start();
    }
}
