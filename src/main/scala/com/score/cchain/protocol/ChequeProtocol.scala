package com.score.cchain.protocol

import java.util.UUID

import com.datastax.driver.core.utils.UUIDs

case class Cheque(bankId: String, id: UUID = UUIDs.random, amount: Int, date: String, img: String)

