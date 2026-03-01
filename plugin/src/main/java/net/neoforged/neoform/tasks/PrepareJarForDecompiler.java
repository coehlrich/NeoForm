package net.neoforged.neoform.tasks;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class PrepareJarForDecompiler extends ToolAction {
    @InputFile
    @Optional
    public abstract RegularFileProperty getClient();

    @InputFile
    @Optional
    public abstract RegularFileProperty getServer();

    @OutputFile
    public abstract RegularFileProperty getOutput();

    @TaskAction
    public void execute() throws IOException {
        Map<String, String> parameters = new HashMap<>();

        if (getClient().isPresent()) {
            parameters.put("inputClientJar", getClient().getAsFile().get().getAbsolutePath());
        }

        if (getServer().isPresent()) {
            parameters.put("inputServerJar", getServer().getAsFile().get().getAbsolutePath());
        }

        parameters.put("output", getOutput().getAsFile().get().getAbsolutePath());

        exec(parameters);
    }
}
