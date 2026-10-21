import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class StudentRegistration extends JFrame implements ActionListener {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/collage_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    // Labels
    JLabel lblName, lblRoll, lblGender, lblBranch;
    // Text Fields
    JTextField txtName, txtRoll, txtBranch;
    // Radio Buttons
    JRadioButton male, female;
    ButtonGroup genderGroup;
    // Check Box
    JCheckBox terms;
    // Buttons
    JButton submit, reset;

    public StudentRegistration() {
        setTitle("Student Registration Form");
        setSize(1000, 760);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Font labelFont = new Font("SansSerif", Font.BOLD, 18);
        Font fieldFont = new Font("SansSerif", Font.PLAIN, 18);
        Font buttonFont = new Font("SansSerif", Font.BOLD, 18);

        // Student Name
        lblName = new JLabel("Student Name");
        lblName.setFont(labelFont);
        lblName.setBounds(50, 50, 220, 40);
        add(lblName);
        
        txtName = new JTextField();
        txtName.setFont(fieldFont);
        txtName.setBounds(300, 50, 520, 40);
        add(txtName);

        // Roll Number
        lblRoll = new JLabel("Roll Number");
        lblRoll.setFont(labelFont);
        lblRoll.setBounds(50, 130, 220, 40);
        add(lblRoll);
        
        txtRoll = new JTextField();
        txtRoll.setFont(fieldFont);
        txtRoll.setBounds(300, 130, 520, 40);
        add(txtRoll);

        // Gender
        lblGender = new JLabel("Gender");
        lblGender.setFont(labelFont);
        lblGender.setBounds(50, 210, 220, 40);
        add(lblGender);
        
        male = new JRadioButton("Male");
        male.setFont(fieldFont);
        male.setBounds(300, 210, 140, 40);
        add(male);
        
        female = new JRadioButton("Female");
        female.setFont(fieldFont);
        female.setBounds(470, 210, 160, 40);
        add(female);
        
        genderGroup = new ButtonGroup();
        genderGroup.add(male);
        genderGroup.add(female);

        // Branch
        lblBranch = new JLabel("Branch");
        lblBranch.setFont(labelFont);
        lblBranch.setBounds(50, 290, 220, 40);
        add(lblBranch);
        
        txtBranch = new JTextField();
        txtBranch.setFont(fieldFont);
        txtBranch.setBounds(300, 290, 520, 40);
        add(txtBranch);

        // Terms & Conditions
        terms = new JCheckBox("I accept Terms & Conditions");
        terms.setFont(fieldFont);
        terms.setBounds(50, 360, 620, 40);
        add(terms);

        // Submit Button
        submit = new JButton("Submit");
        submit.setFont(buttonFont);
        submit.setBounds(260, 430, 180, 55);
        submit.addActionListener(this);
        add(submit);

        // Reset Button
        reset = new JButton("Reset");
        reset.setFont(buttonFont);
        reset.setBounds(520, 430, 180, 55);
        reset.addActionListener(this);
        add(reset);

        // Make the frame visible to the user
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Reset Button Logic: Checks if the reset button was clicked
        if (e.getSource() == reset) {
            // Clearing Fields
            txtName.setText("");
            txtRoll.setText("");
            txtBranch.setText("");
            
            // Clearing Selections
            genderGroup.clearSelection();
            terms.setSelected(false);
            return;
        }

        // Submit Button Logic: Input validation
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Student Name");
            return;
        }
        if (txtRoll.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Roll Number");
            return;
        }
        if (!male.isSelected() && !female.isSelected()) {
            JOptionPane.showMessageDialog(this, "Please select Gender");
            return;
        }
        if (txtBranch.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Branch");
            return;
        }
        if (!terms.isSelected()) {
            JOptionPane.showMessageDialog(this, "Please accept Terms & Conditions");
            return;
        }

        String gender = male.isSelected() ? "Male" : "Female";
        if (saveRegistration(txtName.getText().trim(), txtRoll.getText().trim(), gender, txtBranch.getText().trim())) {
            JOptionPane.showMessageDialog(this,
                    "Registration Successful\n\n"
                            + "Name : " + txtName.getText()
                            + "\nRoll Number : " + txtRoll.getText()
                            + "\nGender : " + gender
                            + "\nBranch : " + txtBranch.getText());
            resetForm();
        }
    }

    private boolean saveRegistration(String name, String roll, String gender, String branch) {
        String insertSql = "INSERT INTO streg (name, roll, gender, branch) VALUES (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(insertSql)) {

            statement.setString(1, name);
            statement.setString(2, roll);
            statement.setString(3, gender);
            statement.setString(4, branch);
            statement.executeUpdate();
            return true;

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return false;
        }
    }

    private void resetForm() {
        txtName.setText("");
        txtRoll.setText("");
        txtBranch.setText("");
        genderGroup.clearSelection();
        terms.setSelected(false);
    }

    public static void main(String[] args) {
        // Run the GUI on the Event Dispatch Thread for thread safety
        SwingUtilities.invokeLater(() -> new StudentRegistration());
    }
}