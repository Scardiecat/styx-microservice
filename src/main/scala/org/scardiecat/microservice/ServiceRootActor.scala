package org.scardiecat.microservice

import akka.actor.Props
import spray.routing.{RouteConcatenation, HttpServiceActor, Route}

object ServiceRootActor extends RouteConcatenation {
  def props(routes: Seq[Route]): Props = Props(classOf[ServiceRootActor], routes.reduce(_ ~ _))
}

class ServiceRootActor(route: Route) extends HttpServiceActor {
  override def receive: Receive = runRoute(route)
}
