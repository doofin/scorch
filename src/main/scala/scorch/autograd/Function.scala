package scorch.autograd

import botkop.numsca.Tensor
import botkop.{numsca => ns}
import com.typesafe.scalalogging.LazyLogging
import ns._

import scala.language.postfixOps

trait Function extends LazyLogging {
  def forward(): Variable
  def backward(gradOutput: Variable): Unit

  def unbroadcast(v: Variable, oldShape: List[Int]): Variable = {
    unbroadcast(v.data, oldShape)
  }

  def unbroadcast(data: Tensor, oldShape: List[Int]): Variable = {
    val t = oldShape.zip(data.shape).zipWithIndex.foldLeft(data) {
      case (d: Tensor, ((oi, ni), i)) =>
        if (oi == ni)
          d
        else if (oi == 1)
          ns.sum(d, axis = i)
        else
          throw new Exception(
            s"unable to unbroadcast shape ${data.shape.toList} to $oldShape")
    }
    Variable(t)
  }
}

case class Add(v1: Variable, v2: Variable) extends Function {
  def forward(): Variable = Variable(v1.data + v2.data, gradFn = Some(this))
  def backward(gradOutput: Variable): Unit = {
    v1.backward(unbroadcast(gradOutput, v1.shape))
    v2.backward(unbroadcast(gradOutput, v2.shape))
  }
}

case class AddConstant(v: Variable, d: Double) extends Function {
  override def forward(): Variable = Variable(v.data + d, Some(this))
  override def backward(gradOutput: Variable): Unit = {
    v.backward(gradOutput)
  }
}

case class Sub(v1: Variable, v2: Variable) extends Function {
  def forward(): Variable = Variable(v1.data - v2.data, gradFn = Some(this))
  def backward(gradOutput: Variable): Unit = {
    v1.backward(unbroadcast(gradOutput, v1.shape))
    v2.backward(unbroadcast(-gradOutput.data, v2.shape))
  }
}

case class SubConstant(v: Variable, d: Double) extends Function {
  override def forward(): Variable = Variable(v.data - d, Some(this))
  override def backward(gradOutput: Variable): Unit = {
    v.backward(gradOutput)
  }
}

case class Mul(v1: Variable, v2: Variable) extends Function {
  override def forward(): Variable = Variable(v1.data * v2.data, Some(this))
  override def backward(gradOutput: Variable): Unit = {
    val dv1 = v2.data * gradOutput.data
    val vdv1 = unbroadcast(dv1, v1.shape)
    val dv2 = v1.data * gradOutput.data
    val vdv2 = unbroadcast(dv2, v2.shape)
    v1.backward(vdv1)
    v2.backward(vdv2)
  }
}

case class MulConstant(v: Variable, d: Double) extends Function {
  override def forward(): Variable = Variable(v.data * d, Some(this))
  override def backward(gradOutput: Variable): Unit = {
    val dv = gradOutput.data * d
    v.backward(Variable(dv))
  }
}

case class Div(v1: Variable, v2: Variable) extends Function {
  override def forward(): Variable = Variable(v1.data / v2.data, Some(this))
  override def backward(gradOutput: Variable): Unit = {
    val rv2 = 1 / v2.data
    val gv1 = gradOutput.data * rv2
    val gv2 = -gradOutput.data * v1.data * (rv2 ** 2)

    val vgv1 = unbroadcast(gv1, v1.shape)
    val vgv2 = unbroadcast(gv2, v2.shape)
    v1.backward(vgv1)
    v2.backward(vgv2)
  }
}

case class DivConstant(v: Variable, d: Double) extends Function {
  override def forward(): Variable = Variable(v.data / d, Some(this))
  override def backward(gradOutput: Variable): Unit = {
    val dv = gradOutput.data / d
    v.backward(Variable(dv))
  }
}

/* Pow of 2 tensors is currently not implemented in numsca
case class Pow(a: Variable, b: Variable) extends Function {


  override def forward(): Variable = {
    Variable(a.data ** b.data, Some(this))
  }

  override def backward(gradOutput: Variable): Unit = {
     val ga = gradOutput.data * b.data * (a.data ** (b.data - 1))
     val gb = gradOutput.data * (a.data ** b.data) * ns.log(a.data)

    val vga = unbroadcast(ga, a.shape)
    val vgb = unbroadcast(gb, b.shape)

    logger.debug(s"pow backward, ga.shape=${vga.shape}, gb.shape=${vgb.shape}")
    a.backward(vga)
    b.backward(vgb)
  }
}
 */

