# @Command

The `@Command` Annotation is the main annotation used to define a command. It is used to define the command name, its
aliases and all subcommands.

It can only be used on classes.

```java
@Command(value = "foo", subCommands = {TestSubCommand.class})
public static class TestCommand {}
```