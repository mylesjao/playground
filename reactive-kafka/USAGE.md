# SBT Project Skeleton

Skeleton for new SBT-based projects. Clone and use.

It defines a basic set of plugins, directory structure and other settings to help with starting
new projects. The content should be universal enough for any kind of project.

Every aspect in this skeleton has its reason based on research and good practice – for example version
numbering (semantic versioning schema), global tests etc. Please mind that when adjusting anything for your project.
 
### Features

 * **multi-module project** to allow fine-grained configuration
 * **support for tests** to help with writing unit tests and property-based tests
 * **benchmarking** to perform micro-benchmarks
 * **code quality assurance** which checks code for potential dangers
 * **packaging** to create distributable packages
 * **command aliases** to make routine tasks more convenient 

The default settings are intentionally very strict. Warnings are fatal (i. e. any compile-time warning
throws compiler error) and there is a wide set of warnings checking many details. It is recommended to
start with the default settings and only disable some warnings when absolutely necessary.

The goal of these strict settings is to help developers write safer code. Although warnings may not seem
to be fatal at the beginning, they usually signalize a weak point that carry potential danger for future.

However, important rule is that this skeleton should not be taken as unchangeable law. It is mainly a guidance
to start new projects faster. 

#### Code Quality Assurance

Skeleton incorporates range of compiler settings, WartRemover plugin to maintain code quality and test
coverage. All these settings can be adjusted in `build.sbt` file in root module under these values:

 * `wartRemoverSettings` → WartRemover settings (for details see https://github.com/puffnfresh/wartremover#warts )
 * `commonScalacOptions` → compiler settings (for details see `scalac -X` and `scalac -Y`)
 * `coverageSettings` → settings of test coverage (for details see https://github.com/scoverage/sbt-scoverage )
 
`wartRemoverSettings` and `commonScalacOptions` are written in format that allows to disable settings easily by commenting
the line out (you don't have to deal with commas).

If warning has to be disabled locally (e. g. it uses 3rd party deprecated method and cannot be used otherwise), one can use
`@silent` annotation:

```scala
import com.github.ghik.silencer.silent

@silent val badugly = List("hello", 100L, true)
```

This snippet would throw two warnings (inferred type parameter is `Any` and value `badugly` is never used). However, by using
the annotation, warning for this specific line is suppressed.

## Directory Structure

```
bench/
  src/main/scala/com/quadas/…   ← Scala source code for benchmarks
  build.sbt                     ← benchmark specific SBT configuration
core/
  src/main/scala/com/quadas/…   ← Scala source code of main logic
  build.sbt                     ← SBT configuration, put dependencies here
  packager.sbt                  ← configuration of sbt-native-packager
project/
  build.properties              ← SBT version used
  plugins.sbt                   ← SBT plugins
tests/
  src/test/scala/com/quadas/…   ← test source code
  build.sbt                     ← SBT configuration
tests-uat
  src/test/scala/com/quadas/…   ← UAT source code
  build.sbt                     ← UAT SBT configuration
.gitignore                      ← ignored files in GIT
build.sbt                       ← root SBT project
README.md                       ← information about project, do not forget to edit
```
## Examples

Skeleton also provides examples for each module (bench, test, core). New project may be based on these
examples.

## SBT Commands Aliases

### `sbt benchmark`

Runs benchmarks as defined in `bench/` directory. Calling this will take several minutes and produce similar lines:

```
[info] # Run complete. Total time: 00:08:19
[info] 
[info] Benchmark                             Mode  Cnt  Score   Error  Units
[info] FactorialBench.factorialFold            ss  100  0.777 ± 0.018   s/op
[info] FactorialBench.factorialMutableState    ss  100  0.711 ± 0.007   s/op
[info] FactorialBench.factorialRecursive       ss  100  0.850 ± 0.024   s/op
```

### `sbt validate`

Runs tests and coverage. If coverage is lower than 80 %, build fails. This setting can be changed in root `build.sbt`.

### `sbt package`, `sbt stage`

Creates distribution package. `sbt package` compiles project and creates .tgz package containing executable 
shell script and all dependencies. The resulting file is placed in `core/target/universal/`.

`sbt stage` performs 'dry run', i. e. it  only prepare directory structure but does not create .tgz package.
It is good for testing. For result visit `core/target/universal/stage/`.

Configuration of packaging is in `core/packager.sbt`. It uses [sbt-native-packager](http://www.scala-sbt.org/sbt-native-packager/).

### `sbt updates`

Lists new versions of library dependencies in all submodules.

### `sbt build`

Compiles source code, validates and creates .tgz package from `core` module.

### `sbt uat`

Runs UAT as defined in `tests-uat` directory.

### `sbt release`

Prepares project for release of new version. By default it asks which version is released and which version is next, runs
`sbt validate`, creates git tag and commits changes. It does not perform publishing (which is done by build server).
 
Example:

```
$> sbt release
Release version [0.1.0] :                 // type version or confirm suggestion
Next version [0.1.1-SNAPSHOT] :           // type next version or confirm suggestion
// validation (test + coverage)
// create tag (v0.1.0)
// change version in version.sbt
// commit & push changes
```
