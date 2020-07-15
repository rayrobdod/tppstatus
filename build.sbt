lazy val root = (project in file("."))
	.enablePlugins(com.typesafe.sbt.web.SbtWeb)
	.enablePlugins(GhpagesPlugin)
	.settings(
		name := "tpp-status",
		organization := "com.rayrobdod",
		organizationHomepage := Some(new URL("http://rayrobdod.name/")),
		version := "SNAPSHOT",
	)
	.settings(
		git.remoteRepo := "https://rayrobdod@github.com/rayrobdod/tppstatus",
		(ghpagesSynchLocal / mappings) := com.typesafe.sbt.web.Import.WebKeys.pipeline.value,
		ghpagesCommitOptions := Seq("-m", s"Render of ${git.gitHeadCommit.value.get}"),
	)
