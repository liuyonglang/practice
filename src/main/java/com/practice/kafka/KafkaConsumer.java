package com.practice.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;

/**
 * @author Higmin
 * @date 2019/11/26 8:26
 * <p>
 * 示例一 ：
 * <p>
 * kafka 消费者 ==> 自动偏移量提交
 * <p>
 * 消费消息由两种方式：
 * 1. 自动偏移量提交：使用自动偏移量提交还可以“至少一次”交付，但要求是必须在每次后续调用之前或关闭使用者之前使用每次调用poll（Duration）所返回的所有数据。
 * 2. 手动偏移控制：使用手动偏移控制的优点是可以直接控制何时将 Records 视为“消耗”。
 * <p>
 * 在创建一个消费者时，默认是自动提交偏移量，当然我们也可以显示设置为自动。
 **/
public class KafkaConsumer {

	private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);
	private Properties props;
	private org.apache.kafka.clients.consumer.KafkaConsumer<String, String> consumer;
	public final static String TOPIC = "TEST-TOPIC";

	/**
	 * 初始化
	 */
	@PostConstruct
	public void init() {
		props = new Properties();
		props.put("bootstrap.servers", "192.168.183.150:9092,192.168.183.151:9092,192.168.183.152:9092"); // kafka 地址
		props.put("group.id", "test"); // 设置消费组
		props.put("client.id", "test"); // 设置消费者ID
		props.put("enable.auto.commit", "true"); //开启自动提交
		props.put("auto.commit.interval.ms", "1000"); // 自动提交设置偏移量提交时间间隔
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"); // key反序列化
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"); // value反序列化
		consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<String, String>(props); // 创建消费者
	}

	/**
	 * 消费消息
	 */
	public void consumeMsg() throws Exception {
		// 订阅主题
		consumer.assign(Arrays.asList(new TopicPartition(TOPIC, 0)));
		Map<TopicPartition, Long> timestampsToSearch = new HashMap<TopicPartition, Long>();
		// 构造待查询的分区
		TopicPartition partition = new TopicPartition("stock-quotation", 0);
		// 设置查询12 小时之前消息的偏移量
		timestampsToSearch.put(partition, (System.currentTimeMillis() - 12 * 3600 * 1000));
		// 会返回时间大于等于查找时间的第一个偏移量
		Map<TopicPartition, OffsetAndTimestamp> offsetMap = consumer.offsetsForTimes(timestampsToSearch);
		OffsetAndTimestamp offsetTimestamp = null;
		// 这里依然用for 轮询，当然由于本例是查询的一个分区，因此也可以用if 处理
		for (Map.Entry<TopicPartition, OffsetAndTimestamp> entry : offsetMap.entrySet()) {
			// 若查询时间大于时间戳索引文件中最大记录索引时间，
			// 此时value 为空,即待查询时间点之后没有新消息生成
			offsetTimestamp = entry.getValue();
			if (null != offsetTimestamp) {
				// 重置消费起始偏移量
				consumer.seek(partition, entry.getValue().offset());
			}
		}
		while (true) {
			// 等待拉取消息
			ConsumerRecords<String, String> records = consumer.poll(1000);
			for (ConsumerRecord<String, String> record : records) {
				// 简单打印出消息内容
				System.out.printf("partition = %d, offset = %d,key= %s value = %s%n", record.partition(), record.offset(), record.key(), record.value());
			}
		}

	}

	/**
	 * 销毁
	 */
	@PreDestroy
	public void destroy() {
		consumer.close();
	}

	public static void main(String[] args) {
		KafkaConsumer consumer = new KafkaConsumer();
		logger.info("开始消费消息...");
		Executors.newSingleThreadExecutor().execute(new Runnable() {
			@Override
			public void run() {
				consumer.init();
				while (true) {
					try {
						consumer.consumeMsg();
					} catch (Exception e) {
						if (consumer != null) {
							try {
								consumer.destroy();
							} catch (Throwable e1) {
								logger.error("Turn off Kafka consumer error! " + e);
							}
						}
					} finally {
						consumer.destroy();
					}
				}
			}
		});
	}
}
