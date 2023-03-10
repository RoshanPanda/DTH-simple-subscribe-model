package actor

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import akka.actor.typed.{ActorRef, Behavior, Scheduler}
import akka.util.Timeout
import model.StateTranscation

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt


object StateActor {

  trait StateRequest

  trait StateResponse

  case class CreateState(state: StateTranscation) extends StateRequest

  case class UpdateState(state: StateTranscation) extends StateRequest

  case class GetState(id: String, replyTo: ActorRef[State]) extends StateRequest

  case class State(state: StateTranscation) extends StateRequest


  def getStateRequest(): Behavior[StateRequest] = {
    Behaviors.setup {
      context =>
        import context.executionContext
        implicit val system = context.system

        def state(s: scala.collection.mutable.Map[String, StateTranscation]): Behavior[StateRequest] = {
          Behaviors.receiveMessage {
            case CreateState(st) =>
              val v = s += (st.userId.get -> st)
              state(v)
              Behaviors.same
            case UpdateState(st) =>
              s.update(st.userId.get, st)
              Behaviors.same
            case GetState(id, replyTo) =>
              replyTo ! State(s.getOrElse(id, StateTranscation.noTran))
              Behaviors.same

          }
        }

        state(scala.collection.mutable.Map.empty)
    }
  }

  def getActor()(implicit actorSystem: ActorSystem): ActorRef[StateRequest] = {
    actorSystem.spawn(getStateRequest(), "state")
  }


  def getStates(uid: String)(implicit ref: ActorRef[StateRequest], actorSystem: ActorSystem): Future[State] = {
    val typedActorSystem = actorSystem.toTyped
    implicit val timeout: Timeout = 15.seconds
    implicit val scheduler: Scheduler = typedActorSystem.scheduler
    ref.ask(ref => GetState(uid, ref))
  }


}
