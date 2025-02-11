package app.ciserver;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
class NotificationServiceTest {

	static String uriStub;
	static NotificationService testNotifyService;
	static NotificationService spyNotifyService;

	@BeforeAll
	static void testSetup() {
		uriStub = "https://www.example.com";
		testNotifyService = new NotificationService();
		spyNotifyService = Mockito.spy(testNotifyService);
	}
	@Test
	void testNotifyFailure() {
	}
	@Test
	void testNotifyPending() {

		doNothing().when(spyNotifyService).requestWrapper("test", "test", uriStub);

	}
	@Test
	void testNotifySuccess() {
	}
	@Test
	void testRequestWrapper() throws IOException, InterruptedException {

		HttpClient.Builder mockedClientBuilder = mock(HttpClient.Builder.class);
		HttpClient mockedClient = mock(HttpClient.class);

		HttpRequest.Builder mockedReqBuilder = mock(HttpRequest.Builder.class);
		HttpRequest mockedRequest = mock(HttpRequest.class);
		// We cannot mock final classes in mockito painlessly unfortunately
		URI fakeMockUri = URI.create("https://example.com");
		// Need to override this one part
		when(mockedReqBuilder.uri(fakeMockUri)).thenReturn(mockedReqBuilder);
		// when(mockedReqBuilder.uri(URI.create(uriStub))).thenReturn(mockedReqBuilder);
		when(mockedReqBuilder.header("Accept", "application/vnd.github+json")).thenReturn(mockedReqBuilder);
		when(mockedReqBuilder.header("Authorization", "funny token")).thenReturn(mockedReqBuilder);
		when(mockedReqBuilder.header("X-GitHub-Api-Version", "some header")).thenReturn(mockedReqBuilder);
		when(mockedReqBuilder.timeout(Duration.ofMinutes(2))).thenReturn(mockedReqBuilder);
		when(mockedReqBuilder.POST(BodyPublishers.ofString("test"))).thenReturn(mockedReqBuilder);

		// .build().thenReturn(mockedRequest);
		when(mockedClientBuilder.build()).thenReturn(mockedClient);

		doReturn(mockedClientBuilder).when(spyNotifyService).clientBuilder();
		doReturn(mockedReqBuilder).when(spyNotifyService).requestBuilder();
		doNothing().when(spyNotifyService).handleResponse(any());
		// mockedClient test when return 200 and not 200 (test 500), and also exceptions

		assertNotNull(uriStub, "uriStub must not be null");
		spyNotifyService.requestWrapper("test", "dummy", uriStub);
		verify(mockedReqBuilder).uri(URI.create(uriStub)).header("Accept", "application/vnd.github+json")
				.header("Authorization", "funny token").header("X-GitHub-Api-Version", "some header")
				.timeout(Duration.ofMinutes(2)).POST(BodyPublishers.ofString("test")).build();
		verify(mockedClient).send(mockedRequest, any());
		verify(mockedClientBuilder).build();
		// dont overwrite the method so dont do nothing
	}
}
