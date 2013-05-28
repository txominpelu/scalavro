package com.gensler.scalavro.protocol

import HandshakeMatch._

case class HandshakeResponse(
  `match`: HandshakeMatch,
  serverProtocol: Option[String],
  serverHash: Option[MD5],
  meta: Map[String, Seq[Byte]])
