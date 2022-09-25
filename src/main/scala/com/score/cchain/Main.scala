package com.score.cchain

import akka.actor.ActorSystem
import com.score.cchain.actor.FinacleIntegrator.HoldAMount
import com.score.cchain.actor.{FinacleIntegrator, SenzActor}
import com.score.cchain.util.{ChainFactory, DbFactory}

object Main extends App {

  // first
  //  1. setup logging
  //  2. setup keys
  //  3. setup db
  ChainFactory.setupLogging()
  //ChainFactory.setupKeys()
  DbFactory.initDb()

  // start senz, block creator
  implicit val system = ActorSystem("senz")
  val a = system.actorOf(FinacleIntegrator.props, name = "SenzActor")
  a ! HoldAMount("2323", 2323)

}
