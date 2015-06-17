Spongie
=======
Spongie is a command and chat bot based on [sk89q's Eduardo bot](https://github.com/sk89q/Eduardo). It is licensed under the GNU Lesser General Public License.

* [Source]
* [Issues]
* [Wiki]

## Prerequisites
* [Java] 8
* [Scala] 2.11.6

## Setup
__Note:__ If you do not have [Gradle] installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for Windows systems in place of any 'gradle' command.

__For [Eclipse]__  
  1. Run `gradle eclipse`
  2. Import Spongie as an existing project (File > Import > General)
  3. Select the root folder for Spongie and make sure `Search for nested projects` is enabled
  4. Check Spongie when it finishes building and click **Finish**

__For [IntelliJ]__  
  1. Make sure you have the Gradle plugin enabled (File > Settings > Plugins).  
  2. Click File > New > Project from Existing Sources > Gradle and select the root folder for Spongie.

## Running
__Note 1:__ The following is aimed to help you setup run configurations for Eclipse and IntelliJ, if you do not want to be able to run Spongie directly from your IDE then you can skip this.  
__Note 2:__ The first time Spongie runs, it will shut down. This is normal. You will need to modify the configuration file to enable plugins, refer to the [wiki] for details.  

__For [Eclipse]__  
  1. Go to **Run > Run Configurations**.  
  2. Right-click **Java Application** and select **New**.  
  3. Set the current project.  
  4. Set the name as `Spongie` and apply the information below.

__For [IntelliJ]__  
  1. Go to **Run > Edit Configurations**.  
  2. Click the green + button and select **Application**.  
  3. Set the name as `Spongie` and apply the information below.

|     Property      | Value                                     |
|:-----------------:|:------------------------------------------|
|    Main class     | com.sk89q.eduardo.Eduardo                 |
|    VM options     | -Dfile.encoding=UTF-8 (Windows only)      |
| Program arguments | --config config.yml                       |
| Working directory | ./run/ (Included in project)              |
| Module classpath  | Spongie (IntelliJ Only)                   |

[Eclipse]: https://www.eclipse.org/
[Gradle]: https://www.gradle.org/
[IntelliJ]: https://www.jetbrains.com/idea/
[Issues]: https://github.com/SpongePowered/Spongie/issues/
[Wiki]: https://github.com/SpongePowered/Spongie/wiki/
[Java]: http://java.oracle.com/
[Scala]: http://scala-lang.org/
[Source]: https://github.com/SpongePowered/Spongie/
