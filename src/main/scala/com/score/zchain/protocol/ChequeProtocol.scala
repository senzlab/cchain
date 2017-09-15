package com.score.zchain.protocol

import java.util.UUID

import com.datastax.driver.core.utils.UUIDs

case class Cheque(bankId: String, id: UUID = UUIDs.random, amount: Int, img: String)

