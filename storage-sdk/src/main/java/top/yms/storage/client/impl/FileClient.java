package top.yms.storage.client.impl;

// FileServer.java
// Netty 文件服务 + SDK 支持 + MySQL 元数据存储 + 日期路径存储 + 支持 InputStream 上传 + 支持文件预览 + 删除接口

// ... （省略服务端代码，保留不变）

// === SDK 客户端代码（可独立为 FileClient.java） ===

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import top.yms.storage.entity.BaseResponse;
import top.yms.storage.entity.DeleteResp;
import top.yms.storage.entity.FileMetaVo;
import top.yms.storage.entity.UploadResp;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class FileClient {
    private final String serverUrl;

    public FileClient(String serverUrl) {
        this.serverUrl = serverUrl+"/file/storage/";
    }

    public BaseResponse<FileMetaVo> getFileMetaInfo(String fileId) throws IOException {
        String urlStr = serverUrl + "get-meta-info/" + fileId;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("请求失败，HTTP代码：" + responseCode);
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try (InputStream is = conn.getInputStream();
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return mapper.readValue(reader, mapper.getTypeFactory()
                    .constructParametricType(BaseResponse.class, FileMetaVo.class));
        }
    }

    public BaseResponse<DeleteResp> delete(String fileId) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(serverUrl + "delete/" + fileId).openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Accept", "application/json");
        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("请求失败，HTTP代码：" + responseCode);
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try (InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return mapper.readValue(reader, mapper.getTypeFactory()
                    .constructParametricType(BaseResponse.class, DeleteResp.class));
        }
    }


    public BaseResponse<UploadResp>  upload(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            return upload(in, file.getName());
        }
    }

    public BaseResponse<UploadResp> upload(InputStream input, String fileName) throws IOException {
        String boundary = UUID.randomUUID().toString();
        HttpURLConnection conn = (HttpURLConnection) new URL(serverUrl + "upload").openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
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
//            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            //RFC 5987 标准：filename*=UTF-8'' + URL 编码的文件名。
//            out.write(("Content-Disposition: form-data; name=\"file\"; filename*=UTF-8''" + encodedFileName + "\r\n").getBytes(StandardCharsets.UTF_8));
            //使用 rfc 5987 发现 服务端无法解析 出现 java.lang.IllegalStateException: argument type mismatch
            // Spring WebFlux 无法把当前请求中的 Part 自动映射到 @RequestPart("file") FilePart filePart 方法参数上。 (value=Part 'file', headers=[content-disposition:"form-data; name="file"; filename*=UTF-8''xxx.xlsx"])
            // filename*=UTF-8''xxx.xlsx RFC 5987 格式的文件名写法，但 Spring WebFlux（尤其是 Spring Boot 2.3.x 使用的 SynchronossPartHttpMessageReader）不支持这种写法的自动绑定！
            //
            //Spring Boot 2.0 ~ 2.5 默认使用 SynchronossPartHttpMessageReader
            //它基于 Synchronoss NIO multipart parser —— 不支持 filename*= 解析为 FilePart
            //到 Spring Boot 2.5 之后，WebFlux 才改成用 DefaultPartHttpMessageReader
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

    public void download(String fileId, File targetFile) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(serverUrl + "download/" + fileId).openConnection();
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (InputStream in = conn.getInputStream();
                 FileOutputStream out = new FileOutputStream(targetFile)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }
        } else {
            throw new IOException("Download failed with code: " + responseCode);
        }
    }

    public void preview(String fileId, OutputStream outputStream) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(serverUrl + "preview/" + fileId).openConnection();
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (InputStream in = conn.getInputStream()) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
            }
        } else {
            throw new IOException("Preview failed with code: " + responseCode);
        }
    }

    public InputStream getInputStream(String fileId) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(serverUrl + "preview/" + fileId).openConnection();
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            return conn.getInputStream();
        } else {
            throw new IOException("Preview failed with code: " + responseCode);
        }
    }


}

// 示例用法：
// FileClient client = new FileClient("http://localhost:8080");
// try (InputStream in = new FileInputStream("example.jpg")) {
//     String fileId = client.upload(in, "example.jpg");
//     client.download(fileId, new File("downloaded.jpg"));
//     client.preview(fileId, System.out); // 用于调试或转发给浏览器输出流
//     client.delete(fileId);
// }

