package app.ciserver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RepositoryModel(@JsonProperty(value = "clone_url", required = true) String cloneUrl,
		@JsonProperty(value = "full_name", required = true) String fullName) {
}
