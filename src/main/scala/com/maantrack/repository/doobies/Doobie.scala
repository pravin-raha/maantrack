package com.maantrack.repository.doobies

import doobie.util.log.{ ExecFailure, LogHandler, ProcessingFailure, Success }
import org.slf4j.{ Logger, LoggerFactory }

import scala.concurrent.duration._

object Doobie {

  private val SlowThreshold  = 200.millis
  private val logger: Logger = LoggerFactory.getLogger(Doobie.getClass)

  implicit val doobieLogHandler: LogHandler = LogHandler {
    case Success(sql, _, exec, processing) =>
      if (exec > SlowThreshold || processing > SlowThreshold) {
        logger.warn(s"Slow query (execution: $exec, processing: $processing): $sql")
      } else {
        logger.info(s"Query (execution: $exec, processing: $processing): $sql")
      }
    case ProcessingFailure(sql, args, exec, processing, failure) =>
      logger.error(s"Processing failure (execution: $exec, processing: $processing): $sql | args: $args", failure)
    case ExecFailure(sql, args, exec, failure) =>
      logger.error(s"Execution failure (execution: $exec): $sql | args: $args", failure)

  }
}
