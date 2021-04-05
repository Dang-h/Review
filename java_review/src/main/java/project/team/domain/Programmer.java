package project.team.domain;

import project.team.service.Status;

/**
 * @version 1.0
 * @Description: 程序员
 * @Date 2020/8/25 20:59
 **/
public class Programmer extends Employee {
	private int memberId;//开发团队Id
	private Status status = Status.FREE;//员工的状态
	private Equipment equipment;

	public Programmer() {
	}

	public Programmer(int id, String name, int age, double salary, Equipment equipment) {
		super(id, name, age, salary);
		this.equipment = equipment;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Equipment getEquipment() {
		return equipment;
	}

	public void setEquipment(Equipment equipment) {
		this.equipment = equipment;
	}

	//2/4     刘强东   24      7300.0    程序员
	public String detail4Team() {
		return getMemberId() + "/" + getId() +"\t" + getName() + "\t"+getAge() + "\t" + getSalary();
	}

	public String getDetail4Team() {
		return detail4Team() + "\t程序员";
	}

	@Override
	//4      刘强东   24       7300.0    程序员   FREE                                戴尔(三星 17寸)
	public String toString() {
		return getDetails() + "\t程序员\t" + status + "\t\t\t" + equipment.getDescription();
	}
}
