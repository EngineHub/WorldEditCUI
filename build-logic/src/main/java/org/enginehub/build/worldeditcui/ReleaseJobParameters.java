package org.enginehub.build.worldeditcui;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;

public interface ReleaseJobParameters {
    @Input
    @Optional // if it's not present, just use the tag name
    Property<String> getReleaseName();

    @Input
    Property<String> getReleaseBody();

    @Input
    Property<String> getRepository();

    @Input
    Property<String> getTagName();

    @Optional
    Property<String> getSourceBranch(); // if set, will create a tag with the provided name

    @Input
    Property<Boolean> getDraft();

    @Input
    Property<Boolean> getPrerelease();

    @Input
    @Optional
    Property<String> getDiscussionCategoryName();

    @Input
    Property<Boolean> getGenerateReleaseNotes(); // todo

    @Input
    Property<LatestState> getMakeLatest(); // todo

    @InputFiles
    ConfigurableFileCollection getArtifacts();

    enum LatestState {
        TRUE,
        FALSE,
        LEGACY;
    }

}
