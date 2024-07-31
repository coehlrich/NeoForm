package net.neoforged.neoform.tasks;

import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService

import javax.inject.Inject

abstract class ToolJarExec extends JavaExec implements NeoFormStep {
    private static final OutputStream NULL_OUTPUT = new OutputStream() { public void write(int b){} }  

    @Input
    abstract MapProperty<String, String> getArguments()
    @Internal
    abstract RegularFileProperty getLog()

    def config(def cfg, def configuration) {
        classpath = configuration
        args = cfg.args
        jvmArgs = cfg.jvmargs

        if (cfg.java_version != null) {
            javaLauncher.set(javaToolchainService.launcherFor {
                it.languageVersion = JavaLanguageVersion.of(cfg.java_version)
            })
        }
    }

    ToolJarExec() {
        def javaTarget = project.ext.JAVA_TARGET
        if (javaTarget != null) {
            javaLauncher.convention(javaToolchainService.launcherFor {
                it.languageVersion = JavaLanguageVersion.of(javaTarget)
            })
        }
    }

    @Inject
    JavaToolchainService getJavaToolchainService() {
        throw new UnsupportedOperationException()
    }

    @Override
    public final void exec() {
        def newArgs = new HashMap<>()
        newArgs.putAll(arguments.get())
        for (entry in outputFiles.get()) {
            newArgs.put(entry.key, entry.value.absolutePath)
        }
        for (entry in outputDirs.get()) {
            newArgs.put(entry.key, entry.value.absolutePath)
        }
        args = Utils.fillVariables(args, newArgs)
        def output
        if (log.present) {
            output = log.get().asFile.newOutputStream()
        } else {
            output = NULL_OUTPUT
        }
        standardOutput output
        errorOutput output
        
        this.preExec()
        super.exec()
        this.postExec()
    }
    
    protected void preExec(){}
    protected void postExec(){}
}