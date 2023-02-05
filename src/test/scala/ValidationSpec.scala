import actor.StateActor
import akka.actor.ActorSystem
import model.{GetDetails, Subscribe, SubscribeChanel, SubscribeValidation, getDetailsValidation, subscribeChanelValidation}
import org.scalatest.wordspec.AsyncWordSpec

class ValidationSpec extends AsyncWordSpec{
  implicit val system = ActorSystem("test")
  implicit val actor = StateActor.getActor()
  val sampleReq = Subscribe(
    "roshan.raj.panda@gmail.com",
    "0123456789",
    "Roshan Raj Panda",
    Some(List("basic")),
    Some(List("DDNational")),
    "Monthly"
  )

  val getDetailsReq = GetDetails("qtyghghj")
  val subChanel = SubscribeChanel(
    Some(List("BTSport")),
    "fkgflfghl"
  )

  "check subscribe validation" in {
    val res = SubscribeValidation.validate(sampleReq)
    assert(res.isValid == true)
  }

  "check getDetails validation" in {
    val res = getDetailsValidation.validate(getDetailsReq)
    println(res)
    assert(res.isValid ==  false)
  }

  "check SubsribeChanel spec" in {
    val res = subscribeChanelValidation.validate(subChanel)
    assert(res.isInvalid == true)
  }

}
