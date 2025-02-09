package app.ciserver;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

public class NotificationService {

	HttpClient.Builder clientBuilder() {

		return HttpClient.newBuilder();
	}

	void handleResponse(HttpResponse<String> responseMessage) {
		int responseCode = responseMessage.statusCode();
		if (responseCode != 200) {
			// Invoke logging here

		}
	}

	HttpRequest.Builder requestBuilder() {

		return HttpRequest.newBuilder();
	}
	void requestWrapper(String jsonBody, String uriStub) {

		HttpClient client = clientBuilder().build();
		HttpRequest request = requestBuilder().uri(URI.create(uriStub)).header("Accept", "application/vnd.github+json")
				.timeout(Duration.ofMinutes(2)).POST(BodyPublishers.ofString(jsonBody)).build();
		try {
			handleResponse(client.send(request, BodyHandlers.ofString()));
		} catch (IOException | InterruptedException e) {
			// TODO Invoke logging here also
			e.printStackTrace();
		}
	}

	// Should be CommitStatus headerContents but not integrated yet
	public void updateCommitStatus(String headerContents, String uriStub) {
		// Once commit status has been integrated, uncomment this
		/*
		 * String jsonBody = "{" + "\"state\":\"" + headerContents.success + "\"," +
		 * "\"target_url\":null," // Change null to proper url down the line maybe +
		 * "\"description\":\"" + headerContents.description + "\"," + "\"context\":\""
		 * + headerContents.context + "\"" + "}";
		 */
		requestWrapper(headerContents, uriStub);
	}

}
