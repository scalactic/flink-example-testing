import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction
import org.apache.flink.streaming.api.scala.createTypeInformation
import org.apache.flink.util.Collector

import scala.util.{Failure, Success, Try}

case class Trx(cardNo: String, amount: Double, timestamp: Int)
case class Status(cardNo: String, status: String)
case class TrxStatus(cardNo: String, amount: Double, status: String)

class StreamJoinFlatMap extends RichCoFlatMapFunction[Trx, Status, TrxStatus] {
  private var lastTrx: ValueState[Trx] = _
  private var lastStatus: ValueState[Status] = _

  override def open(parameters: Configuration): Unit = {
    lastTrx = getRuntimeContext.getState(new ValueStateDescriptor[Trx]("lastTrx", createTypeInformation[Trx]))
    lastStatus = getRuntimeContext.getState(new ValueStateDescriptor[Status]("lastStatus", createTypeInformation[Status]))
  }

  override def flatMap1(trx: Trx, out: Collector[TrxStatus]): Unit = {
    lastTrx.update(trx)
    out.collect(TrxStatus(trx.cardNo, trx.amount, Try(lastStatus.value.status) match {
      case Success(status) => status
      case Failure(exception) => "Open"
    }))
  }

  override def flatMap2(status: Status, out: Collector[TrxStatus]): Unit = {
    lastStatus.update(status)
    out.collect(TrxStatus(status.cardNo, Try(lastTrx.value.amount) match {
      case Success(amount) => amount
      case Failure(exception) => 0.0
    }, status.status))
  }
}
