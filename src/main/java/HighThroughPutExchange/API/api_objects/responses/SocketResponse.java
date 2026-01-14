package HighThroughPutExchange.API.api_objects.responses;

public class SocketResponse {
    private String content;
    private Long updateId;

    public SocketResponse(String content, Long updateId) {
        this.content = content;
        this.updateId = updateId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getUpdateId() {
        return updateId;
    }

    public void setUpdateId(Long updateId) {
        this.updateId = updateId;
    }
}
