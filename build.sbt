name := "tpp-status"

organization := "com.rayrobdod"

organizationHomepage := Some(new URL("http://rayrobdod.name/"))

version := "SNAPSHOT"

pipelineStages := Seq(filter)

includeFilter in filter := "*.json"
