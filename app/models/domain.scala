package models

import play.api.libs.json.Json.{JsValueWrapper, toJsFieldJsValueWrapper => js}
import play.api.libs.json.{JsValue, Json}
import slick.driver.PostgresDriver.api._
import slick.lifted.Tag

/*
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

case class Employee(id: Option[Long], name: String, address: String, dob: Option[Date], joiningDate: Date, designation: Option[String])
*/

trait Model {
  def toJson(extras: (String, JsValueWrapper)*): JsValue
}

trait Requirement {
  val name: String
  val description: String
}

case class Game(id: Long, name: String) extends Model {
  def this(id: Long) = this(id, "")
  def this(body: JsValue) = this(
    (body \ "id").validate[Long].getOrElse(-1L),
    (body \ "name").validate[String].get
  )

  def toJson(extras: (String, JsValueWrapper)*) = Json.obj(
    ("id" -> js(id)) +:
    ("name" -> js(name)) +:
    extras: _*
  )
}

case class Player(id: Long, game: Long, name: String, alias: String) extends Model {
  def this(id: Long) = this(id, 0, "", "")
  def this(body: JsValue) = this(
    (body \ "id").validate[Long].getOrElse(-1L),
    (body \ "game").validate[Long].get,
    (body \ "name").validate[String].get,
    (body \ "alias").validate[String].get
  )

  def toJson(extras: (String, JsValueWrapper)*) = Json.obj(
    ("id" -> js(id)) +:
    ("game" -> js(game)) +:
    ("name" -> js(name)) +:
    extras: _*
  )
}

case class ItemNeeded(id: Long, game: Long, name: String, description: String, owner: Option[Long], found: Boolean){
  def this(item: Item, found: Boolean) = this(item.id, item.game, item.name, item.description, item.owner, found)
  def this(id: Long) = this(new Item(id), false)
}

case class Item(id: Long, game: Long, name: String, description: String, owner: Option[Long]) extends Model with Requirement {
  def this(id: Long) = this(id, 0, "", "", None)
  def this(body: JsValue) = this(
    (body \ "id").validate[Long].getOrElse(-1L),
    (body \ "game").validate[Long].get,
    (body \ "name").validate[String].get,
    (body \ "description").validate[String].get,
    (body \ "owner").validateOpt[Long].get
  )

  def toJson(extras: (String, JsValueWrapper)*) = Json.obj(
    ("id" -> js(id)) +:
    ("game" -> js(game)) +:
    ("name" -> js(name)) +:
    ("description" -> js(description)) +:
    ("owner" -> js(owner)) +:
    extras: _*
  )
}

case class PowerNeeded(id: Long, game: Long, name: String, description: String, found: Boolean) {
  def this(power: Power, found: Boolean) = this(power.id, power.game, power.name, power.description, found)
  def this(id: Long) = this(new Power(id), false)
}

case class Power(id: Long, game: Long, name: String, description: String) extends Model with Requirement {
  def this(id: Long) = this(id, 0, "", "")
  def this(body: JsValue) = this(
    (body \ "id").validate[Long].getOrElse(-1L),
    (body \ "game").validate[Long].get,
    (body \ "name").validate[String].get,
    (body \ "description").validate[String].get
  )

  def toJson(extras: (String, JsValueWrapper)*) = Json.obj(
    ("id" -> js(id)) +:
    ("game" -> js(game)) +:
    ("name" -> js(name)) +:
    ("description" -> js(description)) +:
    extras: _*
  )
}

case class Quest(id: Long, name: String, description: String, game: Long) extends Model {
  def this(id: Long) = this(id, "", "", 0)
  def this(body: JsValue) = this(
    (body \ "id").validate[Long].getOrElse(-1L),
    (body \ "name").validate[String].get,
    (body \ "description").validate[String].get,
    (body \ "game").validate[Long].get
  )

  def toJson(extras: (String, JsValueWrapper)*) = Json.obj(
    ("id" -> js(id)) +:
    ("game" -> js(game)) +:
    ("name" -> js(name)) +:
    ("description" -> js(description)) +:
    extras: _*
  )
}

case class PlayerQuest(player: Long, quest: Long)
case class PlayerPower(player: Long, power: Long)
case class GameItem(game: Long, item: Long)
case class QuestItem(quest: Long, item: Long)
case class QuestPower(quest: Long, power: Long)

