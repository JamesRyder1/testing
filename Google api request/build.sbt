ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "Google-account-status",
    libraryDependencies ++= Seq(
    "com.google.apis" % "google-api-services-content" % "v2.1-rev14-1.25.0",
      "com.google.oauth-client" % "google-oauth-client-jetty" % "1.34.1"
    )
  )
