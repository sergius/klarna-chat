lazy val commonSettings = Seq(
  version := "1.0",
  scalaVersion := "2.11.5",
  libraryDependencies ++=
    Seq(
      "com.typesafe.akka" %% "akka-actor"   % "2.3.9",
      "com.typesafe.akka" %% "akka-agent"   % "2.3.9",
      "com.typesafe.akka" %% "akka-remote"  % "2.3.9",
      "com.typesafe.akka" %% "akka-slf4j"   % "2.3.9",
      "com.typesafe.akka" %% "akka-testkit" % "2.3.9"   % "test",
      "org.scalatest"     %% "scalatest"    % "2.2.3"   % "test",
      "org.specs2"        %% "specs2"       % "2.4.15"  % "test"
    )
)


lazy val root =
  project.in(file(".")).
    settings(
      name := "klarna-chat"
    )
    .aggregate(server, client)


lazy val server = project.in(file("chat-server")).
  settings(commonSettings: _*).
  settings(
    name := "chat-server"
  )


lazy val client = project.in(file("chat-client")).
  settings(commonSettings: _*).
  settings(
    name := "chat-client"
  ).dependsOn(server)
    