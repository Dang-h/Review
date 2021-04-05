package project.team.domain;

/**
 * @version 1.0
 * @Description: 台式机
 * @Date 2020/8/25 20:48
 **/
public class PC implements Equipment {

	private String model;//机器型号
	private String display;//显示器名称

	public PC() {
	}

	public PC(String model, String display) {
		this.model = model;
		this.display = display;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	@Override
	//戴尔(三星 17寸)
	public String getDescription() {
		return model + "(" + display + ")";
	}
}
