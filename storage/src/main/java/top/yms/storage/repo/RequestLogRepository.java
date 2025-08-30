package top.yms.storage.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import top.yms.storage.entity.RequestLog;

public interface RequestLogRepository  extends ReactiveCrudRepository<RequestLog, Long> {

}
