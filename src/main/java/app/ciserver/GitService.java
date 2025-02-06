package app.ciserver;

/**
 * Service that handle git manipulations
 */
public class GitService {
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
		return false;
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
		return false;
	}
}
