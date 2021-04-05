package hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

public class APITest {

	private FileSystem fs;

	@Before
	public void init() throws Exception {
		URI uri = new URI("hdfs://hadoop100:9820");
		Configuration conf = new Configuration();
		String user = "hadoop";
		// 获取一个客户端对象
		fs = FileSystem.get(uri, conf, user);
	}

	@After
	public void close() throws IOException {
		// 关闭资源
		fs.close();
	}

	@Test
	public void testMkdir() throws IOException {
		// 执行相关操作
		fs.mkdirs(new Path("/testMkdir"));
	}

	@Test
	public void testCopyFromLocalFile() throws IOException {
		// 执行相关操作
		Path src = new Path("data/9.spark 3.0AQE动态缩小分区.mp4");
		Path dst = new Path("/testMkdir");
		fs.copyFromLocalFile(false,src, dst);
	}

	@Test
	public void testListFiles() throws IOException {
		RemoteIterator<LocatedFileStatus> listFiles = fs.listFiles(new Path("/testMkdir"), true);

		while (listFiles.hasNext()) {
			LocatedFileStatus status = listFiles.next();
			System.out.println("status.getPermission() = " + status.getPermission());
			System.out.println("status.getGroup() = " + status.getGroup());
			System.out.println("status.getPath().getName() = " + status.getPath().getName());

			System.out.println("status.getBlockLocations().toString() = " + Arrays.toString(status.getBlockLocations()));
		}
	}
}
