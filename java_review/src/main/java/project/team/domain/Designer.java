package project.team.domain;

/**
 * @version 1.0
 * @Description: 设计师
 * @Date 2020/8/25 21:22
 **/
public class Designer extends Programmer {
	private double bonus;

	public Designer() {
	}

	public Designer(int id, String name, int age, double salary, Equipment equipment, double bonus) {
		super(id, name, age, salary, equipment);
		this.bonus = bonus;
	}

	public double getBonus() {
		return bonus;
	}

	public void setBonus(double bonus) {
		this.bonus = bonus;
	}

	@Override
	//5/12   杨致远   27      600.0      设计师   4800.0
	public String getDetail4Team() {
		return detail4Team() + "\t设计师" + "\t" +getBonus();
	}

	@Override
	// 5      雷军      28       10000.0  设计师   FREE    5000.0                 佳能 2900(激光)
	public String toString() {
		return getDetails() + "\t设计师\t" + getStatus() + "\t" + getBonus() + "\t\t" + getEquipment().getDescription();
	}
}
