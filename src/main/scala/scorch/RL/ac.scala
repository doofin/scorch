package scorch.RL

import botkop.numsca.Tensor

/**actor critic*/
object ac {

  /**act_probs, valueOfState = nnForward(state)*/
  def select_action[st](state: st, nnForward: st => (Seq[Float], Float)) = {

    /*def select_action(state1, nnForward):
    state = torch.from_numpy(state1).float()
    act_probs, valueOfState = nnForward(state)
    m = Categorical(act_probs)
    action = m.sample()
    return action.item(), (m.log_prob(action), valueOfState)
     */
    val (act_probs, valueOfState) = nnForward(state)
    val sampled_act = categorical(act_probs)
    (sampled_act, valueOfState)
  }

  def pgUpdate(rewards_actions: Seq[(Tensor, Float)]) = {
    rewards_actions.map {
      case (r, a) =>
        val advantage = r
        val policy_losses = 0
        val value_losses = 0
        (policy_losses, value_losses)
    }
  }
}
