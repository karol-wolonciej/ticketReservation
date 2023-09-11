package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import scala.util.{Try, Success, Failure}


object Conversions {
  import types.Types._ 
 
  def cnvScreeningsInRoom(r: (RoomID, ScreeningData)): String = {
    val (roomId, scrData) = r 
    s"Room $roomId: \n" + scrData.map(_.toString).mkString("\n")
  } 
  
  def cnvScreenings(s: List[(RoomID, ScreeningData)]): String = {
    s.map(cnvScreeningsInRoom(_)).mkString("\n\n")
  }

  def cnvSeats(rows: List[String]): String = {
    rows.mkString("\n") 
  }
}


object DataValidation {
  import scala.util.matching.Regex

  val name, surname = "([a-zA-ZąĄęĘóÓćĆńŃłŁśŚźŹżŻ]{3,20})"
  val optionalSurname = "(-[a-zA-ZąĄęĘóÓćĆńŃłŁśŚźŹżŻ]{3,20}){0,1}"
  val nameSurnameRegex = (name + " " + surname + optionalSurname).r 

  def isNameSurnameValid(nameSurname: String): Boolean = nameSurnameRegex.pattern.matcher(nameSurname).matches
}


object TicketBookingController {
  import cinema._
  import types.Types._

  var newUserId = 0  

  class UserData(val choosenDay: Day, 
                 val availableScreenings: List[(RoomID, ScreeningData)]) { 
    var screeningChosed: Boolean = false 
    var choosenScreeningId: Int = -1
    var choosenRoomId: Int = -1
  }

  
  def getNewUserId(): UserID = { newUserId += 1; newUserId }
  val userRequests = scala.collection.mutable.Map[UserID, UserData]()
  val ticketPrices = Map[String, Double]("adult" -> 25, "student" -> 18, "child" -> 12.5)

  val roomsCount = 9
  val rowSize, rowsCount = 5
  private val kino = new Cinema(roomsCount)
}


@Singleton
class TicketBookingController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  import types.Types._  
  //implicit val screeningDataJson = Json.format[List[(RoomID, ScreeningData)]]
  import TicketBookingController.{kino, UserData, userRequests, ticketPrices, rowSize, rowsCount}  

  def getScreenings(day: Day, 
                    start: MovieStart, 
                    end: MovieEnd) = Action {
    synchronized { 
      val scr = kino.getScreenings(day, start, end)
      import Conversions.{cnvScreenings} 
      import TicketBookingController.{getNewUserId}
      val newUserId = getNewUserId()
      val res = scr match {
        case Nil => Ok("brak filmow tego dnia w tych godzinach\n")
        case _ => Ok(s"${cnvScreenings(scr)}\n\ntwój identyfikator to: $newUserId \n") 
      }
    
      userRequests += (newUserId -> new UserData(day, scr))  
      res     
    } 
  } 

  def chooseScreening(userId: UserID,
                      roomId: Int, 
                      screeningId: Int) = Action {
    import Conversions.{cnvSeats} 
      
    lazy val userExist = userRequests.contains(userId) 
    lazy val userData = userRequests(userId)
    lazy val choose = userData.availableScreenings.filter(_.roomId == roomId).head.screeningData.filter(_.screeningId == screeningId)
    lazy val seats = kino.getFreePlaces(userData.choosenDay, List((userData.choosenRoomId, userData.choosenScreeningId)))
   
    synchronized { 
      if (roomId < 0 || roomId > 8) Ok("błędny numer sali\n") 
      else if (!userExist) Ok("prosze najpierw wyszukac filmy\n")
      else if (choose.isEmpty) Ok("wybrano nieistniejący pokaz\n")
      else {
        userData.choosenRoomId = roomId
        userData.choosenScreeningId = screeningId
        userData.screeningChosed = true
        userRequests += (userId -> userData)
    
        Ok(cnvSeats(seats)) 
      }
    }
  }

  def makeReservation(userId: UserID) = Action {
    request => 
    import DataValidation.{isNameSurnameValid}
    lazy val json = request.body.asJson
    lazy val resStr: List[String] = json.get("reservation").as[List[Int]].map(_.toString)
    lazy val namesSurnames: List[String] = json.get("nameSurname").as[List[String]]
    lazy val ticketTypes: List[String] = json.get("ticketTypes").as[List[String]]
    lazy val userExist = userRequests.contains(userId) 
    lazy val userData = userRequests(userId) 
    lazy val reservationsOp = resStr.map((s: String) => Try(s.toInt).toOption)
    lazy val reservations = reservationsOp.map(_.get) 
    lazy val booking = namesSurnames.zip(reservations).groupBy(_._2 / rowSize).toList

    synchronized {
      if (!json.isDefined) Ok("nierawidłowe parametry body\n")
      if (!(resStr.size == namesSurnames.size) || 
         !(namesSurnames.size == ticketTypes.size)) Ok("listy musza miec taka sama dlugosc\n")
      else if (resStr.isEmpty) Ok("nalezy wybrac conajmniej jedno miejsce\n") 
      else if (!userExist) Ok("prosze najpierw wyszukac filmy\n")
      else if (!userData.screeningChosed) Ok("prosze najpierw wybrac film\n")
      else if (reservationsOp.exists(!_.isDefined)) Ok("miejsce nie jest intem\n") 
      else if (!namesSurnames.forall(isNameSurnameValid(_))) Ok("nieprawidlowe imie i nazwisko\n")
      else if (!ticketTypes.forall((t: String) => ticketPrices.contains(t.toLowerCase))) Ok("nieprawidłowy rodzaj biletu\n")
      else if (!kino.isReservationValid(userData.choosenDay, 
                                        userData.choosenRoomId,
                                        userData.choosenScreeningId,
                                        booking)) Ok("nie istniejące miejsce bądź już zajęte\n")
      else {  
        kino.makeReservation(userData.choosenDay,
                           userData.choosenRoomId,
                           userData.choosenScreeningId,
                           booking) 
     
        userRequests.remove(userId) 
        val totalCost: Double = ticketTypes.map((t: String) => ticketPrices(t.toLowerCase)).toList.sum
        Ok(s"rezerwacja przeprowadzona pomyslne, koszt: ${totalCost}\n")
      }
    }
  }
}
