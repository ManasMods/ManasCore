# Permission

Permissions can be configured for each Command, SubCommand and Executor.

## Command Permissions

To set a permission for a command, you can use the `@Permission` annotation on the Command Class.

```java
@Permission("modid.command.foo")
@Command("foo")
public static class TestCommand { }
```

## SubCommand Permissions

To set a permission for a subcommand, you can use the `@Permission` annotation on the SubCommand Class.

```java
@Permission("modid.command.bar")
@Command("bar")
public static class TestSubCommand { }
```

## Executor Permissions

To set a permission for an executor method, you need to use a [Literal](arguments/04-Literal.md) parameter and the `@Permission` annotation.

```java
@Command("foo")
public static class TestCommand {
    @Permission("modid.command.bar")
    @Execute
    public boolean bar(@Literal("bar") String bar) {
        return true;
    }
}
```