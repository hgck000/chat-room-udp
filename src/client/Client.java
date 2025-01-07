package client;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import task.DBBean;

import java.io.*;
import java.net.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Client implements ActionListener{
	private LogonPane logonPane;//Giao diện đăng nhập
	private ClientModel client;//Mô hình dữ liệu client
	private ClientUI clientUI;//Giao diện trò chuyện client
	private JButton enterButton,exitButton,regstButton;//Đăng nhập, đăng ký

	
	private JFrame logonFrame;

	public Client() {

		logonFrame=new JFrame("Đăng nhập");
		Container contentPane=logonFrame.getContentPane();
		logonPane=new LogonPane();
		regstButton=new JButton("Đăng ký");
		enterButton=new JButton("Đăng nhập");
		exitButton=new JButton("Thoát");	
		logonPane.setRelatedButton(enterButton);
		
		regstButton.addActionListener(this);
	
		enterButton.addActionListener(this);
		exitButton.addActionListener(this);
		JPanel controlPane=new JPanel();
		controlPane.add(regstButton);
		controlPane.add(enterButton);
		controlPane.add(exitButton);
		contentPane.add(logonPane,BorderLayout.CENTER);
		contentPane.add(controlPane,BorderLayout.SOUTH);
	}

	public static void main(String[] args) {
		// TODO: 在这添加你的代码
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch(Exception e){
			e.printStackTrace();
		}
		createAndShowGUI();
	}

	
	@SuppressWarnings("deprecation")
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==enterButton){
			
			String ip=logonPane.getIP();
			String name=logonPane.getName();
			String pwd=logonPane.getpsw();
			
			if (isok(name,pwd)) {
				int port;
				//=============================
				try{
					port=logonPane.getPort();
				}catch(NumberFormatException ne){		
					JOptionPane.showMessageDialog(logonFrame,ne.getMessage());
					return;
				}
				//=============================
				try{
					client=new ClientModel(ip,port);			
				}catch(java.net.UnknownHostException ue){		
					JOptionPane.showMessageDialog(logonFrame,"IP máy chủ không tồn tại："+ue.getMessage());
					return;
				}catch(IOException ie){
					JOptionPane.showMessageDialog(logonFrame,ie.getMessage());
					return;
				}
				//=============================
				boolean valid;
				try{
					valid=client.validate(name);       
				}catch(IOException ie){
					JOptionPane.showMessageDialog(logonFrame,"Máy chủ đã đầy, vui lòng thử lại sau！");
					return;
				}
				//=============================
				if(!valid){
					JOptionPane.showMessageDialog(logonFrame,"Tên không hợp lệ hoặc đang được sử dụng："+name);
					return;
				}else{
					clientUI=new ClientUI(client){
						protected void doWhenStop(){
							JOptionPane.showMessageDialog(clientUI,"Kết nối đến máy chủ bị gián đoạn, vui lòng đăng nhập lại!");
							clientUI.dispose();
							logonFrame.show();
						}
					};
					clientUI.setTitle("Phòng trò chuyện của "+client.name);
					clientUI.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					clientUI.setLocationRelativeTo(logonFrame);
					clientUI.show();
					client.start();
					logonFrame.dispose();
				}
				
			}else {
				JOptionPane.showMessageDialog(logonFrame,"Sai tên hoặc mật khẩu：");
			}
			
		}else if(e.getSource()==exitButton){
			System.exit(1);
		}else if (e.getSource()==regstButton) {
			RegistPanel registPanel = new RegistPanel();
			registPanel.setVisible(true);
			registPanel.setSize(300, 200);
			registPanel.show();
		}
	}
	

	protected void exit(){
		int option=JOptionPane.showConfirmDialog(logonFrame,"Đang kết nối với máy chủ, bạn có muốn thoát không？",
			"Thông báo",JOptionPane.YES_NO_OPTION);
		if(option==JOptionPane.YES_OPTION)
			System.exit(0);	
	}
	
	public static void createAndShowGUI(){
		JFrame frame=new Client().logonFrame;
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.show();
	}
	
	private boolean isok(String name,String pwd) {
		DBBean bean = new DBBean();
		bean.init();
		String sql = "select * from user where " +
				"userName='"+name+"'";		
		
		ResultSet rs = bean.executeQuery(sql);
		try {
			if (rs.next()) {
				String dbpwd = rs.getString("userPWD");
				System.out.println("***************"+dbpwd);
				if (pwd.equals(dbpwd)) {
					return true;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;		
	}
}


