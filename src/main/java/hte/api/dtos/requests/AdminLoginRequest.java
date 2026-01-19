package hte.api.dtos.requests;

public class AdminLoginRequest extends BaseAdminRequest {
    public AdminLoginRequest(String username, String password) {
        super(username, password);
    }
}
