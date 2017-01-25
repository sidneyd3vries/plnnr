# Source file structure

A source file consists of, in order:

1. License or copyright information, if present
2. Package statement: Not line-wrapped
3. Import statements: No wildcards, not line-wrapped, static and non-static in own block separated with one blank line, ASCII sort order
4. **Exactly one top-level class**

Exactly one blank line separates each section that is present.

# Ordering of class contents

The order you choose for the members and initializers of your class can have a great effect on learnability. However, there's no single correct recipe for how to do it; different classes may order their contents in different ways.
What is important is that each class uses some logical order, which its maintainer could explain if asked. For example, new methods are not just habitually added to the end of the class, as that would yield "chronological by date added" ordering, which is not a logical ordering.

# Formatting

Braces are used with if, else, for, do and while statements, even when the body is empty or contains only a single statement.

Braces follow the Kernighan and Ritchie style ("Egyptian brackets") for nonempty blocks and block-like constructs:

... No line break before the opening brace.
... Line break after the opening brace.
... Line break before the closing brace.
... Line break after the closing brace, only if that brace terminates a statement or terminates the body of a method, constructor, or named class. For example, there is no line break after the brace if it is followed by else or a comma.

### Example

```java
return new MyClass() {
  @Override public void method() {
    if (condition()) {
      try {
        something();
      } catch (ProblemException e) {
        recover();
      }
    } else if (otherCondition()) {
      somethingElse();
    } else {
      lastThing();
    }
  }
};
```

# Whitespace

A single blank line appears:

1. Between consecutive members or initializers of a class: fields, constructors, methods, nested classes, static initializers, and instance initializers.
2. Between statements, as needed to organize the code into logical subsections.
3. Optionally before the first member or initializer, or after the last member or initializer of the class (neither encouraged nor discouraged).

Instead of block indentation with +2 spaces, we use +4 spaces, because android studio uses that as standard

## Column limit 100
Exceptions
Lines where obeying the column limit is not possible (for example, a long URL in Javadoc, or a long JSNI method reference).
package and import statements (see Sections 3.2 Package statement and 3.3 Import statements).
Command lines in a comment that may be cut-and-pasted into a shell.

# Variable declaration

1. Every variable declaration (field or local) declares only one variable: declarations such as int a, b; are not used.
2. Local variables are not habitually declared at the start of their containing block or block-like construct. Instead, local variables are declared close to the point they are first used (within reason), to minimize their scope. Local variable declarations typically have initializers, or are initialized immediately after declaration.

# Switches

Each switch statement includes a default statement group, even if it contains no code.

# Naming

1. Package names are all lowercase, with consecutive words simply concatenated together (no underscores).
2. Class names are written in UpperCamelCase.
3. Method names are written in lowerCamelCase.
4. Constant names use CONSTANT_CASE: all uppercase letters, with words separated by underscores.
5. Parameter/local variable/ non-constant names are written in lowerCamelCase.

# Javadocs

Javadoc summary fragment is used for every class and member of a class.
