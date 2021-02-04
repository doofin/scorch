package scorch.supervised

import botkop.{numsca => ns}
import botkop.numsca.Tensor
import scorch.autograd.Variable

case class Linear(weights: Variable, bias: Variable)
    extends Module(Seq(weights, bias)) {

  override def forward(x: Variable): Variable = {
    (x dot weights) + bias //weights.transpose()
  }
}

object Linear {
  def apply(inFeatures: Int, outFeatures: Int): Linear = {
    println("new Linear: ", inFeatures, outFeatures)
    val weights = Variable(ns.randn(inFeatures, outFeatures)) //* math.sqrt(2.0 / outFeatures)
    val bias = Variable(ns.randn(1, outFeatures))
    Linear(weights, bias)
  }
}
