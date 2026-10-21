import java.rmi.Naming;

public class client {
	public static void main(String[] args) {
		try {
			Calculator calc = (Calculator) Naming.lookup("rmi://localhost/CalculatorService");
			int a = 5, b = 3;
			System.out.println(a + "+" + b + " = " + calc.add(a, b));
			System.out.println(a + "-" + b + " = " + calc.subtract(a, b));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
