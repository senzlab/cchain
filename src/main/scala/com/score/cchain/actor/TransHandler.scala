package com.score.cchain.actor

import java.util.UUID

import akka.actor.{Actor, Props}
import com.score.cchain.actor.TransHandler.{CreateTrans, Criteria}
import com.score.cchain.comp.ChainDbCompImpl
import com.score.cchain.config.AppConf
import com.score.cchain.protocol.{Cheque, Msg, Transaction}
import com.score.cchain.util.{SenzFactory, SenzLogger}

object TransHandler {

  case class Criteria(bankId: Option[String], id: Option[UUID], fromAcc: Option[String], toAcc: Option[String], chequeId: Option[UUID])

  case class CreateTrans(from: String, to: String, chequeBank: Option[String], chequeId: Option[String], chequeAmount: Option[Int], payload: Option[String])

  def props = Props(classOf[TransHandler])

}

class TransHandler extends Actor with ChainDbCompImpl with AppConf with SenzLogger {
  val senzActor = context.actorSelection("/user/SenzActor")

  override def preStart(): Unit = {
    logger.debug("Start actor: " + context.self.path)
  }

  override def receive: Receive = {
    case CreateTrans(from, to, None, None, Some(amount), Some(payload)) =>
      // create cheque + trans
      val chq = Cheque(senzieName, amount = amount, img = payload)
      chainDb.createCheque(chq)
      chainDb.createTransaction(Transaction(bankId = senzieName, cheque = chq, from = from, to = to, digsig = "digsig"))

      // forward cheque to 'to'
      // send status back to 'from'
      senzActor ! Msg(SenzFactory.shareTransSenz(to, from, payload))
      senzActor ! Msg(SenzFactory.shareSuccessSenz(from))
    case CreateTrans(from, to, Some(chequeBank), Some(chequeId), _, None) =>
      // check given cheque already transfer by 'from' previously
      // check given cheque already received to 'to' previously
      if (chainDb.transactionAvailable(Criteria(None, None, Some(from), None, Some(UUID.fromString(chequeId)))) ||
        chainDb.transactionAvailable(Criteria(None, None, None, Some(to), Some(UUID.fromString(chequeId))))) {
        // this is double spend
        // send fail status to creator
        senzActor ! Msg(SenzFactory.shareFailSenz(from))
      } else {
        // take cheque with given id
        // create trans
        val chq = chainDb.getCheque(chequeBank, UUID.fromString(chequeId))
        chainDb.createTransaction(Transaction(bankId = senzieName, cheque = chq.get, from = from, to = to, digsig = "digsig"))

        // forward cheque to 'to'
        // send status back to 'from'
        senzActor ! Msg(SenzFactory.shareTransSenz(to, from, "payload"))
        senzActor ! Msg(SenzFactory.shareSuccessSenz(from))
      }
  }

}
