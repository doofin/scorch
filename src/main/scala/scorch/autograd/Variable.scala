package scorch.autograd

import scala.language.implicitConversions

import botkop.numsca.Tensor
import botkop.{numsca => ns}
import com.typesafe.scalalogging.LazyLogging
import scorch.supervised.Module
import scorch.supervised.cnn.MaxPool2d

import Function._

object Variable {
  def apply(d: Double): Variable = Variable(Tensor(d))
  def apply(d: Double, name: Option[String]): Variable =
    Variable(Tensor(d), name = name)

  implicit def moduleApply[T <: Module](m: T): (Variable) => Variable =
    m.forward
}

case class Variable(
    data: Tensor,
    gradFn: Option[Function] = None,
    name: Option[String] = None
) {

  /**mutable */
  lazy val grad: Variable =
    Variable(ns.zerosLike(data), name = name.map(n => s"g_$n"))

  /**default val is all ones
    * gradOutput is du/dz,set to 1 for reverse calc
    * */
  def backward(gradOutput: Variable = Variable(ns.ones(data.shape))): Unit = {

//    in place (element wise) addition of two NDArrays
//    aggre grad for all component , chain rule sums all components of a  vector
//    https://en.wikipedia.org/wiki/Chain_rule#Multivariable_case
    grad.data += gradOutput.data
//    will have gradFn after Function.forward
    for (gf <- gradFn) gf.backward(gradOutput)
  }

  def shape: List[Int] = data.shape.toList
  def detach(name: Option[String] = None) = Variable(data, name = name)

  def +(other: Variable): Variable = Add(this, other).forward()
  def -(other: Variable): Variable = Sub(this, other).forward()
  def *(other: Variable): Variable = Mul(this, other).forward()
  def /(other: Variable): Variable = Div(this, other).forward()

  def dot(other: Variable): Variable = Dot(this, other).forward()

  def unary_- : Variable = Negate(this).forward()
  def +(d: Double): Variable = AddConstant(this, d).forward()
  def -(d: Double): Variable = SubConstant(this, d).forward()
  def *(d: Double): Variable = MulConstant(this, d).forward()
  def /(d: Double): Variable = DivConstant(this, d).forward()
  def **(d: Double): Variable = PowConstant(this, d).forward()

  def transpose(): Variable = Transpose(this).forward()
  def reshape(shape: List[Int]): Variable = Reshape(this, shape).forward()
  def reshape(shape: Int*): Variable = reshape(shape.toList)

  def exp(): Variable = Exp(this).forward()
  def mean(): Variable = Mean(this).forward()
  def mean(axis: Int): Variable = MeanByAxis(this, axis).forward()
  def sigmoid(): Variable = Sigmoid(this).forward()
  def softmax(): Variable = Softmax(this).forward()
  def tanh(): Variable = Tanh(this).forward()
  def relu(): Variable = Threshold(this, 0).forward()
  def variance(): Variable = Variance(this).forward()
  def variance(axis: Int): Variable = VarianceByAxis(this, axis).forward()
  def sqrt(): Variable = Sqrt(this).forward()
  def abs(): Variable = Abs(this).forward()

  def cat(w: Variable, axis: Int = 0): Variable =
    Concat(this, w, axis).forward()

  def maxPool2d(poolHeight: Int, poolWidth: Int, stride: Int): Variable =
    MaxPool2d
      .NaiveMaxPool2dFunction(this, poolHeight, poolWidth, stride)
      .forward()

  // chain operator
  def ~>(f: (Variable) => Variable): Variable = f(this)

  override def toString: String =
    if (name.isDefined) s"name: ${name.get}, data: $data" else s"data: $data"

}
