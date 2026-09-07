# NeoForm - Recompilable Minecraft Sources

NeoForm provides steps to create reproducible and recompilable Minecraft source code.

This fork includes `client` and `server` along with `joined`

## Version Availability

* The `client-and-server` branch contains the latest development version. 
* The `release/<version>` branches contain their respective release version along with patches for that version
* The `special/<version>` branches contain their respective version and is used for versions like April Fools versions

All versions will have a corresponding tag as well.

## How to Use

A typical workflow to change the patches would be:

- Run `gradlew :createPatchWorkspace` in the root, when the patches are already correct for the targeted Minecraft
  version.
- Reload the gradle project in your IDE, there will now be subprojects in `/workspace` for `client`, `server`, and `joined` containing the patched Minecraft
  code.
- Make any desired changes to the source code
- Run `gradlew :createPatches` to create new patch files from the sources currently contained in the workspace.

## Gradle Tasks

| Task                             | Description                                                                                                                                                   |
|----------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `:createPatchWorkspace`          | Decompile Minecraft, apply patches from `src/patches` and place the code in `workspace`.                                                                      |
| `:<distribution>:runClient`           | Runs the client in the <distribution> subproject.                                                                                                                  |
| `:check`                         | Runs the data contained in the current branch through [NeoFormRuntime](https://github.com/neoforged/NeoFormRuntime/), as well as the Eclipse Compiler.        |

## GitHub Actions Workflows

### [check-for-updates.yml](./.github/workflows/check-for-updates.yml) 
A server regularly checks for Minecraft releases, if there is a corresponding `minecraft-dependencies` (checked using a `HEAD` request), it triggers this workflow. This workflow will then check for a Minecraft release, and invoke the `update.yml` workflow for the new version.

Branch is determined by:
* If the version is detected to be a special version (e.g. April Fools) then the update will be performed on the branch following the format `special/<version>`. 
* If the version is a release version or related to a patch version then the update will be performed on the branch following the format `release/<version>` where `version` is the Game Drop release version
* Otherwise the `client-and-server` branch is used

### [update.yml](./.github/workflows/update.yml)
Performs an update to a new Minecraft version in the branch it is triggered in. Can be triggered manually. If the update completes successfully, a publish job is automatically started.                                                                                                                                                                                                                                                                                                 
### [publish.yml](./.github/workflows/publish.yml)
Runs the tests, creates a Git tag (for releases) and publishes to GitHub packages. It can also publish `SNAPSHOT`-versions to the snapshot maven.                                                                                                                                                                                                                                                                                                     
### [build.yml](./.github/workflows/build.yml)
Runs for pushes and pull requests. Essential for PR publishing.                                                                                                                                                                                                                                                                                                                                                                                                                          

