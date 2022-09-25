package com.score.cchain.actor

import java.net.URL

import akka.actor.{Actor, Props}
import com.score.cchain.actor.Authenticator.Authenticate
import com.score.cchain.config.AppConf
import spray.client.pipelining._
import spray.http.{HttpResponse, Uri}

import scala.concurrent.Future
import scala.io.Source
import scala.util.{Failure, Success}

object Authenticator {

  case class Authenticate(username: String, password: String)

  def props = Props(classOf[Authenticator])

}

class Authenticator extends Actor with AppConf {
  override def receive = {
    case Authenticate(username, password) =>
      import scala.concurrent.ExecutionContext.Implicits.global
      doRequestWithPipeline(loginReqest(username, password)).onComplete {
        case Success(value) => println(s"Got the callback, meaning = $value")
        case Failure(e) => e.printStackTrace()
      }
  }

  private def doRequest(reqXml: String): Future[String] = {
    val url = new URL(authApi)
    val conn = url.openConnection.asInstanceOf[java.net.HttpURLConnection]
    conn.setRequestMethod("POST")
    conn.setDoOutput(true)
    conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8")
    conn.setRequestProperty("SOAPAction", "http://mobileServices.web.app.sampath.org/MobileServicesMain/doLoginRequest")
    conn.getOutputStream.write(reqXml.getBytes)

    val resp = Source.fromInputStream(conn.getInputStream).mkString
    Future.successful(resp)
  }

  def doRequestWithPipeline(reqXml: String): Future[String] = {
    //execution context for the future
    import context.dispatcher

    val pipeline = (
      addHeader("Content-Type", "text/xml; charset=utf-8")
        ~> addHeader("SOAPAction", "http://mobileServices.web.app.sampath.org/MobileServicesMain/doLoginRequest")
        ~> sendReceive
        ~> unmarshal[HttpResponse]
      )

    val resp: Future[HttpResponse] = pipeline(Post(Uri(authApi), reqXml.getBytes))
    resp.map(r => r.entity.asString(spray.http.HttpCharsets.`UTF-8`))
  }

  private def loginReqest(username: String, password: String): String = {
    val requestXml =
      s"""
         |<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:mob="http://mobileServices.web.app.sampath.org">
         |  <soapenv:Header/>
         |  <soapenv:Body>
         |    <mob:doLogin>
         |      <mob:userId>$username</mob:userId>
         |      <mob:password>$password</mob:password>
         |    </mob:doLogin>
         |  </soapenv:Body>
         |</soapenv:Envelope>
        """.stripMargin

    requestXml
  }

}

