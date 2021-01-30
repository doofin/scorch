package scorch.nn.rnn

import botkop.{numsca => ns}
import scorch.autograd._
import scorch.nn.Module

object TemporalAffine {
  case class TemporalAffine(w: Variable, b: Variable)
      extends Module(Seq(w, b)) {
    override def forward(x: Variable): Variable =
      TemporalAffineFunction(x, w, b).forward()
  }
  case class TemporalAffineFunction(x: Variable, w: Variable, b: Variable)
      extends Function {

    val List(n, t, d) = x.shape
    val m: Int = w.shape.last

    override def forward(): Variable = {
      val out = x.data.reshape(n * t, d).dot(w.data).reshape(n, t, m) + b.data.transpose
      Variable(out, Some(this))
    }

    override def backward(gradOutput: Variable): Unit = {
      // dOut = n * t * m

      val dOut = gradOutput.data
      val dx = dOut.reshape(n * t, m).dot(w.data.transpose).reshape(n, t, d)
      val dw = dOut.reshape(n * t, m).transpose.dot(x.data.reshape(n * t, d)).transpose
      val db = ns.sum(ns.sum(dOut, axis = 1), axis = 0).transpose // todo: fix numsca

      x.backward(Variable(dx))
      w.backward(Variable(dw))
      b.backward(Variable(db))
    }
  }
}
