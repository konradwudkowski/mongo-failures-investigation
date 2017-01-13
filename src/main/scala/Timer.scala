import java.time.LocalTime

import scala.concurrent.duration.Duration

object Timer {
  def timeNotExceeded(limit: Duration)(implicit startTime: LocalTime): Boolean = {
    startTime.plusNanos(limit.toNanos).isAfter(LocalTime.now())
  }
  def timeReached(limit: Duration)(implicit startTime: LocalTime): Boolean = {
    !timeNotExceeded(limit)
  }
}
