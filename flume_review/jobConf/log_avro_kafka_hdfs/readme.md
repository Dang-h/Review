日志分别在hadoop101和hadoop102上，现在需要将它们汇聚并导入到HDFS中，且数据还需供实施项目使用

通过*avro*sink和source可进行不同节点之间网络传输
- [log-file-avro.conf](log-file-avro.conf)

通过*kafka*对数据进行复用并对导入HDFS进行缓冲
 - [avro-kafka.conf](avro-kafka.conf)
 - [kafka-file-hdfs.conf](kafka-file-hdfs.conf)