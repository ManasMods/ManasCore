# @Command

The `@Command` annotation is the main annotation used to define a command. It is used to define the command name, its
aliases and all subcommands.

It can only be used on classes.

```java
@Command(value = "foo", subCommands = {TestSubCommand.class})
public static class TestCommand {}
```

## Aliases

To define aliases for a command, you can just pass multiple values into the `@Command` annotation.

```java
@Command({"foo", "alias1", "alias2"},)
public static class TestCommand {}
```