public class TestJava {
	public static void main(String[] args) {

//		String name = "DangHao";
		String name = "danghao";

		boolean nameHasUpperCase = false;

		for (int i = 0; i < name.length(); i++) {
			if (Character.isUpperCase(name.charAt(i))) {
				nameHasUpperCase = true;
				break;
			}
		}

		System.out.println("nameHasUpperCase = " + nameHasUpperCase);
	}
}
