package row
import scala.language.higherKinds
import types.Types._

trait BaseList {
  type list[T] <: ListBase[T]

  trait ListBase[T] {
    def +:(h: T): list[T]
    def ++:(r: list[T]): list[T] 
    def isNil: Boolean
    def get: list[T] 
    
    private[row] var next: Option[list[T]] = None
    final def tail: list[T] = next.get 
  } 
 
  def elem[E](h: E, t: list[E]): list[E]
  def nil[E](): list[E]

  trait ElemBase[T] extends ListBase[T] { 
    val head: T
    final def isNil = false
  }

  trait NilBase[T] extends ListBase[T] { def isNil = true }
}

trait GetLast extends BaseList {
  type list[T] <: ListLast[T] 

  trait ListLast[T] extends ListBase[T] { def last: Option[T] }

  trait ElemLast[T] extends ElemBase[T] with ListLast[T] {
    final def last = if (tail.isNil) Some(head) else tail.last
  }
  
  trait NilLast[T] extends NilBase[T] with ListLast[T] { final def last = None }
}

trait GetLimes extends BaseList {
  this: GetLast =>
  type list[T] <: ListLimes[T] 

  trait ListLimes[T] extends ListLast[T] {
    def limes: Option[List[T]]
  }

  trait ElemLimes[T] extends ElemBase[T] with ListLimes[T] with ListLast[T] {
    final def limes = {
      lazy val end = last.get
      if (tail.isNil) Some(List(head)) else Some(List(head, end))
    }
  }

  trait NilLimes[T] extends NilBase[T] with ListLimes[T] with ListLast[T] { final def limes = None }
}

trait AddLast extends BaseList {
  type list[T] <: ListAddLast[T]

  trait ListAddLast[T] extends ListBase[T] { def addLast[E >: T](t: E): list[E] }

  trait ElemAddLast[T] extends ElemBase[T] with ListAddLast[T] {
    final def addLast[E >: T](t: E) = elem(head, tail.addLast(t))
  }

  trait NilAddLast[T] extends NilBase[T] with ListAddLast[T] {
    final def addLast[E >: T](t: E) = elem(t, nil())
  }
}

trait Size extends BaseList {
  type list[T] <: ListSize[T]

  trait ListSize[T] extends ListBase[T] { def size: Int }

  trait ElemSize[T] extends ElemBase[T] with ListSize[T] { def size = 1 + tail.size }

  trait NilSize[T] extends NilBase[T] with ListSize[T] { def size = 0 } 
}

trait ToList extends BaseList {
  type list[T] <: ListToList[T]

  trait ListToList[T] extends ListBase[T] { def toList: List[T] }

  trait ElemToList[T] extends ElemBase[T] with ListToList[T] { 
    final def toList = {
      import scala.collection.mutable.ListBuffer
      var l: list[T] = this.get
      val newList = ListBuffer[T]() 
      while (!l.isNil) {
         newList += l.asInstanceOf[ElemBase[T]].head
         l = l.tail
      }
      newList.toList
    }
  }

  trait NilToList[T] extends NilBase[T] with ListToList[T] {
    final def toList = Nil
  }
}

trait MonadList extends BaseList {
  type list[T] <: ListMonad[T] 

  trait Monad[A] {
    def map[B](f: A => B): list[B]
    def flatMap[B](f: A => list[B]): list[B]
    def withFilter(p: A => Boolean): list[A]
  } 

  trait ListMonad[T] extends ListBase[T] with Monad[T] 

  trait ElemMonad[T] extends ElemBase[T] with ListMonad[T] { 
    final def map[E](f: (T => E)): list[E] = { 
      val h = elem(f(head), nil[E]())
      var t: list[E] = h
      var rest = tail
      while (!rest.isNil) {
        val nx = elem(f(rest.asInstanceOf[ElemBase[T]].head), nil[E]())
        t.next = Some(nx)
        t = nx
        rest = rest.asInstanceOf[ElemBase[T]].tail
      }
      h
    }

    final def flatMap[E](f: T => list[E]): list[E] = {
      val h = nil[E]() 
      var t: list[E] = h
      var rest = this.get
      while (!rest.isNil) {
        var r = f(rest.asInstanceOf[ElemBase[T]].head)
        while (!r.isNil) {
          val nx = elem(r.asInstanceOf[ElemBase[E]].head, nil[E]())
          t.next = Some(nx)
          t = nx
          r = r.tail
        }
        rest = rest.tail
      }
      h.tail
    }

    final def withFilter(p: T => Boolean): list[T] = {
      val h = nil[T]()
      var t: list[T] = h 
      var rest = this.get
      while(!rest.isNil) {
        if (p(rest.asInstanceOf[ElemBase[T]].head)) {
          val nx = elem(rest.asInstanceOf[ElemBase[T]].head, nil[T]())
          t.next = Some(nx)
          t = nx
        }
        rest = rest.tail
      }
      h.tail
    }
  }

