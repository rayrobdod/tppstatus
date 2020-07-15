package com.rayrobdod.tpporg

import scalatags.Text.all._

object PageTemplates {
	private val HTML_NAMESPACE = "http://www.w3.org/1999/xhtml"
	private val htmlDoctype = scalatags.Text.implicits.raw("<!DOCTYPE html>" + System.lineSeparator)

	private[this] val title = tag("title")
	private[this] val main = tag("main")
	private[this] val nav = tag("nav")
	private[this] val section = tag("section")
	private[this] val xmlns = attr("xmlns")
	private[this] val width = attr("width")
	private[this] val height = attr("height")

	def overallPage(pageData:PageData):scalatags.Text.Frag = html(
		xmlns := HTML_NAMESPACE,
		lang := "en-US",
		head(
			title(s"Twitch Plays Pokémon - ${pageData.gameName}"),
			meta(httpEquiv := "content-type", content := "application/xhtml+xml"),
			link(rel := "stylesheet", href := "style/style.css"),
		),
		body(
			header(
				nav(`class` := "remote",
					a(href := "http://twitchplayspokemon.org/", "TwitchPlaysPokemon.org"),
					" ",
					a(href := "http://www.reddit.com/live/ut336fzq0red", "Live Updater"),
				),
				nav(`class` := "local",
					a(href := "moemon.xhtml", "Moemon"),
					" ",
					a(href := "touhoumon.xhtml", "Touhoumon"),
				),
			),
			main(
				h1(s"Twitch Plays Pokémon - ${pageData.gameName}"),
				div(`class` := "lastUpdated", s"Last Updated: ${pageData.lastUpdate}"),
				table(`class` := "props",
					tr(
						th("Checkpoint"),
						th("Protagonist Name"),
						th("ID No"),
					),
					tr(
						td(`class` := "checkpoint", pageData.checkpoint),
						td(`class` := "charName", pageData.charName),
						td(`class` := "charIdno", pageData.charIdno),
					),
				),
				section(
					h2(s"${pageData.monsterType} in Party"),
					listOfPartyPokemon(pageData.genderElemFunc, pageData.party),
				),
				section(
					h2("Elite Four"),
					listOfEliteFour(pageData.eliteFour),
				),
				section(
					h2("Badges"),
					listOfBadges(pageData.badges),
				),
				section(
					h2("Daycare"),
					listOfBoxedPokemon(pageData.genderElemFunc, pageData.daycare),
				),
				section(
					h2("Boxed"),
					listOfBoxedPokemon(pageData.genderElemFunc, pageData.box),
				),
			),
		),
	)


	def listOfPartyPokemon(genderElem:Function1[String, scalatags.Text.Frag], list:Seq[Pokemon]):scalatags.Text.Frag = {
		table(`class` := "party", rowOfPokemon(genderElem, list))
	}

	def listOfBoxedPokemon(genderElem:Function1[String, scalatags.Text.Frag], list:Seq[Pokemon]):scalatags.Text.Frag = {
		val rows = list.grouped(6)
		table(`class` := "boxed", rows.map(x => rowOfPokemon(genderElem, x)).toSeq)
	}

	def rowOfPokemon(genderElem:Function1[String, scalatags.Text.Frag], list:Seq[Pokemon]):scalatags.Text.Frag = Seq(
		tr(`class` := "name",
			list.map(x =>
				td(
					x.ingame,
					img(width := 16, height := 16, alt := x.caughtBall, src := x.caughtBallUrl),
					genderElem(x.gender.toLowerCase),
					br(),
					s"(${x.species})",
					br(),
					span(`class` := s"type ${x.type1.toLowerCase()}", x.type1),
					" ",
					span(`class` := s"type ${x.type2.toLowerCase()}", x.type2),
				)
			)
		),
		tr(`class` := "level",
			list.map(x => td("Level ", x.level.toString))
		),
		tr(`class` := "nicks",
			list.map(x => td(ul(x.nickname.map(y => li(y)))))
		),
		tr(`class` := "held",
			list.map(x => td(x.holding))
		),
		tr(`class` := "moves",
			list.map(x =>
				td(
					ol(
						x.attacks.map(y =>
							if (Set("Cut", "Surf", "Flash", "Strength", "Fly", "Rock Smash").contains(y)) {
								li(strong(y))
							} else {
								li(y)
							}
						)
					)
				)
			)
		),
		tr(`class` := "ability",
			list.map(x => td(x.ability))
		),
		tr(`class` := "nature",
			list.map(x => td(x.nature))
		),
		tr(`class` := "nextMove",
			list.map(x => td(x.nextAttack))
		),
		tr(`class` := "caught",
			list.map(x => td("Caught at: ", x.caughtTime.toString))
		),
	)



	def listOfBadges(list:Seq[Badge]):scalatags.Text.Frag = frag(
		table(`class` := "badges",
			tr(`class` := "name",
				list.map(x => td(x.name))
			),
			tr(`class` := "time",
				list.map(x => td(x.time))
			),
			tr(`class` := "attempts",
				list.map(x => td("Attempts: ", x.attempts.toString))
			),
		)
	)

	def listOfEliteFour(list:Seq[EliteFour]):scalatags.Text.Frag = frag(
		table(`class` := "eliteFour",
			tr(`class` := "name",
				list.map(x => td(x.name))
			),
			tr(`class` := "time",
				list.map(x => td(x.firstWin))
			),
			tr(`class` := "attempts",
				list.map(x => td("Losses: ", x.losses.toString, "; Wins: ", x.wins.toString))
			),
		)
	)


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
