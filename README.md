**Tested on Java LTS versions from <!--property:java-runtime.min-version-->8<!--/property--> to <!--property:java-runtime.max-version-->21<!--/property-->.**

**Tested on Gradle versions from <!--property:gradle-api.min-version-->6.0<!--/property--> to <!--property:gradle-api.max-version-->8.11<!--/property-->.**

# `name.remal.merge-resources` plugin

[![configuration cache: supported from v2](https://img.shields.io/static/v1?label=configuration%20cache&message=supported+from+v2&color=success)](https://docs.gradle.org/current/userguide/configuration_cache.html)

This plugin configures all [`AbstractCopyTask`](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/AbstractCopyTask.html) tasks to merge files with the same [`RelativePath`](https://docs.gradle.org/current/javadoc/org/gradle/api/file/RelativePath.html).

The main use cases are:

* building fat JARs
* generating some files via annotation processors and configuring the same files manually

## Configuration

```groovy
mergeResources {
  metaInfServices {
    enabled = false // To disable merging `META-INF/services/*`
  }
  packageInfo {
    enabled = false // To disable merging `META-INF/services/*`
  }
  metaInfServices {
    enabled = false // To disable merging `**/package-info.class`
  }
  moduleInfo {
    enabled = false // To disable merging `**/module-info.class`
  }
  springFactories {
    enabled = false // To disable merging `META-INF/spring.factories`
  }
  springImports {
    enabled = false // To disable merging `META-INF/spring/*.imports`
  }

  // To merge all `*.jar` files with the same relative path:
  addResourceMerger('**/*.jar') { RelativePath relativePath, Collection<File> files, OutputStream outputStream ->
    outputStream.write(new byte[0])
  }

  // To merge all `*.text` files with the same relative path:
  addTextResourceMerger('**/*.jar', 'US-ASCII') { RelativePath relativePath, Collection<File> files, PrintWriter writer ->
    writer.println('test')
  }
  addTextResourceMerger('**/*.jar' /* UTF-8 is default charset */) { RelativePath relativePath, Collection<File> files, PrintWriter writer ->
    writer.println('test')
  }
}
```

## Built-in mergers

### `META-INF/services/*`

Excluding:

* `META-INF/services/org.codehaus.groovy.runtime.ExtensionModule`

These files are used for Java's [`java.util.ServiceLoader`](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html).

When these files are merged, all comments and duplications are removed.

### `**/package-info.class`

The file with the most number of package annotations is taken.

For the other files, there is a check that validates that their package annotations present in the result file.

### `**/module-info.class`

Different sections of `module-info` are merged.

It's required that all merged files have the same module name.

### `META-INF/spring.factories`

See [`org.springframework.core.io.support.SpringFactoriesLoader`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/io/support/SpringFactoriesLoader.html).

### `META-INF/spring/*.imports`

See [`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-auto-configuration) files.

See [`org.springframework.boot.context.annotation.ImportCandidates`](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/context/annotation/ImportCandidates.html).
