package ioStream;

import org.junit.jupiter.api.Test;

import java.io.*;

/**
 * @version 1.0
 * @Description: TODO
 * @Date 2020/12/11 17:14
 **/
public class IOStreamTest {

	@Test
	public void testFileReader() {
		// 实例化需要读取的文件
		File file = new File("E:\\Develop\\Coding\\Java\\Review\\java_review\\src\\tmp\\hello.txt");
		// 实例化流
		if (file.exists()) {

			FileReader fileReader = null;
			try {
				fileReader = new FileReader(file);
				// 读取操作
				char[] charBuffer = new char[5];
				int len;

				while ((len = fileReader.read(charBuffer)) != -1) {
//					for (int i = 0; i < len; i++) {
//						System.out.print(charBuffer[i]);
//					}
					String s = new String(charBuffer, 0, len);
					System.out.print(s);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				// 关闭流
				if (fileReader != null) {
					try {
						fileReader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			System.out.println("文件不存在！");
		}
	}

	@Test
	public void testWriterReader() {
		FileReader fR = null;
		FileWriter fW = null;
		try {
			File fr = new File("E:\\Develop\\Coding\\Java\\Review\\java_review\\src\\tmp\\hello.txt");
			File fw = new File("testFileWriter.txt");

			fR = new FileReader(fr);
			fW = new FileWriter(fw);

			char[] fRBuf = new char[5];
			int len;
			while ((len = fR.read(fRBuf)) != -1) {
				String s = new String(fRBuf, 0, len);
				fW.write(s, 0, len);
				fW.write("测试！");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fR != null) {
				try {
					fW.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fR != null) {
				try {
					fR.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}

	@Test
	public void testFileInputStream() {
		String srcPath = "E:\\tmp\\test1.rmvb";
//		String destPath = "E:\\tmp\\output.rmvb";//8411
		String destPath = "E:\\tmp\\output1.rmvb";//2330

		long start = System.currentTimeMillis();
//		copyFile(srcPath, destPath);
		copyWithBuffered(srcPath,destPath);
		long end = System.currentTimeMillis();

		System.out.println("spend time:" + (end - start));
	}

	public void copyFile(String srcPath, String destPath) {
		File srcFile = new File(srcPath);
		File destFile = new File(destPath);

		if (srcFile.exists()) {
			FileInputStream srcIS = null;
			FileOutputStream destOS = null;
			try {
				srcIS = new FileInputStream(srcFile);
				destOS = new FileOutputStream(destFile);

				byte[] buffer = new byte[1024];
				int len;
				while ((len = srcIS.read(buffer)) != -1) {
					destOS.write(buffer, 0, len);
//					for (int i = 0; i < len; i++) {
//						destOS.write(buffer[i]);
//					}
				}
				System.out.println("复制完成！");
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (srcIS != null) {
					try {
						srcIS.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				if (destOS != null) {
					try {
						destOS.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	public void copyWithBuffered(String srcPath, String destPath) {
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			// 实例化File
			File srcFile = new File(srcPath);
			File destFile = new File(destPath);

			// 实例化流
			FileInputStream fis = new FileInputStream(srcFile);
			FileOutputStream fos = new FileOutputStream(destFile);

			// 实例化缓冲流
			bis = new BufferedInputStream(fis);
			bos = new BufferedOutputStream(fos);

			// 读取操作
			byte[] buffer = new byte[1024];
			int len;

			while ((len = bis.read(buffer)) != -1) {
				bos.write(buffer,0,len);
			}

			System.out.println("copy success!");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 关闭流
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			if (bis != null) {

				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
