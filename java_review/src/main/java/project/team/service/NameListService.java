package project.team.service;

import project.team.domain.PC;
import project.team.domain.*;

import static project.team.service.Data.*;

/**
 * @version 1.0
 * @Description: 负责将Data中的数据封装到Employee[]数组中，同时提供相关操作Employee[]的方法
 * @Date 2020/8/25 21:25
 **/
public class NameListService {
	private Employee[] employees;

	/**
	 * 给employees及其数组进行初始化
	 */
	public NameListService() {

		//1 根据项目提供的Data类构建相应大小的employees数组
		employees = new Employee[EMPLOYEES.length];

		//2 再根据Data类中的数据构建不同的对象，包括Employee、Programmer、Designer和Architect对象，以及相关联的Equipment子类的对象
		//3 将对象存于数组中
		for (int i = 0; i < employees.length; i++) {
			//获取通用属性
			int type = Integer.parseInt(EMPLOYEES[i][0]);
			int id = Integer.parseInt(EMPLOYEES[i][1]);
			String name = EMPLOYEES[i][2];
			int age = Integer.parseInt(EMPLOYEES[i][3]);
			double salary = Double.parseDouble(EMPLOYEES[i][4]);

			//特有属性
			Equipment equipment;
			double bonus;
			int stock;

			switch (type) {
				case EMPLOYEE:
					employees[i] = new Employee(id, name, age, salary);
					break;
				case PROGRAMMER:
					equipment = createEquipment(i);
					employees[i] = new Programmer(id, name, age, salary, equipment);
					break;
				case DESIGNER:
					equipment = createEquipment(i);
					bonus = Double.parseDouble(EMPLOYEES[i][5]);
					employees[i] = new Designer(id, name, age, salary, equipment, bonus);
					break;
				case ARCHITECT:
					equipment = createEquipment(i);
					bonus = Double.parseDouble(EMPLOYEES[i][5]);
					stock = Integer.parseInt(EMPLOYEES[i][6]);
					employees[i] = new Architect(id, name, age, salary, equipment, bonus, stock);
					break;
			}
		}

	}

	/**
	 * 获取指定index上的设备
	 *
	 * @param index
	 * @return
	 */
	private Equipment createEquipment(int index) {

		int key = Integer.parseInt(EQUIPMENTS[index][0]);
		String modelOrName = EQUIPMENTS[index][1];

		switch (key) {
			case PC:
				String display = EQUIPMENTS[index][2];
				return new PC(modelOrName, display);
			case NOTEBOOK:
				double price = Double.parseDouble(EQUIPMENTS[index][2]);
				return new NoteBook(modelOrName, price);
			case PRINTER:
				String type = EQUIPMENTS[index][2];
				return new Printer(modelOrName, type);
		}

		return null;
	}

	/**
	 * 获取所有员工
	 *
	 * @return 员工信息
	 */
	public Employee[] getAllEmployees() {
		return employees;
	}

	/**
	 * 获取指定id的员工
	 * @param id 员工id
	 * @return 员工信息
	 */
	public Employee getEmployee(int id) throws TeamException {
		for (Employee employee : employees) {
			if (employee.getId() == id) {
				return employee;
			}
		}
		throw new TeamException("找不到指定的员工");
	}
}
