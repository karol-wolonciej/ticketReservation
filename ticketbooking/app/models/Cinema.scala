package cinema
import room.Room 
import types.Types._ 
import scala.util.Random 

class Cinema(roomsCount: Int) {

  val rooms: Array[Room] = new Array[Room](roomsCount) 

  val (cinOpen, cinClose) = (8, 22)
  val monthLen = 30
  val plans = createPlan()
  
  (0 to roomsCount-1).foreach(i => rooms(i) = new Room(i, plans(i)))  

  def createPlan(): Map[RoomID, Map[Day, List[Screening]]] = {
    import data.Data._ 
    val randomVar = new Random
    
    def generateInt() = randomVar.nextInt(moviesNames.size) 
    def generateScreenings(): List[Screening] = (cinOpen to cinClose-1).toList.map(s => (moviesNames(generateInt()), s, s+1))
    def generateMonthPlan(): Map[Day, List[Screening]] = (1 to monthLen).map((_, generateScreenings())).toMap
    
    (0 to roomsCount-1).map((_, generateMonthPlan())).toMap
  }

  def getScreenings(day: Day, start: Hour, end: Hour): List[(RoomID, ScreeningData)] = {
    val scr = for (i <- (0 to roomsCount-1).toList) yield rooms(i).getScreenings(day, start, end)
    scr.filter(!_._2.isEmpty) 
  }

  def getFreePlaces(day: Day, data: List[(RoomID, ScreeningID)]): List[String] = {
    for ((roomId, scrId) <- data) yield rooms(roomId).showFreePlaces(day, scrId)
  } 

  def isReservationValid(day: Day,
                         roomId: RoomID,
                         screeningId: ScreeningID,
                         booking: List[(RowID, List[Booking])]): Boolean = {
      booking.forall(b => rooms(roomId).isBookingValid(day, b._2, screeningId))
  }

  def makeReservation(day: Day, 
                      roomId: RoomID, 
                      screeningId: ScreeningID, 
                      booking: List[(RowID, List[Booking])]): Unit = {
    rooms(roomId).makeReservation(day, screeningId, booking)
  }

}
