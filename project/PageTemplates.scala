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
	
	
	def overallPage(pageData:PageData):Group[Node] = {
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
					Elem(htmlBinding, "main", Attributes(), Group(
						Elem(htmlBinding, "h1", Attributes(), Group(Text("Twitch Plays Pokémon - " + pageData.gameName))),
						Elem(htmlBinding, "div", Attributes("class" -> "lastUpdated"), Group(
							Text("Last Updated: "),
							Text(pageData.lastUpdate)
						)),
						Elem(htmlBinding, "table", Attributes("class" -> "props"), Group(
							Elem(htmlBinding, "tr", Attributes(), Group(
								Elem(htmlBinding, "th", Attributes(), Group(Text("Checkpoint"))),
								Elem(htmlBinding, "th", Attributes(), Group(Text("Protagonist Name"))),
								Elem(htmlBinding, "th", Attributes(), Group(Text("ID No")))
							)),
							Elem(htmlBinding, "tr", Attributes(), Group(
								Elem(htmlBinding, "td", Attributes("class" -> "checkpoint"), Group(Text(pageData.checkpoint))),
								Elem(htmlBinding, "td", Attributes("class" -> "charName"), Group(Text(pageData.charName))),
								Elem(htmlBinding, "td", Attributes("class" -> "charIdno"), Group(Text(pageData.charIdno)))
							))
						)),
						Elem(htmlBinding, "section", Attributes(), Group(
							Elem(htmlBinding, "h2", Attributes(), Group(Text(pageData.monsterType + " in Party"))),
							listOfPartyPokemon(pageData.genderElemFunc, pageData.party)
						)),
						Elem(htmlBinding, "section", Attributes(), Group(
							Elem(htmlBinding, "h2", Attributes(), Group(Text("Elite Four"))),
							listOfEliteFour(pageData.eliteFour)
						)),
						Elem(htmlBinding, "section", Attributes(), Group(
							Elem(htmlBinding, "h2", Attributes(), Group(Text("Badges"))),
							listOfBadges(pageData.badges)
						)),
						Elem(htmlBinding, "section", Attributes(), Group(
							Elem(htmlBinding, "h2", Attributes(), Group(Text("Daycare"))),
							listOfBoxedPokemon(pageData.genderElemFunc, pageData.daycare)
						)),
						Elem(htmlBinding, "section", Attributes(), Group(
							Elem(htmlBinding, "h2", Attributes(), Group(Text("Boxed"))),
							listOfBoxedPokemon(pageData.genderElemFunc, pageData.box)
						))
					))
				))
			))
		)
	}
	
	def listOfPartyPokemon(genderElem:Function1[String, Elem], list:Seq[Pokemon]):Node = {
		Elem(htmlBinding, "table", Attributes("class" -> "party"), rowOfPokemon(genderElem, list))
	}
	
	def listOfBoxedPokemon(genderElem:Function1[String, Elem], list:Seq[Pokemon]):Node = {
		val rows = list.grouped(6)
		Elem(htmlBinding, "table", Attributes("class" -> "boxed"), Group.fromSeq(
			rows.flatMap(rowOfPokemon(genderElem, _)).toList
		))
	}
	
	def rowOfPokemon(genderElem:Function1[String, Elem], list:Seq[Pokemon]):Group[Node] = {
		Group(
			Elem(htmlBinding, "tr", Attributes("class" -> "name"), Group.fromSeq(
				list.map{x =>
					
					Elem(htmlBinding, "td", Attributes(), Group(
						Text(x.ingame),
						Elem(htmlBinding, "img", Attributes("width" -> "16", "height" -> "16", "alt" -> x.caughtBall, "src" -> x.caughtBallUrl)),
						genderElem(x.gender.toLowerCase),
						Elem(htmlBinding, "br"),
						Text("(" + x.species + ")"),
						Elem(htmlBinding, "br"),
						Elem(htmlBinding, "span", Attributes("class" -> ("type " + x.type1.toLowerCase())), Group(Text(x.type1))),
						Text(" "),
						Elem(htmlBinding, "span", Attributes("class" -> ("type " + x.type2.toLowerCase())), Group(Text(x.type2)))
					))
				}
			)),
			Elem(htmlBinding, "tr", Attributes("class" -> "level"), Group.fromSeq(
				list.map{x =>
					Elem(htmlBinding, "td", Attributes(), Group(
						Text("Level "),
						Text(x.level.toString)
					))
				}
			)),
			Elem(htmlBinding, "tr", Attributes("class" -> "nicks"), Group.fromSeq(
				list.map{x =>
					Elem(htmlBinding, "td", Attributes(), Group(
						Elem(htmlBinding, "ul", Attributes(), Group.fromSeq(
							x.nickname.map{y => Elem(htmlBinding, "li", Attributes(), Group(Text(y)))}
						))
					))
				}
			)),
			Elem(htmlBinding, "tr", Attributes("class" -> "held"), Group.fromSeq(
				list.map{x => wrapStringInTd(x.holding)}
			)),
			Elem(htmlBinding, "tr", Attributes("class" -> "moves"), Group.fromSeq(
				list.map{x =>
					Elem(htmlBinding, "td", Attributes(), Group(
						Elem(htmlBinding, "ol", Attributes(), Group.fromSeq(
							x.attacks.map{y => Elem(htmlBinding, "li", Attributes(), Group(
								if (Set("Cut", "Surf", "Flash", "Strength", "Fly", "Rock Smash") contains y) {
									Elem(htmlBinding, "strong", Attributes(), Group(Text(y)))
								} else {
									Text(y)
								}
							))}
						))
					))
				}
			)),
			Elem(htmlBinding, "tr", Attributes("class" -> "ability"), Group.fromSeq(
				list.map{x => wrapStringInTd(x.ability)}
			)),
			Elem(htmlBinding, "tr", Attributes("class" -> "nature"), Group.fromSeq(
				list.map{x => wrapStringInTd(x.nature)}
			)),
			Elem(htmlBinding, "tr", Attributes("class" -> "nextMove"), Group.fromSeq(
				list.map{x => wrapStringInTd(x.nextAttack)}
			)),
			Elem(htmlBinding, "tr", Attributes("class" -> "caught"), Group.fromSeq(
				list.map{x =>
					Elem(htmlBinding, "td", Attributes(), Group(
						Text("Caught at: "),
						Text(x.caughtTime.toString)
					))
				}
			))
		)
	}
	
	
	
	def listOfBadges(list:Seq[Badge]):Node = {
		Elem(htmlBinding, "table", Attributes("class" -> "badges"), Group(
			Elem(htmlBinding, "tr", Attributes("class" -> "name"), Group.fromSeq(
				list.map{x => wrapStringInTd(x.name)}
			)),
			Elem(htmlBinding, "tr", Attributes("class" -> "time"), Group.fromSeq(
				list.map{x => wrapStringInTd(x.time)}
			)),
			Elem(htmlBinding, "tr", Attributes("class" -> "attempts"), Group.fromSeq(
				list.map{x =>
					Elem(htmlBinding, "td", Attributes(), Group(
						Text("Attempts: "),
						Text(x.attempts.toString)
					))
				}
			))
		))
	}
	
	def listOfEliteFour(list:Seq[EliteFour]):Node = {
		Elem(htmlBinding, "table", Attributes("class" -> "eliteFour"), Group(
			Elem(htmlBinding, "tr", Attributes("class" -> "name"), Group.fromSeq(
				list.map{x => wrapStringInTd(x.name)}
			)),
			Elem(htmlBinding, "tr", Attributes("class" -> "time"), Group.fromSeq(
				list.map{x => wrapStringInTd(x.firstWin)}
			)),
			Elem(htmlBinding, "tr", Attributes("class" -> "attempts"), Group.fromSeq(
				list.map{x =>
					Elem(htmlBinding, "td", Attributes(), Group(
						Text("Losses: "),
						Text(x.losses.toString),
						Text("; Wins: "),
						Text(x.wins.toString)
					))
				}
			))
		))
	}
	
	
	private def wrapStringInTd(x:String) = {
		Elem(htmlBinding, "td",  Attributes(), Group(Text(x)))
	}
	
	/**************************************************************/
	
	def tppOrgSql(pageData:PageData):String = {
		val ballSuffix = if (pageData.monsterType == "Bokéna") {" Orb"} else {" Ball"}
		
		"INSERT INTO `pokemon` (`id`, `pokemon`, `name`, `level`, `nickname`, `gender`, `hold_item`, `status`, `box_id`, `poke_ball`, `party_order`, `comment`, `date_created`) VALUES" +
		(
			pageData.party.zipWithIndex.map{x => tppOrgSqlPokemonRow(1 + x._2, 1, x._1, ballSuffix, x._2)} ++
			pageData.daycare.zipWithIndex.map{x => tppOrgSqlPokemonRow(10 + x._2, 6, x._1, ballSuffix)} ++
			pageData.box.zipWithIndex.map{x => tppOrgSqlPokemonRow(20 + x._2, 2, x._1, ballSuffix)}
		).foldLeft(""){_ + ",\n" + _}.tail +
		"\n\n\n\n" +
		"INSERT INTO `badge` (`id`, `name`, `leader`, `time`, `attempts`, `order_id`) VALUES" +
		pageData.badges.zipWithIndex.map{x => tppOrgSqlBadgeRow(1 + x._2, x._1)}.foldLeft(""){_ + ",\n" + _}.tail
		
	}
	
	/*
	INSERT INTO `status` (`id`, `status`) VALUES
	(1, 'In party'),
	(2, 'In box'),
	(3, 'Released'),
	(4, 'Traded'),
	(5, 'Evolved'),
	(6, 'Daycare'),
	(7, 'Hatched'),
	(9, 'Lost');
	*/
	
	private def tppOrgSqlPokemonRow(id:Int, status:Int, pkmn:Pokemon, ballSuffix:String, partyOrder:Int = 999):String = {
		val hasIngameBool:Boolean = (pkmn.ingame != pkmn.species);
		val hasIngameStr:String = if (hasIngameBool) {"'" + pkmn.ingame + "'"} else {"NULL"}
		val nicknameStr:String = if (pkmn.nickname.isEmpty) {"NULL"} else {"'" + pkmn.nickname.foldLeft("")(_ + "%" + _).tail + "'"}
		val genderStr:String = if (pkmn.gender.toLowerCase == "male") {"'m'"} else if (pkmn.gender.toLowerCase == "female") {"'f'"} else {"NULL"}
		val ball = pkmn.caughtBall.head.toString.toUpperCase + pkmn.caughtBall.split("_").apply(0).tail.toLowerCase + ballSuffix
		
		"(" +
			id + ", '" + pkmn.species + "', " + hasIngameStr + ", " +
			pkmn.level + ", " + nicknameStr + ", " + genderStr + ", '" +
			pkmn.holding + "', " + status + ", NULL, '" + ball + "', " +
			partyOrder + ", '" + pkmn.caughtTime + "', '0000-00-00 00:00:00'" +
		")"
	}
	
	private def tppOrgSqlBadgeRow(id:Int, badge:Badge):String = {
		val leader = badge.name match {
			case "Boulder" => "Brock"
			case "Cascade" => "Misty"
			case "Thunder" => "Lt. Surge"
			case "Rainbow" => "Erika"
			case "Soul" => "Koga"
			case "Marsh" => "Sabrina"
			case "Volcano" => "Blaine"
			case "Earth" => "Giovanni"
		}
		val time = ""
		
		"(" +
			id + ", '" + badge.name + "', '" + leader + "', '" +
			time + "', " + badge.attempts + ", " + id +
		")"
	}
}

