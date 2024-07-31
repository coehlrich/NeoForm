package net.neoforged.neoform.tasks

import org.gradle.api.file.*
import org.gradle.api.tasks.*
import java.util.zip.*

@CacheableTask
public abstract class FernflowerTask extends ToolJarExec {
    
    @Override
    protected void postExec() {
        def output = outputFiles.get()['output']
        if (!output.exists())
            throw new IllegalStateException('Could not find fernflower output: ' + dest)
        def failed = []
        new ZipFile(output).withCloseable{ zip -> 
            zip.entries().findAll{ !it.directory && it.name.endsWith('.java') }.each { e ->
                def data = zip.getInputStream(e).text
                if (data.isEmpty() || data.contains("\$VF: Couldn't be decompiled"))
                    failed.add(e.name)
            }
        }
        if (!failed.isEmpty()) {
            logger.lifecycle('Failed to decompile: ')
            failed.each{ logger.lifecycle('  ' + it) }
            throw new IllegalStateException('Decompile failed')
        }
    }
}