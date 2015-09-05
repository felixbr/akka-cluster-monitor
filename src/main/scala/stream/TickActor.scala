package stream

import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.stream.actor.ActorPublisher

import scala.concurrent.duration._

case object Tick

class TickActor extends Actor {

  implicit val ec = context.dispatcher

  val tick = context.system.scheduler.schedule(1.second, 1.second, self, Tick)

  override def receive: Receive = {
    case _ =>
  }
}
