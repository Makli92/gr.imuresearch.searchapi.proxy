import play.PlayJava


name := """gr.imuresearch.searchapi.proxy"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"



libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs
)

libraryDependencies += "org.jsoup" % "jsoup" % "1.7.2"

libraryDependencies += "commons-lang" % "commons-lang" % "2.6"

libraryDependencies += "org.json" % "json" % "20080701"

