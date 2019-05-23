# General

#Code style
The DotWebStack framework uses Java code conventions based on the Google Java Style Guide and provides a
 [checkstyle](http://checkstyle.sourceforge.net/) configuration `checkstyle.xml`to check most conventions. 
 Conventions that are not covered by the checkstyle plugin are described below.

##CheckStyle
### Intellij
Intellij can be configured to use the checkstyle.xml file to auto-format Java code.
1. Add the [checkstyle plugin](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea).
2. Add the DotWebStack `checkstyle.xml` as the active profile in Settings->Other Settings->Checkstyle.
3. Import the `checkstyle.xml`in Settings->Editor->Code Style->Scheme->Import scheme.

Reformatting the code with Ctrl+Alt+L will now use the imported code style.

### Eclipse

## Additional conventions
The following conventions are not validated by checkstyle and should be applied manually:
* `final`: only use the `final` for classes and class fields. Checkstyle provides the `ParameterAssignment` as 
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
    * @RequiredArgsConstructor
    * @NonNull
    * @Data
    * @Slf4j