/*
class Employees(tag: Tag) extends Table[Employee](tag, "EMPLOYEE") {

  implicit val dateColumnType = MappedColumnType.base[Date, Long](d => d.getTime, d => new Date(d))

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def address = column[String]("address")
  def dob = column[Date]("date_of_birth")
  def joiningDate = column[Date]("joining_date")
  def designation = column[String]("designation")

  def * = (id.?, name, address, dob.?, joiningDate, designation.?) <> (Employee.tupled, Employee.unapply)
}
*/

class Items(tag: Tag) extends Table[Item](tag, "items") {

  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def game = column[Long]("game")
  def name = column[String]("name")
  def description = column[String]("description")
  def owner = column[Option[Long]]("owner")

  def * = (id, game, name, description, owner) <> (Item.tupled, Item.unapply)
}

class Powers(tag: Tag) extends Table[Power](tag, "powers") {

  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def game = column[Long]("game")
  def name = column[String]("name")
  def description = column[String]("description")

  def * = (id, game, name, description) <> (Power.tupled, Power.unapply)
}

class Games(tag: Tag) extends Table[Game](tag, "games") {

  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def name = column[String]("name")

  def * = (id, name) <> (Game.tupled, Game.unapply)
//    def ? = (id.?, gameId.?, name.?).shaped.<>({ r => r._1.map(_ => Player.tupled((r._1.get, r._2.get, r._3.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))
}

class Players(tag: Tag) extends Table[Player](tag, "players") {

  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def game = column[Long]("game")
  def name = column[String]("name")
  def alias = column[String]("alias")

  def * = (id, game, name, alias) <> (Player.tupled, Player.unapply)
//    def ? = (id.?, gameId.?, name.?).shaped.<>({ r => r._1.map(_ => Player.tupled((r._1.get, r._2.get, r._3.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))
}

class Quests(tag: Tag) extends Table[Quest](tag, "quests") {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def name = column[String]("name")
  def description = column[String]("description")
  def game = column[Long]("game")

  def * = (id, name, description, game) <> (Quest.tupled, Quest.unapply)
//  def ? = (id.?, name.?, desc.?).shaped.<>({ r =>
//    r._1.map(_ => Quest.tupled((r._1.get, r._2.get, r._3.get)))
//  }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))
}

class PlayerQuests(tag: Tag) extends Table[PlayerQuest](tag, "player_quests"){
  def quest = column[Long]("quest")
  def player = column[Long]("player")
  def side = column[Boolean]("side")

  def * = (player, quest) <> (PlayerQuest.tupled, PlayerQuest.unapply)
}

class PlayerPowers(tag: Tag) extends Table[PlayerPower](tag, "player_powers"){
  def power = column[Long]("power")
  def player = column[Long]("player")

  def * = (player, power) <> (PlayerPower.tupled, PlayerPower.unapply)
}

/*
class PlayerItems(tag: Tag) extends Table[PlayerItem](tag, "player_items"){
  def item = column[Long]("item")
  def player = column[Long]("player")

  def * = (player, item) <> (PlayerItem.tupled, PlayerItem.unapply)
}
*/

class QuestItems(tag: Tag) extends Table[QuestItem](tag, "quest_items"){
  def quest = column[Long]("quest")
  def item = column[Long]("item")

  def * = (quest, item) <> (QuestItem.tupled, QuestItem.unapply)
}

class QuestPowers(tag: Tag) extends Table[QuestPower](tag, "quest_powers"){
  def quest = column[Long]("quest")
  def power = column[Long]("power")

  def * = (quest, power) <> (QuestPower.tupled, QuestPower.unapply)
}

case class PlayerDescription(id: Long, game: Long, name: String, alias: String, mainQuest: Option[Quest], sideQuest: Option[Quest],
                             item1: Option[Item], item2: Option[Item], item3: Option[Item], item4: Option[Item], item5: Option[Item],
                             power1: Option[Power], power2: Option[Power], power3: Option[Power]
                            ){
  def items = Seq(item1, item2, item3, item4, item5).filter(_.nonEmpty).map(_.get)
  def powers = Seq(power1, power2, power3).filter(_.nonEmpty).map(_.get)
}

