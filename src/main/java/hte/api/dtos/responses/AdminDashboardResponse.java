package hte.api.dtos.responses;

public class AdminDashboardResponse extends AbstractMessageResponse {
    private String data;

    public AdminDashboardResponse(String message, String data) {
        super(message);
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
