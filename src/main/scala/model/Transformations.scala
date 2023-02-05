package model

import actor.StateActor
import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import cats.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

object Transformations {

  def transform[A, B](state: A)(f: A => B): B = f(state)

  val transSubToState: Subscribe => StateTranscation = (s: Subscribe) => {

    val packageDtl = s.packageName.map {
      pl =>
        pl.flatMap(p => Package(p))
    }

    val Pchannels: Option[List[Channel]] = packageDtl.map(p => Plan(s.plan).channels(p).toList)
    val additionalChannels: Option[List[Channel]] = s.additionalChannel.map(l => l.map(Channel.apply).toList)
    val totalChannels: Option[List[Channel]] = Pchannels |+| additionalChannels

    StateTranscation(
      s.fullName,
      getOrgen(None),
      s.phNo,
      packageDtl,
      totalChannels,
      Some(Plan(s.plan))
    )
  }

  def transSubscribeChanneltoState(aref: ActorRef[StateActor.StateRequest])(implicit system: ActorSystem): SubscribeChanel => Future[StateTranscation] = (s: SubscribeChanel) => {
    val addchanel: Option[List[Channel]] = for {
      channelList <- s.name
    } yield {
      channelList.map(Channel.apply)
    }

    val cstate = StateActor.getStates(s.uid)(aref, system)
    cstate.map {
      state =>
        state.state.copy(channels = state.state.channels |+| addchanel)
    }
  }

  def StateToSubscribed(state: StateTranscation): Subscribed = {
    Subscribed(
      state.userId.getOrElse("0"),
      state.packageName.map(_.map(_.name)),
      state.plan.map(_.name),
      state.channels.map(_.map(_.name))
    )
  }

  private def generateId: String = Random.alphanumeric.take(10).mkString

  private def getOrgen(id: Option[String]): Option[String] = {
    id.collect {
      case id => id
    }.orElse(Some(generateId))
  }

}
