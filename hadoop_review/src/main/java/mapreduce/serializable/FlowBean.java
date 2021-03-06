package mapreduce.serializable;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * 流量统计的 Bean 对象
 *
 * @author DangHao
 */
public class FlowBean implements Writable {

	private long upFlow;
	private long downFlow;
	private long sumFlow;

	public FlowBean() {
	}

	public FlowBean(long upFlow, long downFlow, long sumFlow) {
		this.upFlow = upFlow;
		this.downFlow = downFlow;
		this.sumFlow = sumFlow;
	}

	public long getUpFlow() {
		return upFlow;
	}

	public void setUpFlow(long upFlow) {
		this.upFlow = upFlow;
	}

	public long getDownFlow() {
		return downFlow;
	}

	public void setDownFlow(long downFlow) {
		this.downFlow = downFlow;
	}

	public long getSumFlow() {
		return sumFlow;
	}

	public void setSumFlow(long sumFlow) {
		this.sumFlow = sumFlow;
	}

	public void setSumFlow() {
		this.sumFlow = this.upFlow + this.downFlow;
	}

	// 实现序列化和反序列化方法，顺序要保持一致

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeLong(upFlow);
		out.writeLong(downFlow);
		out.writeLong(sumFlow);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.upFlow = in.readLong();
		this.downFlow = in.readLong();
		this.sumFlow = in.readLong();
	}


	@Override
	public String toString() {
		// 重写toString方法，如果想自定义排序，还要重写comparable
		return upFlow + "\t" + downFlow + "\t" + sumFlow;
	}
}
