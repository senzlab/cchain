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

  case class CreateTrans(frm: String, to: String, cBnk: Option[String], cId: Option[String], cAmnt: Option[Int],
                         cDate: Option[String], cImg: Option[String], uid: Option[String], digsig: Option[String])

  def props = Props(classOf[TransHandler])

}

class TransHandler extends Actor with ChainDbCompImpl with AppConf with SenzLogger {
  val senzActor = context.actorSelection("/user/SenzActor")

  override def preStart(): Unit = {
    logger.debug("Start actor: " + context.self.path)
  }

  override def receive: Receive = {
    case CreateTrans(frm, to, _, None, Some(amnt), Some(date), Some(img), Some(uid), Some(digsig)) =>
      // new cheque
      logger.debug(s"create cheque and trans $frm $to $amnt")

      // create cheque + trans
      val chq = Cheque(senzieName, amount = amnt, date = date, img = img)
      chainDb.createCheque(chq)
      chainDb.createTransaction(Transaction(bankId = senzieName, cheque = chq, from = frm, to = to, digsig = digsig))

      // forward cheque to 'to'
      // send status back to 'from'
      senzActor ! Msg(SenzFactory.shareTransSenz(to, frm, chq.bankId, chq.id.toString, chq.img, chq.amount, chq.date))
      senzActor ! Msg(SenzFactory.shareSuccessSenz(uid, frm, chq.id.toString, chq.bankId))
    case CreateTrans(from, to, Some(cBnk), Some(cId), _, _, None, Some(uid), Some(digsig)) =>
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
        val chq = chainDb.getCheque(cBnk, UUID.fromString(cId))

        // check weather deposit
        // means 'to' is 'sampath' (bank username)
        if (to.equalsIgnoreCase(senzieName)) {
          // deposit
          // create trans as DEPOSIT
          chainDb.createTransaction(Transaction(bankId = senzieName, cheque = chq.get, from = from, to = to, digsig = digsig, status = "DEPOSIT"))
        } else {
          // not deposit
          // create trans as transfer
          // forward cheque to 'to'
          chainDb.createTransaction(Transaction(bankId = senzieName, cheque = chq.get, from = from, to = to, digsig = digsig))
          senzActor ! Msg(SenzFactory.shareTransSenz(to, from, chq.get.bankId, chq.get.id.toString, chq.get.img, chq.get.amount, chq.get.date))
        }

        // send status back to 'from'
        senzActor ! Msg(SenzFactory.shareSuccessSenz(uid, from, chq.get.id.toString, chq.get.bankId))
      }
    case msg =>
      logger.error(s"unexpected message $msg")
  }

}
