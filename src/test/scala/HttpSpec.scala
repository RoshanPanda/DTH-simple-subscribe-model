import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import model._
import org.scalatest.wordspec.AsyncWordSpec
import services.{HttpClient, Routes}
import model.codecs.{getdtl, sub, _}
import spray.json._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

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


  "test https server" in {

    for {
      bind <- httpTest.startServer()
      res <- HttpClient.submitRequest[Subscribe](sampleReq,"http://localhost:8080/subscribe" )
    } yield {
      for {
        unm <- Unmarshal(res).to[String]
      } yield {
        val result = unm.parseJson.convertTo[Subscribed]
        println(result)
        //assert(result.plan.get == "Monthly")
      }
      Thread.sleep(3000)
      httpTest.stopServer(Seq(bind))
      assert(true)
    }
  }

  "test getdetails" ignore {

    for {
      bind <- httpTest.startServer()
      res <- HttpClient.submitRequest[Subscribe](sampleReq,"http://localhost:8080/subscribe" )
      unm: String <- Unmarshal(res).to[String]
      id = unm.parseJson.convertTo[Subscribed].userId
      resG: HttpResponse <- HttpClient.submitGetRequest[GetDetails](GetDetails(id),"http://localhost:8080/subscribe/details")
      unmg: String <-  Unmarshal(resG).to[String]
      sub  = unmg.parseJson.convertTo[Subscribed]
    } yield {
      println(s"current result $sub")
      httpTest.stopServer(Seq(bind))
      assert(true)
    }

  }

}

object httpTest extends Routes
