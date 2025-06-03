package top.yms.storage.client;

import top.yms.storage.entity.BaseResponse;
import top.yms.storage.entity.FileMetaVo;
import top.yms.storage.entity.UploadResp;

import java.io.File;
import java.io.InputStream;

public interface StorageClient {

    /**
     * 上传一个文件
     * @param file
     * @return
     */
    UploadResp upload(File file);

    UploadResp upload(InputStream is, String fileName);

    FileMetaVo getFileMetaInfo(String fileId);

    InputStream getFileStream(String fileId);

    void destroy(String fileId);

}
