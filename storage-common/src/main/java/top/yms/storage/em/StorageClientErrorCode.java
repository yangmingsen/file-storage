package top.yms.storage.em;

public enum StorageClientErrorCode implements ErrorCode{
    E_205001(205001, "upload fail"),
    E_205002(205002, "获取文件流失败"),
    E_205003(205003, "获取meta信息失败"),
    E_205004(205004, "未找到fileMeta信息"),
    E_205005(205005, "删除file失败"),
    ;

    private int code;
    private String desc;

    StorageClientErrorCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public int getCode() {
        return 0;
    }

    @Override
    public String getDesc() {
        return "";
    }
}