  trait NilMonad[T] extends NilBase[T] with ListMonad[T] {
    final def map[E](f: T => E): list[E] = nil[E]()
    final def flatMap[E](f: T => list[E]): list[E] = nil[E]()
    final def withFilter(p: T => Boolean): list[T] = nil[T]()
  }
}

trait LastLimes extends GetLast with GetLimes {
  type list[T] <: ListLimesLast[T]
  trait ListLimesLast[T] extends ListLimes[T] with ListLast[T]

  trait ElemLimesLast[T] extends ElemLimes[T] with ElemLast[T] with ListLimesLast[T]
  trait NilLimesLast[T] extends NilLimes[T] with NilLast[T] with ListLimesLast[T]
}

trait AddSize extends AddLast with Size {
  type list[T] <: ListAddSize[T]
  trait ListAddSize[T] extends ListAddLast[T] with ListSize[T]

  trait ElemAddSize[T] extends ElemAddLast[T] with ElemSize[T]
  trait NilAddSize[T] extends NilAddLast[T] with NilSize[T]
}

trait LastLimesAddSizeList extends LastLimes with AddSize with ToList {
  type list[T] <: ListLimesLastAddSizeList[T]
  trait ListLimesLastAddSizeList[T] extends ListLimesLast[T] with ListAddSize[T] with ListToList[T]

  trait ElemLimesLastAddSizeList[T] extends ElemLimesLast[T] with ElemAddSize[T] with ElemToList[T]
  trait NilLimesLastAddSizeList[T] extends NilLimesLast[T] with NilAddSize[T] with NilToList[T]
}

trait MonadLastLimesAddSizeList extends LastLimesAddSizeList with MonadList {
  type list[T] <: ListMonadLimesLastAddSizeList[T] 
  trait ListMonadLimesLastAddSizeList[T] extends ListMonad[T] with ListLimesLastAddSizeList[T]

  trait ElemMonadLimesLastAddSizeList[T] extends ElemMonad[T] with ElemLimesLastAddSizeList[T]
  trait NilMonadLimesLastAddSizeList[T] extends NilMonad[T] with NilLimesLastAddSizeList[T]
} 


object Row extends MonadLastLimesAddSizeList {
  type list[T] = ListMonadLimesLastAddSizeList[T]

  trait Elem[T] extends ElemMonadLimesLastAddSizeList[T] with ListMonadLimesLastAddSizeList[T] {
    def +:(h: T): list[T] = elem[T](h, elem[T](head, tail))
    def ++:(r: list[T]): list[T] = {
      val l1 = toList
      val l2 = r.toList
      val buff = new RowBuffer[T]()
      l1.foreach(buff.addOne(_))
      l2.foreach(buff.addOne(_))
      buff.toRow
    } 
    def get = this 
  } 

  trait NilList[T] extends NilMonadLimesLastAddSizeList[T] with ListMonadLimesLastAddSizeList[T] {
     def +:(h: T): list[T] = elem[T](h, nil[T]())
     def ++:(r: list[T]): list[T] = r
     def get = this
  } 
  
  def elem[E](h: E, t: list[E]): list[E] = { val e = new Elem[E] { val head = h } 
                                             e.next = Some(t)
                                             e
  }  
  def nil[E](): list[E] = new NilList[E] {}  
  def fromList[T](l: List[T]): list[T] = {
    val buff = new RowBuffer[T]()
    l.foreach(buff.addOne(_))
    buff.toRow
  } 
}


class RowBuffer[T] {
  import Row._
  private var first: list[T] = nil[T]()
  private var last0: list[T] = null
  private var aliased: Boolean = false

  def addOne(element: T): this.type = {
    if (aliased) throw new UnsupportedOperationException("buffer already turned to list")
    val last1 = elem[T](element, nil[T]())
    if (first.isNil) first = last1 else last0.next = Some(last1)
    last0 = last1
    this
  }
  
  def toRow: list[T] = {
    aliased = first.isNil
    first
  } 
}


object Tests {
  import Row._

  val l = 1 +: 2 +: 3 +: 4 +: 5 +: nil[Int]()

  val l2 = for { i <- l
                 if i != 5 
               } yield (i*2) 
  val rowBuff = new RowBuffer[Int]()
  rowBuff.addOne(1)
  rowBuff.addOne(2)
  rowBuff.addOne(3)
  rowBuff.addOne(4)
  rowBuff.addOne(5)
  val l3 = rowBuff.toRow 


  def runTests(): Boolean = {
    assert(nil[Int]().toList == List[Int]())
    assert(l.toList == List(1,2,3,4,5))
    assert(l2.toList == List(2,4,6,8)) 
    assert(l3.toList == List(1,2,3,4,5)) 
    return true 
  } 
}



/*
@main def main() = {
  import Tests.{runTests}
  println(runTests()) 

}
*/
