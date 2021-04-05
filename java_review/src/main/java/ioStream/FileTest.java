package ioStream;

import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * @version 1.0
 * @Description: TODO
 * @Date 2020/12/10 16:45
 **/
public class FileTest {
	@Test
	public void testFile() throws IOException {
		// 创建File实例
		// TODO 获取路径，IDEA中在Test中的路径默认为当前模块，main中的路径默认为当前工程

		File file1 = new File("E:\\Develop\\Coding\\Java\\Review");
		File file = new File("testFileWriter.txt");

		System.out.println(file.getParent());
		System.out.println(file.getAbsoluteFile());
		System.out.println(file.isFile());
		System.out.println(file.canExecute());
		System.out.println(file.canRead());
		System.out.println(file.canWrite());
		System.out.println(file.getParent());
		System.out.println(file.getCanonicalPath());
		System.out.println(file.length());
		long l = file.lastModified();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println("sdf.format(new Date(l)) = " + sdf.format(new Date(l)));
//		readAllFiles("E:\\Develop\\Coding\\Java\\Review");
//		isDir(file1);

	}

	public void isDir(File file) {
		if (file.isDirectory()) {
			File[] list = file.listFiles();
			for (File s : list) {
				System.out.println("\t|--" + s.getName());
				isDir(s);
			}
		}

	}

	int level = -1;

	public void readAllFiles(String path) {
		File f = new File(path);
		level++;

		if (!f.exists()) {
			System.out.println("No such a file or directory!");
		}

		File[] fList = f.listFiles();

		if (fList == null) {
			System.out.println(f.getName());
		}

		Arrays.sort(fList);

		for (File fTem : fList) {
			for (int i = 0; i < level; i++) {
				System.out.println("|   ");
			}

			System.out.println("|-- " + fTem.getName());

			if (fTem.isDirectory()) {
				readAllFiles(fTem.getAbsolutePath());
			}
		}

		level--;
	}

	@Test
	public void testDelete() {
		File file = new File("E:\\tmp");
		deleteDir(file);
		if (!file.exists()) {
			System.out.println("Delete Success!");
		}
	}

	public void deleteDir(File file) {
		if (file.isDirectory()) {
			File[] listFiles = file.listFiles();
			for (File listFile : listFiles) {
				deleteDir(listFile);
			}
		}

		file.delete();
	}

	@Test
	public void testGetDirSize() {
		File file = new File("E:\\Develop\\Coding\\Java\\Review\\java_review");
		System.out.println(getDirSize(file) / 1024 + "KB");
	}

	public double getDirSize(File file) {
		double size = 0;

		if (file.isFile()) {
			size += file.length();
			System.out.println(file.getName() + "--" + size / 1024 + "kB");
		} else {
			File[] listFiles = file.listFiles();
			for (File listFile : listFiles) {
				size += getDirSize(listFile);
				System.out.println(listFile.getName() + "--" + size / 1024 + "kB");
			}
		}

		return size;
	}

}
