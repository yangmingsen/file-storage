package top.yms.storage.entity;

import top.yms.storage.em.MsgCd;

public class BaseResponse <T>{
    private String code;
    private String message;
    private T data;

    public BaseResponse(MsgCd msgCd) {
        this.code = msgCd.getCode();
        this.message = msgCd.getMessage();
    }

    public BaseResponse(MsgCd msgCd, T data) {
        this.code = msgCd.getCode();
        this.message = msgCd.getMessage();
        this.data = data;
    }

    public BaseResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }


    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(MsgCd.SUCCESS, data);
    }

    public static <T> BaseResponse<T> failed(T data) {
        return new BaseResponse<>(MsgCd.FAILED, data);
    }

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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
