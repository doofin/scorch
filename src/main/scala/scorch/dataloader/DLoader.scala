package scorch.dataloader

//import com.doofin.stdScala._

trait DLoader[t] {
  def file2data(files: Seq[String] = Seq()): t

  def textFileLines(f: String) = scala.io.Source.fromFile(f).getLines()
}
