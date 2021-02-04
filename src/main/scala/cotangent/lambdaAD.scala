package cotangent

/**
Demystifying Differentiable Programming - Shift/Reset the Penultimate Backpropagator
  */
object lambdaAD {
  class NumR(val x: Double, var d: Double = 0) { b =>
    def +(that: NumR) = { (k: NumR => Unit) =>
      val y = new NumR(x + that.x)
      k(y)
      this.d += y.d
      that.d += y.d
    }
    def *(that: NumR) = { (k: NumR => Unit) =>
      val y = new NumR(x * that.x)
      k(y)
      this.d += that.x * y.d
      that.d += this.x * y.d
    }
  }

  def gradR(f: NumR => (NumR => Unit) => Unit)(x: Double) = {
    val z = new NumR(x, 0.0)
    f(z)(r => r.d = 1.0)
    z.d
  }

  // example: d x^2+x^3 =  2*x + 3*x*x
  val df: Double => Double = gradR { x: NumR => (k: NumR => Unit) =>
    (x * x)(y1 => (x * y1)(y2 => (y1 + y2)(k)))
  }

  def run = {
    val f = { x: Double =>
      2 * x + 3 * x * x
    }

    1 to 10 foreach { n =>
      println(df(n), f(n)) // equals,ok
    }
  }
}
