name := "AssistAttendance"

version := "1.0"

scalaVersion := "2.11.8"

// https://mvnrepository.com/artifact/org.json4s/json4s-native_2.11
libraryDependencies += "org.json4s" % "json4s-native_2.11" % "3.5.0"

// https://mvnrepository.com/artifact/net.gpedro.integrations.slack/slack-webhook
libraryDependencies += "net.gpedro.integrations.slack" % "slack-webhook" % "1.2.1"

// https://mvnrepository.com/artifact/com.google.api-client/google-api-client
libraryDependencies += "com.google.api-client" % "google-api-client" % "1.22.0"

// https://mvnrepository.com/artifact/com.google.apis/google-api-services-sheets
libraryDependencies += "com.google.apis" % "google-api-services-sheets" % "v4-rev34-1.22.0"

// https://mvnrepository.com/artifact/com.google.oauth-client/google-oauth-client-jetty
libraryDependencies += "com.google.oauth-client" % "google-oauth-client-jetty" % "1.22.0"

// https://mvnrepository.com/artifact/org.scalatest/scalatest_2.11
libraryDependencies += "org.scalatest" % "scalatest_2.11" % "3.0.1"
