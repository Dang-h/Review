import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestMethods {
	@Test
	public void testSimpleDateFormat() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long l = System.currentTimeMillis();
		System.out.println("sdf.format(new Date()) = " + sdf.format(new Date()));
		Date date = new Date(System.currentTimeMillis());
		String dateStr = "2021-03-06 16:45:58";
		Date parse = sdf.parse(dateStr);
//		System.out.println(date);
	}

	@Test
	public void testFileReader() throws IOException {
		File file = new File("testFileWriter.txt");

		FileReader fileReader = new FileReader(file);
		int data = fileReader.read();
		while (data != -1) {
			System.out.print((char) data);
			data = fileReader.read();
		}

		fileReader.close();
	}

	@Test
	public void testFileReader1() {
		File file = new File("testFileWriter.txt");
		if (file.exists()) {
			FileReader fr = null;
			try {
				fr = new FileReader(file);

				int data = fr.read();
				while (data != -1) {
					System.out.print((char) data);
					data = fr.read();
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					assert fr != null;
					fr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Test
	public void testFileReader2() throws IOException {
		File file = new File("testFileWriter.txt");

		FileReader fr = new FileReader(file);

		char[] buffer = new char[5];
		int len ;
		while ((len = fr.read(buffer)) != -1) {
			String str = new String(buffer, 0, len);
			System.out.print(str);
		}

		fr.close();
	}

	@Test
	public void testBufferedInputStream(){
		File file = new File("testFileWriter.txt");
	}
}
