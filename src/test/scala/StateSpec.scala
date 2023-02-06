
import actor.StateActor
import actor.StateActor.{CreateState, UpdateState}
import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import model.{Annual, Channel, Monthly, Package, Plan, StateTranscation}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AsyncWordSpec


class StateSpec extends AsyncWordSpec {

  implicit val system = ActorSystem("test")

  val sampleState = StateTranscation(
    "Roshan Raj Panda",
    Some("abbcd1234bbbc"),
    "1234567890",
    Some(Seq(Package("basic").get)),
    Some(List(Channel("DDNational"))),
    Some(Plan("monthly"))
  )

  "add actor state" ignore {
    val actor = StateActor.getActor()
    actor ! CreateState(sampleState)
    Thread.sleep(3000)
    val states = StateActor.getStates(sampleState.userId.get)(actor,system)
    for {
      res <- states
    } yield {
      assert(res.state.plan.contains(Monthly))
    }
  }

  "update actor state" in {
    val actor = StateActor.getActor()
    val sampleState2 = sampleState.copy(plan = Some(Plan("annual")))
    actor ! CreateState(sampleState)
    actor ! UpdateState(sampleState2)
    val states = StateActor.getStates(sampleState2.userId.get)(actor,system)
    for {
      res <- states
    } yield {
      assert(res.state.plan.contains(Annual))
    }
  }

}
