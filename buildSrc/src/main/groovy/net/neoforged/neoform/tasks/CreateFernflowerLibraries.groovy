package net.neoforged.neoform.tasks;

import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import de.undercouch.gradle.tasks.download.DownloadAction
import de.undercouch.gradle.tasks.download.DownloadDetails

import java.nio.file.Files
import java.util.stream.Collectors
import java.util.TreeSet

abstract class CreateFernflowerLibraries extends DefaultTask implements NeoFormStep {
    @Input abstract Property<String> getFormat()
    @Input abstract Property<Boolean> getUseJsonLibraries()
    @InputFiles abstract ConfigurableFileCollection getExtras()
    @InputFile abstract RegularFileProperty getJson()
    @Input abstract ListProperty<String> getNeoFormLibraries()
    @Internal abstract RegularFileProperty getOutputDirectory()
    @Internal DownloadAction download
    
    CreateFernflowerLibraries() {
        download = new DownloadAction(project, this)
        download.overwrite(false)
        download.useETag('all')
    }
    
    @TaskAction
    def exec() {
        Utils.init()
        def libs = new TreeSet<>()
        def files = [:]
        def mavenPaths = [] as Set

        libs.addAll(extras.files.stream().flatMap { Files.walk(it.toPath()).filter { Files.isRegularFile(it) }.map { it.toFile() } }.collect(Collectors.toList()))
        download.dest outputDirectory.get().getAsFile()
        download.eachFile(new Action<DownloadDetails>() {
            @Override
            public void execute(DownloadDetails details) {
                details.relativePath = new RelativePath(false, files[details.sourceURL.toString()])
            }
        })
        
        if (useJsonLibraries.get()) {
            json.get().getAsFile().json.libraries.each{ lib ->
                def artifacts = (lib.downloads.artifact == null ? [] : [lib.downloads.artifact]) + lib.downloads.get('classifiers', [:]).values()
                artifacts.each{ art ->
                    if (mavenPaths.add(art.path)) {
                        download.src art.url
                        files[art.url] = art.path
                        libs.add(new File(outputDirectory.get().asFile, art.path))
                    }
                }
            }
        }
        
        neoFormLibraries.get().each { art ->
            if (mavenPaths.add(art.toMavenPath())) {
                download.src('https://maven.neoforged.net/releases/' + art.toMavenPath())
                files['https://maven.neoforged.net/releases/' + art.toMavenPath()] = art.toMavenPath()
                libs.add(new File(outputDirectory.get().asFile, art.toMavenPath()))
            }
        }
        download.execute()
        
        getOutput('output').get().text = libs.collect{ format.get().replace('{library}', it.absolutePath) }.join('\n')
    }
}