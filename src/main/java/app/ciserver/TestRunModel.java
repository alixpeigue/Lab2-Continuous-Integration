package app.ciserver;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record TestRunModel(
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Europe/Stockholm") LocalDateTime timestamp,
		String status, // "success", "failure", "error"
		String commitSHA, String branchName, String pusherName, String buildLogs) {
}
