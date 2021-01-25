package botkop.numsca

case class TensorSelection(
    t: Tensor,
    indexes: Array[Array[Int]],
    shape: Option[Array[Int]]
) {

  def asTensor: Tensor = {
    val newData = indexes.map(ix => t.array.getDouble(ix: _*))
    if (shape.isDefined)
      Tensor(newData).reshape(shape.get)
    else
      Tensor(newData)
  }

  /**
    * Set all entries of the ndarray to the specified value
    */
  def :=(d: Double): Unit = indexes.foreach(t(_) := d)

  def +=(d: Double): Unit = indexes.foreach(t(_) += d)

  def -=(d: Double): Unit = indexes.foreach(t(_) -= d)

  def *=(d: Double): Unit = indexes.foreach(t(_) *= d)

  def /=(d: Double): Unit = indexes.foreach(t(_) /= d)

  def %=(d: Double): Unit = indexes.foreach(t(_) %= d)

  def **=(d: Double): Unit = indexes.foreach(t(_) **= d)

}
