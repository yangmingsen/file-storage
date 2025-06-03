package top.yms.storage.entity;

public class DeleteResp {
    private String fileId;

    public DeleteResp(String fileId) {
        this.fileId = fileId;
    }

    public DeleteResp() {
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}
