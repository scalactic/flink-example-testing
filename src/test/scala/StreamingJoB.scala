import org.scalatest._
import flatspec._
import matchers.should._

import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration
import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.test.util.MiniClusterWithClientResource
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

import java.util
import java.util._

import scala.util._
import scala.collection.JavaConverters._

class StreamingJobIntegrationTest extends AnyFlatSpec with Matchers with BeforeAndAfter {

  val flinkCluster = new MiniClusterWithClientResource(new MiniClusterResourceConfiguration.Builder()
    .setNumberSlotsPerTaskManager(2)
    .setNumberTaskManagers(1)
    .build
  )

  before {
    flinkCluster.before()
  }

  after {
    flinkCluster.after()
  }


  "IncrementFlatMapFunction pipeline" should "incrementValues" in {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    // configure your test environment
    env.setParallelism(2)

    // values are collected in a static variable
    CollectSink.values.clear()

    // create a stream of custom elements and apply transformations
    env
      .fromElements(1L, 21L, 22L)
      .map(new IncrementMapFunction())
      .addSink(new CollectSink())

    // execute
    env.execute()

    // verify your results
    CollectSink.values should contain allOf (2, 22, 23)
  }

  "StreamJoinTest pipeline" should "give latest trx and status" in {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(2)
    TrxStatusSink.values.clear()

    val s1 = env.fromElements(
      Trx("123", 55.55, 1),
      Trx("123", 22.22, 2),
      Trx("123", 33.33, 3)
    )
    val s2 = env.fromElements(
      Status("123", "Fraud"),
      Status("123", "Lost"),
      Status("123", "Stolen")
    )

    s1.keyBy(_.cardNo).connect(s2.keyBy(_.cardNo))
      .flatMap(new StreamJoinFlatMap())
      .addSink(new TrxStatusSink())

    env.execute()

    val allElems = TrxStatusSink.values.asScala.toList
    println(allElems)
    allElems.size shouldBe 6
    allElems.last shouldBe TrxStatus("123", 33.33, "Stolen")
  }
}
// create a testing sink
class CollectSink extends SinkFunction[Long] {
  override def invoke(value: Long, context: SinkFunction.Context): Unit = {
    CollectSink.values.add(value)
  }
}

object CollectSink {
  // must be static
  val values: util.List[Long] = Collections.synchronizedList(new util.ArrayList())
}


class TrxStatusSink extends SinkFunction[TrxStatus] {
  override def invoke(value: TrxStatus, context: SinkFunction.Context): Unit = {
    TrxStatusSink.values.add(value)
  }
}

object TrxStatusSink {
  val values: util.List[TrxStatus] = Collections.synchronizedList[TrxStatus](new util.ArrayList[TrxStatus]())
}
