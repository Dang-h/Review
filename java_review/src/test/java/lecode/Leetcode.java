package lecode;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @version 1.0
 * @Author DangHao
 * @Description:
 * @Date 2021/2/25 16:12
 **/
public class Leetcode {


	@Test
	// TODO 【1】两数之和
	public void testTwoSum() {
		int[] nums = new int[]{2, 7, 11, 15};
		int target = 15;
//		System.out.println(Arrays.toString(twoSum1(nums, target)));
		System.out.println(Arrays.toString(twoSum2(nums, target)));
	}

	public int[] twoSum1(int[] nums, int target) {
		if (nums.length == 0) {
			return new int[0];
		}

		for (int i = 0; i < nums.length; i++) {
			int tar = nums[i];
			System.out.println("tar = " + tar + "|i = " + i + "|target - tar = " + (target - tar));
			for (int j = i + 1; j < nums.length; j++) {
				System.out.println("j = " + j + "|nums[j] = " + nums[j]);
				if (nums[j] == target - tar) {
					return new int[]{i, j};
				}
			}
		}
		return new int[0];
	}

	public int[] twoSum2(int[] nums, int target) {
		HashMap<Integer, Integer> hashMap = new HashMap<>();

		if (nums.length == 0) {
			return new int[0];
		}

		for (int i = 0; i < nums.length; i++) {
			if (hashMap.containsKey(target - nums[i])) {
				return new int[]{
						hashMap.get(target - nums[i]), i
				};

			}
			hashMap.put(nums[i], i);
		}

		return new int[0];
	}

	//=================================================================
	@Test
	// TODO 【359】至少有k个重复字符的最长子串
	public void testLongestSubstring() {
		String s = "aabcccd";
		int k = 2;
//		System.out.println(longestSubstring1(s, k));
		System.out.println(longestSubstring2(s, k));
	}

	public int longestSubstring1(String s, int k) {
		int n = s.length();
		return dfs(s, 0, n - 1, k);
	}

	public int dfs(String s, int l, int r, int k) {
		// 获取字符串s中各字母出现的次数
		int[] cnt = new int[26];
		for (int i = l; i <= r; i++) {
			cnt[s.charAt(i) - 'a']++;
		}

		// 找出不符合长度k的分界字符
		char split = 0;
		for (int i = 0; i < 26; i++) {
			if (cnt[i] > 0 && cnt[i] < k) {
				split = (char) (i + 'a');
				break;
			}
		}
		if (split == 0) {
			return r - l + 1;
		}

		// 切分
		int i = l;
		int ret = 0;
		while (i <= r) {
			while (i <= r && s.charAt(i) == split) {
				i++;
			}
			if (i > r) {
				break;
			}
			int start = i;
			while (i <= r && s.charAt(i) != split) {
				i++;
			}

			int length = dfs(s, start, i - 1, k);
			ret = Math.max(ret, length);
		}
		return ret;
	}

	public int longestSubstring2(String s, int k) {
		if (s.length() < k) {
			return 0;
		}

		HashMap<Character, Integer> counter = new HashMap<>();

		for (int i = 0; i < s.length(); i++) {
			counter.put(s.charAt(i), counter.getOrDefault(s.charAt(i), 0) + 1);
		}

		for (char c : counter.keySet()) {
			if (counter.get(c) < k) {
				int res = 0;
				for (String t : s.split(String.valueOf(c))) {
					res = Math.max(res, longestSubstring2(t, k));
				}

				return res;
			}
		}
		return s.length();
	}

	//=================================================================
	@Test
	//TODO 【7】整数反转
	public void testReverse() {
		// 2147483647
		System.out.println(reverse(1147483649));
//		System.out.println(reverse(123));
	}

	public int reverse(int x) {
		int res = 0;

		while (x != 0) {
			// 取末尾
			int tmp = x % 10;
			if (res > 214748364 || (res == 214748264 && tmp > 7)) {
				return 0;
			}

			if (res < -214748364 || (res == -214748364 && tmp < -8)) {
				return 0;
			}

			res = res * 10 + tmp;
			x /= 10;
		}

		return res;
	}

	//=================================================================
	@Test
	// TODO 按索引反转字串
	public void testReverseString() {
		String str = "abcdefg";
		int startIndex = 5;
		int endIndex = 7;
		System.out.println(reverseString2(str, startIndex, endIndex));
	}

