package com.score.cchain.actor

import akka.actor.{Actor, Props}
import com.score.cchain.actor.BlockSigner.Sign
import com.score.cchain.comp.ChainDbCompImpl
import com.score.cchain.config.AppConf
import com.score.cchain.protocol.Block
import com.score.cchain.util.{BlockFactory, SenzLogger}

import scala.concurrent.duration._

object BlockCreator {

  case class Create()

  def props = Props(classOf[BlockCreator])

}

class BlockCreator extends Actor with ChainDbCompImpl with AppConf with SenzLogger {

  import BlockCreator._
  import context._

  override def preStart(): Unit = {
    logger.debug("Start actor: " + context.self.path)
  }

  override def receive: Receive = {
    case Create =>
      // take transactions from db and create block
      val trans = chainDb.getTransactions
      if (trans.nonEmpty) {
        val timestamp = System.currentTimeMillis
        val merkleRoot = BlockFactory.merkleRoot(trans)
        val block = Block(bankId = senzieName,
          hash = BlockFactory.hash(timestamp.toString, merkleRoot, "prehash"),
          transactions = trans,
          timestamp = timestamp,
          merkleRoot = merkleRoot,
          preHash = "prehash"
        )
        chainDb.createBlock(block)

        logger.debug("block created, send to sign ")

        // start another actor to sign the block
        val signer = context.actorOf(BlockSigner.props)
        signer ! Sign(Option(block), None, None)

        // delete all transaction saved in the block from transactions table
        chainDb.deleteTransactions(block.transactions)
      } else {
        logger.debug("No transactions to create block" + context.self.path)
      }

      // reschedule to create
      context.system.scheduler.scheduleOnce(40.seconds, self, Create)
  }
}
