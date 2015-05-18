package com.rayrobdod.tpporg

import sbt._
import Keys._
import java.nio.file.{Path, Files}
import java.nio.charset.StandardCharsets.UTF_8
import com.typesafe.sbt.web.Import.WebKeys.webTarget
import com.typesafe.sbt.web.Import.Assets

object MyBuild extends Build {
	
	val dataFiles = TaskKey[Seq[File]]("dataFiles")
	val dataObjects = TaskKey[Seq[PageData]]("dataObjects")
	val pages = TaskKey[Seq[File]]("pages")
	
	
	def pageCreationSettings = Seq(
		sourceDirectory in dataFiles in Assets <<= (sourceDirectory in Assets),
		includeFilter in dataFiles in Assets := {
			new FileFilter{
				def accept(f:File) = (f.toString endsWith ".json")
			}
		},
		dataFiles in Assets := {
			(sourceDirectory in dataFiles in Assets).value **
					((includeFilter in dataFiles in Assets).value --
					(excludeFilter in dataFiles in Assets).value)
		}.get,
		dataObjects in Assets := {
			import com.rayrobdod.tpporg.PageDataBuilder
			import com.rayrobdod.json.parser.JsonParser
			
			(dataFiles in Assets).value.map{fileName:File =>
				
				val baseDir = (sourceDirectory in dataFiles in Assets).value.toPath
				var r:java.io.Reader = new java.io.StringReader("{}"); 
				try {
					r = Files.newBufferedReader(fileName.toPath, UTF_8)
					
					new JsonParser(new PageDataBuilder()).parse(r)
							.copy(identifier = baseDir.relativize(fileName.toPath).toString.dropRight(5))
				} catch {
					case _:java.text.ParseException => {
						streams.value.log.error("Could not read file " + fileName)
						null
					}
					case e:IllegalArgumentException => {
						streams.value.log.error("Invalid file: " + fileName + "\n" + e.getMessage)
						null
					}
				} finally {
					r.close();
				}
			}.filter{_ != null}
		},
		pages in Assets := {
			val pageInfos = (dataObjects in Assets).value
			streams.value.log.info("Creating " + pageInfos.size + " pages")
			
			pageInfos.flatMap({(pageInfo:PageData) =>
				val readFileName = (resourceManaged in Assets).value / pageInfo.fileName
				val writeFileName = (resourceManaged in Assets).value / pageInfo.editFileName
				
				val readElem = PageTemplates.overallPage(pageInfo, false)
				val writeElem = PageTemplates.overallPage(pageInfo, true)
				
				IO.createDirectory(readFileName.getParentFile)
				IO.createDirectory(writeFileName.getParentFile)
				IO.write(readFileName, readElem.toString, UTF_8)
				IO.write(writeFileName, writeElem.toString, UTF_8)
				
				Seq(readFileName, writeFileName)
			})
		},
		resourceGenerators in Assets <+= pages in Assets
	)
	
	lazy val root = Project(
			id = "tpporg",
			base = file("."),
			settings = {
				pageCreationSettings ++
				Nil
			}
	).enablePlugins(com.typesafe.sbt.web.SbtWeb)
}
