package scorch

import botkop.numsca.Tensor
import botkop.{numsca => ns}
import scorch._
import scorch.autograd.Variable
import scorch.nn.cnn._
import scorch.nn._
import scorch.optim.{Adam, SGD}

object scorTest {
  val f1: Tensor => Tensor = { t: Tensor =>
    t * 3 + 1.0
  }
  def lr2 = {
    val fc1 = Linear(1, 1) // an affine operation: y = Wx + b

    val optimizer = SGD(fc1.parameters, lr = 0.01)

    (1 to 10) foreach { i =>
      val x = ns.array(i)
      val vx = Variable(x)
      val y = Variable(f1(x))

      val ypred = fc1(vx)

      val loss = scorch.mean((ypred - y) ** 2)

      println(s"loss: ${loss.data}, x: $x, y: ${y.data}, ypred: ${ypred.data}")

      optimizer.zeroGrad()
      loss.backward()
      optimizer.step()
    }
  }
  def lrTest = {
    val fc1 = Linear(1, 1) // an affine operation: y = Wx + b

//    val optimizer = Adam(Seq(fc1) flatMap (_.parameters), lr = 0.01)
    val optimizer = Adam(Seq(fc1) flatMap (_.parameters), lr = 0.1)

    (1 to 100) foreach { i =>
      (1 to 10) foreach { _ =>
        val ypred = fc1.forward(Variable(ns.array(i)))
        val y_ = f1(ns.array(i))
        val y = Variable(y_)
        val loss = scorch.mean((ypred - y) ** 2)
        println(s"loss  : ${loss.data}")
        println(s" y : $y_ ,, y pred : ${ypred.data}")
        optimizer.zeroGrad()
        loss.backward()
        optimizer.step()
      }
    }

  }
}
