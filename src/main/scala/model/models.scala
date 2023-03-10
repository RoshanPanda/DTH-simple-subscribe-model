package model

import spray.json._

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.{Failure, Success, Try}


case class Wrapper[T](state: () => T) {
  def map[A](f: T => A): Wrapper[A] = Wrapper[A](() => f(state()))

  def flatMap[A](f: T => Wrapper[A]): Wrapper[A] = Wrapper[A](() => f(state()).state())
}

object Wrapper {
  def apply[T](state: T): Wrapper[T] = Wrapper[T](() => state)
}


case class StateTranscation(
                             fullname: String,
                             userId: Option[String],
                             phNo: String,
                             packageName: Option[Seq[Package]],
                             channels: Option[List[Channel]],
                             plan: Option[Plan]
                           )
object StateTranscation {
  val noTran = StateTranscation("",None,"",None,None,None)
}


case class Channel(name: String)

object Channel extends DefaultJsonProtocol {
  implicit val channelCodec = jsonFormat1(Channel.apply)
}

case class Package(name: String, channels: Seq[Channel])

object Package extends DefaultJsonProtocol {
  implicit val pavkageCodec = jsonFormat2(Package.apply)

  def validPackage(name: String): Boolean = {
    Try(PackageNames.values.contains(PackageNames.from(name))) match {
      case Success(_) => true
      case Failure(_) => false
    }
  }

  def apply(name: String): Option[Package] = {
    if (validPackage(name)) {
      ChannelList.getChannels(name).map {
        c => Package(name, c.map(l => Channel(l)))
      }
    } else {
      None
    }
  }
}


object ChannelList {
  val basicChannels: Seq[String] = List(
    "DDNational",
    "DDSports",
    "DDNews",
  )
  val entertainment: Seq[String] = basicChannels :+ "Sony" :+ "Starplus" :+ "ZeeTv"
  val sportsPlus: Seq[String] = entertainment.toList ::: List("startSports", "tenSports")
  val extraChannels: Seq[String] = List("BTSports")
  val allChannels: Seq[String] = sportsPlus.toList ::: extraChannels.toList


  def getChannels(name: String): Option[Seq[String]] = {
    PackageNames.from(name) match {
      case PackageNames.basic => Some(basicChannels)
      case PackageNames.entertainment => Some(entertainment)
      case PackageNames.sportsPlus => Some(sportsPlus)
      case _ => None
    }
  }
}


case class UnknownPackageException(packageName: String) extends Exception(packageName)

object PackageNames extends Enumeration {
  val basic, entertainment, sportsPlus = Value

  def from(name: String): PackageNames.Value = {
    name.toLowerCase match {
      case "basic" => PackageNames.basic
      case "entertainment" => PackageNames.entertainment
      case "sportsPlus" => PackageNames.sportsPlus
      case _ => throw UnknownPackageException("Unknown Package")
    }
  }

  def getPackageChannels(name: String): Option[Seq[String]] = {
    from(name) match {
      case PackageNames.basic => Some(ChannelList.basicChannels)
      case PackageNames.entertainment => Some(ChannelList.entertainment)
      case PackageNames.sportsPlus => Some(ChannelList.sportsPlus)
      case _ => None
    }
  }
}


sealed trait Plan {
  val price: Double
  val duration: FiniteDuration
  val name: String

  def channels(packageNames: Seq[Package]): Seq[Channel]
}


case class UnknownPlanException(packageName: String) extends Exception(packageName)

object Plan {
  def apply(plan: String): Plan = {
    plan.toLowerCase match {
      case "monthly" => Monthly
      case "biannual" => BiAnnual
      case "annual" => Annual
      case _ => throw UnknownPlanException("unknown plan")
    }
  }
}

case object Monthly extends Plan {
  override val price: Double = 300
  override val duration = 28 days
  override val name = "Monthly"

  override def channels(packageNames: Seq[Package]): Seq[Channel] = packageNames.foldLeft(List[Channel]())((acc, p) => acc ::: p.channels.toList)

}

case object BiAnnual extends Plan {
  override val price: Double = 1800
  override val duration: FiniteDuration = 180 days
  override val name: String = "BiAnnual"

  override def channels(packageNames: Seq[Package]): Seq[Channel] = packageNames.foldLeft(List[Channel]())((acc, p) => acc ::: p.channels.toList)
}

case object Annual extends Plan {
  override val price: Double = 3600
  override val duration: FiniteDuration = 360 days
  override val name: String = "Annual"

  override def channels(packageNames: Seq[Package]): Seq[Channel] = packageNames.foldLeft(List[Channel]())((acc, p) => acc ::: p.channels.toList)
}







