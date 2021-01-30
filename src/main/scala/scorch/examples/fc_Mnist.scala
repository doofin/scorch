package scorch.examples

import botkop.{numsca => ns}
import scorch.autograd.Variable
import scorch.dataloader.MnistDataLoader_img
import scorch.nn.{Linear, Module}
import scorch.optim.{Adam, SGD}
import scorch._

/** performance contrast:
  * https://github.com/snehalvartak/MNIST
  * Here I have trained a fully connected net for MNIST Classification using Tensorflow.
  * This fully connected network has 5 hidden layers with 1024 hidden units each.
  * This initialization coupled with batch normalization and Adam Optimizer gives an accuracy of 98.3% on test data.*/
object fc_Mnist {
  case class fcNet() extends Module {
    private val inFeat: Int = 28 * 28
    private val outFeat = 10
    private val hid = 900
    val fc1 = Linear(inFeat, 1000)
    val fc2 = Linear(1000, hid)
    val fc3 = Linear(hid, hid)
    val fc4 = Linear(hid, 50)
    val fc5 = Linear(50, outFeat)

    override def forward(x: Variable): Variable =
      x ~> fc1 ~> relu ~> fc2 ~> relu ~> fc3 ~> relu ~> fc4 ~> relu ~> fc5 ~> relu
  }

  def run = {

    val batchSize = 10 //1024
    val net = fcNet()
    val trainSet = new MnistDataLoader_img("train", batchSize)
    val validSet = new MnistDataLoader_img("validate", batchSize)
    val optimizer = Adam(net.parameters, 0.001)

    for (epoch <- 1 to 100) {

      var avgLoss = 0.0
      var avgAccuracy = 0.0
      var count = 0

      trainSet.foreach {
        case (x, y) =>
          count += 1
          net.zeroGrad()
          val output = net(x)
          val loss = softmaxLoss(output, y)
          loss.backward()
          optimizer.step()

          println(s"training: epoch : $epoch: loss: ${loss.data}")
          avgLoss += loss.data.squeeze()
          val accuracy = getAccuracy(output, y)
          avgAccuracy += accuracy
          if (epoch % 10 == 0) evaluate(net, epoch)
      }
    }

    def evaluate(model: Module, epoch: Int): Unit = {
      model.eval()

      var avgLoss = 0.0
      var avgAccuracy = 0.0
      var count = 0

      validSet.take(20).foreach {
        case (x, y) =>
          count += 1
          val output = net(x)
          val guessed = ns.argmax(output.data, axis = 1)
          val accuracy = ns.sum(guessed == y.data) / batchSize
          avgAccuracy += accuracy
          val loss = softmaxLoss(output, y)
          avgLoss += loss.data.squeeze()
      }
      println(
        s"testing:  $epoch: loss: ${avgLoss / count} accuracy: ${avgAccuracy / count}"
      )

      model.train()
    }

    def getAccuracy(yHat: Variable, y: Variable): Double = {
      val guessed = ns.argmax(yHat.data, axis = 1)
      ns.mean(y.data == guessed).squeeze()
    }
  }
}
