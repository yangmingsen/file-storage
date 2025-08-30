package top.yms.storage;


import com.fasterxml.jackson.databind.ObjectMapper;
import top.yms.storage.client.impl.FileClient2;
import top.yms.storage.entity.BaseResponse;
import top.yms.storage.entity.FileMetaVo;
import top.yms.storage.entity.UploadResp;

import java.io.File;
import java.io.IOException;

/**
 * Unit test for simple App.
 */
public class AppTest
{

   // @org.junit.jupiter.api.Test
    public void test() {
        File file = new File("C:\\Users\\yangmingsen\\Downloads\\storage\\pom.xml");
        FileClient2 fileClient = new FileClient2("http://localhost:9005");
        try {
            BaseResponse<UploadResp> upload = fileClient.upload(file);
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(upload);
            System.out.println(jsonString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // @org.junit.jupiter.api.Test
    public void testGetMetaInfo() {
        FileClient2 fileClient = new FileClient2("http://localhost:9005");
        try {
            BaseResponse<FileMetaVo> info = fileClient.getFileMetaInfo(192841468683196825L+"");
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(info);
            System.out.println(jsonString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
