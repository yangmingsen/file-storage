package top.yms.storage.utils;


import top.yms.storage.component.IdWorker;
import top.yms.storage.config.SpringContext;

public class IdWorkerUtils {
    public static long getId() {
       return SpringContext.getBean(IdWorker.class).nextId();
    }
}
