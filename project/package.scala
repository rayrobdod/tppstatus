package com.rayrobdod

package object tpporg {
	import org.json4s.JsonAST._

	def extractPageData(value:JValue):PageData = {
		implicit def format = org.json4s.DefaultFormats
		PageData(
			monsterType = extractString(value \ "monsterType"),
			lastUpdate = extractString(value \ "lastUpdate"),
			gameName = extractString(value \ "gameName"),
			checkpoint = extractString(value \ "checkpoint"),
			charName = extractString(value \ "name"),
			charIdno = extractString(value \ "idno"),
			party = extractArray(value \ "party", extractPokemon _),
			box = extractArray(value \ "box", extractPokemon _),
			daycare = extractArray(value \ "daycare", extractPokemon _),
			badges = (value \ "badges").extract[List[Badge]],
			eliteFour = (value \ "eliteFour").extract[List[EliteFour]],
		)
	}

	def extractPokemon(value:JValue):Pokemon = {
		Pokemon(
			species = extractString(value \ "species"),
			type1 = extractString(value \ "type1"),
			type2 = extractOptionalString(value \ "type2", "None"),
			ability = extractOptionalString(value \ "ability", "???"),
			ingame = extractOptionalString(value \ "ingame", "???"),
			formerIngame = extractArray(value \ "formerIngame", extractString _),
			level = extractInt(value \ "level"),
			attacks = extractArrayWithDefault(value \ "attacks", extractString _, List("???","???","???","???")),
			holding = extractOptionalString(value \ "holding", "???"),
			nature = extractOptionalString(value \ "nature", "???"),
			nickname = extractArray(value \ "nickname", extractString _),
			nextAttack = extractOptionalString(value \ "nextAttack", "???"),
			nextAttacks = extractArray(value \ "nextAttacks", extractString _),
			gender = extractOptionalString(value \ "gender", "???"),
			caughtTime = extractOptionalString(value \ "caughtTime", "???"),
			caughtBall = extractOptionalString(value \ "caughtBall", "???"),
		)
	}

	def extractArray[Z](value:JValue, mapping:Function1[JValue, Z]):List[Z] = value match {
		case JArray(arr) => arr.map(mapping)
		case JNothing => Nil
		case _ => throw new IllegalArgumentException(s"${value}")
	}

	def extractArrayWithDefault[Z](value:JValue, mapping:Function1[JValue, Z], default:List[Z]):List[Z] = value match {
		case JArray(arr) => arr.map(mapping)
		case JNothing => default
		case _ => throw new IllegalArgumentException(s"${value}")
	}

	def extractString(value:JValue):String = value match {
		case JString(s) => s
		case _ => throw new IllegalArgumentException(s"${value}")
	}

	def extractOptionalString(value:JValue, default:String):String = value match {
		case JString(s) => s
		case JNothing => default
		case _ => throw new IllegalArgumentException(s"${value}")
	}

	def extractInt(value:JValue):Int = {
		value.asInstanceOf[JInt].num.intValue
	}
}

package tpporg {
	case class PageData(
		monsterType:String,
		gameName:String,
		lastUpdate:String,
		checkpoint:String,
		charName:String,
		charIdno:String,
		party:Seq[Pokemon],
		box:Seq[Pokemon],
		daycare:Seq[Pokemon],
		badges:Seq[Badge],
		eliteFour:Seq[EliteFour],
		fileName:String = "sdf.html",
		genderElemFunc:Function1[String, scalatags.Text.Frag] = GenderElemFunctions.normal
	)

	case class Badge(
		name:String,
		time:String,
		attempts:Long,
	)

	case class EliteFour(
		name:String,
		firstWin:String,
		wins:Long,
		losses:Long,
	)

	case class Pokemon(
		species:String,
		type1:String,
		type2:String,
		ability:String,
		ingame:String,
		formerIngame:Seq[String],
		level:Long,
		attacks:Seq[String],
		holding:String,
		nature:String,
		nickname:Seq[String],
		nextAttack:String,
		nextAttacks:Seq[String],
		gender:String,
		caughtTime:String,
		caughtBall:String,
	) {
		def caughtBallUrl:String = {
			"images/balls/" + caughtBall.toLowerCase + ".png"
		}
	}


	object GenderElemFunctions {
		import scalatags.Text.all._
		private[this] val width = attr("width")
		private[this] val height = attr("height")

		def normal:Function1[String,scalatags.Text.Frag] = {gender:String =>
			val (sexText, sexColor) = gender.toLowerCase match {
				case "male" => ("♂", "blue")
				case "female" => ("♀", "red")
				case _ => ("", "grey")
			}
			span(style := s"color: ${sexColor}", sexText)
		}

		def yinyang:Function1[String,scalatags.Text.Frag] = {gender:String =>
			val (altV, imgV) = gender.toLowerCase match {
				case "male" => ("♂", "yang")
				case "female" => ("♀", "yin")
				case _ => ("", "neither")
			}
			img(width := 16, height := 16, alt := altV, src := s"images/gender/${imgV}.png")
		}
	}
}
