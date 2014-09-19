Eduardo
=======

Eduardo is a command and chat bot written in Java.

* Connects to IRC, Skype, and anything that you want to throw at it
* Embedded web server
* Extremely simple architecture
* Extremely powerful
* Access to Java's huge 3rd party library ecosystem

Features
--------

### Easily-registered commands

* Support for checking permissions
* Support for rate limiting per-host mask, per-channel, and globally

```java
@AutoRegister public class HelloWorld {
  @Command(aliases = "hello", desc = "Say hello back")
  public void helloWorld(Response response) {
    response.respond("Hello world!")
  }
}
```

### Easily-registered web front end

* Powered by a Sintara-inspired API (via the Spark library)
* Support for static files
* Support for the Mustache templating engine

```java
@AutoRegister public class HelloWorld {
  @Subscribe public void onConfigureRoute(ConfigureRouteEvent event) {
    get("/hello/", (req, resp) -> "Hello World!");
  }
}

```

### IoC-powered

```java
public class Shortener {
  @Inject private URLShortener shortener;

  public void doSomething() {
    URL shortened = shortener.shorten(...);
  }
}
```

### JDBC-based persistence with migration support

* Support for automatic table migrations
* Access data using jOOQ

```java
Bucket b = persistence.connect("alias");

// Migration already performed and versioned

try (Connection conn = b.createConnection()) {
  DSLContext create = DSL.using(conn);

  Result<Record> record = create.select()
    .from(b.table("aliases"))
    .where(field("alias").eq(alias.toLowerCase()))
    .fetch();
  
  // etc.
}
```

### Powerful permissions

```perl
config-perms = {
  policy = [
    {
      users = [ "bob!*@*" ]
      grant = [
        "bob.all.the.things"
      ],
      deny = [
        "bob.all.the.things.but.not.some"
      ]
    },
    {
      channels = [ "#example" ]
      modes = [ OPERATOR ]
      grant = [ "example.perm" ]
    }
  ]
}
```

### Service-oriented

```perl
services = {
  mapping = {
    com.sk89q.eduardo.helper.shortener.URLShortener = com.sk89q.eduardo.helper.shortener.BitlyShortener
  }
}
```

### Easily configured

```perl
modules = {
  enabled = [
    com.sk89q.eduardo.irc.PircBotXClient,
    com.sk89q.eduardo.module.github.WebHookAnnouncer,
    com.sk89q.eduardo.module.Shortener,
  ]
}

irc = {
  version = Scott/1.0

  servers = [
    {
      id = local # server ID
      name = Scott
      host = localhost
      port = 7777
      password = "example"
      ssl = true
      auto-join = [ "#example" ]
    }
  ]
}
```

Compiling
---------

Use Maven 3 to compile the project.

    mvn clean package


Running
-------

    java -jar eduardo.jar -c yourconfig

Contributing
------------

Eduardo is available under the GNU Lesser General Public License.

We happily accept contributions, especially through pull requests on GitHub.

