package org.scardiecat.microservice

import akka.actor.{ActorRef, ActorSystem}
import akka.io.IO
import com.typesafe.config.{ConfigFactory, Config}
import spray.can.Http
import spray.routing.Route

import scala.concurrent.ExecutionContext

trait Microservice {
  /**
    * Implementations must return the entire ActorSystem configuration
    * @return the configuration
    */
  def config: Config

  /**
    * Implementations must return the entire ActorSystem name
    * @return the actorSystemName
    */
  def actorSystemName: String

  /**
    * Starts up the Microservice ActorSystem, binding Akka remoting to ``port`` and exposing all
    * rest services at ``0.0.0.0:restPort``.
    * @param port the Akka port
    * @param restPort the REST services port
    */
  final def actorSystemStartUp(port: Int, restPort: Int): Unit = {
    // Create an Akka system
    val finalConfig =
      ConfigFactory.parseString(
        s"""
           |akka.remote.netty.tcp.port=$port
         """.stripMargin).
        withFallback(config)

    implicit val system = ActorSystem(actorSystemName, finalConfig)

    val transport = IO(Http)

    val microserviceRoutes = getMicroserviceRoutes(system,system.dispatcher)

    startupHttpService(transport, restPort, microserviceRoutes)
  }


  /**
    * Implementations must return sequence of Routes
    * @return the Routes
    */
  def getMicroserviceRoutes(implicit system:ActorSystem, ec:ExecutionContext): Seq[Route]
  /**
    * Startup the REST API handler
    * @param system the (booted) ActorSystem
    * @param port the port
    * @param routes the routes
    */
  private def startupHttpService(transport: ActorRef, port: Int, routes: Seq[Route])(implicit system: ActorSystem): Unit = {
    val api = system.actorOf(ServiceRootActor.props(routes))
    transport ! Http.Bind(api, interface = "0.0.0.0", port = port)
  }
}
