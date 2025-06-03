package top.yms.storage.client.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yms.storage.client.StorageClient;
import top.yms.storage.em.MsgCd;
import top.yms.storage.em.StorageClientErrorCode;
import top.yms.storage.em.StorageClientException;
import top.yms.storage.entity.BaseResponse;
import top.yms.storage.entity.DeleteResp;
import top.yms.storage.entity.FileMetaVo;
import top.yms.storage.entity.UploadResp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class StorageClientImpl implements StorageClient {

    private static final Logger log = LoggerFactory.getLogger(StorageClientImpl.class);
    private FileClient fileClient;

    public StorageClientImpl(FileClient fileClient) {
        this.fileClient = fileClient;
    }

    @Override
    public UploadResp upload(File file) {
        try {
            return fileClient.upload(file).getData();
        } catch (IOException e) {
            log.error("upload error", e);
            throw new StorageClientException(StorageClientErrorCode.E_205001);
        }
    }

    @Override
    public UploadResp upload(InputStream is, String fileName) {
        try {
            return fileClient.upload(is, fileName).getData();
        } catch (IOException e) {
            log.error("upload stream error", e);
            throw new StorageClientException(StorageClientErrorCode.E_205001);
        }
    }

    @Override
    public FileMetaVo getFileMetaInfo(String fileId) {
        try {
            BaseResponse<FileMetaVo> fileMetaInfo = fileClient.getFileMetaInfo(fileId);
            if (MsgCd.FAILED.getCode().equals(fileMetaInfo.getCode())) {
                return null;
            }
            return fileMetaInfo.getData();
        } catch (IOException e) {
            log.error("getFileMetaInfo error", e);
            throw new StorageClientException(StorageClientErrorCode.E_205003);
        }
    }

    @Override
    public InputStream getFileStream(String fileId) {
        try {
            return fileClient.getInputStream(fileId);
        } catch (IOException e) {
            log.error("getFileStream error", e);
            throw new StorageClientException(StorageClientErrorCode.E_205002);
        }
    }

    @Override
    public void destroy(String fileId) {
        BaseResponse<DeleteResp> delRsp = null;
        try {
            delRsp = fileClient.delete(fileId);
        } catch (Exception e) {
            log.error("destroy error", e);
            throw new StorageClientException(StorageClientErrorCode.E_205005);
        }
        if (MsgCd.FAILED.getCode().equals(delRsp.getCode())) {
            throw new StorageClientException(StorageClientErrorCode.E_205005);
        }
        log.debug("fileId={} , destroy resp={}", fileId, delRsp);
    }
}
