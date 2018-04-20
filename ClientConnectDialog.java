package cardGame;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;


public class ClientConnectDialog extends JDialog implements ActionListener {
	/*
	 * caller accessible values
	 */
	boolean dialogStatus = false;	// true if connect fields have been set or ok-ed by user; false otherwise
	String serverName = "";
	String gameName = "";
	String portId = "";
	String userName = "";
	String userPW = "";
	
	private JTextField textField;
	private JTextField textUserField;
	private JTextField textPWField;
	private JComboBox<String> gameBox;
	private JComboBox<String> portBox;
	
	static String games[]= {
			"hearts",
			"spades"
		};
	static String ports[]= {
			"1081"
			};
	final String CANCELBUTTON_LABEL = "Cancel";
	final String OKBUTTON_LABEL = "OK";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			System.out.println("Dialog test.");
			ClientConnectDialog dialog = new ClientConnectDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			//ModalityType.APPLICATION_MODAL
			dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
			ModalityType type = dialog.getModalityType();
			dialog.setVisible(true);
			//dialog.wait();
			if (dialog.dialogStatus) {
				System.out.println(
						"User: " + dialog.userName +
						" PW: " + dialog.userPW +
						" Connecting to " + dialog.serverName + 
						" Game: " + dialog.gameName +
						" Port: " + dialog.portId);
				}
			else {
				System.out.println("User canceled open...");
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
	
		/*
		 * Which button was pressed
		 */
		String command=ev.getActionCommand();
		/*
		 * user has set or affirmed connection values and ok-eyed dialog 
		 */
		if (command.equals(OKBUTTON_LABEL)) {
			System.out.println("User pressed" + OKBUTTON_LABEL);
			// set dialog values
			dialogStatus = true;
			userName = textUserField.getText();
			userPW = textPWField.getText();
			serverName = textField.getText();
			gameName = (String) gameBox.getSelectedItem();
			portId = (String) portBox.getSelectedItem();
			this.setVisible(false);
			}
		else if (command.equals(CANCELBUTTON_LABEL) ) {	// user canceled out
			System.out.println("User pressed" + CANCELBUTTON_LABEL);
			dialogStatus = false;
			this.setVisible(false);
			}
		
		}

	/**
	 * Create the dialog.
	 */
	public ClientConnectDialog() {
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(null);
		{
			this.setTitle("Connect to Game Server");
			JPanel buttonPane = new JPanel();
			buttonPane.setBounds(0, 228, 434, 33);
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane);
			{
				JButton okButton = new JButton(OKBUTTON_LABEL);
				okButton.setActionCommand(OKBUTTON_LABEL);
				okButton.addActionListener(this);

				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton(CANCELBUTTON_LABEL);
				cancelButton.setActionCommand(CANCELBUTTON_LABEL);
				cancelButton.addActionListener(this);

				buttonPane.add(cancelButton);
			}
		}
		/*
		 * username and password
		 */
		JLabel lblUsername = new JLabel("Username");	// Label Username
		lblUsername.setBounds(57, 35, 98, 14);
		getContentPane().add(lblUsername);
		
		textUserField = new JTextField();
		textUserField.setBounds(120, 35, 98, 20);
		textUserField.setText("Buzz");			// Text Username
		getContentPane().add(textUserField);
		textUserField.setColumns(10);

		JLabel lblPassword = new JLabel("Password");	// Label Password
		lblPassword.setBounds(57, 60, 98, 14);
		getContentPane().add(lblPassword);
		
		textPWField = new JTextField();
		textPWField.setBounds(120, 60, 98, 20);
		textPWField.setText("Framitz");			// Text Password
		getContentPane().add(textPWField);
		textUserField.setColumns(10);
				
		// Windowbuilder code below
		int spacingOffset=20;
		JLabel lblNewLabel = new JLabel("Connect to Host");	// Left
		lblNewLabel.setBounds(57, 88+spacingOffset, 98, 14);
		getContentPane().add(lblNewLabel);
		
		textField = new JTextField();
		textField.setBounds(57, 114+spacingOffset, 98, 20);
		textField.setText("Localhost");			// Default host
		getContentPane().add(textField);
		textField.setColumns(10);
		
				
		//JComboBox<String> 
		gameBox = new JComboBox<String>(games);
		gameBox.setBounds(165, 114+spacingOffset, 100, 20);
		//gameBox.addItem("hearts");
		//gameBox.addItem("spades");

		getContentPane().add(gameBox);
		
		JLabel lblNewLabel_1 = new JLabel("Select Game");	// Middle
		lblNewLabel_1.setBounds(165, 88+spacingOffset, 85, 14);
		getContentPane().add(lblNewLabel_1);
		

		//JComboBox<String> 
		portBox = new JComboBox<String>(ports);
		//portBox.addItem("1081");
		portBox.setBounds(320, 114+spacingOffset, 68, 20);
		getContentPane().add(portBox);
		
		JLabel lblNewLabel_2 = new JLabel("Port");			// Right
		lblNewLabel_2.setBounds(320, 88+spacingOffset, 46, 14);
		getContentPane().add(lblNewLabel_2);
	}
}
