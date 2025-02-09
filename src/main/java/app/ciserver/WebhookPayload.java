package app.ciserver;

class Repository {
	private String cloneUrl;

	// Getters and setters

	public String getCloneUrl() {
		return cloneUrl;
	}

	public void setCloneUrl(String cloneUrl) {
		this.cloneUrl = cloneUrl;
	}
}

public class WebhookPayload {
	private String ref;
	private Repository repository;

	// Getters and setters

	public String getRef() {
		return ref;
	}

	public Repository getRepository() {
		return repository;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}
}