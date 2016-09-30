package com.rayrobdod

import sbt._
import java.nio.file.Files
import com.codecommit.antixml.Elem
import com.rayrobdod.json.builder.{Builder, SeqBuilder, CaseClassBuilder}

package object tpporg {
	
	val PokemonBuilder = new CaseClassBuilder(
		Pokemon(),
		Map(
			"attacks" -> new SeqBuilder(),
			"nickname" -> new SeqBuilder(),
			"nextAttacks" -> new SeqBuilder(),
			"formerIngame" -> new SeqBuilder()
		))(
		classOf[Pokemon]
	)
	
	val BadgeBuilder = new CaseClassBuilder(
		Badge(),
		Map())(
		classOf[Badge]
	)
	
	val EliteFourBuilder = new CaseClassBuilder(
		EliteFour(),
		Map())(
		classOf[EliteFour]
	)
	
}

package tpporg {
	
	case class PageData(
		monsterType:String = "Pokémon",
		gameName:String = "Pokémon",
		fileName:String = "sdf.html",
		lastUpdate:String = "??d ??h ??m",
		checkpoint:String = "???",
		charName:String = "???",
		charIdno:String = "???",
		party:Seq[Pokemon] = Nil,
		box:Seq[Pokemon] = Nil,
		daycare:Seq[Pokemon] = Nil,
		badges:Seq[Badge] = Nil,
		eliteFour:Seq[EliteFour] = Nil,
		genderElemFunc:Function1[String,Elem] = GenderElemFunctions.normal
	)
	
	case class Badge(
		name:String = "???",
		time:String = "---",
		attempts:Long = 0
	)
	
	case class EliteFour(
		name:String = "???",
		firstWin:String = "---",
		wins:Long = 0,
		losses:Long = 0
	)
	
	case class Pokemon(
		species:String = "???",
		type1:String = "???",
		type2:String = "None",
		ability:String = "???",
		ingame:String = "???",
		formerIngame:Seq[String] = Nil,
		level:Long = -1,
		attacks:Seq[String] = Seq("???","???","???","???"),
		holding:String = "???",
		nature:String = "???",
		nickname:Seq[String] = Nil,
		nextAttack:String = "???",
		nextAttacks:Seq[String] = Nil,
		gender:String = "???",
		caughtTime:String = "???",
		caughtBall:String = "???"
	) {
		def caughtBallUrl:String = {
			"images/balls/" + caughtBall.toLowerCase + ".png"
		}
	}
	
	
	
	
	
	
	
	class PageDataBuilder() extends Builder[PageData] {
		override val init = PageData()
		override def apply(t:PageData, key:String, value:Any) = key match {
			case "monsterType" => t.copy(monsterType = value.toString)
			case "lastUpdate" => t.copy(lastUpdate = value.toString)
			case "gameName" => t.copy(gameName = value.toString)
			case "checkpoint" => t.copy(checkpoint = value.toString)
			case "name" => t.copy(charName = value.toString)
			case "idno" => t.copy(charIdno = value.toString)
			case "items" => t
			case "party" => t.copy(party = value.asInstanceOf[Seq[Pokemon]])
			case "box" => t.copy(box = value.asInstanceOf[Seq[Pokemon]])
			case "daycare" => t.copy(daycare = value.asInstanceOf[Seq[Pokemon]])
			case "badges" => t.copy(badges = value.asInstanceOf[Seq[Badge]])
			case "eliteFour" => t.copy(eliteFour = value.asInstanceOf[Seq[EliteFour]])
			case _ => throw new IllegalArgumentException("PageDataBuilder apply key: " + key)
		}
		override def childBuilder(key:String) = key match { 
			case "party" => new SeqBuilder(PokemonBuilder)
			case "box" => new SeqBuilder(PokemonBuilder)
			case "daycare" => new SeqBuilder(PokemonBuilder)
			case "badges" => new SeqBuilder(BadgeBuilder)
			case "items" => new SeqBuilder
			case "eliteFour" => new SeqBuilder(EliteFourBuilder)
			case _ => throw new IllegalArgumentException("PageDataBuilder childBuilder key: " + key)
		}
		override def resultType = classOf[PageData]
	}
	
	
	object GenderElemFunctions {
		import com.codecommit.antixml.{Elem, NamespaceBinding, Attributes, Group, Text}
		private val HTML_NAMESPACE = "http://www.w3.org/1999/xhtml"
		private val htmlBinding = NamespaceBinding(HTML_NAMESPACE)
		
		def normal:Function1[String,Elem] = {gender:String =>
			val (sexText, sexColor) = gender.toLowerCase match {
				case "male" => ("♂", "blue")
				case "female" => ("♀", "red")
				case _ => ("", "grey")
			}
			Elem(htmlBinding, "span", Attributes("style" -> ("color:" + sexColor)), Group(Text(sexText)))
		}
		
		def yinyang:Function1[String,Elem] = {gender:String =>
			val (alt, img) = gender.toLowerCase match {
				case "male" => ("♂", "yang")
				case "female" => ("♀", "yin")
				case _ => ("", "neither")
			}
			Elem(htmlBinding, "img", Attributes("width" -> "16", "height" -> "16", "alt" -> alt, "src" -> ("images/gender/" + img + ".png")))
		}
	}

}
