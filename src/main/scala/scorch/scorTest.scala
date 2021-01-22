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

    (1 to 1000) foreach { i =>
      (1 to 1) foreach { _ =>
        val xx = ns.array(i)
        val ypred = fc1.forward(Variable(xx))
        val y = Variable(f1(xx))
//        val loss = Variable(ns.mean(ns.square(ypred.data - y)))
        val loss = scorch.mean((ypred - y) ** 2)
        println(s"los  : ${loss.data}")
        println(s" y : ${f1(xx)} ,, y pred : ${ypred.data}")
        optimizer.zeroGrad()
        loss.backward()
        optimizer.step()
      }
    }

  }
}
/*
  def lr() = {
    val w             = newvar(rand(1))
    val b             = newvar(rand(1))
    val tensorr       = matmul(rand(2), rand(2, 2))
    val f: Int => Int = _ * 3 + 1

    val f2: tensor => tensor = t => matmul(t, tensor2(Seq(Seq(1d, 2d), Seq(1d, 2d)))) + tensor(Seq(2d, 2))

    val ln        = linear(2, 2)
    val ln2       = linear(2, 2)
    val optimizer = dyn.optim.Adam(ln._2 ++ ln2._2, 0.01) //lr = 0.0001
    (1 to 10000) foreach { x => //samples
      val x = rand(2)
      //        p(f2(x).shape)
      (1 to 100) foreach { _ => //epochs
        val yLabel = f2(x)
        val y_pred = ln2._1(ln._1(x))
        //          y_pred.requires_grad(false)
        //          val loss   = ((y - y_pred) ^ 2) mean ()
        p(s"shape : ${y_pred.shape} ,, ${yLabel.shape}")
        //          val ypredview = y_pred.dyn.view(4).as[tensor]
        //          val yview     = yLabel.dyn.view(4).as[tensor]
        //          p(s"shape : ${ypredview.shape} ,, ${yview.shape}")
        //          val input = rand(3, 5)
        //          val target = torch.randint(5, (3,), dtype=torch.int64)
        //          val loss2 = BCELoss(tensor(Seq(0d,1)), tensor(Seq(0.1,1)))
        //          println(s"loss 2 : $loss2")

        val loss = MSELoss(y_pred, yLabel)
        println("loss " + loss)
        println(s"x : $x ,,, y $yLabel ,,, ypred:$y_pred")

        optimizer.zero_grad()
        loss.backward() // reverse mode,calc all diff!
        optimizer.step()
        val gccc = gbc.collect()
        //            val pp=pympler1.dyn.muppy.get_objects()
        //            ppc(interpeter.eval(s"len(${pp.expr})"))
        ppc(jepInterpeter.getValue("len(muppy.get_objects())"))
        if (gccc > 0) ppc(s"gc : ${gccc}")
      }

      (200 to 250) foreach { _ =>
        val x      = rand(2)
        val yLabel = f2(x)
        val y_pred = ln2._1(ln._1(x))
        val loss   = MSELoss(y_pred, yLabel)
        println("test loss " + loss)
        println(s"test x : $x ,,, y $yLabel ,,, ypred:$y_pred")

      }
    }

  }
  override def run(): Unit = {
    lrTest
//    lr2
  }
  def run1(): Unit = {
    val numSamples = 128
    val numClasses = 10
    val nf1        = 40
    val nf2        = 20

    // Define a simple neural net
    case class Net() extends Module {
      val fc1                           = Linear(nf1, nf2) // an affine operation: y = Wx + b
      val fc2                           = Linear(nf2, numClasses) // another one
      override def forward(x: Variable) = x ~> fc1 ~> relu ~> fc2
    }

    val net              = Net()
    val optimizer        = SGD(net.parameters, lr = 0.01)
    val target: Variable = Variable(ns.randint(numClasses, Array(numSamples, 1)))
    val input            = Variable(ns.randn(numSamples, nf1))
    val f = { t: Tensor =>
      t + ns.array(1d)
    }

    for (j <- 0 to 10000000) {
      optimizer.zeroGrad()
      val output = net.forward(input)
      println(output.data.toString())
      val loss = softmaxLoss(output, target)
      if (j % 100 == 0) {
        val guessed  = ns.argmax(output.data, axis = 1)
        val accuracy = ns.sum(target.data == guessed) / numSamples
        println(s"$j: loss: ${loss.data.squeeze()} accuracy: $accuracy")
      }
      loss.backward()
      optimizer.step()
    }
  }
}
 */
