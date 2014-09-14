Eduardo
=======

Eduardo is an IRC bot (+ framework).

* PircBotX powered
* Jetty powered
* Modules are not hot-(re)loadable

Features
--------

### IoC-powered

```java
public class Shortener {
    @Inject private URLShortener shortener;

	public void doSomething() {
		URL shortened = shortener.shorten(...);
	}
}
```

### Simple commands

```java
public class HelloWorld {
    @Inject
    public HelloWorld(CommandProcessor processor) {
        processor.registerCommands(this);
    }

    @Command(aliases = "hello", desc = "Say hello back")
    public void helloWorld(Response response) {
        response.respond("Hello world!")
    }
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

### Web-accessible

```java
public class HelloWorld extends SimpleHandler {
    @Inject
    public HelloWorld(JettyService jetty) {
        ContextHandler handler = new ContextHandler("/hello");
        handler.setHandler(this);
        jetty.registerHandler(handler);
    }

    @Override
    public SimpleResponse respond(String target, Request baseRequest, 
                                  HttpServletRequest request, HttpServletResponse response) {
        return create().body("Hello wolrd!");
    }

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

