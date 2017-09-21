package com.score.cchain.protocol

import java.util.UUID

import com.datastax.driver.core.utils.UUIDs

case class Block(bankId: String,
                 id: UUID = UUIDs.random,
                 transactions: List[Transaction],
                 timestamp: Long,
                 merkleRoot: String,
                 preHash: String,
                 hash: String,
                 signatures: List[Signature] = List())

