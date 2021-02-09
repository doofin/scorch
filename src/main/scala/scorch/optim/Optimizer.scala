package scorch.optim

import scorch.autograd.Variable

abstract class Optimizer(parameters: Seq[Variable]) {
  def step(): Unit

  /**set zeroGrad for model parameters*/
  def zeroGrad(): Unit =
    parameters.map(_.grad).foreach(g => g.data := 0)
}

