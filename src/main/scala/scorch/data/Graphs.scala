package scorch.data

object Graphs {
  case class Graph[nP, eP,gP](nds: Seq[Node[nP]], egs: Seq[Edge[eP]],grProp:gP) {
    def check = {

      /** no duplicate node*/
      val nids = nds.map(_.nId)
      nids.distinct.length == nids.length

      /** no duplicate edge*/
      val eids = egs.map(x => (x.n1, x.n2))
      eids.distinct.length == eids.length

      /**no nonexisting node ,check all nodes are present in graph for all links*/
      val cont = (for {
        e <- egs
      } yield Seq(nds find (n => n.nId == e.n1), nds find (n => n.nId == e.n2))).flatten

      val succ = !(cont contains None)
      (succ, "edgeReferToExistingNode")

      //      assert()
    }

    def getNeighbours(centerId: NodeId) = {
      val egsNew =
        egs.filter(e => or(e.n1 == centerId, e.n2 == centerId)).distinct
      val ndsReferByEdge = egsNew.flatMap(x => Seq(x.n1, x.n2)).distinct
      nds.filter(x => ndsReferByEdge contains x.nId)
    }

  }
  def or(bs: Boolean*): Boolean = bs.exists(x => x)
  def all(bs: Boolean*): Boolean = bs.forall(x => x)

  type Uid = Long
  type NodeId = Uid
  type EdgeId = Uid

  case class Node[P](
      nId: NodeId = 0L,
      data: P
  ) {}

  case class Edge[P](
      n1: NodeId = 0L,
      n2: NodeId = 0L,
      data: P
  ) {}

}
