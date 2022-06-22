import org.scalatest._
import flatspec._
import matchers._


class SimpleIncrementMapFunctionTest extends AnyFlatSpec with should.Matchers {

  "IncrementMapFunction1" should "increment values" in {
    val incrementer: IncrementMapFunction = new IncrementMapFunction()

    incrementer.map(2) should be (3)
  }

  "IncrementMapFunction2" should "increment values" in {
    val incrementer: IncrementMapFunction = new IncrementMapFunction()

    incrementer.map(2) shouldBe 3
  }
}
