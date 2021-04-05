package project.team.utils;


import java.util.Scanner;

/**
 * 键盘访问工具类
 */
public class TSUtility {

	public static Scanner scanner = new Scanner(System.in);

	/**
	 * 读取键盘，如果用户键入’1’-’4’中的任意字符，则方法返回。
	 *
	 * @return 返回值为用户键入字符。
	 */
	public static char readMenuSelection() {
		char c = 0;

		while (true) {
			String str = readKeyboard(1, false);
			c = str.charAt(0);
			if (c != '1' && c != '2' && c != '3' && c != '4') {
				System.out.println("选择错误，请重新输入：");
			} else break;
		}

		return c;
	}

	/**
	 * 读取键盘输入，限定字数和是否返回空白输入
	 *
	 * @param limit       限定读取字符个数
	 * @param blankReturn 是否可以输入空白（按Enter继续）
	 * @return 输入字符串
	 */
	private static String readKeyboard(int limit, boolean blankReturn) {
		String line = "";

		while (scanner.hasNextLine()) {
			line = scanner.nextLine();

			//按Enter继续
			if (line.length() == 0) {
				if (blankReturn) return line;
				else continue;
			}

			if (line.length() > limit) {
				System.out.println("输入长度（不大于" + limit + "）错误，请重新输入：");
				continue;
			}

			break;
		}

		return line;

	}

	/**
	 * 提示并等待，直到用户按回车键后返回。
	 */
	public static void readReturn() {
		System.out.println("按回车键继续...");
		readKeyboard(1, true);
	}

	/**
	 * 从键盘读取一个长度不超过2位的整数，并将其作为方法的返回值。
	 *
	 * @return 长度不超过2位的整数
	 */
	public static int readInt() {
		int n;
		while (true) {
			try {
				String s = readKeyboard(2, false);
				n = Integer.parseInt(s);
				break;
			} catch (NumberFormatException e) {
				System.out.println("数字输入错误，请重新输入：");
			}
		}
		return n;
	}

	/**
	 * 键盘读取‘Y’或’N’，并将其作为方法的返回值。
	 *
	 * @return Y或者N
	 */
	public static char readConfirmSelection() {
		char c;
		while (true) {
			String s = readKeyboard(1, false).toUpperCase();
			c = s.charAt(0);
			if (c == 'Y' || c == 'N') {
				break;
			} else {
				System.out.println("选择错误，请重新输入：");
			}
		}
		return c;
	}
}
