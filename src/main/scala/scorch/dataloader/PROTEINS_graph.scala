package scorch.dataloader

import scorch.data.Graphs._

import java.io.File
import com.doofin.stdScala._
import scorch.getHomeDir

/** PROTEINS-full contains the following comma separated text files:
  *
  *n: total number of nodes,m: total number of edges,N: number of graphs
  *
  *.node_attrs (n lines) : the comma seperated values in the i-th line is the attribute vector of the node with node_id i
  *.node_labels (n lines) : the value in the i-th line corresponds to the node with node_id i
  *.edges (m lines) : sparse ,(block diagonal) ,adjacency matrix for all graphs,each line (row, col)  corresponds to  (node_id, node_id)
  *.graph_labels (N lines) : the value in the i-th line is the class label of the graph with graph_id i
  *.graph_idx (n lines) : the value in the i-th line is the graph_id of the node with node_id i(i-th line)
  *
  *title = {The Network Data Repository with Interactive Graph Analytics and Visualization},
  *url={http://networkrepository.com},
  *year={2015}
  *}
  */
import scala.collection.parallel._

object PROTEINS_graph extends DLoader[Graph[(Array[Float], Int), Unit, Int]] {
  type grType = Graph[(Array[Float], Int), Unit, Int]
  type grListType = List[grType]
  val savedObj = s"${getHomeDir}/mldat/proteinsGraph"

  def r = {
    val dat = file2data()
    val nds = dat.head.nds
    nds.take(10).foreach(x => println("nd feat: ", x.data._1.length))

  }

  override def file2data(files: Seq[String]) = {
    val fDir = s"${getHomeDir}/mass/mldata/PROTEINS-full/"

    val r: grListType = if (new File(savedObj).exists()) {
      println("saved obj exists")
      objSerl.read[grListType](savedObj)
    } else {
      val edges = textFileLines(fDir + "PROTEINS-full.edges").map { s =>
        val t = s.split(',').map(_.toInt)
        (t(0), t(1))
      }.toList

      val graph_labels =
        textFileLines(fDir + "PROTEINS-full.graph_labels").zipWithIndex.map {
          case (str, grI) => (str.toInt, grI + 1)
        }.toSeq

      val grs: grListType =
        textFileLines(fDir + "PROTEINS-full.node_attrs")
          .zip(textFileLines(fDir + "PROTEINS-full.node_labels"))
          .zip(textFileLines(fDir + "PROTEINS-full.graph_idx"))
          .zipWithIndex
          .toList
          .par
          .map {
            case x @ (((na, nl), grIdx), nid) => (((na, nl), grIdx), nid + 1)
          }
          .groupBy(_._1._2.toInt) // list of gr
          .map {
            case (grI, ndss) =>
              //            in same graph
              //            node attr is array float
              val nR = ndss map {
                case (((nAttr, nLab), _), nid) =>
                  (nid, nAttr.split(',').map(_.toFloat), nLab.toInt)
              }

              val nsIds = nR.map(_._1)

              val eR =
                edges.filter(
                  x => (Set(x._1, x._2) intersect nsIds.toSet).nonEmpty
                )

              val g: grType = Graph(
                nR.map(n => Node(n._1, (n._2, n._3))).toList,
                eR.map(e => Edge(e._1, e._2, ())),
                graph_labels.find(_._2 == grI).get._1
              )
              g
          }
          .toList

      objSerl.write(grs, savedObj)
      println("written to savedObj")
      grs
    }

//    Graph[Int, Int](Seq(), Seq())
    r.filter(_.nds.length > 2)
  }
}
