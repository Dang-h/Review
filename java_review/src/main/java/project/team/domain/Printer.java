package project.team.domain;

/**
 * @version 1.0
 * @Description: 打印机
 * @Date 2020/8/25 20:52
 **/
public class Printer implements Equipment {

	private String name;//打印机名称
	private String type;//打印机类型

	public Printer() {
	}

	public Printer(String name, String type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	//佳能 2900(激光)
	public String getDescription() {
		return name + "(+" + type + ")";
	}
}