object PlayerDescription {
  def applyIds(id: Long, game: Long, name: String, alias: String, mainQuest: Option[Long], sideQuest: Option[Long],
               item1: Option[Long], item2: Option[Long], item3: Option[Long], item4: Option[Long], item5: Option[Long],
               power1: Option[Long], power2: Option[Long], power3: Option[Long]) = {
    new PlayerDescription(id, game, name, alias, mainQuest.map(new Quest(_)), sideQuest.map(new Quest(_)),
      item1.map(new Item(_)), item2.map(new Item(_)), item3.map(new Item(_)), item4.map(new Item(_)), item5.map(new Item(_)),
      power1.map(new Power(_)), power2.map(new Power(_)), power3.map(new Power(_))
    )
  }

  def unapplyIds(pd: PlayerDescription): Option[(Long, Long, String, String, Option[Long], Option[Long],
    Option[Long], Option[Long], Option[Long], Option[Long], Option[Long],
    Option[Long], Option[Long], Option[Long])] = {
    Some(pd.id, pd.game, pd.name, pd.alias, pd.mainQuest.map(_.id), pd.sideQuest.map(_.id),
      pd.item1.map(_.id), pd.item2.map(_.id), pd.item3.map(_.id), pd.item4.map(_.id), pd.item5.map(_.id),
      pd.power1.map(_.id), pd.power2.map(_.id), pd.power3.map(_.id)
    )
  }

  def apply(player: Player, mainQuest: Option[Quest], sideQuest: Option[Quest], items: Seq[Item], powers: Seq[Power]) = {
    def maybeItem(i: Int) = if (items.size > i) Some(items(i)) else None
    def maybePower(i: Int) = if (powers.size > i) Some(powers(i)) else None

    new PlayerDescription(player.id, player.game, player.name, player.alias, mainQuest, sideQuest,
      maybeItem(1), maybeItem(2), maybeItem(3), maybeItem(4), maybeItem(5),
      maybePower(1), maybePower(2), maybePower(3)
    )
  }
}

case class QuestDescription(id: Long, name: String, description: String, game: Long,
                            item1: Option[ItemNeeded], item2: Option[ItemNeeded], item3: Option[ItemNeeded],
                            power1: Option[PowerNeeded], power2: Option[PowerNeeded], power3: Option[PowerNeeded]){
  def this(id: Long, name: String, description: String, game: Long) = this(id, name, description, game, None, None, None, None, None, None)
  def items = Seq(item1, item2, item3).filter(_.nonEmpty).map(_.get)
  def powers = Seq(power1, power2, power3).filter(_.nonEmpty).map(_.get)
  def quest = Quest(id, name, description, game)
}

object QuestDescription {
  def applyIds(id: Long, name: String, description: String, game: Long,
                              item1: Option[Long], item2: Option[Long], item3: Option[Long],
                              power1: Option[Long], power2: Option[Long], power3: Option[Long]) = {
    new QuestDescription(id, name, description, game,
      item1.map(new ItemNeeded(_)), item2.map(new ItemNeeded(_)), item3.map(new ItemNeeded(_)),
      power1.map(new PowerNeeded(_)), power2.map(new PowerNeeded(_)), power3.map(new PowerNeeded(_)))
  }

  def unapplyIds(qd: QuestDescription): Option[(Long, String, String, Long, Option[Long], Option[Long], Option[Long], Option[Long], Option[Long], Option[Long])] = {
    Some(qd.id, qd.name, qd.description, qd.game,
      qd.item1.map(_.id), qd.item2.map(_.id), qd.item3.map(_.id),
      qd.power1.map(_.id), qd.power2.map(_.id), qd.power3.map(_.id))
  }

  def apply(quest: Quest, items: Seq[ItemNeeded], powers: Seq[PowerNeeded]) = {
    def maybeItem(i: Int) = if (items.size > i) Some(items(i)) else None
    def maybePower(i: Int) = if (powers.size > i) Some(powers(i)) else None

    new QuestDescription(quest.id, quest.name, quest.description, quest.game,
      maybeItem(0), maybeItem(1), maybeItem(2),
      maybePower(0), maybePower(1), maybePower(2))
  }
}