package project.team.domain;

/**
 * @version 1.0
 * @Description: 普通职员
 * @Date 2020/8/25 20:32
 **/
public class Employee {
	private int id;
	private String name;
	private int age;
	private double salary;

	public Employee() {
	}

	public Employee(int id, String name, int age, double salary) {
		this.id = id;
		this.name = name;
		this.age = age;
		this.salary = salary;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public double getSalary() {
		return salary;
	}

	public void setSalary(double salary) {
		this.salary = salary;
	}

	protected String getDetails() {
		return id + "\t" + name + "\t" + age + "\t" + salary;
	}

	@Override
	//1      马 云      22      3000.0
	public String toString() {
		return getDetails();
	}
}
