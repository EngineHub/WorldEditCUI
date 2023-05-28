package org.enginehub.build.worldeditcui;

import net.kyori.indra.git.IndraGitExtension;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

public class GitHubReleaserPlugin implements Plugin<Project> {
    public static final String GITHUB_RELEASE_EXTENSION_NAME = "githubRelease";
    public static final String GITHUB_RELEASE_TASK_NAME = "publishToGitHub";

    @Override
    public void apply(final Project target) {
        target.getPlugins().apply("net.kyori.indra.git"); // for git operations

        // extension
        final GithubReleaserExtension extension = target.getExtensions().create(
            GithubReleaserExtension.class,
            GITHUB_RELEASE_EXTENSION_NAME,
            GitHubReleaserExtensionImpl.class
        );

        this.configureTasks(target.getTasks(), extension);
        this.registerPublishTask(target.getTasks(), extension);

        extension.getTagName().convention(target.provider(() -> {
            final Ref headTag = target.getExtensions().getByType(IndraGitExtension.class).headTag();
            return headTag == null ? null : Repository.shortenRefName(headTag.getName());
        }));
    }

    private void configureTasks(final TaskContainer tasks, final GithubReleaserExtension extension) {
        tasks.withType(PublishGitHubRelease.class).configureEach(task -> {
            task.getEnterpriseUrl().set(extension.getEnterpriseUrl());
            task.getApiToken().set(extension.getApiToken());
        });
    }

    private void registerPublishTask(final TaskContainer tasks, final ReleaseJobParameters sourceParameters) {
        tasks.register(GITHUB_RELEASE_TASK_NAME, PublishGitHubRelease.class, task -> {
            task.dependsOn("requireClean"); // via indra-git

            task.getReleaseName().set(sourceParameters.getReleaseName());
            task.getReleaseBody().set(sourceParameters.getReleaseBody());
            task.getRepository().set(sourceParameters.getRepository());
            task.getTagName().set(sourceParameters.getTagName());
            task.getSourceBranch().set(sourceParameters.getSourceBranch());
            task.getDraft().set(sourceParameters.getDraft());
            task.getPrerelease().set(sourceParameters.getPrerelease());
            task.getDiscussionCategoryName().set(sourceParameters.getDiscussionCategoryName());
            task.getGenerateReleaseNotes().set(sourceParameters.getGenerateReleaseNotes());
            task.getMakeLatest().set(sourceParameters.getMakeLatest());
            task.getArtifacts().from(sourceParameters.getArtifacts());
        });
    }
}
