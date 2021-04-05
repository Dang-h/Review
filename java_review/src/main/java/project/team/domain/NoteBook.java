package project.team.domain;

/**
 * @version 1.0
 * @Description: 笔记本电脑
 * @Date 2020/8/25 20:44
 **/
public class NoteBook implements Equipment {

	private String model;//机器型号
	private double price;//机器价格

	public NoteBook() {
	}

	public NoteBook(String model, double price) {
		this.model = model;
		this.price = price;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	@Override
	//联想T4(6000.0)
	public String getDescription() {
		return model + "(" + price + ")";
	}
}
