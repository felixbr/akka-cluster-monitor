package webapp.events

trait PubSub {
  type Handler = PartialFunction[Any, Unit]

  trait Subscription {
    def unsubscribe(): Unit
  }

  def publish(event: Any): Unit

  def subscribe(handler: PartialFunction[Any, Unit]): Subscription
}


object PubSub extends PubSub {
  class SubscriptionImpl(handler: PubSub.Handler) extends Subscription {
    override def unsubscribe(): Unit = {
      handlers = handlers - handler
    }
  }

  private var handlers = Set.empty[Handler]

  override def subscribe(handler: Handler): Subscription = {
    handlers += handler

    new SubscriptionImpl(handler)
  }

  override def publish(event: Any): Unit = handlers.foreach(handler => handler(event))
}
