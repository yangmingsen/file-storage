package top.yms.storage.service;

import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.yms.storage.entity.BaseResponse;
import top.yms.storage.entity.DeleteResp;
import top.yms.storage.entity.FileMetaVo;
import top.yms.storage.entity.UploadResp;

public interface StorageService {

    Mono<BaseResponse<UploadResp>> storage(FilePart filePart, ServerWebExchange exchange);

    Mono<ResponseEntity<?>> download(String fileId);

    Mono<ResponseEntity<?>> preview( String fileId);

    Mono<BaseResponse<DeleteResp>> delete(String fileId);

    Mono<BaseResponse<FileMetaVo>> getFileMeta(String fileId);
}
