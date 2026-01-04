# Project Style Guide
## Naming Conventions
The following specifies naming conventions for use in the project.

### General Naming Conventions

| To Be Named            | Naming Convention | Examples                              |
|------------------------|-------------------|---------------------------------------|
| Classes                | PascalCase        | `StewItem`                            |
| Fields (incl. methods) | camelCase         | `Game.name`, `Stew.getSimilarity`     |
| Local Variables        | camelCase         | `category`, `stewFilter`, `nextValue` |

### Acronyms
Acronyms shall be capitalized according to the following rules:
- **Short acronyms** (2 characters) should have the same case for both letters.
Examples:
  * `IDGenerator`  (class name)
  * `idGenerator`  (local variable)
  * `idGenerator.generateID`  (method name)

- **Longer acronyms** should be treated as though they were words. Examples:
  * `UrlVisualizer`  (class name)
  * `Game.coverUrl`  (field name)
  * `urlVisualizer`  (local variable)