	public String reverseString1(String str, int startIndex, int endIndex) {
		if (str != null && startIndex >= 0 && endIndex >= 0 && startIndex <= endIndex && endIndex <= str.length()) {
			StringBuilder strBuilder = new StringBuilder(str.length());
			// 第一部分，未反转部分
			strBuilder.append(str.substring(0, startIndex));
			// 第二部分，反转部分
			for (int i = endIndex; i >= startIndex; i--) {
				strBuilder.append(str.charAt(i));
			}
			// 第三部分，未反转部分
			strBuilder.append(str.substring(endIndex + 1));

			return strBuilder.toString();
		}

		return null;
	}

	public String reverseString2(String str, int startIndex, int endIndex) {
		if (str != null && startIndex >= 0 && endIndex >= 0 && startIndex <= endIndex && endIndex <= str.length()) {
			char[] chars = str.toCharArray();

			for (int x = startIndex, y = endIndex; x < y; x++, y--) {
				char tmp = chars[x - 1];
				chars[x - 1] = chars[y - 1];
				chars[y - 1] = tmp;
			}
			return new String(chars);

		}

		return null;
	}

	//=================================================================
	@Test
	// TODO 子串在主串中出现的次数
	public void testGetCount() {
		String mainStr = "absfsfabasfabdsf";
		String subString = "fa";
		System.out.println(getCount(mainStr, subString));
	}

	public int getCount(String mainStr, String subStr) {
		int mainLength = mainStr.length();
		int subLength = subStr.length();
		int count = 0;
		int index = 0;

		if (mainLength >= subLength) {
			while ((index = mainStr.indexOf(subStr, index)) != -1) {
				count++;
				index += subLength;
			}
			return count;
		}
		return -1;
	}

	//=================================================================
	@Test
	//TODO 获取两个字符串中的最大相同字串；假设只有一个相同字串
	public void testGetMaxSameString() {
		String str1 = "adferhellse";
		String str2 = "ddhello";
		System.out.println(getMaxSameString(str1, str2));
	}

	private String getMaxSameString(String str1, String str2) {
		if (str1 != null && str2 != null) {
			// 区分大小串
			String maxStr = (str1.length() >= str2.length()) ? str1 : str2;
			String minStr = (str1.length() < str2.length()) ? str1 : str2;
			int length = minStr.length();

			for (int i = 0; i < length; i++) {
				// 从右往左取一次，从左往右取一次
				for (int x = 0, y = length - i; y <= length; x++, y++) {
					String subStr = minStr.substring(x, y);
//					System.out.println(subStr);
					if (maxStr.contains(subStr)) {
						return subStr;
					}
				}
			}
		}
		return null;
	}


	//=================================================================
	@Test
	// TODO 【459】 重复的字符串
	public void testRepeatedSubstringPattern() {
		String s = "abab";
//		System.out.println(repeatedSubstringPattern1(s));
		System.out.println(repeatedSubstringPattern2(s));
	}

	public boolean repeatedSubstringPattern1(String s) {
		if (s.length() > 0 && s.length() < 1000) {
			String str = s + s;
			return str.substring(1, str.length() - 1).contains(s);
		}
		return false;
	}

	public boolean repeatedSubstringPattern2(String s) {
		if (s != null || s.length() > 1) {
			int length = s.length();
			String str = s;
			while (length > 1) {
				str = str.charAt(s.length() - 1) + str.substring(0, s.length() - 1);
				if (str.equals(s)) {
					return true;
				}
				length--;
			}
		}
		return false;
	}

	//=================================================================
	@Test
	// TODO 【9】回文数
	// 如果是负数则一定不是回文数，直接返回 false
	//如果是正数，则将其倒序数值计算出来，然后比较和原数值是否相等
	//如果是回文数则相等返回 true，如果不是则不相等 false
	//比如 123 的倒序 321，不相等；121 的倒序 121，相等

	public void testIsPalindrome(){
		int x = 121;
		System.out.println(isPalindrome(x));
	}
	public boolean isPalindrome(int x) {
		if (x < 0)
			return false;

		int res = 0;
		int cur = x;
		while (cur != 0) {
			int tmp = cur % 10;
			// 反转整数
			res = res * 10 + tmp;
			cur /=10;
		}
		return res == x;
	}
}
