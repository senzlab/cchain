package com.score.cchain.util

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

object DateBuilder {
  val TIME_ZONE = TimeZone.getTimeZone("UTC")
  val TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"
  val DATE_FORMAT = "yyyy-MM-dd"

  def formatToDate(date: String, format: String): Date = {
    val sdf = new SimpleDateFormat(format)
    sdf.setTimeZone(TIME_ZONE)
    sdf.parse(date)
  }

  def formatToString(date: Option[Date], format: String): Option[String] = {
    date match {
      case Some(d) =>
        val sdf = new SimpleDateFormat(format)
        sdf.setTimeZone(TIME_ZONE)
        Option(sdf.format(d))
      case None =>
        None
    }
  }
}
