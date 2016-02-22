package org.scardiecat.styx.microservice

import akka.actor.{ActorRef, ActorSystem}
import akka.io.IO
import com.google.inject.{Injector, Module, Guice}
import com.typesafe.config.{Config}
import net.codingwell.scalaguice.InjectorExtensions._
import org.scardiecat.styx.akkaguice.AkkaModule
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
    * Starts up the Microservice ActorSystem, Exposing all
    * rest services at ``0.0.0.0:restPort``.
    * @param restPort the REST services port
    * @param guiceModules Injectable GuiceModules
    */
  final def actorSystemStartUp(port: Int, restPort: Int, guiceModules: Seq[Module]= Seq()): Unit = {
    // Create an Akka system
    val confModule = new ConfigModule(config, actorSystemName)
    val akkaModule = new AkkaModule()

    val injector: Injector = Guice.createInjector((guiceModules :+ confModule :+ akkaModule):_*)

    implicit val system = injector.instance[ActorSystem]

    val transport = IO(Http)

    val microserviceRoutes = getMicroserviceRoutes(system, system.dispatcher)

    startupHttpService(transport, restPort, microserviceRoutes)
  }


  /**
    * Implementations must return sequence of Routes
    * @return the Routes
    */
  def getMicroserviceRoutes(implicit system: ActorSystem, ec: ExecutionContext): Seq[Route]

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
