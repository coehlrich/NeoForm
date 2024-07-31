package net.neoforged.neoform.tasks;

import org.gradle.api.*
import org.gradle.api.model.*
import org.gradle.api.tasks.*
import org.gradle.api.provider.*

import javax.inject.Inject

public abstract class NeoFormStepManager {

    def directory = project.file(project.PATH_CACHED_VERSION)

    def sides = [
        client: [steps: new HashMap<>(), directory: new File(directory, 'client')],
        joined: [steps: new HashMap<>(), directory: new File(directory, 'joined')],
        server: [steps: new HashMap<>(), directory: new File(directory, 'server')]
    ]

    def getStep(def side, def step) {
        if (!sides[side].steps[step]) {
            return setupStep(side, step)
        }
        return sides[side].steps[step]
    }
    
    def setupStep(def side, def step) {
        def json = project.DATA.steps.overrides[side][step]
        if (!json) {
            json = project.DATA.steps.shared[step]
        }
        def name = side + '_' + step
        println name
        def task
        if (json.function == 'decompile') {
            task = decompileTask(side, name, step, json.inputs)
        } else if (json.function == 'listLibraries') {
            task = listLibrariesTask(side, name, json.inputs)
        } else if (json.function == 'strip') {
            task = stripJarTask(side, name, json.inputs)
        } else if (json.function == 'downloadData') {
            task = downloadDataTask(side, name, json.inputs)
        } else if (project.DATA.functions[json.function]) {
            task = toolJarExecTask(json.function, side, name, step, json.inputs)
        } else {
            throw new UnsupportedOperationException('Not implemented function: ' + json.function)
        }
        println name
        for (output in json.outputs.entrySet()) {
            if (output.value.type == 'folder') {
                task.outputDirs[output.key] = new File(sides[side].directory, output.value.name)
            } else {
                task.outputFiles[output.key] = new File(sides[side].directory, output.value.name + '.' + output.value.type)
            }
        }
        println task.inputs.files.files
        sides[side].steps[step] = task
        return task
    }
    
    def getOutput(def side, def step, def name) {
        def task = getStep(side, step)
        return task.getOutput(name)
    }
    
    def parseInputFile(def side, def json, def task) {
        if (json.type == 'step_output') {
            def file = project.layout.file(getOutput(side, json.step, json.name))
            task.dependsOn(sides[side].steps[json.step])
            return file
        } else if (json.type == 'version_json') {
            task.dependsOn(project.tasks.downloadJson)
            return project.layout.file(provider.provider { project.tasks.downloadJson.dest })
        } else if (json.type == 'data') {
            return project.layout.file(provider.provider { project.file(project.DATA.data[json.data])})
        } else {
            throw new UnsupportedOperationException(json.type)
        }
    }
    
    def parseInputString(def side, def json, def task) {
        if (json instanceof String) {
            return provider.provider { json }
        } else if (json.type == 'minecraft_version') {
            return project.name
        } else {
            def file = parseInputFile(side, json, task)
            task.inputs.file(file)
            return file.map { it.asFile.absolutePath }
        }
    }
    
    def decompileTask(def side, def name, def step, def inputs) {
        return project.tasks.create(name, FernflowerTask.class) {
            config(project.DATA.functions.decompile, project.configurations.tool_decompile)
            for (input in inputs.entrySet()) {
                def string = parseInputString(side, input.value, it)
                arguments[input.key] = string
            }
            log = new File(sides[side].directory, step + '.log')
        }
    }
    
    def listLibrariesTask(def side, def name, def inputs) {
        return project.tasks.create(name, CreateFernflowerLibraries.class) {
            format = parseInputString(side, inputs.format, it)
            useJsonLibraries = inputs.use_json_libraries
            for (libraries in inputs.extra) {
                extras.from project.files(parseInputFile(side, libraries, it))
            }
            json = parseInputFile(side, inputs.json, it)
            neoFormLibraries = project.DATA.libraries[side]
            outputDirectory = new File(project.gradle.gradleUserHomeDir, '/caches/neoform/libraries/')
        }
    }

    def toolJarExecTask(def type, def side, def name, def step, def inputs) {
        return project.tasks.create(name, ToolJarExec.class) {
            config(project.DATA.functions[type], project.configurations['tool_' + type])
            for (input in inputs.entrySet()) {
                def string = parseInputString(side, input.value, it)
                arguments[input.key] = string
            }
            log = new File(sides[side].directory, step + '.log')
        }
    }
    
    def stripJarTask(def side, def name, def inputs) {
        return project.tasks.create(name, SplitJar.class) {
            input = parseInputFile(side, inputs.input, it)
            mappings = parseInputFile(side, inputs.mappings, it)
        }
    }
    
    def downloadDataTask(def side, def name, def inputs) {
        return project.tasks.create(name, DownloadData.class) {
            json = parseInputFile(side, inputs.json, it)
            dataName = parseInputString(side, inputs.name, it)
        }
    }

    @Inject
    abstract Project getProject()

    @Inject
    abstract ProviderFactory getProvider()
    
    @Inject
    abstract ObjectFactory getObjects()
}