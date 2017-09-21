package com.score.cchain.util

import com.score.cchain.config.AppConf

object SenzFactory extends AppConf {
  def isValid(msg: String) = {
    msg == null || msg.isEmpty
  }

  def regSenz = {
    // unsigned senz
    val publicKey = RSAFactory.loadRSAPublicKey()
    val timestamp = (System.currentTimeMillis / 1000).toString
    val receiver = switchName
    val sender = senzieName

    s"SHARE #pubkey $publicKey #time $timestamp @$receiver ^$sender"
  }

  def pingSenz = {
    // unsigned senz
    val timestamp = (System.currentTimeMillis / 1000).toString
    val receiver = switchName
    val sender = senzieName

    s"PING #time $timestamp @$receiver ^$sender"
  }

  def blockSenz(blockId: String) = {
    val timestamp = (System.currentTimeMillis / 1000).toString
    val receiver = "*"
    val sender = senzieName

    s"PUT #block $blockId #sign #time $timestamp @$receiver ^$sender"
  }

  def blockSignSenz(blockId: String, bankId: String, signed: Boolean) = {
    val timestamp = (System.currentTimeMillis / 1000).toString
    val receiver = bankId
    val sender = senzieName

    s"DATA #block $blockId #sign $signed #time $timestamp @$receiver ^$sender"
  }

  def shareTransSenz(to: String, from: String, img: String) = {
    val timestamp = (System.currentTimeMillis / 1000).toString
    val sender = senzieName

    s"SHARE #img $img #from $from #time $timestamp @$to ^$sender"
  }

  def shareSuccessSenz(to: String) = {
    val timestamp = (System.currentTimeMillis / 1000).toString
    val sender = senzieName

    s"SHARE #status SUCCESS #time $timestamp @$to ^$sender"
  }

  def shareFailSenz(to: String) = {
    val timestamp = (System.currentTimeMillis / 1000).toString
    val sender = senzieName

    s"SHARE #status FAIL #time $timestamp @$to ^$sender"
  }

}
