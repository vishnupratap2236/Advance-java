import javax.swing.*;
import java.awt.event.*;

public class ControllerCalculator implements ActionListener {
    private ViewsCalculator view;
    private ModelsCalculator mc = new ModelsCalculator();

    public ControllerCalculator(ViewsCalculator view) {
        this.view = view;
        view.btnAdd.addActionListener(this);
        view.btnSub.addActionListener(this);
        view.btnMultiply.addActionListener(this);
        view.btnDivide.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Object src = e.getSource();
            if (src == view.btnAdd) {
                double a = Double.parseDouble(view.txta.getText());
                double b = Double.parseDouble(view.txtb.getText());
                double result = mc.add(a, b);
                view.txtresult.setText(String.valueOf(result));
            } else if (src == view.btnSub) {
                double a = Double.parseDouble(view.txta.getText());
                double b = Double.parseDouble(view.txtb.getText());
                double result = mc.sub(a, b);
                view.txtresult.setText(String.valueOf(result));
            } else if (src == view.btnMultiply) {
                double a = Double.parseDouble(view.txta.getText());
                double b = Double.parseDouble(view.txtb.getText());
                double result = mc.multiply(a, b);
                view.txtresult.setText(String.valueOf(result));
            } else if (src == view.btnDivide) {
                double a = Double.parseDouble(view.txta.getText());
                double b = Double.parseDouble(view.txtb.getText());
                double result = mc.divide(a, b);
                view.txtresult.setText(String.valueOf(result));
            }
        } catch (NumberFormatException ex) {
            view.txtresult.setText("Invalid input");
        }
    }
}