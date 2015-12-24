package com.giampaolotrapasso.phantom

import com.websudos.phantom.connectors.{ ContactPoint, KeySpace, KeySpaceBuilder, KeySpaceDef }

object Config {

  val keySpace = KeySpace("blog")
  val contactPoint: KeySpaceBuilder = ContactPoint(host = "localhost", port = ContactPoint.DefaultPorts.live)
  val keySpaceDefinition: KeySpaceDef = contactPoint.keySpace(keySpace.name)

}