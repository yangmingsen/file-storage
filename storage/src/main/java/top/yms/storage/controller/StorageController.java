package top.yms.storage.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import top.yms.storage.entity.BaseResponse;
import top.yms.storage.entity.DeleteResp;
import top.yms.storage.entity.FileMetaVo;
import top.yms.storage.entity.UploadResp;
import top.yms.storage.service.StorageService;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/file/storage")
public class StorageController {

    @Resource
    private StorageService storageService;

    @PostMapping("/upload")
    public Mono<BaseResponse<UploadResp>> upload(@RequestPart("file") FilePart filePart) {
        // 使用 Reactor 异步流处理上传
        return storageService.storage(filePart);
    }

    @GetMapping("/download/{id}")
    public Mono<ResponseEntity<?>> download(@PathVariable String id) {
        return storageService.download(id);
    }

    @GetMapping("/preview/{id}")
    public Mono<ResponseEntity<?>> preview(@PathVariable String id) {
        return storageService.preview(id);
    }


    @DeleteMapping("/delete/{id}")
    public Mono<BaseResponse<DeleteResp>> delete(@PathVariable String id) {
        return storageService.delete(id);
    }

    @GetMapping("/get-meta-info/{id}")
    public Mono<BaseResponse<FileMetaVo>> getFileMetaInfo(@PathVariable String id) {
        return storageService.getFileMeta(id);
    }
}

