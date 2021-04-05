package project.team.service;

/**
 * @version 1.0
 * @Description: 自定义异常类
 * @Date 2020/8/25 22:27
 **/
public class TeamException extends Exception{
	static final long serialVersionUID = -33875169124229948L;

	public TeamException() {
	}

	public TeamException(String message) {
		super(message);
	}
}
