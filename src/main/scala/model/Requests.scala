package model

import spray.json.DefaultJsonProtocol

sealed trait Request

sealed trait Response

case class GetDetails(uid: String) extends Request

case class Subscribe(
                      email: String,
                      phNo: String,
                      fullName: String,
                      packageName: Option[Seq[String]] = None,
                      additionalChannel: Option[Seq[String]] = None,
                      plan: String
                    )

/*
Requests
 */
case class SubscribeChanel(name: Option[List[String]], uid: String) extends Request
//case class GetPackageDetails(userId:Option[String]) extends Request
//case class GetAllChannels(userId:Option[String]) extends Request
//case class GetPackageChannels(name:String) extends Request


/*
Responses
 */
case class Subscribed(userId: String, packageName: Option[Seq[String]], plan: Option[String], channels: Option[List[String]]) extends Response
//case class AllChannelLists(channels:List[String]) extends Response
//case class PackageChannelLists(channels:List[String]) extends Response

/*
Error response
 */
case class ErrorRes(success: Boolean, message: String)


object codecs extends DefaultJsonProtocol {

  implicit val getdtl = jsonFormat1(GetDetails)
  implicit val sub = jsonFormat6(Subscribe)
  implicit val subChnl = jsonFormat2(SubscribeChanel)
  implicit val error = jsonFormat2(ErrorRes)
  implicit val subd = jsonFormat4(Subscribed)
  //implicit val chnlLst = jsonFormat1(AllChannelLists)
  //implicit val pkgChnls = jsonFormat1(PackageChannelLists)
  //implicit val getPkgDtls = jsonFormat1(GetPackageDetails)
  //implicit val getAChnls = jsonFormat1(GetAllChannels)
  //implicit val getPchaels = jsonFormat1(GetPackageChannels)

}