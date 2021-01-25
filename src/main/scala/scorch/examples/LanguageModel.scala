package scorch.examples

import botkop.{numsca => ns}
import com.typesafe.scalalogging.LazyLogging
import scorch.autograd.Variable
import scorch.crossEntropyLoss
import scorch.nn.LanguageModel
import scorch.nn.rnn.RnnBase
import scorch.optim.{Adam, Nesterov, Optimizer, SGD}

import scala.io.Source
import scala.util.Random

object CharModel  {
  def tokenize(s: String): Array[Char] = s.toCharArray
  def join(s: Seq[Char]) = s.mkString
  val fileName = "src/test/resources/dinos.txt"
  val corpus = Random.shuffle(
    Source
      .fromFile(fileName)
      .getLines()
      .toList
  )

  val model = new LanguageModel(
    corpus = corpus,
    tokenize = tokenize,
    join = join,
    eosSymbol = '\n'
  )
  model.run()
}

object WordModel  {
  def tokenize(s: String): Array[String] =
    s.toLowerCase
      .replaceAll("[\\.',;:\\-!\\?\\(]+", " ")
      .split("\\s+")
      .filterNot(_.isEmpty)

  def join(s: Seq[String]) = s.mkString(" ")
  val fileName = "src/test/resources/sonnets-cleaned.txt"
  val corpus = Random.shuffle(
    Source
      .fromFile(fileName)
      .getLines()
      .toList
  )

  val model = new LanguageModel(
    corpus = corpus,
    tokenize = tokenize,
    join = join,
    learningRate = 0.003,
    cellType = "lstm",
    printEvery = 1000,
    eosSymbol = "<EOS>"
  )
  model.run()

}
