package org.enginehub.build.worldeditcui;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHReleaseBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.GitHubRateLimitHandler;
import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.io.File;
import java.io.IOException;

public abstract class PublishGitHubRelease extends DefaultTask implements ReleaseJobParameters {

    @Input
    @Optional
    public abstract Property<String> getEnterpriseUrl();

    @Input
    public abstract Property<String> getApiToken();

    private GitHub createGitHub() {
        final GitHubBuilder builder = new GitHubBuilder();
        if (this.getEnterpriseUrl().isPresent()) {
            builder.withEndpoint(this.getEnterpriseUrl().get());
        }
        builder.withOAuthToken(this.getApiToken().get());
        builder.withRateLimitHandler(new GitHubRateLimitHandler() {
            @Override
            public void onError(@NotNull GitHubConnectorResponse response) throws IOException {
                getLogger().error(
                        "Exceeded rate limit while trying to publish release (code {}): {}",
                        response.statusCode(),
                        new String(response.bodyStream().readAllBytes())
                );
                throw new GradleException("Rate limmit exceeded! See log for details");
            }
        });

        try {
            return builder.build();
        } catch (IOException e) {
            this.getLogger().error("Failed to create GitHub instance: {}", e.getMessage(), e);
            throw new GradleException("GitHub authentication failed, see log for details");
        }
    }

    @TaskAction
    public void doPublish() {
        final GitHub gh = this.createGitHub();

        final GHRepository repo = runHandlingException(() -> gh.getRepository(this.getRepository().get()));
        final GHReleaseBuilder releaseBuilder = repo.createRelease(this.getTagName().get());

        if (this.getReleaseName().isPresent()) {
            releaseBuilder.name(this.getReleaseName().get());
        }

        releaseBuilder
                .body(this.getReleaseBody().getOrElse(""))
                .draft(true)
                .prerelease(this.getPrerelease().get());

        if (this.getDiscussionCategoryName().isPresent()) {
            releaseBuilder.categoryName(this.getDiscussionCategoryName().get());
        }

        if (this.getSourceBranch().isPresent()) {
            releaseBuilder.commitish(this.getSourceBranch().get());
        }

        // todo: generateReleaseNotes
        // todo: makeLatest

        // update release content
        final GHRelease release = runHandlingException(releaseBuilder::create);
        for (final File file : this.getArtifacts()) {
            if (!file.isFile()) {
                throw new InvalidUserDataException("Release artifact " + file.getAbsolutePath() + " is not a regular file!");
            }
            runHandlingException(() -> release.uploadAsset(file, determineMimeType(file)));
        }

        // Now that all elements have been done successfully, if we are non-draft then mark it as non-draft
        if (!this.getDraft().get()) {
            runHandlingException(release.update().draft(false)::update);
        }
    }

    private String determineMimeType(final File file) {
        final String name = file.getName();
        if (name.endsWith("jar")) {
            return "application/java-archive";
        } else if (name.endsWith("zip")) {
            return "application/zip";
        } else { // unknown // todo: find a better way to determine this
            return "application/octet-stream";
        }
    }

    @FunctionalInterface
    interface GHCallable<O> {
        O execute() throws IOException;
    }

    private <T> T runHandlingException(final GHCallable<T> item) throws GradleException {
        try {
            return item.execute();
        } catch (final IOException ex) {
            this.getLogger().error("Failed to execute GitHub API operation", ex);
            throw new GradleException("GitHub API error occurred, see log for details: " + ex.getMessage());
        }
    }
}
