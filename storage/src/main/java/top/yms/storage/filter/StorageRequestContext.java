package top.yms.storage.filter;

import java.time.LocalDateTime;

public class StorageRequestContext {

    /**
     * 系统标识
     */
    private  String sysId;

    /**
     * 请求标识
     */
    private  String reqId;

    /**
     * 请求时间
     */
    private LocalDateTime reqTime;


    public String getSysId() {
        return sysId;
    }

    public void setSysId(String sysId) {
        this.sysId = sysId;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public LocalDateTime getReqTime() {
        return reqTime;
    }

    public void setReqTime(LocalDateTime reqTime) {
        this.reqTime = reqTime;
    }
}
