package net.neoforged.neoform.tasks;

import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*

interface NeoFormStep {
    @OutputFiles MapProperty<String, File> getOutputFiles()
    @OutputDirectories MapProperty<String, File> getOutputDirs()
    
    default getOutput(def name) {
        def file = outputFiles.getting(name)
        if (file.present) {
            return file
        } else {
            return outputDirs.getting(name)
        }
    }
}