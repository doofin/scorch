package scorch

import botkop.numsca.Tensor

package object RL {
  def categorical[t](probs: Seq[Float]) = {
    probs.head
  }
  case class Env() {
    def step(action: Tensor): (Int, Float, Boolean) = {
//    state, reward, done, _ = env.step(action)
      (0, 0f, true)
    }
  }
}
