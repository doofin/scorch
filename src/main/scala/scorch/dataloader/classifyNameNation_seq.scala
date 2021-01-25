package scorch.dataloader

import java.io.File
import botkop.numsca.Tensor
import botkop.{numsca => ns}
import org.nd4j.linalg.api.buffer.DataBuffer
import org.nd4j.linalg.factory.Nd4j
import scorch.autograd._
import scorch.nn._
import scorch._
import scorch.optim.{Optimizer, SGD}
import Function._

import scala.io.Source
import scala.language.postfixOps
import scala.util.Random

import botkop.numsca.Tensor

import scala.io.Source

object classifyNameNation_seq {
  val namesDir = "src/test/resources/names"
  def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  def readNames(): Map[String, List[String]] = {

    val files = getListOfFiles(namesDir)

    files map { f =>
      val lang = f.getName.replaceFirst("\\.txt$", "")
      val names = Source.fromFile(f).getLines().toList
      lang -> names
    } toMap
  }
  def letterToTensor(letter: Char, letters: Map[Char, Int]): Tensor = {
    val t = ns.zeros(1, letters.size)
    t(0, letters(letter)) := 1
    t
  }

  def lineToTensor(line: String, letters: Map[Char, Int]): Tensor = {
    val t = ns.zeros(line.length, 1, letters.size)
    line.toCharArray.zipWithIndex.foreach {
      case (c, i) =>
        t(i, 0, letters(c)) := 1
    }
    t
  }
  def randomTrainingPair(
      categories: List[String],
      names: Map[String, List[String]],
      letters: Map[Char, Int]
  ): (String, String, Tensor, Tensor) = {

    val categoryIndex = Random.nextInt(categories.size)
    val category = categories(categoryIndex)

    val lineIndex = Random.nextInt(names(category).length)
    val line = names(category)(lineIndex)

    val categoryTensor = Tensor(categoryIndex)
    val lineTensor = lineToTensor(line, letters)
    (category, line, categoryTensor, lineTensor)
  }

}
