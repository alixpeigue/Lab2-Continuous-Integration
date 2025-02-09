package app.ciserver;
import static org.mockito.Mockito.*;

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
	static void testUpdateCommitStatus() {

		HttpClient.Builder clientBuilder = mock(HttpClient.Builder.class, Mockito.RETURNS_DEEP_STUBS);
		HttpClient mockedClient = mock(HttpClient.class);

		HttpRequest.Builder reqBuilder = mock(HttpRequest.Builder.class, Mockito.RETURNS_DEEP_STUBS);
		HttpRequest mockedRequest = mock(HttpRequest.class);

		when(reqBuilder.uri(URI.create(uriStub)).header("Accept", "application/vnd.github+json")
				.timeout(Duration.ofMinutes(2)).build()).thenReturn(mockedRequest);
		HttpRequest request = reqBuilder.build();

		when(clientBuilder.build()).thenReturn(mockedClient);
		HttpClient client = clientBuilder.build();

		NotificationService testNotifService = new NotificationService();
		verify(reqBuilder).uri(URI.create(uriStub)).header("Accept", "application/vnd.github+json")
				.timeout(Duration.ofMinutes(2)).build();
		verify(clientBuilder).build();

		NotificationService spyNotifService = Mockito.spy(testNotifService);
		doNothing().when(spyNotifService).updateCommitStatus(anyString(), anyString());
		spyNotifService.updateCommitStatus("test", "test");

	}
}
