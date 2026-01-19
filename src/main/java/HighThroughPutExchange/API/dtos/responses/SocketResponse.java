package HighThroughPutExchange.api.dtos.responses;

public class SocketResponse {
    private String content;
    private Long seq;

    public SocketResponse(String content, Long seq) {
        this.content = content;
        this.seq = seq;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }
}
