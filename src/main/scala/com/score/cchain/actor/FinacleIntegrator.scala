package com.score.cchain.actor

import java.net.URL

import akka.actor.{Actor, Props}
import com.score.cchain.actor.FinacleIntegrator.HoldAMount
import com.score.cchain.config.AppConf
import spray.client.pipelining._
import spray.http.{HttpResponse, Uri}

import scala.concurrent.Future
import scala.io.Source
import scala.util.{Failure, Success}
import scala.xml.Elem

object FinacleIntegrator {

  case class HoldAMount(account: String, amount: Int)

  def props = Props(classOf[FinacleIntegrator])

}

class FinacleIntegrator extends Actor with AppConf {
  override def receive = {
    case HoldAMount(account, amount) =>
      import scala.concurrent.ExecutionContext.Implicits.global

      //doRequest(getRequest(account, amount)).onComplete {
      doRequestWithPipeline().onComplete {
        case Success(value) => println(s"Got the callback, meaning = $value")
        case Failure(e) => e.printStackTrace()
      }
  }

  private def getRequest(account: String, amount: Int): String = {
    val requestXml =
      s"""
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:mob="http://mobileServices.web.app.sampath.org">
   <soapenv:Header/>
   <soapenv:Body>
      <mob:doLogin>
         <mob:userId>erangas</mob:userId>
         <mob:password>33223</mob:password>
      </mob:doLogin>
   </soapenv:Body>
</soapenv:Envelope>
        """.stripMargin.replaceAll("\n", "").replaceAll("\r", "")

    //println(requestXml)
    requestXml
  }

  private def doRequest(requestXml: String): Future[String] = {
    val url = new URL(authApi)
    val conn = url.openConnection.asInstanceOf[java.net.HttpURLConnection]
    val req = <IsAlive xmlns="https://api.authorize.net/soap/v1/"/>
    val outs = requestXml.getBytes
    conn.setRequestMethod("POST")
    conn.setDoOutput(true)
    conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8")
    //conn.setRequestProperty("Content-Length", outs.length.toString)
    conn.getOutputStream.write(outs)

    val xml = Source.fromInputStream(conn.getInputStream).mkString
    println(xml)
    Future.successful(xml)
  }

  def doRequestWithPipeline() = {
    //execution context for the future
    import context.dispatcher

    val req =
      <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:mob="http://mobileServices.web.app.sampath.org">
        <soapenv:Header/>
        <soapenv:Body>
          <mob:doLogin>
            <mob:userId>eranga</mob:userId>
            <mob:password>33223</mob:password>
          </mob:doLogin>
        </soapenv:Body>
      </soapenv:Envelope>
    //val outs = wrap(req).getBytes
    val outs = req.toString().getBytes

    //val url = "http://www.dneonline.com/calculator.asmx"
    //val url = "http://ws.cdyne.com/WeatherWS/Weather.asmx"
    //val url = "https://apitest.authorize.net/soap/v1/Service.asmx"
    val pipeline = (
      // we want to get json
      addHeader("Content-Type", "text/xml; charset=utf-8")
        ~> addHeader("SOAPAction", "http://mobileServices.web.app.sampath.org/MobileServicesMain/doLoginRequest")
        // if this shows a compilation error in IntelliJ, it is just a bug in the IDE - it compiles fine
        ~> sendReceive
        ~> unmarshal[HttpResponse]
      )

    val resp: Future[HttpResponse] = pipeline(Post(Uri(authApi), outs))
    resp.map(r => r.entity.asString(spray.http.HttpCharsets.`UTF-8`))
  }

  def wrap(xml: Elem): String = {
    val buf = new StringBuilder
    buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n")
    buf.append("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n")
    buf.append("<SOAP-ENV:Body>\n")
    buf.append(xml.toString)
    buf.append("\n</SOAP-ENV:Body>\n")
    buf.append("</SOAP-ENV:Envelope>\n")
    val s = buf.toString
    printf(s)

    s
  }

}
