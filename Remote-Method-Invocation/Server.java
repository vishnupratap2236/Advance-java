import java.rmi.Naming;

public class Server {
    public static void main(String[] args) {
        try {
            CalculatorImpl calc = new CalculatorImpl();
            Naming.rebind("rmi://localhost/CalculatorService", calc);
            System.out.println("RMI Service is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}