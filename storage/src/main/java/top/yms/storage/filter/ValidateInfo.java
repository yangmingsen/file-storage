package top.yms.storage.filter;

public class ValidateInfo {

    private String sysId;

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSysId() {
        return sysId;
    }

    public void setSysId(String sysId) {
        this.sysId = sysId;
    }

    public static ValidateInfo packToken(String token, String sysId) {
        ValidateInfo validateInfo = new ValidateInfo();
        validateInfo.setToken(token);
        validateInfo.setSysId(sysId);
        return validateInfo;
    }
}
