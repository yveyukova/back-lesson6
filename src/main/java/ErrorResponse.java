import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@With
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
        "status",
        "message",
        "timestamp"
})
public class ErrorResponse {

    @JsonProperty("status")
    public Integer status;
    @JsonProperty("message")
    public String message;
    @JsonProperty("timestamp")
    public String timestamp;

}

