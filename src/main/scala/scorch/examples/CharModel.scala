package scorch.examples

import botkop.{numsca => ns}
import com.typesafe.scalalogging.LazyLogging
import scorch.autograd.Variable
import scorch.crossEntropyLoss
import scorch.nn.rnnLanguageModel
import scorch.nn.rnn.RnnBase
import scorch.optim.{Adam, Nesterov, Optimizer, SGD}

import scala.io.Source
import scala.util.Random

object CharModel {
  def run = {
    def tokenize(s: String): Array[Char] = s.toCharArray

    def join(s: Seq[Char]) = s.mkString

    val fileName = "src/test/resources/dinos.txt"
    val corpus = Random.shuffle(
      Source
        .fromFile(fileName)
        .getLines()
        .toList
    )

    val model = new rnnLanguageModel(
      corpus = corpus,
      tokenize = tokenize,
      join = join,
      eosSymbol = '\n'
    )
    model.run()
  }
}


