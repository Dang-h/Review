package array;

import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * @version 1.0
 * @Description: TODO
 * @Date 2020/9/11 11:37
 **/
public class Goods implements Comparable {
	private String name;
	private double price;

	public Goods() {
	}

	public Goods(String name, double price) {
		this.name = name;
		this.price = price;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	@Override
	public String toString() {
		return "Goods{" +
				"name='" + name + '\'' +
				", price=" + price +
				'}';
	}

	/*
	当前对象this>形参对象obj，返回正整数
	当前对象this<形参对象obj，返回负整数
	当前对象this=形参对象obj，返回0
	 */
	@Override
	public int compareTo(Object o) {
		if (o instanceof Goods) {
			Goods goods = (Goods) o;
			if (this.price > goods.price) {
				return 1;
			} else if (this.price < goods.price) {
				return -1;
			} else {
				//价格相同按名称首字母从低到高排序
				return this.name.compareTo(goods.name);
			}
		}

		throw new RuntimeException("传入的数据类型不一致！");
	}
}

class TestGoods {

	@Test
	public void testComparable() {
		Goods[] goodsArr = new Goods[4];
		goodsArr[0] = new Goods("xiaomi", 10.1);
		goodsArr[1] = new Goods("rog", 102.1);
		goodsArr[2] = new Goods("huawei", 10.1);
		goodsArr[3] = new Goods("huawei", 9.1);

//		Arrays.sort(goodsArr);
		//按名字首字母从大到小，名字相同按价格从高到低
		Arrays.sort(goodsArr, new Comparator<Goods>() {
			@Override
			public int compare(Goods o1, Goods o2) {
				if (o1 instanceof Goods && o2 instanceof Goods) {

					if (o1.getName().equals(o2.getName())) {
						return -Double.compare(o1.getPrice(), o2.getPrice());
					} else {
						return o1.getName().compareTo(o2.getName());
					}
				}
				throw new RuntimeException("输入类型错误！");
			}
		});
		System.out.println(Arrays.toString(goodsArr));
	}

	@Test
	public void testDate() throws ParseException {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String date = "2020-09-11";
		Date parse1 = simpleDateFormat.parse(date);
		long time = parse1.getTime();
		java.sql.Date sqlDate = new java.sql.Date(time);
		System.out.println(sqlDate);

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String format = dateTimeFormatter.format(LocalDate.now());
		System.out.println(format);

		TemporalAccessor parse = dateTimeFormatter.parse(date);
		System.out.println(parse);


	}

}
