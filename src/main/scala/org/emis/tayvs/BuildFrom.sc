import scala.util.{Failure, Try}
Try("sds") match {
  case FailureEx(ex) => 2
  case Failure(th) => 1
  case _ => 3
}

object FailureEx {
  def unapply[T](arg: Try[T]): Option[Exception] =
    Option(arg).collect { case Failure(ex: Exception) => ex }
}