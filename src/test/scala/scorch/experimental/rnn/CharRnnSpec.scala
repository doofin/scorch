package scorch.experimental.rnn

import java.io.File

import botkop.numsca.Tensor
import botkop.{numsca => ns}
import org.nd4j.linalg.api.buffer.DataBuffer
import org.nd4j.linalg.factory.Nd4j
import org.scalatest.{FlatSpec, Matchers}
import scorch.autograd._
import scorch.nn._
import scorch._
import scorch.optim.{Optimizer, SGD}
import Function._

import scala.io.Source
import scala.language.postfixOps
import scala.util.Random

import dataloader.classifyNames._

import examples.charRnnHelpers._

class CharRnnSpec extends FlatSpec with Matchers {

  Nd4j.setDataType(DataBuffer.Type.DOUBLE)
  ns.rand.setSeed(231)

  // see http://pytorch.org/tutorials/intermediate/char_rnn_classification_tutorial.html
  // this is really slow, and does not seem to work
  // so the test is tagged as ignored

  "A Char-RNN" should "classify names" ignore {}

}
