package project.team.service;

import project.team.domain.Architect;
import project.team.domain.Designer;
import project.team.domain.Employee;
import project.team.domain.Programmer;

/**
 * @version 1.0
 * @Description: 关于开发团队成员的管理：添加、删除等
 * @Date 2020/8/26 10:07
 **/
public class TeamService {
	private static int counter = 1;//给memberId赋值
	private static final int MAX_MEMBER = 5;//开发团队总人数
	private Programmer[] team = new Programmer[MAX_MEMBER];//保存开发团队的成员
	private int total = 0;//记录开发团队中实际的开发人数

	/**
	 * 关于开发团队成员的管理：添加、删除等
	 */
	public TeamService() {
	}

	/**
	 * 获取开发团队中的所有成员
	 *
	 * @return 团队成员信息
	 */
	public Programmer[] getTeam() {
		Programmer[] team = new Programmer[total];

		for (int i = 0; i < team.length; i++) {
			team[i] = this.team[i];
		}

		// 改写
//		if (project.team.length >= 0)
//			System.arraycopy(this.project.team, 0, project.team, 0, project.team.length);

		return team;
	}

	/**
	 * 将指定的员工添加到开发团队当中
	 *
	 * @param e 指定员工
	 */
	public void addMember(Employee e) throws TeamException {
//		失败信息包含以下几种：
//		成员已满，无法添加
		if (total >= MAX_MEMBER) {
			throw new TeamException("成员已满，无法添加");
		}
//		该成员不是开发人员，无法添加
		if (!(e instanceof Programmer)) {
			throw new TeamException("该成员不是开发人员，无法添加");
		}
//		该员工已在本开发团队中
		if (isExist(e)) {
			throw new TeamException("该员工已在本开发团队中");
		}
//		该员工已是某团队成员
//		该员正在休假，无法添加
		Programmer p = (Programmer) e;
		if ("BUSY".equals(p.getStatus().getName())) {
			throw new TeamException("该员工已是某团队成员");
		} else if ("VOCATION".equals(p.getStatus().getName())) {
			throw new TeamException("该员正在休假，无法添加");
		}
//		团队中至多只能有一名架构师
//		团队中至多只能有两名设计师
//		团队中至多只能有三名程序员
		int numOfArc = 0, numOfDes = 0, numOfPro = 0;

		for (int i = 0; i < total; i++) {
			if (team[i] instanceof Architect) {
				numOfArc++;
			} else if (team[i] instanceof Designer) {
				numOfDes++;
			} else if (team[i] instanceof Programmer) {
				numOfPro++;
			}
		}

		if (p instanceof Architect) {
			if (numOfArc >= 1) {
				throw new TeamException("团队中至多只能有一名架构师");
			}
		} else if (p instanceof Designer) {
			if (numOfDes >= 2) {
				throw new TeamException("团队中至多只能有两名设计师");
			}
		} else {
			if (numOfPro >= 3) {
				throw new TeamException("团队中至多只能有三名程序员");
			}
		}

		//将p（或者e）添加到现有的team中
		team[total++] = p;
		//更改员工状态并赋予团队Id
		p.setStatus(Status.BUSY);
		p.setMemberId(counter++);

	}

	/**
	 * 判断指定的员工是否已经存在于现有的开发团队中
	 *
	 * @param e 指定员工
	 * @return true-存在；false-不存在
	 */
	private boolean isExist(Employee e) {
		for (int i = 0; i < total; i++) {
			if (team[i].getId() == e.getId()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 移除指定成员Id的成员
	 *
	 * @param memberId 成员Id
	 */
	public void removeMember(int memberId) throws TeamException {
		int i = 0;
		for (i = 0; i < total; i++) {
			if (team[i].getMemberId() == memberId) {
				team[i].setStatus(Status.FREE);
				break;
			}
		}

		//未找到则抛出异常
		if (i == total) {
			throw new TeamException("找不到指定memberId的员工");
		}

		// 后一个元素覆盖前一个元素，实现删除
		for (int j = i + 1; j < total; j++) {
			team[j - 1] = team[j];
		}

		//将最后的元素置为null
		team[--total] = null;

	}
}
