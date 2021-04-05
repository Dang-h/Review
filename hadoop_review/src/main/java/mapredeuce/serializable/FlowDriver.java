package mapredeuce.serializable;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * @author DangHao
 */
public class FlowDriver {
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

		args = new String[]{"data/phone_data.txt", "data_output"};

		// 获取配置并创建Job实例
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);

		// 指定Driver的jar包
		job.setJarByClass(FlowDriver.class);

		// 指定Mapper和Reducer的jar包
		job.setMapperClass(FlowMapper.class);
		job.setReducerClass(FlowReducer.class);

		// 指定Mapper输出的k-v类型
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(FlowBean.class);

		// 指定最终输出的k-v类型
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(FlowBean.class);

		// 指定输入文件路径和最终结果输出路径
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		// 提交Job
		boolean result = job.waitForCompletion(true);
		System.exit(result ? 0 : 1);
	}
}
