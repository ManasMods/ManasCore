# @Literal

The `@Literal` Annotation is used to define a literal argument in the command.

```java
@Execute
public boolean example(@Literal("perms") String perms) {
    return true;
}
```

## Valid Parameter Types

- `String`