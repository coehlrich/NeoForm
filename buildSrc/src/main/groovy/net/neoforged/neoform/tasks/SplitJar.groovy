package net.neoforged.neoform.tasks

import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.tasks.*

import java.util.zip.*

import net.neoforged.srgutils.IMappingFile

public abstract class SplitJar extends DefaultTask implements NeoFormStep {
    @InputFile abstract RegularFileProperty getMappings()
    @InputFile abstract RegularFileProperty getInput()
    
    @TaskAction
    def exec() {
        Utils.init()
        def classes = IMappingFile.load(mappings.get().getAsFile()).classes.collect{ it.original + '.class' }.toSet()
        
        new ZipOutputStream(outputFiles.get()['slim'].newOutputStream()).withCloseable{ so ->
            new ZipOutputStream(outputFiles.get()['extra'].newOutputStream()).withCloseable{ eo ->
                new ZipInputStream(input.get().getAsFile().newInputStream()).withCloseable{ jin ->
                    for (def entry = jin.nextEntry; entry != null; entry = jin.nextEntry) {
                        def out = classes.contains(entry.name) ? so : eo
                        def oentry = new ZipEntry(entry.name)
                        oentry.lastModifiedTime = entry.lastModifiedTime
                        out.putNextEntry(oentry)
                        out << jin
                    }
                }
            }
        }
    }
}