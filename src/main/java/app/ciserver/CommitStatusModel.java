package app.ciserver;

public class CommitStatusModel {
    public record CommitStatus(String state, String description, String context) {
    }
}
