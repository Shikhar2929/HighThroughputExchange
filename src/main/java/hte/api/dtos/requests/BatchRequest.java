package hte.api.dtos.requests;

import hte.api.dtos.operations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class BatchRequest extends BasePrivateRequest {
    @NotNull @Valid private List<Operation> operations;

    BatchRequest(String username, String sessionToken, List<Operation> operations) {
        super(username, sessionToken);
        this.operations = operations;
    }

    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

    public List<Operation> getOperations() {
        return operations;
    }
}
