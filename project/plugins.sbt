resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.0.0")

//Plugin to produce dep-graph.
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.5.2")

//------------------ Avro Plugin
resolvers += "cavorite" at "http://files.cavorite.com/maven/"

addSbtPlugin("com.cavorite" % "sbt-avro" % "0.1")