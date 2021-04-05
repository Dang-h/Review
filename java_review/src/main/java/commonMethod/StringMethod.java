package commonMethod;


import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

/**
 * 1.模拟一个trim方法，去除字符串两端的空格。
 * <p>
 * 2.将一个字符串进行反转。将字符串中指定部分进行反转。比如将“abcdefg”反转为”abfedcg”
 * <p>
 * 3.获取一个字符串在另一个字符串中出现的次数。
 * 比如：获取“ab”在 “cdabkkcadkabkebfkabkskab”
 * 中出现的次数
 * <p>
 * 4.获取两个字符串中最大相同子串。比如：
 * str1 = "abcwerthelloyuiodef“;str2 = "cvhellobnm"//10
 * 提示：将短的那个串进行长度依次递减的子串与较长
 * 的串比较。
 * <p>
 * 5.对字符串中字符进行自然顺序排序。"abcwerthelloyuiodef"
 * 提示：
 * 1）字符串变成字符数组。
 * 2）对数组排序，选择，冒泡，Arrays.sort(str.toCharArray());
 * 3）将排序后的数组变成字符串。
 **/

public class StringMethod {

	public String[] getMaxSubStringArray(String str1, String str2) {
		if (str1 != null && str2 != null) {
			StringBuilder stringBuilder = new StringBuilder();
			String maxStr = (str1.length() >= str2.length()) ? str1 : str2;
			String minStr = (str1.length() < str2.length()) ? str1 : str2;

			int len = minStr.length();

			for (int i = 0; i < len; i++) {
				for (int x = 0, y = len - i; y <= len; x++, y++) {
					String subStr = minStr.substring(x, y);
					if (maxStr.contains(subStr)) {
						stringBuilder.append(subStr + ",");
					}
				}

				if (stringBuilder.length() != 0) {
					break;
				}
			}

			// 接收多个子串

			return stringBuilder.toString().replaceAll(",$", "").split("\\,");
		}
		return null;
	}

	/**
	 * 获取两个字符串中最大相同子串（假设至多只有一个相同子串）
	 *
	 * @param str1
	 * @param str2
	 * @return
	 */
	public String getMaxSubString(String str1, String str2) {
		if (str1 != null && str2 != null) {
			String maxStr = (str1.length() >= str2.length()) ? str1 : str2;
			String minStr = (str1.length() < str2.length()) ? str1 : str2;

			int len = minStr.length();

			for (int i = 0; i < len; i++) {
				for (int x = 0, y = len - i; y <= len; x++, y++) {
//					System.out.println("x=" + x + " y=" + y + " str=" + minStr.substring(x, y));
					if (maxStr.contains(minStr.substring(x, y))) {
						return minStr.substring(x, y);
					}
				}
			}
		}

		return "";
	}

	/**
	 * 3.获取一个字符串在另一个字符串中出现的次数。
	 *
	 * @param mainStr 另一个字符串
	 * @param subStr  一个字符串
	 * @return 出现次数
	 */
	public int getCount(String mainStr, String subStr) {
		int mainLength = mainStr.length();
		int subLength = subStr.length();
		int index = 0; // 子串位置索引
		int count = 0;

		if (mainLength >= subLength) {
			while ((index = mainStr.indexOf(subStr, index)) != -1) {
				count++;
				index += subLength;
			}

			return count;
		} else {
			return 0;
		}
	}

	/**
	 * 2.将一个字符串进行反转。将字符串中指定部分进行反转。比如将“abcdefg”反转为”abfedcg”
	 *
	 * @param start 反转开始索引
	 * @param end   反转结束索引
	 * @param str   待反转字符串
	 * @return 指定索引反转后的字符串
	 */
	public String reverseStr(String str, int start, int end) {
		if (str != null) {
			StringBuilder strBuilder = new StringBuilder(str.length());

			strBuilder.append(str.substring(0, start));

			for (int i = end; i >= start; i--) {
				strBuilder.append(str.charAt(i));
			}

			strBuilder.append(str.substring(end + 1));

			return strBuilder.toString();
		}
		return "";
	}

	/**
	 * 模拟一个trim方法，去除字符串两端的空格
	 *
	 * @param str 需要去除空格的字符串
	 * @return 去除空格后的字符串
	 */
	public String myTrim(String str) {
		if (str != null) {
			int startIndex = 0;
			int endIndex = str.length() - 1;

			while (startIndex < endIndex && str.charAt(startIndex) == ' ') {
				startIndex++;
			}

			while (startIndex < endIndex && str.charAt(endIndex) == ' ') {
				endIndex--;
			}

			if (str.charAt(startIndex) == ' ') {
				return "";
			}

			return str.substring(startIndex, endIndex + 1);

		}
		return null;
	}

	@Test
	public void testTrim() {
		String str = "  asd   ssdfds  ";
		System.out.println(myTrim(str));
	}

	@Test
	public void testSubString() {
		String testStr = "012345";
		System.out.println(testStr.length());
		System.out.println(testStr.substring(0, 1));
	}

	@Test
	public void testReverseStr() {
		String str = "abcdefg";
		System.out.println(reverseStr(str, 2, 5));

		String str2 = "cdabkkcadkabkebfkabkskab";
		System.out.println(getCount(str2, "ab"));
	}

	@Test
	public void testGetMaxSubString() {
		String testStr = "abcwerthelloyuiodef";
		String testStr2 = "cvhellobnmiodef";

//		System.out.println(getMaxSubString(testStr, testStr2));
		System.out.println(Arrays.toString(getMaxSubStringArray(testStr,testStr2)));
	}

	@Test
	public void testDateTimeClass() {
		System.out.println(LocalDate.now());
		System.out.println(LocalTime.now());
		System.out.println(LocalDateTime.now());

	}
}
