public class ModelsCalculator{
    public double add (double a, double b){
        return a+b;
    }
     public double sub (double a, double b){
        return a-b;
    }
     public double multiply (double a, double b){
        return a*b;
    }
 public double divide(double a, double b) {
        if (b == 0) {
            return 0;
        } else {
            return a / b;
        }
    }
}

