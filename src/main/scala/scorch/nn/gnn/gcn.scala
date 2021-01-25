package scorch.nn.gnn

import botkop.numsca.Tensor
import org.nd4j.linalg.factory.Nd4j
import scorch.autograd.Variable
import scorch.data.Graphs.Graph
import scorch.nn.{BatchNorm, Dropout, Linear, Module}

object gcn {
  case class gcn1(inFeatures: Int) {

    val fc1 = Linear(inFeatures, 100)
    val fc2 = Linear(100, 10)

    val bn = BatchNorm(100)
    val drop = Dropout()

    def forward(g: Graph[Seq[Float], Unit]) = {

      val newNd = g.nds map { n =>
        val neibs = g.getNeighbours(n.nId)
//        aggr neibs
//        scorch.mean(_)
        val nDArray = Nd4j.create(neibs.map(_.data.toArray).toArray)
        val aggr = scorch.mean(Variable(new Tensor(nDArray)))
        val r = fc1(aggr)
//        loss(r,n.data)
        val loss = scorch.softmaxLoss(r, Variable(Tensor(n.data.toArray)))
//        update node attr
        loss.backward()

        n.copy()

      }
      g.copy(nds = newNd)
    }
  }
}
