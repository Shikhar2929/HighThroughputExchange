package HighThroughPutExchange.API.api_objects.Operations;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LimitOrderOperation.class, name = "limit_order"),
        @JsonSubTypes.Type(value = MarketOrderOperation.class, name = "market_order"),
        @JsonSubTypes.Type(value = RemoveOperation.class, name = "remove"),
        @JsonSubTypes.Type(value = RemoveAllOperation.class, name = "remove_all")
})
public abstract class Operation {
    @NotNull
    private String type;
    public Operation(String type){
        this.type = type;
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

