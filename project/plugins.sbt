resolvers += "spray repo" at "http://repo.spray.io"

addSbtPlugin("io.spray" % "sbt-revolver" % "0.8.0")

addSbtPlugin("io.spray" % "sbt-twirl" % "0.7.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.1")

logLevel := Level.Warn
