package com.score.cchain.config

import com.typesafe.config.ConfigFactory

import scala.util.Try

trait SchemaConf {
  // config object
  val schemaConf = ConfigFactory.load("schema.conf")

  // cassandra config
  lazy val schemaCreateKeyspace = Try(schemaConf.getString("schema.createKeyspace")).getOrElse("")
  lazy val schemaCreateTypeCheque = Try(schemaConf.getString("schema.createTypeCheque")).getOrElse("")
  lazy val schemaCreateTypeTransaction = Try(schemaConf.getString("schema.createTypeTransaction")).getOrElse("")
  lazy val schemaCreateTypeSignature = Try(schemaConf.getString("schema.createTypeSignature")).getOrElse("")
  lazy val schemaCreateTableCheques = Try(schemaConf.getString("schema.createTableCheques")).getOrElse("")
  lazy val schemaCreateTableTransactions = Try(schemaConf.getString("schema.createTableTransactions")).getOrElse("")
  lazy val schemaCreateTableTrans = Try(schemaConf.getString("schema.createTableTrans")).getOrElse("")
  lazy val schemaCreateTableBlocks = Try(schemaConf.getString("schema.createTableBlocks")).getOrElse("")
  lazy val schemaCreateChequeIndex = Try(schemaConf.getString("schema.createChequeIndex")).getOrElse("")
  lazy val schemaCreateLuceneIndex = Try(schemaConf.getString("schema.createLuceneIndex")).getOrElse("")
}
