package room
import row.Row
import types.Types._


object Room {
  val rowSize = 5
  val rowCount = 5
}

class Room(roomId: Int, plan: Map[Day, List[Screening]]) {
  import Row._ 
  import Room._ 
  val month = 30

  type MovieReservations = Array[list[Booking]]
  type DayReservations = Array[MovieReservations]
  type MonthReservations = Array[DayReservations]

  private val monthReservations = new Array[DayReservations](month)
  private val emptyRoom: MovieReservations = Array.fill(rowCount)(nil[Booking]())
  private def moviesOnParticDay(day: Day): Int = plan.getOrElse(day, Nil).size  
  
  (0 to (month-1)).foreach((i) => monthReservations(i) = Array.fill(moviesOnParticDay(i))(emptyRoom))  

  def isRoomFull(res: MovieReservations): Boolean = !res.exists(_.size < rowSize) 

  def getScreenings(day: Day, start: Hour, end: Hour): (RoomID, ScreeningData) = {
    val screenings: List[Screening] = plan.getOrElse(day, Nil)
    val dayReservations = monthReservations(day) 
    val scr = for { 
      (s,i) <- (screenings zip (0 to screenings.size))
      if s.start >= start
      if s.end <= end
      if !isRoomFull(dayReservations(i))
      } yield (i, s)

    (roomId, scr) 
  }

  def showPlaces(room: MovieReservations): String = {
    val booked = room.flatMap(_.toList.map(_.placeId)).to(collection.immutable.Set)
    val allSeats: List[Int] = (1 to rowSize * rowCount).toList
    val freeSeats = allSeats.filter(!booked.contains(_))
    freeSeats.map(_.toString).mkString(",") + "\n"
  }

  def showFreePlaces(day: Day, i: ScreeningID): String = {
    showPlaces(monthReservations(day)(i))
  }

  def isRowBookingValid(rowId: Int, booking: List[Booking], row: list[Booking]): Boolean = {
    val rowStart = rowId*rowSize+1
    val leftLim = rowStart
    val rightLim = rowStart+rowSize-1 
    val currBook = row.toList.map(_.placeId)
    val bookIds = booking.map(_.placeId)
    val inter = bookIds intersect currBook
    val newBook: List[PlaceID] = (currBook:::bookIds)
    inter.isEmpty && (newBook.min to (newBook.min + newBook.size -1)).toList == newBook && newBook.min >= leftLim && newBook.max <= rightLim
  }

  def isBookingValid(day: Day, booking: List[Booking], id: ScreeningID): Boolean = {
    val reservation = monthReservations(day)(id)
    val bookingMap = booking.groupBy(_.placeId / rowSize)
    bookingMap.forall{ case (rowId, b) => isRowBookingValid(rowId, b, reservation(rowId)) }
  } 

  def makeReservationToRow(booking: List[Booking], rows: Array[list[Booking]], rowId: RowID): Unit = {
    val sorted = booking.sortWith(_.placeId >= _.placeId)
    if (rows(rowId).isNil) rows(rowId) = Row.fromList(sorted); return 
    lazy val limes = rows(rowId).limes.get  
    lazy val (l, r) = (limes(0), limes(1))
    lazy val (leftBook, rightBook) = (sorted.filter(_.placeId < l.placeId), sorted.filter(_.placeId > r.placeId))
    
    if (limes.size == 1) {
      if (booking.map(_.placeId).max < l.placeId) rows(rowId) = Row.fromList(sorted) ++: rows(rowId)
      else rows(rowId) = Row.fromList(sorted) ++: rows(rowId)
    }
    else rows(rowId) = Row.fromList(leftBook) ++: rows(rowId) ++: Row.fromList(rightBook)
  }

  def makeReservation(day: Day, screeningId: ScreeningID, booking: List[(RowID, List[Booking])]): Unit = {
    booking.foreach{ case (rowId, b) => makeReservationToRow(b, monthReservations(day)(screeningId), rowId) }
  }
}
