package HighThroughPutExchange.API.api_objects.responses;

public class SocketResponse {
    private String content;

    public SocketResponse(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
