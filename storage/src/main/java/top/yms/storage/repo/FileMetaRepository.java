package top.yms.storage.repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import top.yms.storage.entity.FileMeta;

public interface FileMetaRepository extends ReactiveCrudRepository<FileMeta, Long> {

    Mono<FileMeta> findByFileId(String fileId);
}
