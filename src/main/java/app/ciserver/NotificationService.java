package app.ciserver;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class NotificationService {

	// TODO replace and integrate with webhook
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandService.class);

	HttpClient.Builder clientBuilder() {

		return HttpClient.newBuilder();
	}
	String getGithubToken() {
		return System.getenv("GITHUB_API_TOKEN");
	}
	void handleResponse(HttpResponse<String> responseMessage) {
		int responseCode = responseMessage.statusCode();
		if (responseCode != 201) {
			LOGGER.error("Error while waiting for response statuscode : {}, message: {}", responseCode,
					responseMessage.body());

		}
	}

	public void notifyError(String description, HookEventModel pathParams) {
		requestWrapper(new CommitStatusModel("error", description, "continuous-integration/ciserver"), pathParams);
	}

	public void notifyFailure(String description, HookEventModel pathParams) {
		requestWrapper(new CommitStatusModel("failure", description, "continuous-integration/ciserver"), pathParams);
	}

	public void notifyPending(String description, HookEventModel pathParams) {
		requestWrapper(new CommitStatusModel("pending", description, "continuous-integration/ciserver"), pathParams);
	}
	public void notifySuccess(String description, HookEventModel pathParams) {

		requestWrapper(new CommitStatusModel("success", description, "continuous-integration/ciserver"), pathParams);
	}
	HttpRequest.Builder requestBuilder() {

		return HttpRequest.newBuilder();
	}
	void requestWrapper(CommitStatusModel bodyParams, HookEventModel pathParams) {
		try {
			String json = new ObjectMapper().writeValueAsString(bodyParams);
			HttpClient client = clientBuilder().build();
			HttpRequest request = requestBuilder()
					.uri(URI.create("https://api.github.com/repos/" + pathParams.repository().fullName() + "/statuses/"
							+ pathParams.after()))
					.header("Accept", "application/vnd.github+json")
					.header("Authorization", "Bearer " + getGithubToken()).header("X-GitHub-Api-Version", "2022-11-28")
					.timeout(Duration.ofMinutes(2)).POST(BodyPublishers.ofString(json)).build();
			handleResponse(client.send(request, BodyHandlers.ofString()));
		} catch (IOException | InterruptedException e) {
			LOGGER.error("Error while attempting to send request '{}' : {}", String.join(" ", "requestWrapper fail"),
					e.getMessage());
		}
	}
}
