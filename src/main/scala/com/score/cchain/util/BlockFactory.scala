package com.score.cchain.util

import com.score.cchain.protocol.Transaction

import scala.annotation.tailrec

object BlockFactory {

  def merkleRoot(transactions: List[Transaction]): String = {
    @tailrec
    def merkle(ins: List[String], outs: List[String]): String = {
      ins match {
        case Nil =>
          // empty list
          if (outs.size == 1) outs.head
          else merkle(outs, List())
        case x :: Nil =>
          // one element list
          merkle(Nil, outs :+ RSAFactory.sha256(x + x))
        case x :: y :: l =>
          // have at least two elements in list
          // concat them and sign them
          merkle(l, outs :+ RSAFactory.sha256(x + y))
      }
    }

    merkle(transactions.map(t => RSAFactory.sha256(t.id.toString)), List())
  }

  def hash(timestamp: String, markleRoot: String, preHash: String): String = {
    val p = timestamp + markleRoot + preHash
    RSAFactory.sha256(p)
  }

}
