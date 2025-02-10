package app.ciserver;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class NotificationServiceTest {

	static String uriStub;

	@BeforeAll
	static void testSetup() {
		uriStub = "silly/domain";
	}
	@Test
	void testRequestWrapper() throws IOException, InterruptedException {
		// good
		HttpClient.Builder mockedClientBuilder = mock(HttpClient.Builder.class, Mockito.RETURNS_DEEP_STUBS);
		HttpClient mockedClient = mock(HttpClient.class);

		HttpRequest.Builder mockedReqBuilder = mock(HttpRequest.Builder.class, Mockito.RETURNS_DEEP_STUBS);
		HttpRequest mockedRequest = mock(HttpRequest.class);
		// stop good
		when(mockedReqBuilder.uri(URI.create(uriStub)).header("Accept", "application/vnd.github+json")
				.timeout(Duration.ofMinutes(2)).build()).thenReturn(mockedRequest);
		when(mockedClientBuilder.build()).thenReturn(mockedClient);
		// good
		// not needed under
		// verify call after calling them
		NotificationService testNotifService = new NotificationService();

		NotificationService spyNotifyService = Mockito.spy(testNotifService);
		doReturn(mockedClientBuilder).when(spyNotifyService).clientBuilder();
		doReturn(mockedReqBuilder).when(spyNotifyService).requestBuilder();
		doNothing().when(spyNotifyService).handleResponse(any());
		// mockedClient test when return 200 and not 200 (test 500), and also exceptions

		spyNotifyService.requestWrapper("test", uriStub);
		verify(mockedReqBuilder, Mockito.times(2)).uri(URI.create(uriStub))
				.header("Accept", "application/vnd.github+json").timeout(Duration.ofMinutes(2)).build();
		verify(mockedClient).send(mockedRequest, any());
		verify(mockedClientBuilder).build();
		// dont overwrite the method so dont do nothing
		// what to log, look at main, create static final logger notifservice.class , do
		// logger.error
	}
}
