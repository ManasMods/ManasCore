# @Sender

The `@Sender` Annotation is used to define the parameter that should be injected with the sender of the command.

```java
@Execute
public boolean example(@Sender CommandSourceStack sender) {
    return true;
}
```

## Valid Parameter Types

- `CommandSourceStack`
- `CommandSource`
- `ServerPlayer`

:::note
The `ServerPlayer` type automatically pevents the command from being executed by the console.
:::
