# @Execute

The `@Execute` Annotation is used to define the method that should be called when the command is executed. All
parameters of this method need to be annotated with the appropriate annotations to be injected by the command
dispatcher.

## Available Parameter Types

The following parameter types can be used in execution methods

### `Boolean` / `boolean`

They require a [@Bool](arguments/05-Boolean.md) Annotation.

### `Double` / `double`

They require a [@DoubleNumber](arguments/09-Double.md) Annotation.

### `Float` / `float`

They require a [@FloatNumber](arguments/08-Float.md) Annotation.

### `Integer` / `int`

They require a [@IntegerNumber](arguments/06-Integer.md) Annotation.

### `Long` / `long`

They require a [@LongNumber](arguments/07-Long.md) Annotation.

### `String`

They require a [@Text](arguments/10-String.md) Annotation.

### `CommandSourceStack` / `CommandSource` / `ServerPlayer`

They require a [@Sender](arguments/03-Sender.md) Annotation.

### Enum

They require a [@Enum](arguments/11-Enum.md) Annotation.

### Literal

They require a [@Literal](arguments/04-Literal.md) Annotation.

