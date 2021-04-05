package reflection;

/**
 * @version 1.0
 * @Description: TODO
 * @Date 2020/12/15 11:15
 **/
public class Person {
	private String name;
	private	int age;
	private String gender;

	public Person() {
	}

	public Person(String name, int age, String gender) {
		this.name = name;
		this.age = age;
		this.gender = gender;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	@Override
	public String toString() {
		return "Person{" +
				"name='" + name + '\'' +
				", age=" + age +
				", gender='" + gender + '\'' +
				'}';
	}

	private String show(String name) {
		return "my name is " + name;
	}

	public static void info() {
		System.out.println("I'm a static method");
	}
}
