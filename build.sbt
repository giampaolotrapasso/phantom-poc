import com.typesafe.sbt.SbtScalariform._

name := """phantom-poc"""

version := "1.0"

scalaVersion := "2.11.7"
lazy val phantomVersion = "1.12.2"
lazy val cassandraVersion = "2.1.4"

resolvers ++= Seq(
  "Typesafe repository snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
  "Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype snapshots"               at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging",
  "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
  "Twitter Repository"               at "http://maven.twttr.com",
  Resolver.url(
    "com.websudos",
     url("https://dl.bintray.com/websudos/oss-releases/"))(
       Resolver.ivyStylePatterns)

)

// Change this to another test framework if you prefer
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "com.websudos" %% "phantom-dsl" % phantomVersion,
  "com.websudos" %% "phantom-testkit" % phantomVersion,
  "joda-time" % "joda-time" % "2.9.1",
  "org.apache.cassandra" % "cassandra-all" % cassandraVersion,
  "org.apache.cassandra" % "cassandra-thrift" % cassandraVersion,
  "com.datastax.cassandra" % "cassandra-driver-core" % cassandraVersion,
  "commons-pool" % "commons-pool" % "1.6",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

// Uncomment to use Akka
//libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.11"

fork in run := true

scalariformSettings
