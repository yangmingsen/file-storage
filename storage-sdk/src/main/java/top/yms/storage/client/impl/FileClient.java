package top.yms.storage.client.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import top.yms.storage.constants.StorageConstants;
import top.yms.storage.entity.BaseResponse;
import top.yms.storage.entity.DeleteResp;
import top.yms.storage.entity.FileMetaVo;
import top.yms.storage.entity.UploadResp;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class FileClient {

    private final String serverUrl;
    private final String token;
    private final String sysId;

    private final ObjectMapper mapper;

    public FileClient(String serverUrl, String token, String sysId) {
        this.serverUrl = serverUrl + "/file/storage/";
        this.token = token;
        this.sysId = sysId;

        this.mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private String getReqId() {
        return UUID.randomUUID().toString().replace("-","");
    }

    // -------------------- 公共方法 --------------------
    private void setCommonHeaders(HttpURLConnection conn) {
        if (token != null) conn.setRequestProperty(StorageConstants.REQ_TOKEN, token);
        conn.setRequestProperty(StorageConstants.REQ_ID, getReqId());
        if (sysId != null) conn.setRequestProperty(StorageConstants.SYS_ID, sysId);
        conn.setRequestProperty("Accept", "application/json");
    }

    private <T> BaseResponse<T> doRequest(String path, String method, RequestBodyWriter bodyWriter,
                            TypeReference<BaseResponse<T>> typeRef) throws IOException {
        URL url = new URL(serverUrl + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setDoOutput(bodyWriter != null);
        setCommonHeaders(conn);
        if (bodyWriter != null) {
            try (OutputStream out = conn.getOutputStream()) {
                bodyWriter.write(out);
            }
        }
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Request failed: HTTP " + responseCode);
        }
        try (InputStream is = conn.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return mapper.readValue(reader, typeRef);
        }
    }

    // -------------------- SDK 接口 --------------------
    public BaseResponse<FileMetaVo> getFileMetaInfo(String fileId) throws IOException {
        return doRequest(
                "get-meta-info/" + fileId,
                "GET",
                null,
                new TypeReference<BaseResponse<FileMetaVo>>() {}
        );
    }

    public BaseResponse<DeleteResp> delete(String fileId) throws IOException {
        return doRequest(
                "delete/" + fileId,
                "DELETE",
                null,
                new TypeReference<BaseResponse<DeleteResp>>() {}
        );
    }

    public BaseResponse<UploadResp> upload(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            return upload(in, file.getName());
        }
    }

    public BaseResponse<UploadResp> upload(InputStream input, String fileName) throws IOException {
        String boundary = UUID.randomUUID().toString();
        HttpURLConnection conn = (HttpURLConnection) new URL(serverUrl + "upload").openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        //set common
        setCommonHeaders(conn);
        //
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        // 根据文件名探测Content-Type，没探测到用 application/octet-stream
        String contentType = Files.probeContentType(Paths.get(fileName));
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        try (OutputStream out = conn.getOutputStream()) {
            // 写入分界线
            out.write(("--" + boundary + "\r\n").getBytes());
            // 文件部分头
            out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n").getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes());
            // 写文件内容
            byte[] buffer = new byte[8192];
            int len;
            while ((len = input.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.write("\r\n".getBytes());  // 结束文件内容后换行
            // 写结束分界线
            out.write(("--" + boundary + "--\r\n").getBytes());
        } finally {
            input.close();
        }
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try ( InputStream is = conn.getInputStream();
                  BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(reader, new TypeReference<BaseResponse<UploadResp>>() {});
            }
        } else {
            throw new IOException("Upload failed with code: " + responseCode);
        }
    }

    public BaseResponse<UploadResp> upload_bak(InputStream input, String fileName) throws IOException {
        String boundary = UUID.randomUUID().toString();
        return doRequest(
                "upload",
                "POST",
                out -> {
                    // 写入分界线和文件头
                    out.write(("--" + boundary + "\r\n").getBytes());
                    out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n").getBytes(StandardCharsets.UTF_8));

                    String contentType = Files.probeContentType(Paths.get(fileName));
                    if (contentType == null) contentType = "application/octet-stream";

                    out.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes());

                    // 写文件内容
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = input.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    out.write("\r\n".getBytes());
                    out.write(("--" + boundary + "--\r\n").getBytes());
                },
                new TypeReference<BaseResponse<UploadResp>>() {}
        );
    }

    public void download(String fileId, File targetFile) throws IOException {
        URL url = new URL(serverUrl + "download/" + fileId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        setCommonHeaders(conn);

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Download failed: HTTP " + responseCode);
        }

        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }

    public void preview(String fileId, OutputStream outputStream) throws IOException {
        URL url = new URL(serverUrl + "preview/" + fileId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        setCommonHeaders(conn);

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Preview failed: HTTP " + responseCode);
        }

        try (InputStream in = conn.getInputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        }
    }

    public InputStream getInputStream(String fileId) throws IOException {
        URL url = new URL(serverUrl + "preview/" + fileId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        setCommonHeaders(conn);

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Preview failed: HTTP " + responseCode);
        }
        return conn.getInputStream();
    }

    // -------------------- 功能接口 --------------------
    @FunctionalInterface
    private interface RequestBodyWriter {
        void write(OutputStream out) throws IOException;
    }
}

