# General
This page describes the development guidelines that should be followed when contributing to the project.

# Code style
The DotWebStack framework uses Java code conventions based on the Google Java Style Guide. Most conventions
are enforced by the Checkstyle and Spotless plugins as described below. The conventions that are not supported
by these tools are described below should be applied manually.

## CheckStyle
The project uses the [checkstyle](http://checkstyle.sourceforge.net/) plugin during the validation stage, using
 the configuration `checkstyle.xml`to check most conventions.

## Spotless
The project uses the [spotless](https://github.com/diffplug/spotless/tree/master/plugin-maven) plugin during the validation stage.
The plugin can also be used to auto-format the code, using `mvn spotless:apply`.

## IDE support
### Intellij
Intellij can be configured to use the checkstyle.xml file to auto-format Java code.
1. Add the [checkstyle plugin](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea).
2. Add the DotWebStack `checkstyle.xml` as the active profile in Settings-->Other Settings-->Checkstyle.
3. Import the `checkstyle.xml`in Settings-->Editor-->Code Style-->Scheme-->Import scheme.

Reformatting the code with Ctrl+Alt+L will now use the imported code style.

Alternatively, the Eclipse formatter file can be imported after installing the Eclipse Code Formatter.

### Eclipse
Eclipse can be configured to use the included formatter and import order file.
1. Import the Eclipse formatter `eclipse-java-style-dws.xml` in Window-->Preferences-->Java-->Code Style-->Formatter-->Import.
2. Import the Eclipse import order `dws.importorder` in Window-->Preferences-->Java-->Code Style-->Organize Imports-->Import.
3. Enable auto formatting and import organizing on save in Window-->Preferences-->Java-->Editor-->Save Actions.

## Additional conventions
The following conventions are not validated by checkstyle and should be applied manually:
* `final`: only use `final` for classes and class fields. Checkstyle provides the `ParameterAssignment` as 
an alternative to declaring method and constructor arguments as final.
* private constructors: private constructors used to hide public constructors for utility classes should
not throw exceptions.
* imports: the static imports block should be separated the non-static imports block by a newline. 
Import groups should not be separated by a newline.
* last class member newline: the last class member should not be followed by a newline before the last `}`.
* constructor and method line wrapping: constructors and methods should be wrapped at the start of the first
argument.

# Library guidelines
* immutable collections: whenever possible, collections should be made immutable using the [Guava](https://github.com/google/guava) library.
* [lombok](https://projectlombok.org/): Lombok should be used to decrease boilerplate java code in the project. The following should be
standard:
    * @NonNull
    * @Data
    * @Slf4j



