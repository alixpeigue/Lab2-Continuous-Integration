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
/**
 * The {@code NotificationService} is used to send HTTP POST requests to notify
 * upstream about the status of the automated CI action
 */
public class NotificationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommandService.class);

	HttpClient.Builder clientBuilder() {

		return HttpClient.newBuilder();
	}
	/**
	 * Retrieve the required API-token from an environment variable
	 * 
	 * @return - The token in String form
	 */
	String getGithubToken() {
		return System.getenv("GITHUB_API_TOKEN");
	}
	/**
	 * Handle the HTTP response from GitHub after sending the POST request In case
	 * of non 201 response code logging is invoked
	 *
	 * @param responseMessage
	 *            - The response as a String wrapped in a HttpResponse
	 */
	void handleResponse(HttpResponse<String> responseMessage) {
		int responseCode = responseMessage.statusCode();
		if (responseCode != 201) {
			LOGGER.error("Error while waiting for response statuscode : {}, message: {}", responseCode,
					responseMessage.body());

		}
	}

	/**
	 * Sends an error commit status to GitHub
	 * 
	 * @param description
	 *            - The description entry in the json-body of the request
	 * @param pathParams
	 *            - A HookEventModel instance containing the path parameters
	 *            required by GitHub API
	 */
	public void notifyError(String description, HookEventModel pathParams) {
		requestWrapper(new CommitStatusModel("error", description, "continuous-integration/ciserver"), pathParams);
	}

	/**
	 * Sends an failure commit status to GitHub
	 * 
	 * @param description
	 *            - The description entry in the json-body of the request
	 * @param pathParams
	 *            - A HookEventModel instance containing the path parameters
	 *            required by GitHub API
	 */
	public void notifyFailure(String description, HookEventModel pathParams) {
		requestWrapper(new CommitStatusModel("failure", description, "continuous-integration/ciserver"), pathParams);
	}

	/**
	 * Sends an pending commit status to GitHub
	 * 
	 * @param description
	 *            - The description entry in the json-body of the request
	 * @param pathParams
	 *            - A HookEventModel instance containing the path parameters
	 *            required by GitHub API
	 */
	public void notifyPending(String description, HookEventModel pathParams) {
		requestWrapper(new CommitStatusModel("pending", description, "continuous-integration/ciserver"), pathParams);
	}

	/**
	 * Sends an success commit status to GitHub
	 * 
	 * @param description
	 *            - The description entry in the json-body of the request
	 * @param pathParams
	 *            - A HookEventModel instance containing the path parameters
	 *            required by GitHub API
	 */
	public void notifySuccess(String description, HookEventModel pathParams) {

		requestWrapper(new CommitStatusModel("success", description, "continuous-integration/ciserver"), pathParams);
	}

	HttpRequest.Builder requestBuilder() {

		return HttpRequest.newBuilder();
	}

	/**
	 * Package-private method that constructs and sends the actual HTTP Request
	 * 
	 * @param bodyParams
	 *            - CommitStatusModel instance that contains the parameters for the
	 *            json body
	 * @param pathParams
	 *            - A HookEventModel instance containing the path parameters
	 *            required by GitHub API
	 */
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
