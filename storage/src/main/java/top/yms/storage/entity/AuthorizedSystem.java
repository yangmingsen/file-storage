package top.yms.storage.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Date;

/**
 *
 * create by yangmingsen
 * t_authorized_system
 */
@Table("t_authorized_system")
public class AuthorizedSystem {
    /**
     * pk
     */
    @Id
    private Long id;

    /**
     * ct
     */
    private LocalDateTime createTime;

    /**
     * ut
     */
    private LocalDateTime updateTime;

    /**
     */
    private LocalDateTime authExpireTime;

    /**
     * 授权token
     */
    private String authToken;

    /**
     * A-Acitve, C-Close, P-Pending
     */
    private String status;

    /**
     * 系统id
     */
    private String systemId;

    /**
     * 系统名称
     */
    private String systemName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public LocalDateTime getAuthExpireTime() {
        return authExpireTime;
    }

    public void setAuthExpireTime(LocalDateTime authExpireTime) {
        this.authExpireTime = authExpireTime;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken == null ? null : authToken.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName == null ? null : systemName.trim();
    }
}