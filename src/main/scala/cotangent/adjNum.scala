package cotangent

import org.platanios.tensorflow.api.tensors._
//http://platanios.org/tensorflow_scala/guides/tensors.html
object adjNum {
  def run = {
    val a = new adjNum(null)
    val b = new adjNum(null)
    val r = a + b
    r.pull()
  }

}
case class adjNum(
    nDArray: Tensor[Float],
    gradF: Option[adjNum => adjNum] = None
) {
  def push(): Unit = {}

  /**backward*/
  def pull(): Unit = {}

// d (a+b)=da + db
  def +(b: adjNum) = {

    b.gradF
    val f = for {
      a <- gradF
      b <- b.gradF
    } yield {}
    this
  }
}
