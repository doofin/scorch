package scorch.examples
import java.io.File

import botkop.numsca.Tensor
import botkop.{numsca => ns}
import org.nd4j.linalg.api.buffer.DataBuffer
import org.nd4j.linalg.factory.Nd4j
import scorch.autograd._
import scorch.nn._
import scorch._
import scorch.optim.{Adam, Optimizer, SGD}
import Function._

import scala.io.Source
import scala.language.postfixOps
import scala.util.Random
import dataloader.classifyNames._

import rnn._

object charRnnHelpers {
  case class CharRnn(inputSize: Int, hiddenSize: Int, outputSize: Int) {
    val i2h = Linear(inputSize + hiddenSize, hiddenSize)
    val i2o = Linear(inputSize + hiddenSize, outputSize)

    def forward(input: Variable, hidden: Variable): (Variable, Variable) = {
      val combined =
        Concat(input, hidden, axis = 1).forward()
      // Variable(ns.concatenate(Seq(input.data, hidden.data), axis = 1))
      val newHidden = i2h(combined)
      val output = softmax(i2o(combined))
      (output, newHidden)
    }

    def initHidden = Variable(ns.zeros(1, hiddenSize))

    def apply(input: Variable, hidden: Variable): (Variable, Variable) =
      forward(input, hidden)

    def parameters(): Seq[Variable] = i2o.parameters ++ i2h.parameters
  }

  def train(
      rnn: CharRnn,
      optimizer: Optimizer,
      categoryTensor: Tensor,
      lineTensor: Tensor
  ): (Variable, Double) = {

    val h0 = rnn.initHidden
    val o0 = Variable(Tensor(0))

    optimizer.zeroGrad()

    val (output, _) = (0 until lineTensor.shape.head).foldLeft(o0, h0) {
      case ((_, h), i) =>
        val lv = Variable(lineTensor(i))
        rnn(lv, h)
    }

    val loss = SoftmaxLoss(output, Variable(categoryTensor)).forward()
//    println(loss.data)
    loss.backward()

    optimizer.step()
    (output, loss.data.squeeze())
  }

  def predict(
      rnn: CharRnn,
      line: String,
      letters: Map[Char, Int],
      categories: List[String]
  ): (Int, String) = {
    val lineTensor = lineToTensor(line, letters)
    val h0 = rnn.initHidden
    val o0 = Variable(Tensor(0))

    val (output, _) = (0 until lineTensor.shape.head).foldLeft(o0, h0) {
      case ((_, h), i) =>
        val lv = Variable(lineTensor(i))
        rnn(lv, h)
    }
    val gi = ns.argmax(output.data).squeeze.toInt
    (gi, categories(gi))
  }
  def accuracy(
      rnn: CharRnn,
      names: Map[String, List[String]],
      letters: Map[Char, Int]
  ) = {

    val categories: List[(String, String)] = names.toList.flatMap {
      case (category, lines) =>
        lines.map(line => (category, line))
    }

    val n = 1000

    val sample = Random.shuffle(categories).take(n)
    // println(sample)

    val correct = sample.foldLeft(0) {
      case (acc, (category, line)) =>
        val (gi, g) = predict(rnn, line, letters, names.keys.toList)
        if (g == category) {
          acc + 1
        } else {
          acc
        }
    }

    correct.toDouble / n
  }

  def startTrain = {
    val names: Map[String, List[String]] = readNames()
    val categories = names.keys.toList.sorted
    val nCategories = categories.size
    val letters: List[Char] =
      names.values.flatten.flatMap(_.toCharArray).toList.distinct.sorted
    val nLetters = letters.length
    val letterToIndex: Map[Char, Int] = letters.zipWithIndex.toMap

    val nHidden = 128
    val rnn = CharRnn(nLetters, nHidden, nCategories)
//    val rnn = GruCell(nLetters, nHidden, nCategories)
//    rnn2.parameters
    val optimizer = Adam(rnn.parameters, lr = 0.01)
    val numEpochs = 10000
//    var currentLoss = 0.0

    for (epoch <- 1 to numEpochs) {
      val (category, line, categoryTensor, lineTensor) =
        randomTrainingPair(categories, names, letterToIndex)
      val (output, loss) = train(rnn, optimizer, categoryTensor, lineTensor)
//      currentLoss += loss

      if (epoch % 1000 == 0) {
        val guess = categories(ns.argmax(output.data).squeeze.toInt)
        println(s"epoch: $epoch $line: category: $category guessed: $guess")
//        println("loss: " + currentLoss / printEvery)
//        currentLoss = 0
        println("accuracy: " + accuracy(rnn, names, letterToIndex))
        println
      }
    }
  }
}
