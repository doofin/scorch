package scorch.examples

import scorch.nn.rnnLanguageModel

import scala.io.Source
import scala.util.Random

object rnn_wordModel {
  def run = {
    def tokenize(s: String): Array[String] =
      s.toLowerCase
        .replaceAll("[\\.',;:\\-!\\?\\(]+", " ")
        .split("\\s+")
        .filterNot(_.isEmpty)

    def join(s: Seq[String]) = s.mkString(" ")

    //  data : a poem
    val fileName = "src/test/resources/sonnets-cleaned.txt"
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
      learningRate = 0.003,
      printEvery = 10,
      eosSymbol = "<EOS>"
    )
    model.run()

  }
}
