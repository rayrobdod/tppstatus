package com.rayrobdod

import sbt._
import java.nio.file.Files
import com.rayrobdod.json.builder.{Builder, SeqBuilder, CaseClassBuilder}

package object tpporg {
	
	val PokemonBuilder = new CaseClassBuilder(
		classOf[Pokemon],
		Pokemon(),
		Map(
			"attacks" -> new SeqBuilder(),
			"nickname" -> new SeqBuilder(),
			"nextAttacks" -> new SeqBuilder(),
			"formerIngame" -> new SeqBuilder()
		)
	)
	
	val BadgeBuilder = new CaseClassBuilder(
		classOf[Badge],
		Badge(),
		Map()
	)
	
}

package tpporg {
	
	case class PageData(
		monsterType:String = "Pokémon",
		gameName:String = "Pokémon",
		identifier:String = "sdf",
		lastUpdate:String = "??d ??h ??m",
		checkpoint:String = "???",
		party:Seq[Pokemon] = Nil,
		box:Seq[Pokemon] = Nil,
		daycare:Seq[Pokemon] = Nil,
		badges:Seq[Badge] = Nil
	) {
		def editFileName:String = {
			val index = fileName.lastIndexOf('.')
			fileName.substring(0, index) + "_edit" + fileName.substring(index)
		}
		
		def fileName:String = identifier + ".xhtml"
	}
	
	case class Badge(
		name:String = "???",
		time:String = "---",
		attempts:Long = 0
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
	)
	
	
	
	
	
	
	
	class PageDataBuilder() extends Builder[PageData] {
		override val init = PageData()
		override def apply(t:PageData, key:String, value:Any) = key match {
			case "monsterType" => t.copy(monsterType = value.toString)
			case "lastUpdate" => t.copy(lastUpdate = value.toString)
			case "gameName" => t.copy(gameName = value.toString)
			case "checkpoint" => t.copy(checkpoint = value.toString)
			case "name" => t
			case "idno" => t
			case "items" => t
			case "party" => t.copy(party = value.asInstanceOf[Seq[Pokemon]])
			case "box" => t.copy(box = value.asInstanceOf[Seq[Pokemon]])
			case "daycare" => t.copy(daycare = value.asInstanceOf[Seq[Pokemon]])
			case "badges" => t.copy(badges = value.asInstanceOf[Seq[Badge]])
			case _ => throw new IllegalArgumentException("PageDataBuilder apply key: " + key)
		}
		override def childBuilder(key:String) = key match { 
			case "party" => new SeqBuilder(PokemonBuilder)
			case "box" => new SeqBuilder(PokemonBuilder)
			case "daycare" => new SeqBuilder(PokemonBuilder)
			case "badges" => new SeqBuilder(BadgeBuilder)
			case "items" => new SeqBuilder
			case _ => throw new IllegalArgumentException("PageDataBuilder childBuilder key: " + key)
		}
		override def resultType = classOf[PageData]
	}
	
}

