Eduardo
=======

Eduardo is a command and chat bot written in Java 8 and Scala.

* Multi-server and multi-network
* Embedded web server (optional)
* Flexible and extensible
* Access to Java's huge 3rd party library ecosystem

## Usage

*This project is currently under development and the information below is tentative. In addition, there is currently little documentation.*

Run the bot with `java -jar build/libs/eduardo-all.jar --config config.yml` to generate a configuration file.

```
[info] com.sk89q.eduardo.Eduardo: Searching loaded libraries for known plugins...
[info] com.sk89q.eduardo.Eduardo: Plugins loaded; initializing...
[info] com.sk89q.eduardo.Eduardo: Initialization complete.
```

Since no plugins are configured, the bot quits immediately.

### Enabling Plugins

Open up the configuration file and add a list of plugins.

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

Run the bot again, and the configuration file will be filled with additional configuration for the enabled plugins.

The bot will likely quit again after initializing because you need to configure the IRC connector to connect to at least one server.

### Configuration

Modify the configuration file and fill in any additional needed information.

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

Now run the bot again.

## Writing Plugins

**Note:** The bot is under development and is subject to massive changes with no prior notice.

The following is Scala code.

```scala
@Plugin(id = "hello")
class HelloPlugin @Inject() (config: Config) {

  val apiKey = config.stringAt("hello.api-key", "")

  @Command(aliases = Array("hello"), desc = "Say hi")
  def helloCommand(r: Response, name: String) = {
    val response = Requests.request("GET", "https://example.com/api/hello",
      params = List(
        ("key", apiKey.get()),
        ("q", name)
      ))

    r.respond(response.statusCode match {
      case 200 => response.json(classOf[String])
      case _ => "Uh oh! Something went wrong"
    })
  }

}
```

* A `@Singleton` annotation can be put on `HelloPlugin` to prevent another plugin that references this plugin in an `@Inject` (relevant because dependency injection is used) from causing another instance of your plugin class to be created.
* `@Inject` is to tell [Guice](https://github.com/google/guice) to inject objects in the constructor (`config: Config`) magically(tm).
* `apiKey` is a (Java 8) `Supplier<String>` that reads from the latest version of the configuration when `.get()` is called. This allows changes to propagate even if the configuration is later modified.
* `@Command` automatically registers the command "hello".
* `Requests.request` is a HTTP library bundled with Eduardo that is modeled after Python's [Requests library](http://docs.python-requests.org/).

Plugins are detected at runtime by searching classpath for the `@Plugin` annotation.

### Service and Event-Oriented

Plugins provide specific services that can be consumed by other plugins. For example, there is a `URLShortener` interface that a a plugin can implement to provide shortening services to other plugins.

A plugin that provides must a service must also use the `@Provides` annotation as illustrated below, which causes Eduardo to create a Guice binding before plugin loading starts (if the plugin is enabled).

```java
@Provides(Array(classOf[URLShortener]))
```

### Upcoming Changes

* Add support for per-channel "features" to replace permissions for most use cases (the permissions code would still exist for administrative functions)
* Add support for per-channel preferences (i.e. to configure a greeting message for a greeting plugin)
* Make the need for `@RateLimit` less.

## Compiling

Use Gradle to compile the project.

    ./gradlew clean build

If you are on Windows, remove `./` from the line above and run it in command prompt.

Java 8 must be used to compile and run Eduardo. Please set your `JAVA_HOME` environment variable accordingly, as illustrated below for non-Windows systems.

	JAVA_HOME=/usr/lib/jvm/jdk1.8.x_xx/ ./gradlew clean build

## License

Eduardo is available under the GNU Lesser General Public License.

We happily accept contributions, especially through pull requests on GitHub.

