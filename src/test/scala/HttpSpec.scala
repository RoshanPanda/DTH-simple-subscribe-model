import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import model._
import model.codecs.{getdtl, sub, _}
import org.scalatest.wordspec.AsyncWordSpec
import services.{HttpClient, Routes}
import spray.json._

class HttpSpec extends AsyncWordSpec {
  implicit val system = ActorSystem("test")

  val sampleReq = Subscribe(
    "roshan.raj.panda@gmail.com",
    "0123456789",
    "Roshan Raj Panda",
    Some(List("basic")),
    Some(List("DDNational")),
    "Monthly"
  )


  "test https server" ignore {

    for {
      bind <- httpTest.startServer()
      res <- HttpClient.submitRequest[Subscribe](sampleReq,"http://localhost:8080/subscribe" )
    } yield {
      for {
        unm <- Unmarshal(res).to[String]
      } yield {
        val result = if(res.status.intValue() == 200) unm.parseJson.convertTo[Subscribed] else unm.parseJson.convertTo[ErrorRes]
        //println(result)
        //assert(result.plan.get == "Monthly")
      }
      Thread.sleep(3000)
      httpTest.stopServer(Seq(bind))
      assert(true)
    }
  }

  "test getdetails" in {

    for {
      bind <- httpTest.startServer()
      res <- HttpClient.submitRequest[Subscribe](sampleReq,"http://localhost:8080/subscribe" )
      unm: String <- Unmarshal(res).to[String]
      id = unm.parseJson.convertTo[Subscribed].userId
      resG: HttpResponse <- HttpClient.submitGetRequest[GetDetails](GetDetails(id),"http://localhost:8080/subscribe/details")
      unmg: String <-  Unmarshal(resG).to[String]
      sub  = unmg.parseJson.convertTo[Subscribed]
    } yield {
      println(unm)
      println(s"current result $sub")
      httpTest.stopServer(Seq(bind))
      assert(sub.plan.contains("Monthly"))
    }

  }

  "validate Subscriptin RouterSupport" ignore {
    val res = httpTest.subscriptionRouteSupport(sampleReq).state()
    assert(res.plan.contains("Monthly"))
  }

}

object httpTest extends Routes
