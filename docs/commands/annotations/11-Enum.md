# @Enum
The `@Enum` Annotation is used to define a parameter as an `Enum` value in the execution method of a command.
Enums are a special case in terms of usage. They need to be registered using the `CommandArgumentRegistrationEvent`.

```java
CommandArgumentRegistrationEvent.EVENT.register(registry -> {
    registry.registerEnum(TestEnum.class);
});
```

after registering the enum you can use it in your command execution method.

```java
@Execute
public boolean example(@Enum(TestEnum.class) TestEnum _enum) {
    return true;
}
```