package top.yms.storage.em;

import top.yms.storage.constants.StorageConstants;

public enum MsgCd {
    SUCCESS(StorageConstants.SUCCESS_CODE, "成功"),
    FAILED(StorageConstants.FAILED_CODE, "失败"),
    ;

    MsgCd(String code, String message) {
        this.code = code;
        this.message = message;
    }

    private String code;
    private String message;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
