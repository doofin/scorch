package scorch.dataloader

import scorch.data.Graphs._

object MUTAG_graph extends DLoader[Graph.type] {
  override def file2data(files: Seq[String]): Graph.type = ???
}
