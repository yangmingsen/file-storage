package top.yms.storage.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Date;

/**
 *
 * create by yangmingsen
 * t_request_log
 */
@Table("t_request_log")
public class RequestLog {
    /**
     * pk
     */
    @Id
    private Long id;

    /**
     * 第三方系统请求id
     */
    private String reqId;

    /**
     * req sys id
     */
    private String reqSysId;

    /**
     * 请求时间
     */
    private LocalDateTime createTime;

    /**
     * 请求路径
     */
    private String reqPath;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId == null ? null : reqId.trim();
    }

    public String getReqSysId() {
        return reqSysId;
    }

    public void setReqSysId(String reqSysId) {
        this.reqSysId = reqSysId == null ? null : reqSysId.trim();
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getReqPath() {
        return reqPath;
    }

    public void setReqPath(String reqPath) {
        this.reqPath = reqPath == null ? null : reqPath.trim();
    }
}