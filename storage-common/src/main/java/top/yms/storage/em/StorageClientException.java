package top.yms.storage.em;

public class StorageClientException extends RuntimeException{
    private ErrorCode errorCode;

    public StorageClientException(ErrorCode errorCode) {
        super(errorCode.getDesc());
        this.errorCode = errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
