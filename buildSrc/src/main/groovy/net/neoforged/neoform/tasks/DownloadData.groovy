package net.neoforged.neoform.tasks

import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import de.undercouch.gradle.tasks.download.*

public abstract class DownloadData extends DefaultTask implements NeoFormStep {
    @InputFile
    abstract RegularFileProperty getJson()
    @Input
    abstract Property<String> getDataName()
    @Internal
    DownloadAction downloadAction
    
    DownloadData() {
        downloadAction = new DownloadAction(project, this)
        downloadAction.useETag('all')
        downloadAction.onlyIfModified(true)
        downloadAction.quiet(true)
    }
    
    @TaskAction
    def exec() {
        Utils.init()
        
        downloadAction.src(json.get().asFile.json.downloads[dataName.get()].url)
        downloadAction.dest(outputFiles.get()['output'])
        downloadAction.execute()
    }
}