import org.apache.flink.api.common.functions.MapFunction

class IncrementMapFunction extends MapFunction[Long, Long] {

  override def map(record: Long): Long = {
    record + 1
  }
}
