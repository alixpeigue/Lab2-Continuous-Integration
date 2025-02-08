package app.ciserver;

public class CommitStatusModel {
	record CommitStatus(String state, String description, String context) {
	}
}
