public class Runmain {
    public static void main(String[] args) {
        ViewsCalculator view = new ViewsCalculator();
        ControllerCalculator controller = new ControllerCalculator(view);
    }
}