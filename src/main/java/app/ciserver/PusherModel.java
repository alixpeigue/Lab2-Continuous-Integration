package app.ciserver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PusherModel(@JsonProperty(required = true) String email, @JsonProperty(required = true) String name,
		@JsonProperty(required = true) String username) {
}
