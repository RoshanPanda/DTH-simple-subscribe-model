package services

import actor.StateActor
import actor.StateActor.{CreateState, UpdateState}
import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCode}
import akka.http.scaladsl.server.Directives.{entity, _}
import akka.stream.ActorMaterializer
import cats.data.Validated.{Invalid, Valid}
import model._
import model.codecs._
import spray.json._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

trait Routes extends SprayJsonSupport {

  implicit val system = ActorSystem("PaymentSystem")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  val actor = StateActor.getActor()


  val subscriptionRoute =
    (post & path("subscribe") & entity(as[Subscribe])) {
      req =>
        SubscribeValidation.validate(req)(actor,system) match {
          case Valid(a) =>
            Try {
              val transFormedRes = subscriptionRouteSupport(a)
              transFormedRes.state()
            } match {
              case Success(value) => complete(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, value.toJson.compactPrint)))
              case Failure(err) =>
                complete(HttpResponse(status = StatusCode.int2StatusCode(300), entity = HttpEntity(ContentTypes.`application/json`, ErrorRes(false, err.getMessage).toJson.compactPrint)))
            }
          case Invalid(e) =>
            complete(HttpResponse(status = StatusCode.int2StatusCode(300), entity = HttpEntity(ContentTypes.`application/json`, ErrorRes(false, e.toString).toJson.compactPrint)))
        }
    }


  val addChannelRoute =
    (post & path("subscribe" / "addChannel") & entity(as[SubscribeChanel])) {
      req =>
        subscribeChanelValidation.validate(req)(actor,system) match {
          case Valid(a) =>
            val transRes = sunscribeChannelSupport(req)
            Try {
              transRes.state()
            } match {
              case Success(value) => complete(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, value.toJson.compactPrint)))
              case Failure(err) => complete(HttpResponse(status = StatusCode.int2StatusCode(300), entity = HttpEntity(ContentTypes.`application/json`, ErrorRes(false, err.getMessage).toJson.compactPrint)))
            }
          case Invalid(e) => complete(HttpResponse(status = StatusCode.int2StatusCode(300), entity = HttpEntity(ContentTypes.`application/json`, ErrorRes(false, e.toString).toJson.compactPrint)))
        }

    }

  val getState =
    (get & path("subscribe" / "details") & entity(as[GetDetails])) {
      req =>
        getDetailsValidation.validate(req)(actor,system) match {
          case Valid(a) =>
            val transRes: Wrapper[Subscribed] = getStateSupport(req)
            Try {
              transRes.state()
            } match {
              case Success(value) => complete(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, value.toJson.compactPrint)))
              case Failure(err) => complete(HttpResponse(status = StatusCode.int2StatusCode(300), entity = HttpEntity(ContentTypes.`application/json`, ErrorRes(false, err.getMessage).toJson.compactPrint)))
            }
          case Invalid(e) =>
          complete(HttpResponse(status = StatusCode.int2StatusCode(300), entity = HttpEntity(ContentTypes.`application/json`, ErrorRes(false,e.toString).toJson.compactPrint)))
        }
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

   def subscriptionRouteSupport(request: Subscribe): Wrapper[Subscribed] = {
    val transFormRes = Wrapper(request).map {
      res =>
        Transformations.transform(res)(Transformations.transSubToState)
    }
      .map {
        res =>
          actor ! CreateState(res)
          res
      }.map {
      s => Transformations.transform(s)(Transformations.StateToSubscribed)
    }
    transFormRes
  }

  private def sunscribeChannelSupport(request: SubscribeChanel): Wrapper[Subscribed] = {
    Wrapper(request).map {
      res =>
        val f = Transformations.transSubscribeChanneltoState(actor) _
        Await.result(Transformations.transform(res)(f), 15 seconds)
    }.map {
      res =>
        actor ! UpdateState(res)
        Await.result(
          StateActor.getStates(res.userId.get)(actor, system).map {
            state =>
              Transformations.transform(state.state)(Transformations.StateToSubscribed)
          }
          , 15 seconds
        )
    }
  }

  private def getStateSupport(request: GetDetails) = {
    Wrapper(request).map {
      req =>
        Await.result(StateActor.getStates(req.uid)(actor, system), 20 seconds)
    }.map {
      req =>
        Transformations.transform(req.state)(Transformations.StateToSubscribed)
    }
  }
}

