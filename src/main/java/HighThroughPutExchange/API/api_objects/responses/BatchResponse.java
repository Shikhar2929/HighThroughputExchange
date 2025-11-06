package HighThroughPutExchange.API.api_objects.responses;

import java.util.List;

public class BatchResponse {

    private String status;
    private List<OperationResponse> results;

    public BatchResponse(String status, List<OperationResponse> results) {
        this.status = status;
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setResults(List<OperationResponse> results) {
        this.results = results;
    }

    public List<OperationResponse> getResults() {
        return results;
    }
}