case class PowConstant(v: Variable, d: Double) extends Function {
  override def forward(): Variable = Variable(ns.power(v.data, d), Some(this))
  val cache: Tensor = d * ns.power(v.data, d - 1)
  override def backward(gradOutput: Variable): Unit = {
    val dv = cache * gradOutput.data
    v.backward(Variable(dv))
  }
}

case class Sqrt(v: Variable) extends Function {
  val cache: Tensor = ns.sqrt(v.data)
  override def forward(): Variable = Variable(cache, Some(this))
  override def backward(gradOutput: Variable): Unit = {
    val dv = (1.0 / (2 * cache)) * gradOutput.data
    v.backward(Variable(dv))
  }
}

case class Abs(v: Variable) extends Function {
  val cache: Tensor = ns.abs(v.data)
  override def forward(): Variable = Variable(cache, Some(this))
  override def backward(gradOutput: Variable): Unit = {
    val dv = (v.data / cache) * gradOutput.data
    v.backward(Variable(dv))
  }
}

case class Negate(v: Variable) extends Function {
  override def forward(): Variable = Variable(-v.data, Some(this))
  override def backward(gradOutput: Variable): Unit = {
    val dv = -gradOutput.data
    v.backward(Variable(dv))
  }
}

case class Dot(v1: Variable, v2: Variable) extends Function {
  val w: Tensor = v1.data
  val x: Tensor = v2.data

  override def forward(): Variable = Variable(w dot x, Some(this))
  override def backward(gradOutput: Variable): Unit = {
    val dd = gradOutput.data
    val dw = dd dot x.T
    val dx = w.T dot dd
    v1.backward(Variable(dw))
    v2.backward(Variable(dx))
  }
}

case class Transpose(v: Variable) extends Function {
  override def forward(): Variable = Variable(v.data.transpose, Some(this))
  override def backward(gradOutput: Variable): Unit =
    v.backward(Variable(gradOutput.data.transpose))
}

// todo test this
case class Reshape(v: Variable, shape: List[Int]) extends Function {
  val oldShape: List[Int] = v.shape
  override def forward(): Variable =
    Variable(v.data.reshape(shape: _*), Some(this))
  override def backward(gradOutput: Variable): Unit = {
    val dv = gradOutput.data.reshape(oldShape: _*)
    v.backward(Variable(dv))
  }
}

case class Concat(v1: Variable, v2: Variable, axis: Int = 0) extends Function {

  require(axis == 0 || axis == 1, "axis must be either 0 or 1")

  override def forward(): Variable = {
    Variable(ns.concatenate(Seq(v1.data, v2.data), axis), Some(this))
  }

  override def backward(gradOutput: Variable): Unit =
    if (axis == 0) {
      val d = gradOutput.data.data
      val (d1, d2) = d.splitAt(v1.shape.product)
      val dv1 = Tensor(d1).reshape(v1.shape: _*)
      val dv2 = Tensor(d2).reshape(v2.shape: _*)
      v1.backward(Variable(dv1))
      v2.backward(Variable(dv2))
    } else {
      val dv1 = gradOutput.data(:>, 0 :> v1.shape(axis))
      val dv2 = gradOutput.data(:>, v1.shape(axis) :>)
      v1.backward(Variable(dv1))
      v2.backward(Variable(dv2))
    }
}

//===============================================================

case class Exp(v: Variable) extends Function {
  val cache: Tensor = ns.exp(v.data)
  def forward() = Variable(data = cache, gradFn = Some(this))
  def backward(gradOutput: Variable): Unit = {
    v.backward(Variable(gradOutput.data * cache))
  }
}

case class Tanh(v: Variable) extends Function {
  val cache: Tensor = ns.tanh(v.data)
  override def forward(): Variable = Variable(cache, Some(this))
  override def backward(gradOutput: Variable): Unit =
    v.backward(Variable((1 - ns.square(cache)) * gradOutput.data))
}

case class Sigmoid(v: Variable) extends Function {
  lazy val sigmoid: Tensor = ns.sigmoid(v.data)
  override def forward(): Variable = Variable(sigmoid, Some(this))
  override def backward(gradOutput: Variable): Unit =
    v.backward(Variable(gradOutput.data * sigmoid * (1 - sigmoid)))
}

