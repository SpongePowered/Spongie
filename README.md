Eduardo
=======

Eduardo is a command and chat bot written in Java and Scala.

* Multi-server and multi-network
* Embedded web server (optional)
* Flexible and extensible
* Access to Java's huge 3rd party library ecosystem

**WARNING:** The bot is under development and is subject to massive changes with no notice.  There are currently some unfinished parts that exist in a state merely to get everything else working.

## Usage

Run the bot with `java -jar eduardo-all.jar path_to_config.yml`

The configuration file will be created if it does not exist, and at which point, you can fill it with plugins after shutting down the bot:

```yaml
plugins:
  enabled:
  - alias
  - irc
  - responses
  - perms-tester
  - google-search
  - shortener
  - help
```

Some plugins need more configuration, so those entries will be added to the configuration file. Stop the bot, edit the file and start the bot again.

A connection to an IRC server can be configured as illustrated below:

```yaml
irc:
  servers:
  - auto-join:
    - '#example'
    ssl: false
    password: ''
    port: 6667
    host: example.com
    name: your_bot_name
    id: myserver
```

## Writing Plugins

**WARNING:** The bot is under development and is subject to massive changes with no notice.  There are currently some unfinished parts that exist in a state merely to get everything else working.

```scala
@Plugin(id = "hello")
class HelloPlugin {

  @Command(aliases = Array("hello"), desc = "Say hi")
  def helloCommand(response: Response, name: String) = {
    response.respond(s"Hi there, $name!")
  }

}
```

A `@Singleton` annotation can be put on `HelloPlugin` to prevent another plugin that references this plugin in an `@Inject` (relevant because dependency injection is used) from causing another instance of your plugin class to be created.

### Upcoming big changes

* Add support for per-channel "features" to replace permissions for most use cases (the permissions code would still exist for administrative functions)
* Add support for per-channel preferences (i.e. to configure a greeting message for a greeting plugin)
* Make the need for `@RateLimit` less.

## Compiling

Use Gradle to compile the project.

    ./gradlew clean build

If you are on Windows, remove `./` from the line above and run it in command prompt.

## License

Eduardo is available under the GNU Lesser General Public License.

We happily accept contributions, especially through pull requests on GitHub.

