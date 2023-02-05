package model

import actor.StateActor
import actor.StateActor.StateRequest
import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import cats.data.ValidatedNec
import cats.implicits._
import javafx.util.Duration.seconds

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}

sealed trait ValidationError {
    def errorMessage: String
  }


case class InvalidError(err:String) extends ValidationError {
    def errorMessage: String = s"Invalid value $err"
  }

object ValidationType {
  type ValidationResult[A] = ValidatedNec[ValidationError, A]
}

import ValidationType._



trait Validation[A] {
  def validate(request: A)(implicit ref:ActorRef[StateRequest],system:ActorSystem): ValidationResult[A]

   def checkId(id: String)(implicit ref: ActorRef[StateRequest], system: ActorSystem) = {
    StateActor.getStates(id).map {
      state =>
        state.state
    }
  }
}

object SubscribeValidation extends Validation[Subscribe] {
  override def validate(request: Subscribe)(implicit ref:ActorRef[StateRequest],system:ActorSystem): ValidationResult[Subscribe] = {
    lazy val emailValidation: ValidationResult[String] = if(request.email.nonEmpty) request.email.validNec else InvalidError("Email should not be empty").invalidNec
    lazy val phNoValidation: ValidationResult[String] = if(request.phNo.nonEmpty && request.phNo.length == 10) request.phNo.validNec else InvalidError("Phone number should be a valid one").invalidNec
    lazy val fullNameValidation: ValidationResult[String] = if(request.fullName.nonEmpty && request.fullName.length > 2)  request.fullName.validNec else InvalidError("Full ame should be greater than 2 char").invalidNec
    lazy val planValidation: ValidationResult[String] = Try(Plan(request.plan)) match {
      case Success(_) => request.plan.validNec
      case Failure(_) => InvalidError("Select valid plan from Monthly, Bimonthly, Yearly").invalidNec
    }
    val validPackage: ValidationResult[Option[Seq[String]]] = request.packageName.validNec
    val validChannel: ValidationResult[Option[Seq[String]]] = request.additionalChannel.validNec
    (emailValidation,phNoValidation,fullNameValidation,validPackage,validChannel,planValidation).mapN(Subscribe)
  }
}

object getDetailsValidation extends Validation[GetDetails] {
  override def validate(request: GetDetails)(implicit ref:ActorRef[StateRequest],system:ActorSystem): ValidationResult[GetDetails] = {
    lazy val uidValidation: ValidationResult[String] = {
      Try {
        Await.result(checkId(request.uid)(ref,system),15 seconds)
      } match {
        case Success(_) =>
          request.uid.validNec
        case Failure(_) =>  InvalidError("use valid uid").invalidNec
      }
    }
    (uidValidation).map(GetDetails)
  }
}

object subscribeChanelValidation extends Validation[SubscribeChanel] {
  override def validate(request: SubscribeChanel)(implicit ref: ActorRef[StateRequest], system: ActorSystem): ValidationResult[SubscribeChanel] = {
    lazy val nameValidation: ValidationResult[Option[List[String]]] = if(request.name.nonEmpty && request.name.get.nonEmpty) request.name.validNec else InvalidError("add valid chanel name").invalidNec
    lazy val idValidation: ValidationResult[String] = {
      Try {
        Await.result(checkId(request.uid)(ref, system), 15 seconds)
      } match {
        case Success(_) => request.uid.validNec
        case Failure(_) => InvalidError("use valid uid").invalidNec
      }
    }
    (nameValidation,idValidation).mapN(SubscribeChanel)
  }
}
