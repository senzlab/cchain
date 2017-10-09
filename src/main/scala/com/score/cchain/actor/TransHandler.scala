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

  case class CreateTrans(frm: String, to: String, cBnk: Option[String], cId: Option[String], cAmnt: Option[Int], cImg: Option[String], uid: Option[String] = None)

  def props = Props(classOf[TransHandler])

}

class TransHandler extends Actor with ChainDbCompImpl with AppConf with SenzLogger {
  val senzActor = context.actorSelection("/user/SenzActor")

  override def preStart(): Unit = {
    logger.debug("Start actor: " + context.self.path)
  }

  override def receive: Receive = {
    case CreateTrans(frm, to, _, None, Some(amnt), Some(img), Some(uid)) =>
      // new cheque
      logger.debug(s"create cheque and trans $frm $to $amnt")

      // create cheque + trans
      val chq = Cheque(senzieName, amount = amnt, img = img)
      chainDb.createCheque(chq)
      chainDb.createTransaction(Transaction(bankId = senzieName, cheque = chq, from = frm, to = to, digsig = "digsig"))

      // forward cheque to 'to'
      // send status back to 'from'
      senzActor ! Msg(SenzFactory.shareTransSenz(to, frm, chq.bankId, chq.id.toString, chq.img))
      senzActor ! Msg(SenzFactory.shareSuccessSenz(uid, frm, chq.id.toString, chq.bankId))
    case CreateTrans(from, to, Some(cBnk), Some(cId), _, None, Some(uid)) =>
      // cheque transfer
      logger.debug(s"create trans $from $to $cId")

      // check given cheque already transfer by 'from' previously
      // check given cheque already received to 'to' previously
      if (chainDb.transactionAvailable(Criteria(None, None, Some(from), None, Some(UUID.fromString(cId)))) ||
        chainDb.transactionAvailable(Criteria(None, None, None, Some(to), Some(UUID.fromString(cId))))) {
        // this is double spend
        // send fail status to creator
        senzActor ! Msg(SenzFactory.shareFailSenz(uid, from, cId, cBnk))
      } else {
        // take cheque with given id
        // create trans
        val chq = chainDb.getCheque(cBnk, UUID.fromString(cId))
        chainDb.createTransaction(Transaction(bankId = senzieName, cheque = chq.get, from = from, to = to, digsig = "digsig"))

        // forward cheque to 'to'
        // send status back to 'from'
        senzActor ! Msg(SenzFactory.shareTransSenz(to, from, chq.get.bankId, chq.get.id.toString, chq.get.img))
        senzActor ! Msg(SenzFactory.shareSuccessSenz(uid, from, chq.get.id.toString, chq.get.bankId))
      }
    case msg =>
      logger.error(s"unexpected message $msg")
  }

}
