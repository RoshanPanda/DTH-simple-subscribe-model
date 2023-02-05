package services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, Uri}
import spray.json._

import scala.concurrent.Future

object HttpClient {
def submitRequest[T](req:T,endpoint:String)(implicit system:ActorSystem,  json: RootJsonFormat[T]):Future[HttpResponse] = {
  lazy val request = HttpRequest(
    HttpMethods.POST,
    Uri(endpoint),
    entity = HttpEntity(ContentTypes.`application/json`, req.toJson.compactPrint)
  )
  Http(system).singleRequest(request)
}

  def submitGetRequest[T](req: T, endpoint: String)(implicit system: ActorSystem, json: RootJsonFormat[T]): Future[HttpResponse] = {
    lazy val request = HttpRequest(
      HttpMethods.GET,
      Uri(endpoint),
      entity = HttpEntity(ContentTypes.`application/json`, req.toJson.compactPrint)
    )
    Http(system).singleRequest(request)
  }

}
