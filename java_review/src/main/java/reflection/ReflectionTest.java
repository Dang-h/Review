package reflection;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @version 1.0
 * @Description: TODO
 * @Date 2020/12/15 11:15
 **/
public class ReflectionTest {

	@Test
	public void testClassInstance() throws ClassNotFoundException {
		// 调用Class的静态方法：forName(String classPath)
		Class clazz = Class.forName("reflection.Person");
		System.out.println("clazz = " + clazz);
	}

	@Test
	public void testMethod() throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
		Class<Person> clazz = Person.class;

		Person person = clazz.newInstance();

		Method show = clazz.getDeclaredMethod("show", String.class);

		show.setAccessible(true);

		Object tom = show.invoke(person, "Tom");
		System.out.println(tom);

		System.out.printf("==============================");

		Method info = clazz.getDeclaredMethod("info");
		info.setAccessible(true);
		Object returnVal = info.invoke(null);

		System.out.println(returnVal);


	}
}
