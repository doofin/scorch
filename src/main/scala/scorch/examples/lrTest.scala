package scorch.examples

/**linear regression*/
object lrTest {
  import botkop.numsca.Tensor
  import scorch.autograd.Variable
  import scorch.supervised.Linear
  import scorch.optim.{Adam, SGD}
  import botkop.{numsca => ns}
  val f1: Tensor => Tensor = { t: Tensor =>
    t * 3 + 1.0
  }

  def run = {
    val fc1 = Linear(1, 1) // an affine operation: y = Wx + b

    val optimizer = SGD(fc1.parameters, lr = 0.001)
    //todo bug!  overfit to inf
    (1 to 1000) foreach { i =>
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
}
