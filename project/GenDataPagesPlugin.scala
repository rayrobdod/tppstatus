package com.rayrobdod.tpporg

import sbt._
import Keys._
import java.nio.file.{Path, Files}
import java.nio.charset.StandardCharsets.UTF_8
import com.typesafe.sbt.web.Import.WebKeys.webTarget
import com.typesafe.sbt.web.Import.Assets

object GenDataPagesPlugin extends AutoPlugin {
	object autoImport {
		val genDataPages = taskKey[Seq[File]]("")
	}
	import autoImport.genDataPages

	override def requires = com.typesafe.sbt.web.SbtWeb
	override def trigger = allRequirements

	override lazy val projectSettings = Seq(
		Assets / genDataPages / sourceDirectory := (Compile / sourceDirectory).value / "database",
		Assets / genDataPages / includeFilter := "*.json",
		Assets / genDataPages / sources := {
			(Assets / genDataPages / sourceDirectory).value **
				((Assets / genDataPages / includeFilter).value --
				(Assets / genDataPages / excludeFilter).value)
		}.get,
		Assets / genDataPages := {
			import org.json4s.native.JsonMethods.parse
			val streamsvalue = streams.value
			val baseDir = (Assets / genDataPages / sourceDirectory).value.toPath
			val datas = (Assets / genDataPages / sources).value.map({(fileName:File) =>
				var r:java.io.Reader = new java.io.StringReader("{}")
				try {
					r = Files.newBufferedReader(fileName.toPath, UTF_8)
					extractPageData(parse(r)).copy(
						fileName = baseDir.relativize(fileName.toPath).toString.dropRight(4) + "xhtml",
						genderElemFunc = (if (fileName.toPath.toString.endsWith("touhoumon.json")) {GenderElemFunctions.yinyang} else {GenderElemFunctions.normal})
					)
				} catch {
					case e:java.text.ParseException => {
						streamsvalue.log.error("Could not read file " + fileName + "\n" + e.getMessage)
						null
					}
					case e:IllegalArgumentException => {
						streamsvalue.log.error("Invalid file: " + fileName + "\n" + e.getMessage)
						null
					}
				} finally {
					r.close();
				}
			}).filter{_ != null}

			streams.value.log.info("Creating " + datas.size + " html pages")

			datas.map({(data:PageData) =>
				val fileName = (resourceManaged in Assets).value / data.fileName

				val elem = PageTemplates.overallPage(data)
				val prefix = """<?xml version="1.0" encoding="utf-8"?>""" + "\n<!DOCTYPE html>\n"

				IO.createDirectory(fileName.getParentFile)
				IO.write(fileName, prefix ++ elem.toString, UTF_8)
				fileName
			})
		},
		Assets / resourceGenerators += (Assets / genDataPages).taskValue
	)
}
