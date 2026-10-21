import javax.swing.*;
import java.awt.*;

public class ViewsCalculator extends JFrame {
    public JLabel a, b, result;
    public JTextField txta, txtb, txtresult;
    public JButton btnAdd, btnSub, btnMultiply, btnDivide;

    public ViewsCalculator() {
        setTitle("Calculator");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        a = new JLabel("Enter first number:");
        a.setBounds(50, 50, 150, 30);
        add(a);
        txta = new JTextField(10);
        txta.setBounds(200, 50, 150, 30);
        add(txta);

        b = new JLabel("Enter second number:");
        txtb = new JTextField(10);
        b.setBounds(50, 100, 150, 30);
        txtb.setBounds(200, 100, 150, 30);
        add(b);
        add(txtb);

        btnAdd = new JButton("Add");
        btnAdd.setBounds(50, 200, 100, 30);
        add(btnAdd);

        btnSub = new JButton("Subtract");
        btnSub.setBounds(200, 200, 100, 30);
        add(btnSub);

        btnMultiply = new JButton("Multiply");
        btnMultiply.setBounds(350, 200, 100, 30);
        add(btnMultiply);

        btnDivide = new JButton("Divide");
        btnDivide.setBounds(500, 200, 100, 30);
        add(btnDivide);

        result = new JLabel("Result:");
        result.setBounds(50, 300, 100, 30);
        txtresult = new JTextField(10);
        txtresult.setEditable(false);
        txtresult.setBounds(200, 300, 150, 30);
        add(result);
        add(txtresult);
        setVisible(true);
    }
}