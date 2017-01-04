lazy val commonSettings = Seq(
  organization := "eu.tznvy",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.0",
  libraryDependencies ++= Seq(
    Dependencies.scalatest
  ),
  test in assembly := {}
)

lazy val jancyCore = project
  .in(file("jancy-core"))
  .settings(commonSettings: _*)

lazy val submodules = TaskKey[Unit]("submodules", "Initialize submodules")

lazy val submodulesSettings =
  submodules := {
    streams.value.log.info("Testing if submodules should be initialized")
    val submodulesFiles = file("submodules").listFiles
    if (submodulesFiles == null || submodulesFiles.isEmpty || submodulesFiles.exists(_.listFiles.isEmpty)) {
      streams.value.log.info("Empty dir found, initializing submodules ...")
      Seq("git", "submodule", "init").!
      Seq("git", "submodule", "update").!
    }
  }

lazy val jancyModulesGen = project
  .in(file("jancy-modulesgen"))
  .dependsOn(jancyCore)
  .settings(commonSettings: _*)
  .settings(
    mainClass in Compile := Some("eu.tznvy.jancy.modulesgen.Main"),
    libraryDependencies ++= Seq(
      Dependencies.snakeyaml,
      Dependencies.handlebars,
      Dependencies.scalaArm
    ),
    submodulesSettings,
    compile in Compile := (compile in Compile).dependsOn(submodules).value
  )

lazy val generateSources = TaskKey[Seq[java.io.File]]("generateSources", "Generate sources")

lazy val generateSourcesSettings =
  generateSources := {
    streams.value.log.info("Testing if jancy-modules should be regenerated")

    def getLastModificationDate(f: File): Long =
      if (f.exists) maxOrZero(Helpers.getFilesRecursively(f).map(_.lastModified))
      else 0

    def maxOrZero(xs: Seq[Long]) = if (xs.isEmpty) 0.toLong else xs.max

    val modulesGenLastModified = getLastModificationDate(file("jancy-modulesgen/src"))
    val modulesLastModified = getLastModificationDate(file("jancy-modules/src"))
    val submodulesLastModified = getLastModificationDate(file("submodules"))

    if (modulesGenLastModified >= modulesLastModified || submodulesLastModified > modulesLastModified) {
      streams.value.log.info("jancy-modulesgen changed, regenerating jancy-modules sources ...")

      Seq("rm", "-rf", "jancy-modules/src/*").!

      (testLoader in Test in jancyModulesGen)
        .value
        .loadClass("eu.tznvy.jancy.modulesgen.Main")
        .getMethod("main", Array[String]().getClass)
        .invoke(null, Array[String]())

      Helpers.getFilesRecursively(file("jancy-modules/src")).filter(_.getName.endsWith(".java")).map(_.getAbsoluteFile)
    } else Seq[java.io.File]()
  }

lazy val jancyModules = project
  .in(file("jancy-modules"))
  .dependsOn(jancyCore)
  .settings(commonSettings: _*)
  .settings(
    generateSourcesSettings,
    sourceGenerators in Compile += generateSources.taskValue,
    cleanFiles += file("jancy-modules/src")
  )

lazy val jancyCommon = project
  .in(file("jancy-common"))
  .dependsOn(jancyModules, jancyCore)
  .settings(commonSettings: _*)
  .settings(
    name := "jancy-common",
    artifactName in (Compile, packageBin) := { (scalaVersion: ScalaVersion, module: ModuleID, artifact: Artifact) =>
      artifact.name + "-" + module.revision + "." + artifact.extension
    },
   artifactName in (Compile, packageSrc) := { (scalaVersion: ScalaVersion, module: ModuleID, artifact: Artifact) =>
      artifact.name + "-" + module.revision + "-sources." + artifact.extension
    },
    mappings in (Compile, packageBin) ++= {
      Seq("jancy-core", "jancy-modules")
        .map(_ + "/target/scala-2.12/classes")
        .flatMap({ p =>
          Helpers.getFilesRecursively(file(p))
            .filter(_.getName.endsWith(".class"))
            .map({ f =>
              val output = f.getPath.substring(p.length + 1)
              (f, output)
            })
        })
    },
    mappings in (Compile, packageSrc) ++= {
      Seq("jancy-core", "jancy-modules")
        .map(_ + "/src/main/java")
        .flatMap({ p =>
          Helpers.getFilesRecursively(file(p))
            .filter(_.getName.endsWith(".java"))
            .map({ f =>
              val output = f.getPath.substring(p.length + 1)
              (f, output)
            })
        })
    }
  )

lazy val jancyTranspiler = project
  .in(file("jancy-transpiler"))
  .dependsOn(jancyModules)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.snakeyaml,
      Dependencies.scalaArm,
      Dependencies.commonsCli
    ),
    name := "jancy-transpiler",
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(prependShellScript = Some(sbtassembly.AssemblyPlugin.defaultShellScript)),
    assemblyJarName in assembly := "jancy"
  )

TaskKey[Unit]("buildAll", "Build all artifacts") := {
  (packageBin in Compile in jancyCommon).value
  (packageSrc in Compile in jancyCommon).value
  (assembly in jancyTranspiler).value
}

lazy val lampSimpleExample = project
  .in(file("examples/lamp_simple"))
  .dependsOn(jancyCommon)
  .settings(commonSettings: _*)

lazy val examples = project
  .aggregate(lampSimpleExample)
