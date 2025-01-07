package client;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

//import org.omg.CORBA.Current;

import task.DBBean;
/**
 * 
 * @author lpm
 *
 */
public class RegistPanel extends JFrame implements ActionListener{
	JLabel namelab,pswd1lab,pswd2lab;
	JTextField tfname,tfpswd1,tfpswd2;
	JButton okbutton,nobutton;
	Color lightblue=new Color(214, 238, 226);
	
	
	void buildConstraints(GridBagConstraints gbc,int gx,int gy,int gw,int gh,int wx,int wy){
		gbc.gridx = gx;
		gbc.gridy = gy;
		gbc.gridwidth = gw;
		gbc.gridheight = gh;
		gbc.weightx = wx;
		gbc.weighty = wy;
	}
	
	public RegistPanel(){			
		
		GridBagLayout gridbag = new GridBagLayout();		
		JPanel pane = new JPanel();
		pane.setLayout(gridbag);
		pane.setBackground(lightblue);
		GridBagConstraints constraints = new GridBagConstraints();	
		//namelabel
		
		
		buildConstraints(constraints,0,1,1,1,10,20);
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.EAST;
		namelab = new JLabel("Tên:",JLabel.LEFT);
		gridbag.setConstraints(namelab,constraints);
		pane.add(namelab);
		//nametext
		buildConstraints(constraints,1,1,1,1,40,20);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		tfname = new JTextField();
		gridbag.setConstraints(tfname,constraints);
		pane.add(tfname);
		//password1 label
		buildConstraints(constraints,2,1,1,1,10,20);
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.EAST;
		pswd1lab = new JLabel("Mật khẩu:",JLabel.LEFT);
		gridbag.setConstraints(pswd1lab,constraints);
		pane.add(pswd1lab);	
		//password1 text
		buildConstraints(constraints,3,1,1,1,40,20);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		tfpswd1 = new JTextField();
		gridbag.setConstraints(tfpswd1,constraints);
		pane.add(tfpswd1);
		//password2 label
		buildConstraints(constraints,2,2,1,1,0,20);
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.EAST;
		pswd2lab = new JLabel("Nhập lại mật khẩu:",JLabel.LEFT);
		gridbag.setConstraints(pswd2lab,constraints);
		pane.add(pswd2lab);
		//password2 text
		buildConstraints(constraints,3,2,1,1,0,20);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		tfpswd2 = new JTextField();
		gridbag.setConstraints(tfpswd2,constraints);
		pane.add(tfpswd2);
		//ok button
		buildConstraints(constraints,1,4,1,1,0,0);
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.CENTER;
		okbutton = new JButton("OK");
		okbutton.setBackground(lightblue);
		okbutton.addActionListener(this);
		gridbag.setConstraints(okbutton,constraints);
		pane.add(okbutton);	
		//no button
		buildConstraints(constraints,3,4,1,1,0,0);
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.CENTER;
		nobutton = new JButton("Thoát");
		nobutton.setBackground(lightblue);
		nobutton.addActionListener(this);
		gridbag.setConstraints(nobutton,constraints);
		pane.add(nobutton);
		setContentPane(pane);																
	}
	
	public void actionPerformed(ActionEvent e){
		if(e.getActionCommand()=="OK"&&check()==true){ 				
            DBBean bean = new DBBean();
    		bean.init();
    		//String sql = "insert into T_user values('"+(int)(Math.random()*1000)+"','"+tfname.getText()+"','"+tfpswd1.getText()+"','"+"helloword@qq.com"+"','"+5+"')";    		
    		String sql = "insert into user values('"+tfname.getText()+"','"+tfpswd1.getText()+"')";
    		System.out.println(sql);
    		if (bean.executeinsert(sql)) {
    			 String string="Đăng ký thành công！\nTên của bạn:"+tfname.getText()+"\nMật khẩu của bạn:"+tfpswd1.getText();
    	         JOptionPane.showMessageDialog(null,string, "!yes!", 1);
			}else {
				System.out.println("Đã xảy ra lỗi！");
				JOptionPane.showMessageDialog(null,"Đăng ký thất bại！", "!sorry!", 1);
			}
            bean.colse();
            cancle();
	    }
	    else if(e.getActionCommand()=="Thoát"){
	    	
			cancle();	    	
	    }
    }
	
    boolean check(){
		if(tfname.getText().equals("")){
			JOptionPane.showMessageDialog(this,"Vui lòng nhập tên","Error",JOptionPane.INFORMATION_MESSAGE);
			return(false);
		}	
		else if(!tfpswd1.getText().equals(tfpswd2.getText())){
			JOptionPane.showMessageDialog(this,"Mật khẩu không khớp! Nhập lại!","Error",JOptionPane.INFORMATION_MESSAGE); 
			return(false);
		}
		else
		    return(true);   	
    }
    void cancle(){
    	System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
    	this.dispose();
    }
//    public static void main(String[] args){
//    	RegistPanel panel = new RegistPanel();
//    	panel.show();
//    }
}
