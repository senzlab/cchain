package com.score.cchain.actor

import akka.actor.{Actor, Props}
import com.score.cchain.actor.TransHandler.CreateTrans
import com.score.cchain.comp.ChainDbCompImpl
import com.score.cchain.config.AppConf
import com.score.cchain.protocol.Msg
import com.score.cchain.util.{SenzFactory, SenzLogger}

object TransHandler {

  case class CreateTrans(from: String, to: String, chequeId: Option[String], payload: Option[String])

  def props = Props(classOf[TransHandler])

}

class TransHandler extends Actor with ChainDbCompImpl with AppConf with SenzLogger {
  val senzActor = context.actorSelection("/user/SenzActor")

  override def preStart(): Unit = {
    logger.debug("Start actor: " + context.self.path)
  }

  override def receive: Receive = {
    case CreateTrans(from, to, None, Some(payload)) =>
      // create cheque + trans

      // forward cheque to 'to'
      senzActor ! Msg(SenzFactory.shareTransSenz(to, from, payload.getBytes()))
    case CreateTrans(from, to, Some(chequeId), None) =>
      // check given cheque already transfer by 'from' previously

      // check given cheque already received to 'to' previously

      // create trans

      // forward cheque to 'to'
      senzActor ! Msg(SenzFactory.shareTransSenz(to, from, "payload".getBytes()))
  }

}