case class Softmax(v: Variable) extends Function {
  lazy val softmax: Tensor = ns.softmax(v.data)
  override def forward(): Variable = Variable(softmax, Some(this))
  override def backward(gradOutput: Variable): Unit = {
    // from https://stackoverflow.com/questions/33541930/how-to-implement-the-softmax-derivative-independently-from-any-loss-function
    val y = softmax
    val dy = gradOutput.data

    val dx = y * dy
    val s = ns.sum(dx, axis = dx.shape.length - 1)
    dx -= y * s
    v.backward(Variable(dx))
  }
}

case class Mean(v: Variable) extends Function {
  def forward() = Variable(data = ns.mean(v.data), gradFn = Some(this))
  def backward(gradOutput: Variable): Unit = {
    val n = v.shape.product
    v.backward(Variable(gradOutput.data / n))
  }
}

case class MeanByAxis(v: Variable, axis: Int) extends Function {
  def forward() = Variable(data = ns.mean(v.data, axis), gradFn = Some(this))
  def backward(gradOutput: Variable): Unit = {
    val n = v.shape(axis)
    v.backward(Variable(gradOutput.data / n))
  }
}

case class Variance(v: Variable) extends Function {
  override def forward(): Variable = ((v - v.mean()) ** 2).mean()
  override def backward(gradOutput: Variable): Unit =
    v.backward(gradOutput)
}

case class VarianceByAxis(v: Variable, axis: Int) extends Function {
  override def forward(): Variable = ((v - v.mean(axis)) ** 2).mean(axis)
  override def backward(gradOutput: Variable): Unit =
    v.backward(gradOutput)
}

case class Max(x: Variable, y: Variable) extends Function {
  def forward(): Variable = {
    val max: Tensor = ns.maximum(x.data, y.data)
    Variable(max, Some(this))
  }
  override def backward(gradOutput: Variable): Unit = {
    x.backward(Variable((x.data >= y.data) * gradOutput.data))
    y.backward(Variable((x.data <= y.data) * gradOutput.data))
  }
}

case class Threshold(x: Variable, d: Double) extends Function {
  override def forward(): Variable = Variable(ns.maximum(x.data, d), Some(this))
  override def backward(gradOutput: Variable): Unit = {
    x.backward(Variable(gradOutput.data * (x.data > d)))
  }
}

//============================================
// Loss functions
case class SoftmaxLoss(actual: Variable, target: Variable) extends Function {
  val x: Tensor = actual.data
  val y: Tensor = target.data.T

  val shiftedLogits: Tensor = x - ns.max(x, axis = 1)
  val z: Tensor = ns.sum(ns.exp(shiftedLogits), axis = 1)
  val logProbs: Tensor = shiftedLogits - ns.log(z)
  val n: Int = x.shape.head
  val loss: Double = -ns.sum(logProbs(ns.arange(n), y)) / n

  override def forward(): Variable = Variable(Tensor(loss), Some(this))

  override def backward(gradOutput: Variable /* not used */ ): Unit = {
    val dx = ns.exp(logProbs)
    dx(ns.arange(n), y) -= 1
    dx /= n

    actual.backward(Variable(dx))
  }
}

/**
  * Computes the cross-entropy loss
  * @param actuals sequence of yHat variables
  * @param targets sequence of Y indices (ground truth)
  */
case class CrossEntropyLoss(actuals: Seq[Variable], targets: Seq[Int])
    extends Function {

  /**
    * Computes the cross entropy loss, and wraps it in a variable.
    * The variable can be back propped into, to compute the gradients of the parameters
    * @return the cross entropy loss variable
    */
  override def forward(): Variable = {
    val seqLoss = actuals.zip(targets).foldLeft(0.0) {
      case (loss, (yht, y)) =>
        loss - ns.log(yht.data(y, 0)).squeeze()
    }
    Variable(Tensor(seqLoss), Some(this))
  }

  /**
    * Compute the loss of each generated character, and back prop from last to first
    * @param gradOutput not used
    */
  override def backward(gradOutput: Variable): Unit = {
    actuals.zip(targets).reverse.foreach {
      case (yh, y) =>
        val dy = ns.copy(yh.data)
        dy(y, 0) -= 1
        yh.backward(Variable(dy))
    }
  }
}
