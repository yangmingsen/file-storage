package top.yms.storage.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import top.yms.storage.component.IdWorker;
import top.yms.storage.constants.StorageConstants;
import top.yms.storage.em.MsgCd;
import top.yms.storage.entity.*;
import top.yms.storage.repo.FileMetaRepository;
import top.yms.storage.service.StorageService;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class StorageServiceImpl implements StorageService {

    private final static Logger log = LoggerFactory.getLogger(StorageServiceImpl.class);

    @Value("${storage.base.path}")
    private String basePath;

    @Value("${storage.base.url}")
    private String baseUrl;

    @Value("${storage.write-strategy:DATE}")
    private String writeStrategy;

    @Resource
    private FileMetaRepository fileMetaRepository;

    @Resource
    private IdWorker idWorker;

    private String getWriteStrategy() {
        return writeStrategy;
    }


    @Override
    public Mono<BaseResponse<UploadResp>> storage(FilePart filePart) {
        long id = idWorker.nextId();
        String filename = filePart.filename();
        String finalFileName = id + "_" + filename;
        MediaType contentType = filePart.headers().getContentType();
        log.debug("file={}, content type = {}", finalFileName, contentType);
        try {
            if (contentType == null) {
                String guessedType = Files.probeContentType(Paths.get(filePart.filename()));
                if (guessedType != null) {
                    contentType = MediaType.parseMediaType(guessedType);
                } else {
                    contentType = MediaType.APPLICATION_OCTET_STREAM;
                }
            }
        } catch (Exception e) {
            log.error("get file type error", e);
        }
        String fileType = contentType.toString();
        String tmpRelativePath;
        Path dirPath;
        //path set
        if (StorageConstants.HASH_STRATEGY.equals(getWriteStrategy())) {
            // 基于 hash 的策略：800 个目录
            int hashBucket = Math.abs(filename.hashCode()) % 800;
            String hashDir = String.format("hash/%03d", hashBucket); // 目录形如 hash/000 ~ hash/799
            dirPath = Paths.get(basePath, hashDir);
            tmpRelativePath = hashDir + "/" + finalFileName;
        } else {
            String datePath = LocalDate.now().toString().replace("-", "/");
            dirPath = Paths.get(basePath, datePath);
            tmpRelativePath = datePath+"/"+finalFileName;
        }
        log.debug("relativePath={}", tmpRelativePath);
        final String relativePath = tmpRelativePath;
        Path filePath = dirPath.resolve(finalFileName);
        File file = filePath.toFile();
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        MessageDigest md5Digest;
        try {
            md5Digest = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            return Mono.error(e);
        }
        return filePart.content()
                .publishOn(Schedulers.boundedElastic())
                .reduce(new long[]{0}, (acc, buffer) -> {
                    byte[] bytes = new byte[buffer.readableByteCount()];
                    buffer.read(bytes);
                    try (FileOutputStream fos = new FileOutputStream(file, true);
                         FileChannel channel = fos.getChannel()) {
                        channel.write(ByteBuffer.wrap(bytes));
                        md5Digest.update(bytes);
                        acc[0] += bytes.length;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return acc;
                })
                .flatMap(acc -> {
                    FileMeta metadata = new FileMeta();
                    metadata.setName(filename);
                    metadata.setPath(relativePath);
                    metadata.setFileId(id+"");
                    metadata.setSize(acc[0]);
                    metadata.setType(fileType);
                    metadata.setCreateTime(LocalDateTime.now());
                    metadata.setMd5(bytesToHex(md5Digest.digest()));
                    UploadResp uploadResp = new UploadResp();
                    uploadResp.setFileId(id+"");
                    uploadResp.setViewUrl(baseUrl+"preview/"+id);
                    uploadResp.setDownloadUrl(baseUrl+"download/"+id);
                    BaseResponse<UploadResp> rsp = new BaseResponse<>(MsgCd.SUCCESS, uploadResp);
                    return fileMetaRepository.save(metadata).thenReturn(rsp);
                });
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }


    public Mono<ResponseEntity<?>> download(String fileId) {
        return fileMetaRepository.findByFileId(fileId)
                .flatMap(meta -> {
                    Path filePath = Paths.get(basePath+meta.getPath());
                    org.springframework.core.io.Resource resource = new FileSystemResource(filePath.toFile());
                    if (!resource.exists()) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return Mono.just(ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + meta.getName() + "\"")
                            .contentType(MediaType.parseMediaType(meta.getType()))
                            .contentLength(filePath.toFile().length())
                            .body(resource));
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    public Mono<ResponseEntity<?>> preview( String fileId) {
        return fileMetaRepository.findByFileId(fileId)
                .flatMap(meta -> {
                    Path filePath = Paths.get(basePath+meta.getPath());
                    org.springframework.core.io.Resource resource = new FileSystemResource(filePath.toFile());
                    if (!resource.exists()) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return Mono.just(ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(meta.getType()))
                            .contentLength(filePath.toFile().length())
                            .body(resource));
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<BaseResponse<DeleteResp>> delete(String fileId) {
        return fileMetaRepository.findByFileId(fileId)
                .flatMap(meta -> {
                    Path filePath = Paths.get(basePath+meta.getPath());
                    try {
                        Files.deleteIfExists(filePath);
                    } catch (IOException e) {
                        log.error("delete error", e);
                        return Mono.error(e);
                    }
                    BaseResponse<DeleteResp> baseResponse = BaseResponse.success(new DeleteResp(fileId));
                    return fileMetaRepository.delete(meta).then(Mono.just(baseResponse));
                })
                .defaultIfEmpty(BaseResponse.failed(new DeleteResp(fileId)));
    }

    @Override
    public Mono<BaseResponse<FileMetaVo>> getFileMeta(String fileId) {
        return fileMetaRepository.findByFileId(fileId).flatMap(meta -> {
            FileMetaVo fileMetaVo = new FileMetaVo();
            BeanUtils.copyProperties(meta, fileMetaVo);
            BaseResponse<FileMetaVo> baseResponse = new BaseResponse<>(MsgCd.SUCCESS, fileMetaVo);
            return Mono.just(baseResponse);
        }).defaultIfEmpty(new BaseResponse<>(MsgCd.FAILED, new FileMetaVo()));
    }
}
