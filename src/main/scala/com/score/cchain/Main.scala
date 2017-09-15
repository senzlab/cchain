package com.score.cchain

import akka.actor.ActorSystem
import com.score.cchain.actor.SenzActor
import com.score.cchain.util.{DbFactory, CchainFactory}

object Main extends App {

  // first
  //  1. setup logging
  //  2. setup keys
  //  3. setup db
  CchainFactory.setupLogging()
  CchainFactory.setupKeys()
  DbFactory.initDb()

  // start senz, block creator, block signer
  implicit val system = ActorSystem("senz")
  system.actorOf(SenzActor.props, name = "SenzActor")

}
