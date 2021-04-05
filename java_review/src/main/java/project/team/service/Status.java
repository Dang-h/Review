package project.team.service;

/**
 * @version 1.0
 * @Description: 员工状态
 * @Date 2020/8/25 21:12
 **/
public class Status {
	private String name;

	public Status(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	public static final Status FREE = new Status("FREE");//空闲
	public static final Status BUSY = new Status("BUSY");//已分配团队
	public static final Status VOCATION = new Status("VOCATION");//休假

	@Override
	public String toString() {
		return name;
	}
}
