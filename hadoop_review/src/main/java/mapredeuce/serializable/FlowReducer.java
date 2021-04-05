package mapredeuce.serializable;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * @author DangHao
 */
public class FlowReducer extends Reducer<Text, FlowBean, Text, FlowBean> {
	private FlowBean outV = new FlowBean();

	@Override
	protected void reduce(Text key, Iterable<FlowBean> values, Context context) throws IOException, InterruptedException {
		long totalUp = 0;
		long totalDown = 0;

		// 遍历values，将upFlow和downFlow相加
		for (FlowBean value : values) {
			totalUp += value.getUpFlow();
			 totalDown += value.getDownFlow();
		}

		// 封装outK-V
		outV.setUpFlow(totalUp);
		outV.setDownFlow(totalDown);
		outV.setSumFlow();

		// 输出
		context.write(key, outV);
	}
}
