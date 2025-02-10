package app.ciserver;
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
	private String uriStub = "dummy/uri";
	HttpClient.Builder clientBuilder() {

		return HttpClient.newBuilder();
	}

	private String getToken() {
		CommandService commandService = new CommandService();
		String[] command = {"echo", "$GITHUB_API_TOKEN"};
		CommandService.CommandResult result = commandService.runCommand(command);
		return result.output();
	}
	void handleResponse(HttpResponse<String> responseMessage) {
		int responseCode = responseMessage.statusCode();
		if (responseCode != 200) {
			LOGGER.error("Error while waiting for response '{}' : {}", String.join(" ", "status code"),
					String.valueOf(responseCode));
		}
	}

	String[] makeHeader() {

		String[] header = new String[3];
		header[0] = "application/vnd.github+json";
		header[1] = "Bearer " + getToken();
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

	public void notifyError(String message) {
		requestWrapper(message, "error");
	}

	public void notifyFailure(String message) {
		requestWrapper(message, "failure");
	}

	public void notifyPending(String message, String state) {
		requestWrapper(message, "pending");
	}
	public void notifySucccess(String message) {
		requestWrapper(message, "success");
	}
	HttpRequest.Builder requestBuilder() {

		return HttpRequest.newBuilder();
	}
	void requestWrapper(String message, String state) {
		try {
			String jsonBody = makeJsonBody(message, state);
			String[] header = makeHeader();

			HttpClient client = clientBuilder().build();
			HttpRequest request = requestBuilder().uri(URI.create(uriStub)).header("Accept", header[0])
					.header("Authorization", header[1]).header("X-GitHub-Api-Version", header[2])
					.timeout(Duration.ofMinutes(2)).POST(BodyPublishers.ofString(jsonBody)).build();

			handleResponse(client.send(request, BodyHandlers.ofString()));
		} catch (IOException | InterruptedException e) {
			LOGGER.error("Error while attempting to send request '{}' : {}", String.join(" ", "requestWrapper fail"),
					e.getMessage());
		}
	}
	// This should be removed
	public void updateCommitStatus(String headerContents, String uriStub, String state) {
		requestWrapper(headerContents, state);
	}
}
