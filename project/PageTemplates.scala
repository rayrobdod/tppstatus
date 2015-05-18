package com.rayrobdod.tpporg

import sbt._
import java.nio.file.Files
import java.util.Date
import java.text.SimpleDateFormat
import com.codecommit.antixml.{Group, Text, Elem, Attributes,
		Node, NamespaceBinding, XMLConvertable, ProcInstr
}

object PageTemplates {
	private val HTML_NAMESPACE = "http://www.w3.org/1999/xhtml"
	private val htmlBinding = NamespaceBinding(HTML_NAMESPACE)
	private val xmlProcInstr = ProcInstr("xml", "version=\"1.0\" encoding=\"utf-8\"")
	private object htmlDoctype extends ProcInstr("DOCTYPE", "html") {
		override def toString = "<!DOCTYPE html>"  
	}
	
	
	def overallPage(pageData:PageData, isEditing:Boolean):Group[Node] = {
		val data:Group[Node] = Group(
			Elem(htmlBinding, "h1", Attributes(), Group(Text("Twitch Plays Pokémon - " + pageData.gameName))),
			Elem(htmlBinding, "div", Attributes("class" -> "lastUpdated"), Group(
				Text("Last Updated: "),
				TextValue("lastUpdate", pageData.lastUpdate, isEditing)
			)),
			Elem(htmlBinding, "div", Attributes("class" -> "checkpoint"), Group(
				Text("Checkpoint: "),
				TextValue("checkpoint", pageData.checkpoint, isEditing)
			)),
			Elem(htmlBinding, "section", Attributes(), Group(
				Elem(htmlBinding, "h2", Attributes(), Group(Text(pageData.monsterType + " in Party"))),
				listOfPartyPokemon(pageData.party, isEditing)
			)),
			Elem(htmlBinding, "section", Attributes(), Group(
				Elem(htmlBinding, "h2", Attributes(), Group(Text("Badges"))),
				listOfBadges(pageData.badges, isEditing)
			)),
			Elem(htmlBinding, "section", Attributes(), Group(
				Elem(htmlBinding, "h2", Attributes(), Group(Text("Daycare"))),
				listOfBoxedPokemon(pageData.daycare, "daycare", isEditing)
			)),
			Elem(htmlBinding, "section", Attributes(), Group(
				Elem(htmlBinding, "h2", Attributes(), Group(Text("Boxed"))),
				listOfBoxedPokemon(pageData.box, "box", isEditing)
			))
		)
		val dataInForm:Group[Node] = {
			if (isEditing) {
				Group(
					Elem(htmlBinding, "form", Attributes(
							"method" -> "POST",
							"enctype" -> "application/x-www-form-urlencoded",
							"action" -> "http://localhost:8086/edit.py"
					), (
						Elem(htmlBinding, "input", Attributes("type" -> "text", "name" -> "authToken")) +:
						Elem(htmlBinding, "input", Attributes("type" -> "hidden", "name" -> "identifier", "value" -> pageData.identifier)) +:
						Elem(htmlBinding, "input", Attributes("type" -> "submit", "value" -> "submit")) +:
						data)
					)
				)
			} else {
				data
			}
		}
		
		
		
		Group(xmlProcInstr, Text("\n"), htmlDoctype, Text("\n"),
			Elem(htmlBinding, "html", Attributes("lang" -> "en-US"), Group(
				Elem(htmlBinding, "head", Attributes(), Group(
					Elem(htmlBinding, "title", Attributes(), Group(Text("Twitch Plays Pokémon - " + pageData.gameName))),
					Elem(htmlBinding, "meta", Attributes("http-equiv" -> "content-type", "content" -> "application/xhtml+xml")),
					Elem(htmlBinding, "link", Attributes("rel" -> "stylesheet", "href" -> "style/style.css"))
				)),
				Elem(htmlBinding, "body", Attributes(), Group(
					XMLConvertable.ElemConvertable(
						<header xmlns="http://www.w3.org/1999/xhtml">
							<nav class="remote">
								<a href="http://twitchplayspokemon.org/">TwitchPlaysPokemon.org</a>
								<a href="http://www.reddit.com/live/ut336fzq0red">Live Updater</a>
							</nav>
							<nav class="local">
								<a href="moemon.xhtml">Moemon</a>
								<a href="touhoumon.xhtml">Touhoumon</a>
							</nav>
						</header>
					),
					Elem(htmlBinding, "main", Attributes(), 
						dataInForm
					)
				))
			))
		)
	}
	
