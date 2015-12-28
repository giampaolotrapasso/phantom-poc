resolvers += Resolver.url(
   "websudos",
    url("https://dl.bintray.com/websudos/oss-releases/"))(
       Resolver.ivyStylePatterns)

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.4.0")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

addSbtPlugin("websudos" % "phantom-sbt" % "1.12.2")
