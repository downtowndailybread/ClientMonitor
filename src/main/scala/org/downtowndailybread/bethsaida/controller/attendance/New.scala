package org.downtowndailybread.bethsaida.controller.attendance

import akka.http.scaladsl.server.Directives._
import org.downtowndailybread.bethsaida.controller.ControllerBase
import org.downtowndailybread.bethsaida.exception.attendance.DuplicateAttendanceException
import org.downtowndailybread.bethsaida.exception.event.DuplicateRecordsException
import org.downtowndailybread.bethsaida.json.JsonSupport
import org.downtowndailybread.bethsaida.model.{AttendanceAttribute, EventAttribute}
import org.downtowndailybread.bethsaida.providers.{AuthenticationProvider, DatabaseConnectionProvider, SettingsProvider}
import org.downtowndailybread.bethsaida.request.{AttendanceRequest, EventRequest}
import org.postgresql.util.PSQLException

trait New extends ControllerBase {
  this: JsonSupport with DatabaseConnectionProvider with SettingsProvider with AuthenticationProvider =>

  val attendance_newRoute = path("new") {
    authorizeNotAnonymous {
      implicit iu =>
        post {
          entity(as[AttendanceAttribute]) {
            ea =>
              futureCompleteCreated {
                try {
                  runSql(c =>
                    new AttendanceRequest(settings, c).createAttendance(ea))
                }
                catch {
                  case e: PSQLException =>
                    if(e.getServerErrorMessage.getConstraint() == "unique_event_and_client") {
                      throw new DuplicateAttendanceException()
                    }
                    throw e
                }
              }
          }
        }
    }
  }
}
