package scorch.nn

import botkop.{numsca => ns}
import com.typesafe.scalalogging.LazyLogging
import scorch.autograd.Variable
import scorch.crossEntropyLoss
import scorch.nn.rnn.RnnBase
import scorch.optim.{Adam, Nesterov, Optimizer, SGD}

class LanguageModel[T](
    corpus: Seq[String],
    tokenize: (String) => Seq[T],
    join: Seq[T] => String,
    eosSymbol: T,
    cellType: String = "gru",
    optimizerType: String = "adam",
    learningRate: Double = 0.001,
    na: Int = 50,
    numIterations: Int = Int.MaxValue,
    maxSentenceSize: Int = 60,
    numSentences: Int = 8,
    printEvery: Int = 100,
)(implicit ordering: Ordering[T])
    extends LazyLogging {

  val tokens: Seq[T] =
    corpus.flatMap(tokenize).distinct.sorted :+ eosSymbol

  val vocabSize: Int = tokens.length
  val tokenToIdx: Map[T, Int] = tokens.zipWithIndex.toMap

  val bosIndex: Int = -1 // index for beginning of sentence
  val eosIndex: Int = tokenToIdx(eosSymbol) // index for end of sentence
  val (nx, ny) = (vocabSize, vocabSize)

  logger.info(s"corpus size: ${corpus.length}")
  logger.info(s"vocab size: $vocabSize")
  logger.info(s"vocabulary: $tokens")

  // define the RNN model
  val rnn = RnnBase(cellType, na, nx, ny)

  val optimizer: Optimizer = optimizerType match {
    case "sgd"      => SGD(rnn.parameters, learningRate)
    case "adam"     => Adam(rnn.parameters, learningRate)
    case "nesterov" => Nesterov(rnn.parameters, learningRate)
    case u          => throw new Error(s"unknown optimizer type $u")
  }

  /**
    * Execute one step of the optimization to train the model
    *
    * @param xs list of integers, where each integer is a number that maps to a character in the vocabulary
    * @param ys list of integers, exactly the same as xs but shifted one index to the left
    * @return the value of the loss function (cross-entropy)
    */
  def optimize(xs: Seq[Int], ys: Seq[Int]): Double = {
    optimizer.zeroGrad()
    val yHat = rnn.forward(xs.map(encode))
    val loss = crossEntropyLoss(yHat, ys)
    loss.backward()
    optimizer.step()
    loss.data.squeeze()
  }

  def run(): Unit = {
    var totalLoss = 0.0
    for (j <- 1 to numIterations) {
      val index = j % corpus.length

      val xs: Seq[Int] = bosIndex +: tokenize(corpus(index)).map(tokenToIdx)
      val ys: Seq[Int] = xs.tail :+ eosIndex

      val loss = optimize(xs, ys)
      totalLoss += loss

      if (j % printEvery == 0) {
        println(s"Iteration: $j, Loss: ${totalLoss / printEvery}")
        for (_ <- 1 to numSentences) {
          val sampledIndices =
            rnn.sample(encode, bosIndex, eosIndex, maxSentenceSize)
          val sentence = join(sampledIndices.init.map(tokens))
          println(sentence)
        }

        println()
        totalLoss = 0.0
      }
    }
  }

  /**
    * one hot encoding within vocabSize
    * @param x index to encode
    * @return one hot encoded index
    */
  def encode(x: Int): Variable = {
    val xt = Variable(ns.zeros(vocabSize, 1))
    if (x != bosIndex)
      xt.data(x, 0) := 1
    xt
  }

}




