package mapredeuce.wordCount;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * @author DangHao
 */
public class WordCountDriver {

	public static void main(String[] args) throws Exception {

		args = new String[]{"data/wordCount.txt", "data_output"};

		// 获取配置信息及job实例
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);

		// 关联Driver程序的jar
		job.setJarByClass(WordCountDriver.class);

		// 关联Mapper和Reducer的jar
		job.setMapperClass(WordCountMapper.class);
		job.setReducerClass(WordCountReducer.class);

		// 设置Mapper输出的k-v类型
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);

		// 设置最终输出结果的k-v类型
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		// 设置数据输入路径和结果输出路径
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		// 提交Job
		boolean result = job.waitForCompletion(true);
		System.exit(result ? 0 : 1);
	}
}