	def listOfPartyPokemon(list:Seq[Pokemon], isEditing:Boolean):Node = {
		Elem(htmlBinding, "table", Attributes("class" -> "party"),
			rowOfPokemon(list, "party", 0, isEditing)
		)
	}
	
	def listOfBoxedPokemon(list:Seq[Pokemon], key:String, isEditing:Boolean):Node = {
		val rows = list.grouped(6).zipWithIndex;
		Elem(htmlBinding, "table", Attributes("class" -> key), Group.fromSeq(
			rows.flatMap{x => rowOfPokemon(x._1, key, (x._2 * 6), isEditing)}.toList
		))
	}
	
	def rowOfPokemon(list:Seq[Pokemon], key:String, startIndex:Int, isEditing:Boolean):Group[Node] = {
		Group(
			Elem(htmlBinding, "tr", Attributes("class" -> "name"), Group.fromSeq(
				list.zipWithIndex.map{x =>
					Elem(htmlBinding, "td", Attributes(), Group(
						TextValue(key + "_" + (startIndex + x._2) + "_ingame", x._1.ingame.toString, isEditing),
						Elem(htmlBinding, "br"),
						Text("("),
						TextValue(key + "_" + (startIndex + x._2) + "_species", x._1.species.toString, isEditing),
						Text(")"),
						Elem(htmlBinding, "br"),
						TypeField(key + "_" + (startIndex + x._2) + "_type1", x._1.type1, isEditing),
						Text(" "),
						TypeField(key + "_" + (startIndex + x._2) + "_type2", x._1.type2, isEditing)
					))
				}
			)),
			Elem(htmlBinding, "tr", Attributes("class" -> "level"), Group.fromSeq(
				list.zipWithIndex.map{x =>
					Elem(htmlBinding, "td", Attributes(), Group(
						Text("Level "),
						TextValue(key + "_" + (startIndex + x._2) + "_level", x._1.level.toString, isEditing)
					))
				}
			)),
			Elem(htmlBinding, "tr", Attributes("class" -> "nicks"), Group.fromSeq(
				list.zipWithIndex.map{x =>
					Elem(htmlBinding, "td", Attributes(), Group(
						NicknamesField(key + "_" + (startIndex + x._2) + "_nickname", x._1.nickname, isEditing)
					))
				}
			)),
			Elem(htmlBinding, "tr", Attributes("class" -> "held"), Group.fromSeq(
				list.zipWithIndex.map{x => 
					Elem(htmlBinding, "td", Attributes(), Group(
						TextValue(key + "_" + (startIndex + x._2) + "_holding", x._1.holding, isEditing)
					))
				}
			)),
			Elem(htmlBinding, "tr", Attributes("class" -> "moves"), Group.fromSeq(
				list.zipWithIndex.map{x =>
					Elem(htmlBinding, "td", Attributes(), Group(
						MovesField(key + "_" + (startIndex + x._2) + "_attacks", x._1.attacks, isEditing)
					))
				}
			)),
			Elem(htmlBinding, "tr", Attributes("class" -> "ability"), Group.fromSeq(
				list.zipWithIndex.map{x => 
					Elem(htmlBinding, "td", Attributes(), Group(
						TextValue(key + "_" + (startIndex + x._2) + "_ability", x._1.ability, isEditing)
					))
				}
			)),
			Elem(htmlBinding, "tr", Attributes("class" -> "nature"), Group.fromSeq(
				list.zipWithIndex.map{x => 
					Elem(htmlBinding, "td", Attributes(), Group(
						TextValue(key + "_" + (startIndex + x._2) + "_nature", x._1.nature, isEditing)
					))
				}
			)),
			Elem(htmlBinding, "tr", Attributes("class" -> "nextMove"), Group.fromSeq(
				list.zipWithIndex.map{x => 
					Elem(htmlBinding, "td", Attributes(), Group(
						TextValue(key + "_" + (startIndex + x._2) + "_nextAttack", x._1.nextAttack, isEditing)
					))
				}
			)),
			Elem(htmlBinding, "tr", Attributes("class" -> "caught"), Group.fromSeq(
				list.zipWithIndex.map{x =>
					Elem(htmlBinding, "td", Attributes(), Group(
						Text("Caught at: "),
						TextValue(key + "_" + (startIndex + x._2) + "_caughtTime", x._1.caughtTime, isEditing)
					))
				}
			))
		)
	}
	
	
	
