package com.score.zchain.comp

import java.util.UUID

import com.datastax.driver.core.UDTValue
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.querybuilder.QueryBuilder._
import com.score.zchain.protocol._
import com.score.zchain.util.DbFactory

import scala.collection.JavaConverters._

trait ChainDbCompImpl extends ChainDbComp {

  val chainDb = new ChainDbImpl

  class ChainDbImpl extends ChainDb {
    def createTransaction(transaction: Transaction): Unit = {
      val chqType = DbFactory.cluster.getMetadata.getKeyspace("zchain").getUserType("cheque")
      val chq = chqType.newValue
        .setString("bank_id", transaction.cheque.bankId)
        .setUUID("id", transaction.cheque.id)
        .setInt("amount", transaction.cheque.amount)

      // insert query
      val statement = QueryBuilder.insertInto("transactions")
        .value("bank_id", transaction.bankId)
        .value("id", transaction.id)
        .value("cheque", chq)
        .value("from_acc", transaction.from)
        .value("to_acc", transaction.to)
        .value("timestamp", transaction.timestamp)
        .value("digsig", transaction.digsig)

      DbFactory.session.execute(statement)
    }

    def getTransaction(bankId: String, id: UUID): Option[Transaction] = {
      // select query
      val selectStmt = select()
        .all()
        .from("transactions")
        .where(QueryBuilder.eq("bank_id", bankId)).and(QueryBuilder.eq("id", id))
        .limit(1)

      val resultSet = DbFactory.session.execute(selectStmt)
      val row = resultSet.one()

      if (row != null) {
        // get cheque
        val transUdt = row.getUDTValue("cheque")
        val cheque = Cheque(transUdt.getString("bank_id"),
          transUdt.getUUID("id"),
          transUdt.getInt("amount"),
          transUdt.getString("img"))

        // transaction
        Option(Transaction(row.getString("bank_id"),
          row.getUUID("id"),
          cheque,
          row.getString("from_acc"),
          row.getString("to_acc"),
          row.getLong("timestamp"),
          row.getString("digsig")))
      }
      else None
    }

    def getTransactions: List[Transaction] = {
      // select query
      val selectStmt = select()
        .all()
        .from("transactions")

      // get all transactions
      val resultSet = DbFactory.session.execute(selectStmt)
      resultSet.all().asScala.map { row =>
        // get cheque
        val transUdt = row.getUDTValue("cheque")
        val cheque = Cheque(transUdt.getString("bank_id"),
          transUdt.getUUID("id"),
          transUdt.getInt("amount"),
          transUdt.getString("img"))

        // transaction
        Transaction(row.getString("bank_id"),
          row.getUUID("id"),
          cheque,
          row.getString("from_acc"),
          row.getString("to_acc"),
          row.getLong("timestamp"),
          row.getString("digsig"))
      }.toList
    }

    def deleteTransactions(transactions: List[Transaction]): Unit = {
      for (t <- transactions) {
        // delete query
        val delStmt = delete()
          .from("transactions")
          .where(QueryBuilder.eq("bank_id", t.bankId)).and(QueryBuilder.eq("id", t.id))

        DbFactory.session.execute(delStmt)
      }
    }

    def createBlock(block: Block): Unit = {
      // UDT
      val transType = DbFactory.cluster.getMetadata.getKeyspace("zchain").getUserType("transaction")

      // transactions
      val trans = block.transactions.map(t =>
        transType.newValue
          .setString("bank_id", t.bankId)
          .setUUID("id", t.id)
          .setString("cheque_bank_id", t.cheque.bankId)
          .setUUID("cheque_id", t.cheque.id)
          .setInt("cheque_amount", t.cheque.amount)
          .setString("cheque_img", t.cheque.img)
          .setString("from_acc", t.from)
          .setString("to_acc", t.to)
          .setLong("timestamp", t.timestamp)
          .setString("digsig", t.digsig)
      ).asJava

      // insert query
      val statement = QueryBuilder.insertInto("blocks")
        .value("bank_id", block.bankId)
        .value("id", block.id)
        .value("transactions", trans)
        .value("timestamp", block.timestamp)
        .value("markel_root", block.markelRoot)
        .value("pre_hash", block.preHash)
        .value("hash", block.hash)

      DbFactory.session.execute(statement)
    }

    def getBlock(bankId: String, id: UUID): Option[Block] = {
      // select query
      val selectStmt = select()
        .all()
        .from("blocks")
        .where(QueryBuilder.eq("bank_id", bankId)).and(QueryBuilder.eq("id", id))
        .limit(1)

      val resultSet = DbFactory.session.execute(selectStmt)
      val row = resultSet.one()

      if (row != null) {
        // get transactions
        val trans = row.getSet("transactions", classOf[UDTValue]).asScala.map(t =>
          Transaction(t.getString("bank_id"),
            t.getUUID("id"),
            Cheque(t.getString("cheque_bank_id"), t.getUUID("cheque_id"), t.getInt("cheque_amount"), t.getString("cheque_img")),
            t.getString("from_acc"),
            t.getString("to_acc"),
            t.getLong("timestamp"),
            t.getString("digsig")
          )
        ).toList

        // get signatures
        val sigs = row.getSet("signatures", classOf[UDTValue]).asScala.map(s =>
          Signature(s.getString("bank_id"), s.getString("digsig"))
        ).toList

        // create block
        Option(
          Block(bankId,
            id,
            trans,
            row.getLong("timestamp"),
            row.getString("markel_root"),
            row.getString("pre_hash"),
            row.getString("hash"),
            sigs)
        )
      }
      else None
    }

    def getBlocks: List[Block] = {
      // select query
      val selectStmt = select()
        .all()
        .from("blocks")

      // get all transactions
      val resultSet = DbFactory.session.execute(selectStmt)
      resultSet.all().asScala.map { row =>
        val trans = row.getSet("transactions", classOf[UDTValue]).asScala.map(t =>
          Transaction(t.getString("bank_id"),
            t.getUUID("id"),
            Cheque(t.getString("cheque_bank_id"), t.getUUID("cheque_id"), t.getInt("cheque_amount"), t.getString("cheque_img")),
            t.getString("from_acc"),
            t.getString("to_acc"),
            t.getLong("timestamp"),
            t.getString("digsig")
          )
        ).toList

        // get signatures
        val sigs = row.getSet("signatures", classOf[UDTValue]).asScala.map(s =>
          Signature(s.getString("bank_id"), s.getString("digsig"))
        ).toList

        // create block
        Block(row.getString("bank_id"),
          row.getUUID("id"),
          trans,
          row.getLong("timestamp"),
          row.getString("markel_root"),
          row.getString("pre_hash"),
          row.getString("hash"),
          sigs)
      }.toList
    }

    def updateBlockSignature(block: Block, signature: Signature): Unit = {
      // signature type
      val sigType = DbFactory.cluster.getMetadata.getKeyspace("zchain").getUserType("signature")

      // signature
      val sig = sigType.newValue.setString("bank_id", signature.bankId).setString("digsig", signature.digsig)

      // existing signatures + new signature
      val sigs = block.signatures.map(s =>
        sigType.newValue
          .setString("bank_id", s.bankId)
          .setString("digsig", s.digsig)
      ) :+ sig

      // update query
      val statement = QueryBuilder.update("blocks")
        .`with`(QueryBuilder.add("signatures", sig))
        .where(QueryBuilder.eq("bank_id", block.bankId)).and(QueryBuilder.eq("id", block.id))

      DbFactory.session.execute(statement)
    }
  }

}
