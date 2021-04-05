package project.team.domain;

/**
 * @version 1.0
 * @Description: 架构师
 * @Date 2020/8/25 21:24
 **/
public class Architect extends Designer {
	private int stock;

	public Architect() {
	}

	public Architect(int id, String name, int age, double salary, Equipment equipment, double bonus, int stock) {
		super(id, name, age, salary, equipment, bonus);
		this.stock = stock;
	}

	public int getStock() {
		return stock;
	}

	public void setStock(int stock) {
		this.stock = stock;
	}

	@Override
	//3/2     马化腾   32      18000.0  架构师   15000.0  2000
	public String getDetail4Team() {
		return detail4Team() + "\t架构师" + "\t" + getBonus() + "\t" + getStock();
	}

	@Override
	//马化腾   32       18000.0  架构师   FREE    15000.0   2000    联想T4(6000.0)
	public String toString() {
		return getDetails() + "\t架构师\t" + getStatus() + "\t" + getBonus() + "\t" + getEquipment().getDescription() +
				"\t" + getStock();
	}
}
