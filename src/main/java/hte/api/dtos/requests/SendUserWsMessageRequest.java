package hte.api.dtos.requests;

import jakarta.validation.constraints.NotNull;

public class SendUserWsMessageRequest extends BaseAdminRequest {
    @NotNull private String targetUsername;
    @NotNull private String message;

    public SendUserWsMessageRequest(
            String adminUsername, String adminPassword, String targetUsername, String message) {
        super(adminUsername, adminPassword);
        this.targetUsername = targetUsername;
        this.message = message;
    }

    public String getTargetUsername() {
        return targetUsername;
    }

    public void setTargetUsername(String targetUsername) {
        this.targetUsername = targetUsername;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
