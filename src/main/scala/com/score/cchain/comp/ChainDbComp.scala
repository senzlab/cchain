package com.score.cchain.comp

import java.util.UUID

import com.score.cchain.actor.TransHandler.Criteria
import com.score.cchain.protocol.{Block, Cheque, Signature, Transaction}


trait ChainDbComp {

  val chainDb: ChainDb

  trait ChainDb {
    def createCheque(cheque: Cheque): Unit

    def getCheque(bankId: String, id: UUID): Option[Cheque]

    def getCheques: List[Cheque]

    def createTransaction(transaction: Transaction): Unit

    def getTransaction(bankId: String, id: UUID): Option[Transaction]

    def getTransactions: List[Transaction]

    def transactionAvailable(criteria: Criteria): Boolean

    def deleteTransactions(transactions: List[Transaction])

    def createBlock(block: Block): Unit

    def getBlock(bankId: String, id: UUID): Option[Block]

    def getBlocks: List[Block]

    def updateBlockSignature(block: Block, signature: Signature)
  }

}

