package scorch.dataloader

import scorch.autograd.Variable

import java.io.{
  FileInputStream,
  FileOutputStream,
  ObjectInputStream,
  ObjectOutputStream
}

object objSerl {

  def write[t](obj: t, path: String) = {
    val out = new ObjectOutputStream(new FileOutputStream(path))
    out.writeObject(obj)
    out.close()
  }

  def read[t](path: String) = {
    val in = new ObjectInputStream(new FileInputStream(path))
    val fooToRead = in.readObject()
    in.close()
    fooToRead.asInstanceOf[t]
  }

  //    assert(fooToWrite == fooToRead)
}
trait DataLoader
    extends scala.collection.immutable.Iterable[(Variable, Variable)] {
  def numSamples: Int
  def numBatches: Int
  def mode: String
}

object DataLoader {
  def instance(
      dataSet: String,
      mode: String,
      miniBatchSize: Int,
      take: Option[Int] = None
  ): DataLoader =
    dataSet match {
      case "cifar-10" =>
        new Cifar10DataLoader(mode, miniBatchSize, take = take)
      case "mnist" =>
        new MnistDataLoader_img(mode, miniBatchSize, take)
    }
}

@SerialVersionUID(123L)
case class YX(y: Float, x: Array[Float]) extends Serializable
