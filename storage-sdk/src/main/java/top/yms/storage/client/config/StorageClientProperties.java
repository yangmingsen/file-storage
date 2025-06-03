package top.yms.storage.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.client")
public class StorageClientProperties {

    // 默认配置
    private String host = "localhost";
    private int port = 9005;

    public String getBaseUrl() {
        return "http://" + host + ":" + port;
    }

    // getter / setter
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    @Override
    public String toString() {
        return "StorageClientProperties{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
