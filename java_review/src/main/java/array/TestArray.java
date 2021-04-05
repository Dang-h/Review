package array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;


public class TestArray {
	public static void main(String[] args) {
		int[] data = new int[]{3, 2, 1, 0, 5, -1, 0, 10};

		ArrayList<Integer> integers = new ArrayList<>();
		integers.add(1);
		integers.add(2);
		integers.add(1);
		integers.add(10);
		integers.add(-1);
		Iterator<Integer> iterator = integers.iterator();
		while (iterator.hasNext()) {
			System.out.println(iterator.next());
		}

		Collection obj = new ArrayList();
		obj.add(1);
		obj.add("String");
		obj.add(false);

		for (Object o : obj) {
			System.out.println(o);
		}

		TestArray testArray = new TestArray();

//		System.out.println(Arrays.toString(testArray.bubbleSort(data)));
//		System.out.println(Arrays.toString(testArray.quickSort(data)));
	}

	/**
	 * @return int[]
	 * @Author D_h
	 * @Description //TODO
	 * @Date 10:49 2020/8/25
	 * @Param [data]
	 **/
	int[] bubbleSort(int[] data) {
		for (int i = 0; i < data.length - 1; i++) {
			for (int j = 0; j < data.length - 1 - i; j++) {
				if (data[j] > data[j + 1]) {
					swap(data, j, j + 1);
				}
				System.out.println("内：" + Arrays.toString(data) + " ");
			}
			System.out.println("外：" + Arrays.toString(data) + " ");
			System.out.println();
		}
		return data;
	}

	int[] quickSort(int[] data) {
		subSort(data, 0, data.length - 1);

		return data;
	}


	static void swap(int[] data, int i, int j) {
		int temp = data[i];
		data[i] = data[j];
		data[j] = temp;
//		System.out.print("data[" + j + "] " + data[j] + " " + "data[" + j + "] " + data[j] + " ");
	}

	
	static void subSort(int[] data, int start, int end) {
		if (start < end) {
			int base = data[start];
			int low = start;
			int high = end + 1;

			while (true) {
				while (low < end && data[++low] - base <= 0) {
					;
				}
				while (high > start && data[--high] - base >= 0) {
					;
				}
				if (low < high) {
					swap(data, low, high);
				} else {
					break;
				}
			}
			swap(data, start, high);

			subSort(data, start, high - 1);//递归调用
			subSort(data, high + 1, end);
		}
	}
}

