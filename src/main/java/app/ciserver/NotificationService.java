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
	private static final String API_TOKEN = System.getenv("GITHUB_API_TOKEN");

	HttpClient.Builder clientBuilder() {

		return HttpClient.newBuilder();
	}

	void handleResponse(HttpResponse<String> responseMessage) {
		int responseCode = responseMessage.statusCode();
		if (responseCode != 200) {
			LOGGER.error("Error while waiting for response '{}' : {}", String.join(" ", "status code"),
					String.valueOf(responseCode));
		}
	}

	private CommitStatusModel makeCommitStatus(String state, String description, String context) {
		CommitStatusModel bodyParams = new CommitStatusModel(state, description, context);
		return bodyParams;
	}

	String[] makeHeader() {

		String[] header = new String[3];
		header[0] = "application/vnd.github+json";
		header[1] = "Bearer " + API_TOKEN;
		header[2] = "2022-11-18"; // Unsure what this should actually be
		return header;

	}

	String makeJsonBody(String message, String state) {

		String jsonBody = "{" + "\"state\":\"" + state + "\"," + "\"target_url\":null," + // Change null to proper url
																							// down the line maybe
				"\"description\":\"" + message + "\"," + "\"context\":\"" + "default" + "\"" + "}"; // TODO use context?
																									// default is
																									// default
		return jsonBody;
	}

	public void notifyError(String description, HookEventModel pathParams) {
		requestWrapper(makeCommitStatus("error", description, "context"), pathParams);
	}

	public void notifyFailure(String description, HookEventModel pathParams) {
		requestWrapper(makeCommitStatus("failure", description, "context"), pathParams);
	}

	public void notifyPending(String description, HookEventModel pathParams) {
		requestWrapper(makeCommitStatus("pending", description, "context"), pathParams);
	}
	public void notifySucccess(String description, HookEventModel pathParams) {

		requestWrapper(makeCommitStatus("success", description, "context"), pathParams);
	}
	HttpRequest.Builder requestBuilder() {

		return HttpRequest.newBuilder();
	}
	void requestWrapper(CommitStatusModel bodyParams, HookEventModel pathParams) {
		try {
			String json = new ObjectMapper().writeValueAsString(bodyParams);
			HttpClient client = clientBuilder().build();
			String[] header = makeHeader();
			HttpRequest request = requestBuilder()
					.uri(URI.create(pathParams.repository().fullName() + "/statuses/" + pathParams.after()))
					.header("Accept", header[0]).header("Authorization", header[1])
					.header("X-GitHub-Api-Version", header[2]).timeout(Duration.ofMinutes(2))
					.POST(BodyPublishers.ofString(json)).build();
			handleResponse(client.send(request, BodyHandlers.ofString()));
		} catch (IOException | InterruptedException e) {
			LOGGER.error("Error while attempting to send request '{}' : {}", String.join(" ", "requestWrapper fail"),
					e.getMessage());
		}
	}
}