	def listOfBadges(list:Seq[Badge], isEditing:Boolean):Node = {
		Elem(htmlBinding, "table", Attributes("class" -> "badges"), Group(
			Elem(htmlBinding, "tr", Attributes("class" -> "name"), Group.fromSeq(
				list.zipWithIndex.map{x => 
					Elem(htmlBinding, "td", Attributes(), Group(
						TextValue("badges_" + x._2 + "_name", x._1.name, isEditing)
					))
				}
			)),
			Elem(htmlBinding, "tr", Attributes("class" -> "time"), Group.fromSeq(
				list.zipWithIndex.map{x => 
					Elem(htmlBinding, "td", Attributes(), Group(
						TextValue("badges_" + x._2 + "_time", x._1.time, isEditing)
					))
				}
			)),
			Elem(htmlBinding, "tr", Attributes("class" -> "attempts"), Group.fromSeq(
				list.zipWithIndex.map{x =>
					Elem(htmlBinding, "td", Attributes(), Group(
						Text("Attempts: "),
						TextValue("badges_" + x._2 + "_attempts", x._1.attempts.toString, isEditing)
					))
				}
			))
		))
	}
	
	
	
	private def TextValue(key:String, value:String, isEditing:Boolean) = {
		if (isEditing) {
			Elem(htmlBinding, "input", Attributes("name" -> key, "value" -> value, "type" -> "text"))
		} else {
			Text(value)
		}
	}
	
	private def TypeField(key:String, value:String, isEditing:Boolean) = {
		if (isEditing) {
			Elem(htmlBinding, "input", Attributes("name" -> key, "value" -> value, "type" -> "text"))
		} else {
			Elem(htmlBinding, "span", Attributes("class" -> ("type " + value.toLowerCase())), Group(Text(value)))
		}
	}
	
	private def NicknamesField(key:String, values:Seq[String], isEditing:Boolean) = {
		if (isEditing) {
			Elem(htmlBinding, "ul", Attributes(), Group.fromSeq(
				(values ++ Seq("","","")).take(3).zipWithIndex.map{y => Elem(htmlBinding, "li", Attributes(), Group(
					Elem(htmlBinding, "input", Attributes("name" -> (key + "_" + y._2), "value" -> y._1, "type" -> "text"))
				))}
			))
		} else {
			Elem(htmlBinding, "ul", Attributes(), Group.fromSeq(
				values.map{y => Elem(htmlBinding, "li", Attributes(), Group(Text(y)))}
			))
		}
	}
	
	private def MovesField(key:String, values:Seq[String], isEditing:Boolean) = {
		if (isEditing) {
			Elem(htmlBinding, "ol", Attributes(), Group.fromSeq(
				(values ++ Seq("","","","")).take(4).zipWithIndex.map{y =>
					Elem(htmlBinding, "li", Attributes(), Group(
						Elem(htmlBinding, "input", Attributes("name" -> (key + "_" + y._2), "value" -> y._1, "type" -> "text"))
					))
				}
			))
		} else {
			Elem(htmlBinding, "ol", Attributes(), Group.fromSeq(
				values.map{y => 
					Elem(htmlBinding, "li", Attributes(), Group(
						if (Set("Cut", "Surf", "Flash", "Strength", "Fly") contains y) {
							Elem(htmlBinding, "strong", Attributes(), Group(Text(y)))
						} else {
							Text(y)
						}
					))
				}
			))
		}
	}
}

