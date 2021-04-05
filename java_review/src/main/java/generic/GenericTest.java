package generic;



import org.testng.annotations.Test;

import java.util.*;

/**
 * @version 1.0
 * @Description: TODO
 * @Date 2020/12/10 15:20
 **/
public class GenericTest {

	@Test
	public void test4Array(){
		ArrayList<Integer> list = new ArrayList<>();

		list.add(23);
		list.add(23);
		list.add(23);
		list.add(23);
		list.add(23);

		for (Integer integer : list) {
			System.out.println(integer);
		}

		Iterator<Integer> iterator = list.iterator();
		while (iterator.hasNext()) {
			Integer next = iterator.next();
			System.out.print(next);
		}
	}

	@Test
	public void test4Map(){
		HashMap<String, Integer> map = new HashMap<>();

		map.put("Tom", 87);
		map.put("Jerry", 87);
		map.put("Jack", 87);

		Set<Map.Entry<String, Integer>> entry = map.entrySet();

		for (Map.Entry<String, Integer> e : entry) {
			String key = e.getKey();
			Integer value = e.getValue();
			System.out.println(key + " " + value);
		}
	}
}
