resolvers += Resolver.sonatypeRepo("releases")

resolvers ++= Seq( 
  "Typesafe Releases Repository" at "http://repo.typesafe.com/typesafe/releases/", 
  Resolver.url("sbt snapshot plugins", url("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots"))(Resolver.ivyStylePatterns), 
  Resolver.sonatypeRepo("snapshots"), 
  "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/", 
  Resolver.mavenLocal 
) 

libraryDependencies += ("no.arktekk" %% "anti-xml" % "0.6.0")

libraryDependencies += ("com.rayrobdod" %% "json" % "2.0-RC1")
