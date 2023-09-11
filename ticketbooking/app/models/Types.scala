package types

object Types {
  type MovieName = String
  type Person = String
  type PlaceID = Int
  type Booking = (Person, PlaceID)
  type Day = Int
  type Hour = Int
  type MovieStart = Hour
  type MovieEnd = Hour
  type Screening = (MovieName, MovieStart, MovieEnd)
  type RoomID = Int
  type ScreeningID = Int 
  type RowID = Int
  type Plan = Map[Day, List[Screening]]
  type ScreeningData = List[(ScreeningID, Screening)]
  type UserID = Int

  implicit class Screening_(val s: Screening) {
    def name = s._1 
    def start = s._2
    def end = s._3
  }

  implicit class Booking_(val b: Booking) {
    def person = b._1
    def placeId = b._2
  } 

  implicit class ScreeningIDScreening(val s: (ScreeningID, Screening)) {
    def screeningId = s._1
    def screening = s._2
  }

  implicit class RoomIDScreeningData(val r: (RoomID, ScreeningData)) {
    def roomId = r._1
    def screeningData = r._2
  }

}


