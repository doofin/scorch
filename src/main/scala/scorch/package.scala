import scorch.autograd._
import scorch.nn.Dropout

import Function._

package object scorch {
  lazy val getHomeDir: String = System.getProperty("user.home")
  implicit class AutoGradDoubleOps(d: Double) {
    def +(v: Variable): Variable = v + d
    def -(v: Variable): Variable = -v + d
    def *(v: Variable): Variable = v * d
    def /(v: Variable): Variable = (v ** -1) * d
  }

  // single parameter functions
  def exp(v: Variable): Variable = v.exp()
  def mean(v: Variable): Variable = v.mean()
  def mean(v: Variable, axis: Int): Variable = v.mean(axis)
  def sigmoid(v: Variable): Variable = v.sigmoid()
  def softmax(v: Variable): Variable = v.softmax()
  def tanh(v: Variable): Variable = v.tanh()
  def relu(v: Variable): Variable = v.relu()
  def sqrt(v: Variable): Variable = v.sqrt()
  def abs(v: Variable): Variable = v.abs()
  def variance(v: Variable): Variable = v.variance()
  def variance(v: Variable, axis: Int): Variable = v.variance(axis)

  def maxPool(
      v: Variable,
      poolHeight: Int,
      poolWidth: Int,
      stride: Int
  ): Variable =
    v.maxPool2d(poolHeight, poolWidth, stride)
  def maxPool(v: Variable, poolSize: Int, stride: Int): Variable =
    v.maxPool2d(poolSize, poolSize, stride)

  // multi parameter functions
  def cat(v: Variable, w: Variable, axis: Int = 0): Variable =
    Concat(v, w, axis).forward()

  /** loss for classification
    * The loss is 0 when the prediction is 1
    * https://towardsdatascience.com/cross-entropy-for-classification-d98e7f974451
    * */


  /** single version of crossEntropyLoss*/
  def softmaxLoss(x: Variable, y: Variable): Variable =
    SoftmaxLoss(x, y).forward()

  /**
    * Instantiates a CrossEntropyLoss object, and applies it.
    * https://towardsdatascience.com/cross-entropy-for-classification-d98e7f974451
    * shape : (feat,1) -- nth
    * example : List(53, 1) , 13
    * Batched ! nth Var -- nth target
    * @param actuals source for the loss function
    * @param targets targets (classes) to compute the loss against
    * @return the loss variable, which can be backpropped into
    */
  def crossEntropyLoss(actuals: Seq[Variable], targets: Seq[Int]): Variable =

    CrossEntropyLoss(actuals, targets).forward()

}
