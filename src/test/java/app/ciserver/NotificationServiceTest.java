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
	private static String API_TOKEN;

	@BeforeAll
	static void testSetup() {
		uriStub = "https://www.example.com";
		API_TOKEN = System.getenv("GITHUB_API_TOKEN");
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

	@Mock
	private CommitStatusModel mockedBodyParams;

	@Mock
	private HookEventModel mockedPathParams;

	@Mock
	private RepositoryModel mockedRepoModel;

	@Spy
	private NotificationService spyNotificationService;

	@BeforeEach
	void initMocks() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testNotifyFailure() {
	}

	@Test
	void testNotifyPending() {

		doNothing().when(spyNotificationService).requestWrapper(any(CommitStatusModel.class),
				any(HookEventModel.class));

	}

	@Test
	void testNotifySuccess() {
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

		when(mockedBodyParams.state()).thenReturn("pending");
		when(mockedBodyParams.description()).thenReturn("silly");
		when(mockedBodyParams.context()).thenReturn("context");

		when(mockedPathParams.after()).thenReturn("sha23");
		when(mockedPathParams.repository()).thenReturn(mockedRepoModel);
		when(mockedRepoModel.fullName()).thenReturn("test/testman");

		doReturn(mockedClientBuilder).when(spyNotificationService).clientBuilder();
		doReturn(mockedRequestBuilder).when(spyNotificationService).requestBuilder();
		doNothing().when(spyNotificationService).handleResponse(any());

		spyNotificationService.requestWrapper(mockedBodyParams, mockedPathParams);

		verify(mockedRequestBuilder)
				.uri(URI.create(mockedPathParams.repository().fullName() + "/statuses/" + mockedPathParams.after()));
		verify(mockedRequestBuilder).header("Accept", "application/vnd.github+json");
		verify(mockedRequestBuilder).header("Authorization", "Bearer " + API_TOKEN);
		verify(mockedRequestBuilder).header("X-GitHub-Api-Version", "2022-11-18");
		verify(mockedRequestBuilder).timeout(Duration.ofMinutes(2));
		verify(mockedRequestBuilder).POST(any(HttpRequest.BodyPublisher.class));
		verify(mockedRequestBuilder).build();

		verify(mockedClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
		verify(mockedClientBuilder).build();

		verify(spyNotificationService).handleResponse(mockedResponse);
	}
}
