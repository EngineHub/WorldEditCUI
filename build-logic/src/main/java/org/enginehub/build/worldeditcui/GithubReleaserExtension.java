package org.enginehub.build.worldeditcui;

import org.gradle.api.provider.Property;

public interface GithubReleaserExtension extends ReleaseJobParameters {
    /**
     * Get an endpoint override for GitHub.
     *
     * <p>Only required if using GitHub enterprise.</p>
     *
     * @return the base url for the GitHub instance
     */
    Property<String> getEnterpriseUrl();

    /**
     * Get the API token used to authenticate with GitHub.
     *
     * <p>By default, this is read from the {@code GITHUB_TOKEN} environment variable.</p>
     *
     * @return the api token property
     */
    Property<String> getApiToken();
}
