package net.minecraftforge.mcpconfig.tasks;

import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*

abstract class CreateFernflowerLibraries extends SingleFileOutput {
    @InputFiles abstract ConfigurableFileCollection getLibraries()
    @InputFiles abstract ConfigurableFileCollection getExtras()
    
    @TaskAction
    def exec() {
        Utils.init()
        def libs = new TreeSet<>()
        
        libs.addAll(extras)
        
        libraries.each{ lib ->
            libs.add(lib)
        }
        
        dest.get().getAsFile().write(libs.collect{ '-e=' + it.absolutePath }.join('\n'), "UTF-8")
    }
}