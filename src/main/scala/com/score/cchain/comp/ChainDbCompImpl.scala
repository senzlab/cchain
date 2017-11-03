package com.score.cchain.comp

import java.util.UUID

import com.datastax.driver.core.UDTValue
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.querybuilder.QueryBuilder._
import com.score.cchain.actor.TransHandler.Criteria
import com.score.cchain.protocol._
import com.score.cchain.util.{DbFactory, LuceneBuilder}

import scala.collection.JavaConverters._

trait ChainDbCompImpl extends ChainDbComp {

  val chainDb = new ChainDbImpl

  class ChainDbImpl extends ChainDb {
    def createCheque(cheque: Cheque): Unit = {
      // insert query
      val statement = QueryBuilder.insertInto("cheques")
        .value("bank_id", cheque.bankId)
        .value("id", cheque.id)
        .value("amount", cheque.amount)
        .value("date", cheque.date)
        .value("img", cheque.img)

      DbFactory.session.execute(statement)
    }

    def getCheque(bankId: String, id: UUID): Option[Cheque] = {
      // select query
      val selectStmt = select()
        .all()
        .from("cheques")
        .where(QueryBuilder.eq("bank_id", bankId)).and(QueryBuilder.eq("id", id))
        .limit(1)

      val resultSet = DbFactory.session.execute(selectStmt)
      val row = resultSet.one()

      if (row != null) {
        Option(
          Cheque(row.getString("bank_id"),
            row.getUUID("id"),
            row.getInt("amount"),
            row.getString("date"),
            row.getString("img")
          )
        )
      }
      else None
    }

    def getCheques: List[Cheque] = {
      // select query
      val selectStmt = select()
        .all()
        .from("cheques")

      // get all transactions
      val resultSet = DbFactory.session.execute(selectStmt)
      resultSet.all().asScala.map { row =>
        Cheque(row.getString("bank_id"),
          row.getUUID("id"),
          row.getInt("amount"),
          row.getString("date"),
          row.getString("img")
        )
      }.toList
    }

    def createTransaction(transaction: Transaction): Unit = {
      def create(table: String) = {
        // insert query
        val statement = QueryBuilder.insertInto(table)
          .value("bank_id", transaction.bankId)
          .value("id", transaction.id)
          .value("cheque_bank_id", transaction.cheque.bankId)
          .value("cheque_id", transaction.cheque.id)
          .value("cheque_amount", transaction.cheque.amount)
          .value("cheque_date", transaction.cheque.date)
          .value("cheque_img", transaction.cheque.img)
          .value("from_acc", transaction.from)
          .value("to_acc", transaction.to)
          .value("timestamp", transaction.timestamp)
          .value("digsig", transaction.digsig)
          .value("status", transaction.status)

        DbFactory.session.execute(statement)
      }

      // insert in to two tables
      create("trans")
      create("transactions")
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
        Option(
          Transaction(row.getString("bank_id"),
            row.getUUID("id"),
            Cheque(
              row.getString("cheque_bank_id"),
              row.getUUID("cheque_id"),
              row.getInt("cheque_amount"),
              row.getString("cheque_date"),
              row.getString("cheque_img")),
            row.getString("from_acc"),
            row.getString("to_acc"),
            row.getLong("timestamp"),
            row.getString("digsig"),
            row.getString("status")
          )
        )
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
        Transaction(row.getString("bank_id"),
          row.getUUID("id"),
          Cheque(
            row.getString("cheque_bank_id"),
            row.getUUID("cheque_id"),
            row.getInt("cheque_amount"),
            row.getString("cheque_date"),
            row.getString("cheque_img")),
          row.getString("from_acc"),
          row.getString("to_acc"),
          row.getLong("timestamp"),
          row.getString("digsig"),
          row.getString("status")
        )
      }.toList
    }

    def transactionAvailable(criteria: Criteria): Boolean = {
      val selectStmt = LuceneBuilder.buildLuceneQuery(criteria)

      val resultSet = DbFactory.session.execute(selectStmt)
      val row = resultSet.one()

      if (row != null) true else false
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
      val transType = DbFactory.cluster.getMetadata.getKeyspace("cchain").getUserType("transaction")

      // transactions
      val trans = block.transactions.map(t =>
        transType.newValue
          .setString("bank_id", t.bankId)
          .setUUID("id", t.id)
          .setString("cheque_bank_id", t.cheque.bankId)
          .setUUID("cheque_id", t.cheque.id)
          .setInt("cheque_amount", t.cheque.amount)
          .setString("cheque_date", t.cheque.date)
          .setString("cheque_img", t.cheque.img)
          .setString("from_acc", t.from)
          .setString("to_acc", t.to)
          .setLong("timestamp", t.timestamp)
          .setString("digsig", t.digsig)
          .setString("status", t.status)
      ).asJava

      // insert query
      val statement = QueryBuilder.insertInto("blocks")
        .value("bank_id", block.bankId)
        .value("id", block.id)
        .value("transactions", trans)
        .value("timestamp", block.timestamp)
        .value("merkle_root", block.merkleRoot)
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
            Cheque(
              t.getString("cheque_bank_id"),
              t.getUUID("cheque_id"),
              t.getInt("cheque_amount"),
              t.getString("cheque_date"),
              t.getString("cheque_img")),
            t.getString("from_acc"),
            t.getString("to_acc"),
            t.getLong("timestamp"),
            t.getString("digsig"),
            t.getString("status")
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
            row.getString("merkle_root"),
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
            Cheque(t.getString("cheque_bank_id"),
              t.getUUID("cheque_id"),
              t.getInt("cheque_amount"),
              t.getString("cheque_date"),
              t.getString("cheque_img")),
            t.getString("from_acc"),
            t.getString("to_acc"),
            t.getLong("timestamp"),
            t.getString("digsig"),
            t.getString("status")
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
          row.getString("merkle_root"),
          row.getString("pre_hash"),
          row.getString("hash"),
          sigs)
      }.toList
    }

    def updateBlockSignature(block: Block, signature: Signature): Unit = {
      // signature type
      val sigType = DbFactory.cluster.getMetadata.getKeyspace("cchain").getUserType("signature")

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
