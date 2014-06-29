publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomExtra := (
  <url>http://github.com/mefellows/respite</url>
    <licenses>
      <license>
        <name>MIT License</name>
        <url>http://opensource.org/licenses/MIT</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:mefellows/respite.git</url>
      <connection>scm:git:git@github.com:mefellows/respite.git</connection>
    </scm>
    <developers>
      <developer>
        <id>mfellows</id>
        <name>Matt Fellows</name>
        <url>http://www.onegeek.com.au</url>
      </developer>
    </developers>)