package server;

import task.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
import java.util.logging.*;

public class Server extends JFrame implements ActionListener {

	protected Logger logger = Logger.getLogger("server");// Trình xây dựng nhật ký
	private ServerModel model;// Mô hình dữ liệu máy chủ
	JTabbedPane tabbedPane;
	private JTextPane logArea;// Khu vực hiển thị nhật ký
	private JTable table;// Danh sách người dùng trực tuyến
	private JButton startButton, closeButton, exitButton;// Các nút Start, exit, close
	private JFormattedTextField portField, sizeField;

	/**
	 * Method main
	 * 
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		createAndShowGUI();
	}

	/**
	 * ethod Server
	 * 
	 * 
	 */
	public Server() {
		super("Máy chủ");
		model = new ServerModel();
		buildUI();
		configureLogging();
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
	}

	/**
	 * GUI
	 */
	private void buildUI() {
		Container contentPane = getContentPane();
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("Nhật ký", createLogPane());
		tabbedPane.addTab("Thông tin", createParamPane());
		tabbedPane.addTab("Danh sách người dùng", createUserPane());
		tabbedPane.setSelectedIndex(1);
		tabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		contentPane.add(createButtonPane(), BorderLayout.SOUTH);
	}

	/**
	 * Tạo bảng điều khiển nhật ký
	 */
	private Container createLogPane() {
		logArea = new JTextPane();
		logArea.setEditable(false);
		logArea.setBackground(Color.black);
		logArea.setForeground(Color.red);
		logArea.setPreferredSize(new Dimension(600, 400));
		return new JScrollPane(logArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}

	/**
	 * Tạo bảng cài đặt tham số
	 */
	private Container createParamPane() {
		portField = new JFormattedTextField(new Integer(8001));
		sizeField = new JFormattedTextField(new Integer(5));
		portField.setColumns(15);
		JPanel paramPane = new JPanel();
		JPanel c = new JPanel(new SpringLayout());
		c.setBorder(BorderFactory.createEmptyBorder(40, 10, 10, 10));
		addLabel(c, portField, "Cổng máy chủ");
		addLabel(c, sizeField, "Số lượng kết nối tối đa");
		SpringUtilities.makeCompactGrid(c, 2, 2, 10, 10, 10, 10);
		paramPane.add(c);
		return paramPane;
	}

	/**
	 * Tạo bảng điều khiển người dùng
	 */
	private Container createUserPane() {
		// Tạo danh sách người dùng
		table = new JTable(model.getUserTableModel());
		JScrollPane tablePane = new JScrollPane(table);
		// Tạo nút để ngắt kết nối người dùng đã chọn
		Action remove_action = new AbstractAction("Ngắt kết nối người dùng đã chọn") {
			public void actionPerformed(ActionEvent e) {
				int[] selectedRows = table.getSelectedRows();
				try {
					for (int index : selectedRows)
						model.removeUser(index);
				} catch (IOException ie) {
					ie.printStackTrace(System.err);
				}
			}
		};
		// Tạo nút để ngắt kết nối tất cả người dùng
		Action remove_all_action = new AbstractAction("Ngắt kết nối tất cả người dùng") {
			public void actionPerformed(ActionEvent e) {
				int option = JOptionPane.showConfirmDialog(Server.this,
						"Bạn có chắc muốn ngắt kết nối tất cả người dùng？", "Lưu ý", JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION) {
					try {
						for (int i = 0; i < table.getRowCount(); i++) {
							model.removeUser(i);
						}
					} catch (IOException ie) {
						ie.printStackTrace(System.err);
					}
				}
			}
		};
		JButton remove_button = new JButton(remove_action);
		JButton remove_all_button = new JButton(remove_all_action);
		JPanel buttons = new JPanel();
		buttons.add(remove_button);
		buttons.add(remove_all_button);
		// Tạo hộp văn bản để gửi tin nhắn hệ thống
		final JTextField textField = new JTextField();
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Information info = new Information(Information.MESSAGE,
						Setting.SERVER, textField.getText());
				model.sendMessage(info);
				textField.setText("");
			}
		});
		JLabel label = new JLabel("Gửi tin nhắn hệ thống");
		label.setLabelFor(textField);
		JPanel fieldPane = new JPanel(new BorderLayout());
		fieldPane.add(label, BorderLayout.WEST);
		fieldPane.add(textField);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(tablePane);
		panel.add(buttons, BorderLayout.NORTH);
		panel.add(fieldPane, BorderLayout.SOUTH);
		return panel;
	}

	private Container createButtonPane() {
		// Xây dựng nút
		startButton = new JButton("Khởi động");
		closeButton = new JButton("Ngắt");
		exitButton = new JButton("Thoát");
		startButton.addActionListener(this);
		closeButton.addActionListener(this);
		exitButton.addActionListener(this);
		closeButton.setEnabled(false);
		JPanel buttonPane = new JPanel();
		buttonPane.add(startButton);
		buttonPane.add(closeButton);
		buttonPane.add(exitButton);
		return buttonPane;
	}

	/**
	 * Method actionPerformed
	 * 
	 * 
	 * @param e
	 * 
	 */
	public void actionPerformed(ActionEvent e) {
		// TODO: add code
		if (e.getSource() == startButton) {
			int port = (Integer) portField.getValue(), mostConnect = (Integer) sizeField
					.getValue();
			model.setMostConnect(mostConnect);
			try {
				model.startOn(port);
				startButton.setEnabled(false);
				closeButton.setEnabled(true);
				portField.setEditable(false);
				tabbedPane.setSelectedIndex(0);
			} catch (IOException ie) {
				logger.warning(ie.getMessage());
				JOptionPane.showMessageDialog(this, ie.getMessage());
			}
		} else if (e.getSource() == closeButton) {
			if (model.getConnectNumber() != 0) {
				JOptionPane.showMessageDialog(Server.this,
						"Đang có người dùng kết nối với máy chủ, hãy ngắt kết nối người dùng trước！");
				tabbedPane.setSelectedIndex(2);
			} else {
				try {
					model.stop();
					startButton.setEnabled(true);
					closeButton.setEnabled(false);
					portField.setEditable(true);
					logger.info("server closed.");
					tabbedPane.setSelectedIndex(1);
				} catch (Exception ie) {
					ie.printStackTrace();
				}
			}

		} else if (e.getSource() == exitButton) {
			exit();
		}
	}

	public static void createAndShowGUI() {
		JFrame server = new Server();
		server.pack();
		server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		server.show();
	}

	private static void addLabel(Container c, Component tc, String label) {
		JLabel l = new JLabel(label);
		l.setLabelFor(tc);
		c.add(l);
		c.add(tc);
	}

	private void exit() {
		if (model.isRunning()) {
			int option = JOptionPane.showConfirmDialog(this,
					"Máy chủ đang chạy, bạn có muốn thoát không？", "Lưu ý", JOptionPane.YES_NO_OPTION);
			if (option != JOptionPane.YES_OPTION)
				return;
		}
		logger.info("application is exited.");
		System.exit(0);
	}

	/**
	 * Cấu hình ghi nhật ký
	 */
	protected void configureLogging() {
		logger.setUseParentHandlers(false);
		logger.addHandler(new TextPaneHandler(logArea));
		//Cấu hình tệp để ghi nhật ký
		try {
			Handler fileHandle = new FileHandler("server%g.log", 1000000, 2,
					true);
			fileHandle.setFormatter(new InfoFormatter());
			logger.addHandler(fileHandle);
		} catch (IOException e) {
			logger.warning(e.getMessage());
		}
		logger.info("Nhật ký khởi động......\n");
	}
	/**
	 * ghi nhật ký trong JTextPane
	 */
	class TextPaneHandler extends Handler {
		protected JTextPane logArea;
		protected SimpleAttributeSet attributeSet;
		protected SimpleAttributeSet focusAttributeSet;

		public TextPaneHandler(JTextPane textPane) {
			logArea = textPane;
			setFormatter(new InfoFormatter());
			attributeSet = new SimpleAttributeSet();
			focusAttributeSet = new SimpleAttributeSet();
			StyleConstants.setForeground(attributeSet, Color.RED);
			StyleConstants.setFontFamily(attributeSet, "Arial");
			StyleConstants.setFontSize(attributeSet, 14);

			StyleConstants.setForeground(focusAttributeSet, Color.LIGHT_GRAY);
			StyleConstants.setFontFamily(focusAttributeSet, "Arial");
			StyleConstants.setFontSize(focusAttributeSet, 14);

		}

		public void publish(LogRecord record) {
			String info = getFormatter().format(record);
			Document docs = logArea.getDocument();
			try {
				if (record.getLevel().intValue() > Level.INFO.intValue()) {
					docs.insertString(docs.getLength(), info, attributeSet);
				} else {
					docs.insertString(docs.getLength(), info, focusAttributeSet);
				}

				logArea.setCaretPosition(docs.getLength());
			} catch (BadLocationException e) {
				logger.warning(e.getMessage());
			}
		}

		public void close() {
		}

		public void flush() {
		}
	}


	class InfoFormatter extends SimpleFormatter {
		String newline = System.getProperty("line.separator");
		java.text.DateFormat dateFormatter = new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss ");

		public String format(LogRecord record) {
			if (record.getLevel() != Level.INFO) {
				return super.format(record);
			} else {
				Object[] params = record.getParameters();
				String param = "";
				if (params != null)
					param = params[0].toString();
				return dateFormatter.format(new Date()) + param + newline
						+ "Thông tin: " + record.getMessage() + newline;
			}
		}
	}
}