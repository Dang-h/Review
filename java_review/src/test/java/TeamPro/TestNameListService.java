package TeamPro;

import org.junit.Test;
import project.team.domain.Employee;
import project.team.service.NameListService;
import project.team.service.TeamException;

/**
 * @version 1.0
 * @Description: TODO
 * @Date 2020/8/25 22:31
 **/
public class TestNameListService {

	@Test
	public void testGetAllEmployees() {
		NameListService nameListService = new NameListService();

		Employee[] allEmployees = nameListService.getAllEmployees();

		for (Employee allEmployee : allEmployees) {
			System.out.println(allEmployee);
		}
	}

	@Test
	public void testGetEmployee() {
		NameListService nameListService = new NameListService();
		int id = 15;

		try {
			System.out.println(nameListService.getEmployee(id));
		} catch (TeamException e) {
			System.out.println(e.getMessage());
		}
	}
}
