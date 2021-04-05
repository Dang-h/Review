## 创建Topic：
	kafka-topics.sh --zookeeper hadoop100:2181 --create --replication-factor 3 --partitions 4 --topic first

## 查看Topic：
### 查看Topic列表：
    kafka-topics.sh --zookeeper hadoop100:2182 --list
### 查看指定Topic详情：
    kafka-topics.sh --zookeeper hadoop100:2181 --describe --topic first

## 修改Topic：
### 增加分区：
    kafka-topics.sh --zookeeper hadoop100:2181 --alter --topic first --partitions 4
### 删除Topic：
    kafka-topics.sh --zookeeper hadoop100:2181 --delete --topic first

## 生产消息
	kafka-console-producer.sh --broker-list hadoop100:9092 --topic first

## 消费消息：
### 消费指定消费者组，并从头开始消费
    kafka-console-consumer.sh --bootstrap-server hadoop100:9092
                              --topic first
                              --from-beginning
                              --consumer-property group.id=group_first

## 消费者组
### 查看消费者组列表
    kafka-consumer-groups.sh --bootstrap-server hadoop100:9092 --list
### 查看指定消费者组详情
    kafka-consumer-groups.sh --bootstrap-server hadoop100:9092 --describe --group group_first
|GROUP|TOPIC|PARTITION|CURRENT - OFFSET|LOG - END - OFFSET|LAG|CONSUMER - ID|HOST|CLIENT - ID|
|-----|-----|---------|----------------|------------------|---|-------------|----|-----------|
|group_first|first|2|35|35|0|-|-|-|
|group_first|first|1|27|27|0|-|-|-|
|group_first|first|0|32|32|0|-|-|-|

> LAG = HW - ConsumerOffset 
> 
> LogStartOffset：表示一个Partition的起始位移，初始为0，虽然消息的增加以及日志清除策略的影响，这个值会阶段性的增大。
> 
>ConsumerOffset：消费位移，表示Partition的某个消费者消费到的位移位置。 
> 
>HighWatermark：简称HW，代表消费端所能“观察”到的Partition的最高日志位移，HW大于等于ConsumerOffset的值。
> 
>LogEndOffset：简称LEO, 代表Partition的最高日志位移，其值对消费者不可见。

### 查看Topic各分区的偏移量
kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list hadoop100:9092 --topic first