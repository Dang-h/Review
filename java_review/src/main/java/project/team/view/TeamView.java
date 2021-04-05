package project.team.view;

import project.team.domain.Employee;
import project.team.domain.Programmer;
import project.team.service.NameListService;
import project.team.service.TeamException;
import project.team.service.TeamService;
import project.team.utils.TSUtility;

/**
 * @version 1.0
 * @Description: 静态页面功能展示
 * @Date 2020/8/26 23:33
 **/
public class TeamView {
	private NameListService listService = new NameListService();
	private TeamService teamService = new TeamService();

	/**
	 * 主界面显示及控制方法
	 */
	public void enterMainMenu() {
		boolean loopFlag = true;
		char menu = 0;

		while (loopFlag) {

			if (menu != '1') {
				listAllEmployees();
			}

			System.out.print("1-团队列表  2-添加团队成员  3-删除团队成员 4-退出   请选择(1-4)：");
			menu = TSUtility.readMenuSelection();
			System.out.println();


			switch (menu) {
				case '1':
					getTeam();
					break;
				case '2':
					addMember();
					break;
				case '3':
					deleteMember();
					break;
				case '4':
					System.out.print("确认是否退出(Y/N)：");
					char exitSelection = TSUtility.readConfirmSelection();
					if (exitSelection == 'Y') {
						loopFlag = false;
					}
					break;
			}
		}


	}

	/**
	 * 以表格形式列出公司所有成员
	 */
	private void listAllEmployees() {
		System.out.println("\n-------------------------------开发团队调度软件--------------------------------\n");
		Employee[] allEmployees = listService.getAllEmployees();

		if (allEmployees == null || allEmployees.length == 0) {
			System.out.println("公司中没有任何员工信息！");
		} else {
			System.out.println("ID\t姓名\t年龄\t工资\t职位\t状态\t奖金\t股票\t领用设备");

			for (Employee allEmployee : allEmployees) {
				System.out.println(allEmployee);
			}
		}

		System.out.println("-------------------------------------------------------------------------------");
	}

	/**
	 * 获取开发团队所有成员
	 */
	public void getTeam() {
		Programmer[] team = teamService.getTeam();

		System.out.println("\n--------------------团队成员列表---------------------\n");
		if (team == null || team.length == 0) {
			System.out.println("开发团队目前没有成员！");
		} else {
			System.out.println("TID/ID\t姓名\t年龄\t工资\t职位\t奖金\t股票");
			for (Programmer programmer : team) {
				System.out.println(programmer.getDetail4Team());
			}
		}

		System.out.println("-----------------------------------------------------");
	}

	/**
	 * 实现添加成员操作
	 */
	private void addMember() {
		System.out.println("---------------------添加成员---------------------");
		System.out.print("请输入要添加的员工ID：");
		int id = TSUtility.readInt();

		try {
			Employee employee = listService.getEmployee(id);
			teamService.addMember(employee);
			System.out.println("添加成功！");
		} catch (TeamException e) {
			System.out.println("添加失败，原因：" + e.getMessage());
		}

		//按回车继续
		TSUtility.readReturn();
	}

	/**
	 * 从团队中删除指定id的成员
	 */
	private void deleteMember() {
		System.out.println("---------------------删除成员---------------------");
		System.out.print("请输入要删除员工的TID：");
		int id = TSUtility.readInt();
		System.out.print("确认是否删除(Y/N)：");
		char yn = TSUtility.readConfirmSelection();

		if (yn == 'N') {
			return;
		}

		try {
			teamService.removeMember(id);
			System.out.println("删除成功！");
		} catch (TeamException e) {
			System.out.println("删除失败，原因：" + e.getMessage());
		}

		TSUtility.readReturn();
	}


	public static void main(String[] args) {
		TeamView teamView = new TeamView();
		teamView.enterMainMenu();
	}
}
