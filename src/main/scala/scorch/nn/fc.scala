package scorch.nn

import scorch._
import scorch.autograd.Variable

case class fc(in: Int = 784) extends Module {

  val fc1 = Linear(in, 100)
  val fc2 = Linear(100, 10)

  val bn = BatchNorm(100)
  val drop = Dropout()

  override def forward(x: Variable): Variable =
    x ~> fc1 ~> relu ~> drop ~> fc2 ~> relu
}
