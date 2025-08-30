package top.yms.storage.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.client")
public class StorageClientProperties {

    // 默认配置
    private String host = "localhost";

    private int port = 9005;

    private String sysId;

    private String token;

    public String getBaseUrl() {
        return "http://" + host + ":" + port;
    }

    // getter / setter
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getSysId() {
        return sysId;
    }

    public void setSysId(String sysId) {
        this.sysId = sysId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "StorageClientProperties{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", sysId='" + sysId + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
