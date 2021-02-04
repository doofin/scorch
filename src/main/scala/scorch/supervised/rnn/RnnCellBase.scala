package scorch.supervised.rnn

import botkop.{numsca => ns}
import scorch.autograd.Variable
import scorch.supervised.SeqModule

/**
  * Extension of MultiVarModule to allow storing the number of previous states
  * and a method for generating initial states
  * @param vs local parameters of the module
  */
abstract class RnnCellBase(vs: Seq[Variable]) extends SeqModule(vs) {

  /**
    * Dimension of hidden states
    */
  def na: Int

  /**
    * Number of states to keep track of.
    * For example, in an LSTM you keep track of the hidden state and the cell state.
    * In this case this would be = 2
    * For an RNN, it would be 1, because you only keep track of the hidden state.
    */
  def numTrackingStates: Int

  /**
    * Provide a sequence of states to start an iteration with
    */
  def initialTrackingStates: Seq[Variable] =
    Seq.fill(numTrackingStates)(Variable(ns.zeros(na, 1)))
}
