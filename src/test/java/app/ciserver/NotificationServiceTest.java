package app.ciserver;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class NotificationServiceTest {

	static String uriStub;
	static HookEventModel pathParams;

	@BeforeAll
	static void testSetup() {
		uriStub = "https://www.example.com";
		pathParams = new HookEventModel("sha23", "ref", new PusherModel("silly@mail", "name"),
				new RepositoryModel("cloneUrl", "FullName"));
	}

	@Mock
	private HttpClient mockedClient;

	@Mock
	private HttpClient.Builder mockedClientBuilder;

	@Mock
	private HttpRequest mockedRequest;

	@Mock
	private HttpRequest.Builder mockedRequestBuilder;

	@Mock
	private HttpResponse<String> mockedResponse;

	@Spy
	private NotificationService spyNotificationService;

	@BeforeEach
	void initMocks() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testNotifyError() {

		CommitStatusModel bodyParams = new CommitStatusModel("error", "silly", "continuous-integration/ciserver");
		doNothing().when(spyNotificationService).requestWrapper(any(CommitStatusModel.class),
				any(HookEventModel.class));
		spyNotificationService.notifyError("silly", pathParams);
		verify(spyNotificationService).requestWrapper(bodyParams, pathParams);
	}
	@Test
	void testNotifyFailure() {

		CommitStatusModel bodyParams = new CommitStatusModel("failure", "silly", "continuous-integration/ciserver");
		doNothing().when(spyNotificationService).requestWrapper(any(CommitStatusModel.class),
				any(HookEventModel.class));
		spyNotificationService.notifyFailure("silly", pathParams);
		verify(spyNotificationService).requestWrapper(bodyParams, pathParams);
	}

	@Test
	void testNotifyPending() {

		CommitStatusModel bodyParams = new CommitStatusModel("pending", "silly", "continuous-integration/ciserver");
		doNothing().when(spyNotificationService).requestWrapper(any(CommitStatusModel.class),
				any(HookEventModel.class));
		spyNotificationService.notifyPending("silly", pathParams);
		verify(spyNotificationService).requestWrapper(bodyParams, pathParams);

	}

	@Test
	void testNotifySuccess() {

		CommitStatusModel bodyParams = new CommitStatusModel("success", "silly", "continuous-integration/ciserver");
		doNothing().when(spyNotificationService).requestWrapper(any(CommitStatusModel.class),
				any(HookEventModel.class));
		spyNotificationService.notifySuccess("silly", pathParams);
		verify(spyNotificationService).requestWrapper(bodyParams, pathParams);
	}

	@Test
	void testRequestWrapper() throws IOException, InterruptedException {

		when(mockedRequestBuilder.uri(any(URI.class))).thenReturn(mockedRequestBuilder);
		when(mockedRequestBuilder.header(anyString(), anyString())).thenReturn(mockedRequestBuilder);
		when(mockedRequestBuilder.timeout(any(Duration.class))).thenReturn(mockedRequestBuilder);
		when(mockedRequestBuilder.POST(any(HttpRequest.BodyPublisher.class))).thenReturn(mockedRequestBuilder);
		when(mockedRequestBuilder.build()).thenReturn(mockedRequest);

		when(mockedClientBuilder.build()).thenReturn(mockedClient);

		when(mockedClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockedResponse);

		CommitStatusModel bodyParams = new CommitStatusModel("pending", "silly", "continuous-integration/ciserver");
		doReturn(mockedClientBuilder).when(spyNotificationService).clientBuilder();
		doReturn(mockedRequestBuilder).when(spyNotificationService).requestBuilder();
		doNothing().when(spyNotificationService).handleResponse(any());
		doReturn("API TOKEN EXAMPLE").when(spyNotificationService).getGithubToken();

		spyNotificationService.requestWrapper(bodyParams, pathParams);

		verify(mockedRequestBuilder).uri(URI.create("FullName/statuses/sha23"));
		verify(mockedRequestBuilder).header("Accept", "application/vnd.github+json");
		verify(mockedRequestBuilder).header("Authorization", "Bearer " + spyNotificationService.getGithubToken());
		verify(mockedRequestBuilder).header("X-GitHub-Api-Version", "2022-11-28");
		verify(mockedRequestBuilder).timeout(Duration.ofMinutes(2));
		verify(mockedRequestBuilder).POST(any(HttpRequest.BodyPublisher.class));
		verify(mockedRequestBuilder).build();

		verify(mockedClient).send(mockedRequest, HttpResponse.BodyHandlers.ofString());
		verify(mockedClientBuilder).build();

		verify(spyNotificationService).handleResponse(mockedResponse);
	}
}
