package scorch.examples

import botkop.numsca.Tensor
import botkop.{numsca => ns}
import scorch.autograd.Variable
import scorch.dataloader.MnistDataLoader_img
import scorch.supervised.{Linear, Module}
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
    /*
    val fc1 = Linear(inFeat, 1000)
    val fc2 = Linear(1000, hid)
    val fc3 = Linear(hid, hid)
    val fc4 = Linear(hid, 50)
    val fc5 = Linear(50, outFeat)

     */
    val fc1 = Linear(inFeat, 500)
    val fc2 = Linear(500, outFeat)

    override def forward(x: Variable): Variable = {
//      x ~> fc1 ~> relu ~> fc2 ~> relu ~> fc3 ~> relu ~> fc4 ~> relu ~> fc5 ~> relu
      x ~> fc1 ~> relu ~> fc2 ~> relu
    }

  }

  val dl4jMnist = org.deeplearning4j.datasets.DataSets.mnist()

  def run = {
    val batchSize = 10 //1024

    val net = fcNet()
    val trainSet = new MnistDataLoader_img("train", batchSize)
    val validSet = new MnistDataLoader_img("validate", batchSize)
    val optimizer = SGD(net.parameters, 0.001)

    /**The MNIST database of handwritten digits
      * training set of 60,000 examples,
      * and a test set of 10,000 examples. It is a subset of a larger set available from NIST.
      * The digits have been size-normalized and centered in a fixed-size image. */
//    (List(1, 784),List(1, 10)))
    val d4jset = 1 to 10000 map { i =>
      val fe = dl4jMnist.getFeatures.getRow(i)
      val lab = dl4jMnist.getLabels.getRow(i)
      (fe, lab)
      (Variable(new Tensor(fe)), Variable(new Tensor(lab.transpose())))
    }
//    println("dl4jMnist", d4jset.head)
//    val (epch, tset) = (1, trainSet.take(1))
    val (epch, tset) = (100, trainSet)
//    val (epch, tset) = (100, d4jset)
//    val (epch, tset) = (1, d4jset.take(1))
    for (epoch <- 1 to epch) {

      var avgLoss = 0.0
      var avgAccuracy = 0.0
      var count = 0
      var avgAccuracyEva = 0.0
      tset.zipWithIndex.foreach {
        case ((x, y), idx) =>
          count += 1

          val y_pred = net(x)
//          println(y.toString)
//          println("x y shape:", x.shape, y.shape) // todo bug! (x y shape:,List(10, 784),List(10, 1))
          val loss = softmaxLoss(y_pred, y)
          optimizer.zeroGrad()
          loss.backward()
          optimizer.step()

          println(
            s"training: idx ${idx} epoch : $epoch: loss: ${loss.data} ,accu: ${avgAccuracyEva}"
          )
          avgLoss += loss.data.squeeze()
          val accuracy = getAccuracy(y_pred, y)
          avgAccuracy += accuracy
          if (idx % 1000 == 0) avgAccuracyEva = evaluate(net, epoch)
      }
    }

    def evaluate(model: Module, epoch: Int) = {
      model.eval()

      var avgLoss = 0.0
      var accu = 0.0
      var count = 0

      validSet.take(20).foreach {
        case (x, y) =>
          count += 1
          val output = net(x)
          val guessed = ns.argmax(output.data, axis = 1)
          val accuracy = ns.sum(guessed == y.data) / batchSize
          accu += accuracy
          val loss = softmaxLoss(output, y)
          avgLoss += loss.data.squeeze()
      }
      val avgAccuracy = accu / count
      println(
        s"testing:  $epoch: loss: ${avgLoss / count} accuracy: $avgAccuracy"
      )

      model.train()
      avgAccuracy
    }

    def getAccuracy(yHat: Variable, y: Variable): Double = {
      val guessed = ns.argmax(yHat.data, axis = 1)
      ns.mean(y.data == guessed).squeeze()
    }
  }
}
