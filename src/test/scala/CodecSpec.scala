import model.{Subscribe, SubscribeChanel, codecs}
import org.scalatest.wordspec.AsyncWordSpec
import spray.json._
import codecs._

class CodecSpec extends AsyncWordSpec {

  "encode Request to Json" in {
    val sampleReq1 = Subscribe(
      "roshan.raj.panda@gmail.com",
      "0123456789",
      "Roshan Raj Panda",
      Some(List("basic")),
      Some(List("DDNational")),
      "Monthly"
    )
    val smapleReq2 = SubscribeChanel(
      Some(List("BTSports")),
      "123456789"
    )

    val jsonStrSuscribe = sampleReq1.toJson.compactPrint
    val jsonStrSubChanel = smapleReq2.toJson.compactPrint

    assert(jsonStrSuscribe ==
      """{"additionalChannel":["DDNational"],"email":"roshan.raj.panda@gmail.com","fullName":"Roshan Raj Panda","packageName":["basic"],"phNo":"0123456789","plan":"Monthly"}""".stripMargin)
    assert(jsonStrSubChanel  == """{"id":"123456789","name":["BTSports"]}""" )
  }



}
