package com.score.cchain.util

import com.score.cchain.actor.TransHandler.Criteria

import scala.annotation.tailrec

object LuceneBuilder extends SenzLogger {
  val toUnderscore = (name: String) => "[A-Z\\d]".r.replaceAllIn(name, { m =>
    "_" + m.group(0).toLowerCase()
  })

  def buildLuceneQuery(criteria: Criteria): String = {
    val query = s"SELECT * FROM transactions WHERE expr(transactions_index, ${buildLuceneFilter(criteria)});"
    logger.info(s"Lucene query: $query")

    query
  }

  def buildLuceneFilter(criteria: Criteria): String = {
    var f = "'{filter: ["

    @tailrec
    def build(l: List[(String, Option[String])]): String = {
      l match {
        case Nil =>
          f += "]}'"
          f
        case i :: Nil =>
          // last
          f += s"{type: ${'"'}wildcard${'"'}, field:${'"'}${toUnderscore(i._1)}${'"'}, value:${'"'}${i._2.get}${'"'}}"
          build(Nil)
        case i :: rl =>
          f += s"{type: ${'"'}wildcard${'"'}, field:${'"'}${toUnderscore(i._1)}${'"'}, value:${'"'}${i._2.get}${'"'}}, "
          build(rl)
      }
    }

    build(criteria.getClass.getDeclaredFields.map(_.getName).zip(criteria.productIterator.to.map(_.asInstanceOf[Option[String]])).toList.filter(c => c._2.isDefined))
  }
}
