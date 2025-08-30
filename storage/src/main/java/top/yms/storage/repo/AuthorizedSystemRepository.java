package top.yms.storage.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import top.yms.storage.entity.AuthorizedSystem;

public interface AuthorizedSystemRepository extends ReactiveCrudRepository<AuthorizedSystem, Long> {

    Mono<AuthorizedSystem> findBySystemId(String systemId);

}
