package app.ciserver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HookEventModel(@JsonProperty(required = true) String after, @JsonProperty(required = true) String ref,
		@JsonProperty(required = true) PusherModel pusher, @JsonProperty(required = true) RepositoryModel repository) {

	public Optional<String> getBranchName() {
		if (ref.startsWith("refs/heads/")) {
			return Optional.of(ref.substring("refs/heads/".length()));
		} else {
			return Optional.empty();
		}
	}
}