package org.emis.tayvs

object Utils {
  
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import akka.actor.ActorSystem
  import akka.pattern.after
  import scala.concurrent.duration.FiniteDuration
  
  implicit class FutureExtensions[T](f: Future[T]) {
    def withTimeout(timeout: => Throwable)(implicit duration: FiniteDuration, system: ActorSystem): Future[T] = {
      Future firstCompletedOf Seq(f, after(duration, system.scheduler)(Future.failed(timeout)))
    }
  }
  
}
