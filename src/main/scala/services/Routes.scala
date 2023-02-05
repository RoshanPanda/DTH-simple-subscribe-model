package services

import actor.StateActor
import actor.StateActor.{CreateState, UpdateState}
import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives.{entity, _}
import akka.stream.ActorMaterializer
import model._
import model.codecs._
import spray.json._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

trait Routes extends SprayJsonSupport {

  implicit val system = ActorSystem("PaymentSystem")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher
  val actor = StateActor.getActor()


  val subscriptionRoute =
    (post & path("subscribe") & entity(as[Subscribe])) {
      req =>
        val v = Wrapper(req).map {
          r =>
            Transformations.transform(r)(Transformations.transSubToState)
        }
          .map {
            r =>
              actor ! CreateState(r)
              r
          }.map {
          s => Transformations.transform(s)(Transformations.StateToSubscribed)
        }

        complete(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`,v.state().toJson.compactPrint)))
    }

  val addChannelRoute =
    (post & path("subscribe" / "addonpack") & entity(as[SubscribeChanel])) {
      req =>
        val transRes = Wrapper(req).map {
          r =>
            val f = Transformations.transSubscribeChanneltoState(actor) _
            Await.result(Transformations.transform(r)(f),15 seconds)
        }.map {
          r =>
            actor ! UpdateState(r)
            Await.result(
              StateActor.getStates(r.userId.get)(actor,system).map {
                state =>
                  Transformations.transform(state.s)(Transformations.StateToSubscribed)
              }
              ,15 seconds
              )
        }
        complete(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`,transRes.state().toJson.compactPrint)))
    }

  val getState =
    (get & path("subscribe" / "details") & entity(as[GetDetails])) {
      req =>
        val transRes = Wrapper(req).map {
          r =>
            val getRes = Await.result(StateActor.getStates(r.uid)(actor,system),20 seconds)
            getRes
        }.map {
          r  =>
           Transformations.transform(r.s)(Transformations.StateToSubscribed)
        }

       complete(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`,transRes.state().toJson.compactPrint)))
    }

  val routes = subscriptionRoute ~ addChannelRoute ~ getState

  lazy val bindingFuture = Http().bindAndHandle(routes, "0.0.0.0", 8080).map {
    binding =>
      println(s"Server started at ${binding.localAddress}")
  }

   def stopServer(bindingFuture: Seq[Http.ServerBinding]) = {
    bindingFuture.map(serverBinding => serverBinding.unbind())
    Future(Done)
  }

   def startServer(): Future[Http.ServerBinding] = {
    val bindingFuture: Future[Http.ServerBinding] =
      Http().bindAndHandle(routes, "0.0.0.0", 8080)
    println(s"Server online at http://localhost:8080/\n")
    bindingFuture

  }

}

