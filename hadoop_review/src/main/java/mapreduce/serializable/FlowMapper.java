package mapreduce.serializable;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * @author DangHao
 */
public class FlowMapper extends Mapper<LongWritable, Text, Text, FlowBean> {

	private Text outK = new Text();
	private FlowBean outV = new FlowBean();

	@Override
	protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		// 获取一行数据，转换成String
		String line = value.toString();

		// 切割数据
		String[] datas = line.split("\t");

		// 获取需要的数据：手机号，上行流量，下行流量
		String phone = datas[1];
		String upFlow = datas[datas.length - 3];
		String downFlow = datas[datas.length - 2];

		// 封装输出的k-v
		outK.set(phone);
		outV.setUpFlow(Long.parseLong(upFlow));
		outV.setDownFlow(Long.parseLong(downFlow));
		outV.setSumFlow();

		// 输出
		context.write(outK, outV);
	}
}
