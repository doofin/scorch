package scorch.nn.gnn

import botkop.numsca.Tensor
import org.nd4j.linalg.factory.Nd4j
import scorch.autograd.Variable
import scorch.data.Graphs.Graph
import scorch.nn.{BatchNorm, Dropout, Linear, Module}
import scorch.relu
import scorch.dataloader._

import scala.language.postfixOps
//GIN How Powerful are Graph Neural Networks2019
object GIN {
  def r = {
    var nth = 0
    val gn1 = GIN()
    val gn2 = GIN()
    val trainset = PROTEINS_graph.file2data()
    println("trainset len ", trainset.length)
    1 to 100 foreach { ep =>
      trainset.foreach { g =>
        nth += 1
        gn1.forward(g, nth)
      }
    }
  }

  case class GIN(val inFeatures: Int = 29, val outFeatures: Int = 2) {
    // = gr node feat

    val fc1 = Linear(inFeatures, 100)
    val fc2 = Linear(100, outFeatures) // for cross entropy

//    val bn = BatchNorm(100)
//    val drop = Dropout()

    def forward(g: PROTEINS_graph.grType, nth: Int = 1) = {

      //for each node,gather msg from neibs
      val newNd = g.nds map { n =>
        /** do aggregate from neibs*/
        val neibs = g.getNeighbours(n.nId)
        val neibsAttr: Array[Array[Float]] =
          neibs.map(_.data._1.toArray).toArray
        val neibSum = neibsAttr.foldLeft(1 to inFeatures map (_ => 0f) toArray)(
          (
              a,
              b
          ) => a.zip(b).map(x => x._1 + x._2)
        )
//        val nDArray = Nd4j.create(neibsAttr)
//        val aggr = scorch.mean(Variable(new Tensor(nDArray)), 0)
        val aggr = Variable(new Tensor(Nd4j.create(neibSum)))
//        val aggr2 = scorch.mean(Variable(new Tensor(nDArray)),1)
//        val ndAttr = Variable(aggr)

        /**pass through nn,output 1D*/
        val r = aggr ~> fc1 ~> relu ~> fc2 // ~> relu

//        println("ndAttr shape", aggr.shape, "r spe:", r.shape)

        /** return a scalar,r spe:,List(1, 2)*/
        r
      }

      /**global readout :
        * GIN replaces READOUT in Eq. 4.2 with summing all node features
        * from the same iterations (we do not need an extra MLP before summation for the same reason as
        * in Eq. 4.1), it provably generalizes the WL test and the WL subtree kernel.*/
//      val glob1 = newNd.reduce(_ + _)
      val zerTs = Variable(Tensor(Array(0f, 0f)))
//      println(zerTs.shape, newNd.head.shape)
      val glob1 = newNd.foldLeft(zerTs)(_ + _)
      val grLabelPred = glob1.transpose()
      val grLabel = g.grProp - 1
      //      println("glob spe ", glob.shape, glob.data, "newnd ", newNd.head.data)
//      val loss = scorch.softmaxLoss(glob, Variable(Tensor(g.grProp)))
      val loss = scorch.crossEntropyLoss(Seq(grLabelPred), Seq(grLabel))
      println(s"${nth} loss: ", loss.data) //grLabelPred.shape : 2,1
      loss.backward()

      if (nth % 10 == 0) {
        val pred = Nd4j.argMax(grLabelPred.data.array).getFloat(0).toInt
        println("pred,real:", pred, grLabel)

      }
//      g.copy(nds = newNd)
      g
    }
  }
}
