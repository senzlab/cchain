package com.score.cchain.protocol

object SenzType extends Enumeration {
  type SenzType = Value
  val SHARE, GET, PUT, DATA, STREAM, PING, TAK, TIK, TUK, AWA, GIYA = Value
}

import com.score.cchain.protocol.SenzType._

case class Msg(data: String)

case class Ping()

case class SenzMsg(senz: Senz, data: String)

case class Senz(senzType: SenzType, sender: String, receiver: String, attributes: Map[String, String], signature: Option[String])

