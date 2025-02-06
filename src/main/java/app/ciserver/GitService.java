package app.ciserver;

import javafx.util.Pair;

/**
 * Service that handle git manipulations
 */
public class GitService {

	private final CommandService commandService;

	GitService(CommandService commandService) {
		this.commandService = commandService;
	}

	/**
	 * Checks out a git repository to a revision
	 * 
	 * @param repositoryFolder
	 *            the repository location
	 * @param revision
	 *            the name of the revision to checkout to, can be a tag, a branch or
	 *            a commit hash
	 * @return false if the checkout failed, true otherwise
	 */
	public boolean checkout(String repositoryFolder, String revision) {
		Pair<Integer, String> result = commandService
				.runCommand(new String[]{"git", "-C", repositoryFolder, "checkout", revision});
		return result.getKey() == 0;
	}

	/**
	 * Clones the repository in the destination folder
	 * 
	 * @param repositoryUrl
	 *            the URL of the repository to clone
	 * @param destinationFolder
	 *            the folder where the repository should be cloned
	 * @return false if the clone failed, true otherwise
	 */
	public boolean clone(String repositoryUrl, String destinationFolder) {
		Pair<Integer, String> result = commandService
				.runCommand(new String[]{"git", "clone", repositoryUrl, destinationFolder});
		return result.getKey() == 0;
	}
}
