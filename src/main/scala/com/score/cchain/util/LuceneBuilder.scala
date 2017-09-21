package com.score.cchain.util

import com.score.cchain.actor.TransHandler.Criteria

import scala.annotation.tailrec

object LuceneBuilder {
  val toUnderscore = (name: String) => "[A-Z\\d]".r.replaceAllIn(name, { m =>
    "_" + m.group(0).toLowerCase()
  })

  def buildLuceneQuery(criteria: Criteria): String = {
    s"SELECT * FROM documents WHERE expr(documents_index, ${buildLuceneFilter(criteria)});"
  }

  def buildLuceneFilter(criteria: Criteria): String = {
    var f = "'{filter: ["

    @tailrec
    def build(l: List[(String, Any)]): String = {
      l match {
        case Nil =>
          f += "]}'"
          f
        case i :: Nil =>
          // last
          f += s"{type: ${'"'}wildcard${'"'}, field:${'"'}${toUnderscore(i._1)}${'"'}, value:${'"'}${if (i._2 == None) "*" else i._2.asInstanceOf[Option[Any]].get}${'"'}}"
          build(Nil)
        case i :: rl =>
          f += s"{type: ${'"'}wildcard${'"'}, field:${'"'}${toUnderscore(i._1)}${'"'}, value:${'"'}${if (i._2 == None) "*" else i._2.asInstanceOf[Option[Any]].get}${'"'}}, "
          build(rl)
      }
    }

    build(criteria.getClass.getDeclaredFields.map(_.getName).zip(criteria.productIterator.to).toList)
  }
}
