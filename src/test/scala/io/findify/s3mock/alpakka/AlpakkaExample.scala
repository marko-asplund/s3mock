package io.findify.s3mock.alpakka

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.ActorMaterializer
import org.apache.pekko.stream.connectors.s3.scaladsl.S3
import org.apache.pekko.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import scala.jdk.CollectionConverters._

object AlpakkaExample {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.parseMap(Map(
      "pekko.connectors.s3.proxy.host" -> "localhost",
      "pekko.connectors.s3.proxy.port" -> 8001,
      "pekko.connectors.s3.proxy.secure" -> false,
      "pekko.connectors.s3.path-style-access" -> true,
      "pekko.connectors.s3.aws.credentials.provider" -> "static",
      "pekko.connectors.s3.aws.credentials.access-key-id" -> "foo",
      "pekko.connectors.s3.aws.credentials.secret-access-key" -> "bar",
      "pekko.connectors.s3.aws.region.provider" -> "static",
      "pekko.connectors.s3.aws.region.default-region" -> "us-east-1"
    ).asJava)
    implicit val system = ActorSystem.create("test", config)
    implicit val mat = ActorMaterializer()
    import system.dispatcher
    val posibleSource = S3.download("bucket", "key").runWith(Sink.head)
    val contents = posibleSource.flatMap( obj => obj.map( content => content._1.runWith(Sink.head).map(_.utf8String)).getOrElse(Future.successful("")))
  }
}
