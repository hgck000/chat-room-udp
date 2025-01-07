package client;

import javax.swing.*;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.EventListener;


public class LogonPane extends JPanel implements ActionListener{
	
	private ClientModel client;
	private JTextField nameField;
	private JTextField ipField;
	private JTextField portField;
	private JTextField pswFiled;
	private JButton relatedButton=null;

	/**
	 * Method LogonPane
	 *
	 *
	 */
	public LogonPane() {
		
		super(new SpringLayout());
		ipField=addLabeledField(this,"IP máy chủ:",this);
		portField=addLabeledField(this,"Cổng máy chủ:",this);
		nameField=addLabeledField(this,"Tên người dùng:",this);
		pswFiled=addLabeledField(this, "Mật khẩu:", this);
		
		ipField.setText("192.168.190.185");
		portField.setText("8001");
		task.SpringUtilities.makeCompactGrid(this,
                                        4, 2,			 //rows, cols
                                        10, 10,        //initX, initY
                                        6, 10);       //xPad, yPad
                   
    }                                    
		
	public String getIP(){
		return ipField.getText();
	}
	
	public int getPort() throws NumberFormatException{
		return Integer.parseInt(portField.getText());
	}
	
	public String getName(){
		return nameField.getText();
	}
	
	public String getpsw(){
		return pswFiled.getText();
		
	}
		
	public void setRelatedButton(JButton button){
		relatedButton=button;
	}

	protected static JTextField addLabeledField(Container c,String label,ActionListener als){
		JLabel l=new JLabel(label);
		c.add(l);
		JTextField field=new JTextField(15);
		field.addActionListener(als);
		l.setLabelFor(field);
		c.add(field);
		return field;
	}

	/**
	 * Method actionPerformed
	 *
	 *
	 * @param e
	 *
	 */
	public void actionPerformed(ActionEvent e) {
		Object source=e.getSource();
		if(source==ipField){
			portField.grabFocus();
			portField.selectAll();
		}else if(source==portField){
			nameField.grabFocus();
			nameField.selectAll();
		}else if(source==nameField){
			if(relatedButton!=null)
				relatedButton.doClick();
		}
	}
	
	
}
