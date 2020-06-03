import com.cra.figaro.algorithm.factored.VariableElimination
import com.cra.figaro.language._
import com.cra.figaro.library.compound._

object patch {
  def main(args: Array[String]): Unit = {
    //-------------Constraint--------------
    val Constraint_0 = Flip(0.8)
    val Constraint_1 = Flip(0.8)
    val Constraint_2 = Flip(0.8)

    //-------------Semantic--------------
    val Var_posX = Flip(0.5)
    val Var_posY = Flip(0.5)
    val Var_tempA = If(Var_posX, Flip(1.0), Flip(0.05))
    val Var_tempB = RichCPD(Var_posX, Var_tempA, 
      (OneOf(true), OneOf(true)) -> Flip(1.0),
      (*, *) -> Flip(0.05))
    val Var_this_even = RichCPD(Var_tempA, Constraint_0, 
      (OneOf(true), OneOf(true)) -> Flip(1.0),
      (*, *) -> Flip(0.05))
    val Var_this_size = RichCPD(Var_tempB, Constraint_1, 
      (OneOf(true), OneOf(true)) -> Flip(1.0),
      (*, *) -> Flip(0.05))
    val Var_this_ret = RichCPD(Var_tempB, Var_tempA, Constraint_2, 
      (OneOf(true), OneOf(true), OneOf(true)) -> Flip(1.0),
      (*, *, *) -> Flip(0.05))
    val Ret = If(Var_this_ret, Flip(1.0), Flip(0.05))

    //-------------Observation--------------
    Var_posX.observe(true)
    Var_posY.observe(true)

    //-------------Sampling--------------
    val samplePatchValid = VariableElimination(Ret)
    samplePatchValid.start()
    println(samplePatchValid.probability(Ret, true))
    samplePatchValid.kill()
  }
}

