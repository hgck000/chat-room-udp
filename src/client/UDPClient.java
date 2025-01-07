package client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Observable;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultStyledDocument;

import task.Information;
import task.Setting;
import task.VoiceChat;



public class UDPClient extends Observable implements ActionListener {

	protected JFrame frame;
	protected UDPClientModel model;
	private JLabel label;
	private JTextPane editor;
	protected String name;
	protected String remoteName;
	protected SocketAddress remoteAddress;

	/**
	 * Method UDPClient
	 * 
	 * 
	 */
	public UDPClient(UDPClientModel model, String name) {
		this.model = model;
		this.name = name;
		label = new JLabel();

		frame = new JFrame();
		
		editor = new JTextPane();
		editor.setPreferredSize(new Dimension(350, 180));
		JScrollPane editorPane = new JScrollPane(editor);
		editorPane.setOpaque(false);
		
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPane.setOpaque(false);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
				ActionEvent.CTRL_MASK, true);
		Setting.createButton("Giọng nói(V)", 'C', "voice", stroke, buttonPane, this);
		Setting.createButton("Tệp(F)", 'C', "file", stroke, buttonPane, this);
		Setting.createButton("Đóng(C)", 'C', "close", null, buttonPane, this);
		Setting.createButton("Gửi(S)", 'C', "send", stroke, buttonPane, this);

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBackground(Setting.color1);
		contentPane.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
		contentPane.add(new EditToolBar(editor), BorderLayout.NORTH);
		contentPane.add(editorPane, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.SOUTH);
		frame.setContentPane(contentPane);
		frame.pack();
	}

	public void setRemoteSymbol(String name, SocketAddress address) {
		remoteName = name;
		remoteAddress = address;
		frame.setTitle(remoteName + " - Cuộc trò chuyện");
	}

	
	public void showIn(Component owner) {
		frame.setLocationRelativeTo(owner);
		frame.show();
	}

	
	protected boolean send() throws java.io.IOException {
		DefaultStyledDocument doc = (DefaultStyledDocument) editor
				.getStyledDocument();
		if (doc.getLength() == 0) {
			JOptionPane.showMessageDialog(frame, "Vui lòng nhập nội dung!");
			return false;
		} else {
			Information info = new Information(Information.MESSAGE, name, doc);
			model.send(info, remoteAddress);
			setChanged();
			notifyObservers(new Information(Information.MESSAGE, remoteName,
					doc));
			editor.setDocument(editor.getEditorKit().createDefaultDocument());
			return true;
		}
	}

	/**
	 * Method actionPerformed
	 * 
	 * @param e
	 * 
	 */
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();
		if (command.equals("close")) {
			frame.dispose();
		} else if (command.equals("send")) {
			try {
				if (send()) {
					frame.dispose();
				}
			} catch (Exception ie) {
				ie.printStackTrace();
			}

		} // In UDPClient.java, update the file transfer code:

else if (command == "file") {
    JFileChooser jf = new JFileChooser();
    jf.setPreferredSize(new Dimension(400, 300));
    int returnVal = jf.showOpenDialog(jf);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
        final String myfile = jf.getSelectedFile().getPath();
        final String filename = jf.getSelectedFile().getName();    
        Thread thread = new Thread() {
            public void run() {
                ServerSocket serverSocket = null;
                Socket clientSocket = null;
                FileInputStream fis = null;
                OutputStream netOut = null;
                OutputStream bufferedOut = null;
                
                try {
                    File file = new File(myfile);
                    fis = new FileInputStream(file);
                    
                    // Create server socket with timeout
                    serverSocket = new ServerSocket(3108);
                    serverSocket.setSoTimeout(10000); // 10 second timeout
                    
                    // Wait for client connection
                    clientSocket = serverSocket.accept();
                    
                    // Get output streams
                    netOut = clientSocket.getOutputStream();
                    bufferedOut = new DataOutputStream(
                            new BufferedOutputStream(netOut));
                    
                    // Transfer file
                    byte[] buf = new byte[2048];
                    int num;
                    while ((num = fis.read(buf)) != -1) {
                        bufferedOut.write(buf, 0, num);
                        bufferedOut.flush();
                    }
                    
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(frame, 
                        "Lỗi khi gửi file: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    // Close all resources in reverse order
                    try {
                        if (bufferedOut != null) bufferedOut.close();
                        if (netOut != null) netOut.close();
                        if (fis != null) fis.close();
                        if (clientSocket != null) clientSocket.close();
                        if (serverSocket != null) serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
        
        // Send file information to receiver
        try {
            Information info = new Information(Information.FILE, name, filename);
            model.send(info, remoteAddress);
            setChanged();
            notifyObservers(new Information(Information.FILE, remoteName, filename));
        } catch (IOException e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(frame,
                "Lỗi khi gửi thông tin file: " + e1.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}else if(command == "voice"){
			int response = JOptionPane.showConfirmDialog(null,
					   "Bạn có chắc muốn gửi tin nhắn thoại không？",
					   "Tin nhắn thoại",
					   JOptionPane.YES_NO_OPTION,
					   JOptionPane.QUESTION_MESSAGE);
			if (response==JOptionPane.YES_OPTION) {	
				
				Thread thread = new Thread(){
					@Override
					public void run() {
						String remotesString = remoteAddress.toString();
						int a = remotesString.indexOf(":");
						String ip = remotesString.substring(1, a);
						System.out.println(ip+"+++++++"+remoteAddress.toString());
						
						VoiceChat vChat = new VoiceChat(ip);								
					}
				};
				thread.start();
				{	
					Information info = new Information(Information.VOICE, name, "VOICE");
					try {
						model.send(info, remoteAddress);
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					setChanged();
					notifyObservers(new Information(Information.VOICE, remoteName,"VOICE"));
				}
				//thread.start();
			}else if (response==JOptionPane.NO_OPTION) {
				this.frame.dispose();
			}	
		}
		
	}
}
