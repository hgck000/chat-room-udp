package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UTFDataFormatException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import task.Information;
import task.Setting;
import task.VoiceChat;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;

public abstract class ClientUI extends JFrame 
        implements Runnable, ActionListener, Observer {
    
    private ClientModel model;
    private UDPClientModel udpModel;
    private UDPClient udpClient;
    private Vector<String> names;
    private JList nameList;
    private JTextPane sendArea;
    private JTextPane receiveArea;
    protected java.text.SimpleDateFormat format;
    protected String newline=System.getProperty("line.separator");
    protected String name;
    protected SimpleAttributeSet sourceAttribute;
    protected SimpleAttributeSet serverAttribute;

    public ClientUI(ClientModel mod) {
        model=mod;
        try{
            udpModel=new UDPClientModel(model.getLocalPort());
            udpModel.addObserver(this);
        }catch(IOException e){
            e.printStackTrace();
        }
        try{
            names=model.getNames();
        }catch(Exception e){
            e.printStackTrace();
        }
        name=model.getName();
        format=new java.text.SimpleDateFormat("HH:mm:ss");
        
        nameList=new JList(names);
        sendArea=new JTextPane();
        receiveArea=new JTextPane();
        nameList.setCellRenderer(new CellRenderer());

        layoutUI();
        
        addUDPListenning();
        createAttributeSets();
        new Thread(this).start();
        addWindowFocusListener(new WindowAdapter(){
            public void windowGainedFocus(WindowEvent e){
                sendArea.requestFocusInWindow();
            }
        });
    }

    private void layoutUI(){
        nameList.setFixedCellWidth(140);
        nameList.setFixedCellHeight(20);
        
        receiveArea.setEditable(false);
        JScrollPane scrollPane1=new JScrollPane(nameList);
        JScrollPane scrollPane2=new JScrollPane(sendArea);
        JScrollPane scrollPane3=new JScrollPane(receiveArea);
        scrollPane1.setBorder(BorderFactory.createTitledBorder("Danh sách người dùng"));
        scrollPane1.setOpaque(false);
        scrollPane2.setOpaque(false);
        scrollPane3.setOpaque(false);
        
        JPanel work_pane=new JPanel(new BorderLayout()),
               button_pane=new JPanel(new FlowLayout(FlowLayout.RIGHT));
        work_pane.setOpaque(false);
        button_pane.setOpaque(false);
        KeyStroke stroke=KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,ActionEvent.CTRL_MASK,true);
        Setting.createButton("Thoát(E)",'E',"exit",null,button_pane,this);
        Setting.createButton("Gửi(S)",'S',"send",stroke,button_pane,this);
        work_pane.add(new EditToolBar(sendArea),BorderLayout.NORTH);
        work_pane.add(scrollPane2);
        work_pane.add(button_pane,BorderLayout.SOUTH);
        
        setLayout(new BorderLayout());
        JSplitPane sp1=new JSplitPane(JSplitPane.VERTICAL_SPLIT,true,
                                        scrollPane3,work_pane);
        sp1.setResizeWeight(0.75);
        sp1.setPreferredSize(new Dimension(350,400));
        
        sp1.setOpaque(false);

        sp1.setDividerSize(1);
        sp1.setBorder(BorderFactory.createEmptyBorder(18,10,0,0));
        Container contentPane=getContentPane();
        
        contentPane.add(sp1);
        contentPane.add(scrollPane1,BorderLayout.EAST);
        contentPane.setBackground(Setting.color1);
        pack();
    }

    private void addUDPListenning(){
        nameList.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                if(e.getClickCount()==2){
                    String remoteName=(String)nameList.getSelectedValue();
                    if(remoteName.equals(name)){
                        JOptionPane.showMessageDialog(ClientUI.this,
                            "Bạn không thể trò chuyện với chính mình！");
                    }else{
                        if(udpClient==null){
                            udpClient=new UDPClient(udpModel,name);
                            udpClient.addObserver(ClientUI.this);
                        }
                        udpClient.setRemoteSymbol(remoteName,model.getAddress(remoteName));
                        udpClient.showIn(ClientUI.this);
                    }
                }
            }
        });
    }
    
    private void createAttributeSets(){
        sourceAttribute=new SimpleAttributeSet();
        serverAttribute=new SimpleAttributeSet();
        StyleConstants.setForeground(sourceAttribute,Color.blue);
        StyleConstants.setForeground(serverAttribute,new Color(0,128,64));
    }

    public void run(){
        while(true){
            Information info=model.getMessage();
            if(info==null){
                continue;
            }else if(info.type==Information.ENTER){
                if(!names.contains(info.source)){
                    String serverMessage=format.format(new Date())+": "+info.source+" Đã tham gia..."+newline;
                    try{
                        insertMessage(serverMessage,serverAttribute);
                    }catch(BadLocationException e){
                        System.err.println(e.getMessage());
                    }
                    
                    names.add(info.source);
                    nameList.updateUI();
                }
            }else if(info.type==Information.EXIT){
                if(info.source==Setting.SERVER){
                    doWhenStop();
                    break;
                }else{
                    String serverMessage=format.format(new Date())+": "+info.source+" Đã rời đi..."+newline;
                    try{
                        insertMessage(serverMessage,serverAttribute);
                    }catch(BadLocationException e){
                        System.err.println(e.getMessage());
                    }
                    names.remove(info.source);
                    nameList.updateUI();
                }                   
            }else if(info.type==Information.MESSAGE){
                try{
                    if(info.source.equals(Setting.SERVER)){
                        insertMessage(format.format(new Date())+newline+
                            "[Thông báo hệ thống]  "+info.content+newline,serverAttribute);
                    }else{
                        String source=info.source+"  ("+format.format(new Date())+")"+": ";
                        insertMessage(source,sourceAttribute);
                        insertMessage((StyledDocument)info.content);
                    }
                }catch(BadLocationException e){
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        String command=e.getActionCommand();
        if(command.equals("exit")){
            exit();
        }else if(command.equals("send")){
            DefaultStyledDocument doc=(DefaultStyledDocument)sendArea.getStyledDocument();
            if(doc.getLength()==0){
                JOptionPane.showMessageDialog(this,"Vui lòng nhập nội dung！");
            }else{
                model.putMessage(doc);
                sendArea.setDocument(sendArea.getEditorKit().createDefaultDocument());
            }
        }
    }

    protected void insertMessage(String message,SimpleAttributeSet attset)
            throws BadLocationException {
        Document docs=receiveArea.getDocument();
        docs.insertString(docs.getLength(),message,attset);
        receiveArea.setCaretPosition(docs.getLength());
    }

    protected void insertMessage(StyledDocument doc)
            throws BadLocationException {
        
        StyledDocument receive_doc=receiveArea.getStyledDocument();
        int base=receive_doc.getLength();
        String text=doc.getText(0,doc.getLength())+newline;

        receive_doc.insertString(base,text,null);
        LinkedList<Element> list=new LinkedList<Element>();
        for(Element e:doc.getRootElements()){
            Setting.getAllElements(list,e);
        }
        for(Element e:list){
            int offset=base+e.getStartOffset(),
                length=e.getEndOffset()-e.getStartOffset();
            receive_doc.setCharacterAttributes(offset,length,e.getAttributes(),false);
        }
        receiveArea.setCaretPosition(receive_doc.getLength());
    }
    
    protected void exit(){
        int option=JOptionPane.showConfirmDialog(this,"Bạn có chắc chắn muốn thoát không？",
            "Lưu ý",JOptionPane.YES_NO_OPTION);
        if(option==JOptionPane.YES_OPTION)
            System.exit(0);  
    }
                    
    protected abstract void doWhenStop();

    public void update(Observable o, Object object) {
        if(o==udpModel){
            if(object instanceof Information){
                final Information info=(Information)object;
             
                if (info.type == Information.FILE) {
                    Thread thread = new Thread() {
                        public void run() {
                            Socket socket = null;
                            InputStream in = null;
                            RandomAccessFile raf = null;
                            
                            try {
                                // Thông báo đang bắt đầu nhận file
                                String startMessage = format.format(new Date()) + ": Đang nhận file từ " + name + "...\n";
                                insertMessage(startMessage, serverAttribute);

                                // Tạo thư mục
                                String userDirectory = "C:\\Users\\vligh\\Desktop\\Files\\" + info.source;
                                File userDir = new File(userDirectory);
                                if (!userDir.exists()) {
                                    userDir.mkdirs();
                                }

                                // Xử lý tên file
                                String originalFileName = (String) info.content;
                                File file = new File(userDirectory + "\\" + originalFileName);
                                
                                // Thiết lập kết nối
                                socket = new Socket();
                                socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), 3108), 5000); // timeout kết nối 5 giây
                                socket.setSoTimeout(5000); // timeout đọc 5 giây
                                
                                in = socket.getInputStream();
                                raf = new RandomAccessFile(file, "rw");
                                
                                byte[] buffer = new byte[8192]; // Tăng kích thước buffer
                                int bytesRead;
                                long totalBytes = 0;
                                long lastReadTime = System.currentTimeMillis();

                                while (true) {
                                    try {
                                        bytesRead = in.read(buffer);
                                        if (bytesRead == -1) break;
                                        
                                        raf.write(buffer, 0, bytesRead);
                                        totalBytes += bytesRead;
                                        lastReadTime = System.currentTimeMillis();
                                    } catch (SocketTimeoutException e) {
                                        // Kiểm tra xem đã quá lâu kể từ lần đọc cuối cùng chưa
                                        if (System.currentTimeMillis() - lastReadTime > 10000) { // 10 giây
                                            throw new IOException("Không nhận được dữ liệu trong 10 giây, hủy nhận file");
                                        }
                                        continue; // Thử đọc lại nếu chưa quá timeout
                                    }
                                }

                                // Thông báo thành công
                                String successMessage = format.format(new Date()) + ": Đã nhận file thành công từ " + info.source + "\n";
                                insertMessage(successMessage, sourceAttribute);
                                
                                String fileDetails = "Tệp: " + file.getName() + " (" + 
                                                   (totalBytes / 1024) + " KB)\n" +
                                                   "Đường dẫn: " + file.getAbsolutePath() + "\n";
                                insertMessage(fileDetails, serverAttribute);

                            } catch (Exception e) {
                                try {
                                    String errorMessage = "Lỗi khi nhận file: " + e.getMessage() + "\n";
                                    insertMessage(errorMessage, serverAttribute);
                                    e.printStackTrace();
                                } catch (BadLocationException ex) {
                                    ex.printStackTrace();
                                }
                            } finally {
                                // Đóng tất cả các resource
                                try {
                                    if (raf != null) raf.close();
                                    if (in != null) in.close();
                                    if (socket != null) socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    thread.start();
                
                } else if(info.type == Information.VOICE){
                    int response = JOptionPane.showConfirmDialog(null,
                               "Chấp nhận cuộc gọi ？",
                               "Yêu cầu giọng nói",
                               JOptionPane.YES_NO_OPTION,
                               JOptionPane.QUESTION_MESSAGE);
                    if (response==JOptionPane.YES_OPTION) {  
                        Thread thread = new Thread(){
                            @Override
                            public void run() { 
                                String remoteName=(String)nameList.getSelectedValue();
                                String remoteStr=model.getAddress(remoteName).toString();
                                int a = remoteStr.indexOf(":");
                                String ip = remoteStr.substring(1, a);
                                System.out.println(remoteStr+"============"+ip);
                                VoiceChat vc = new VoiceChat(ip);
                            }
                        };
                        thread.start();
                    }else if(response==JOptionPane.NO_OPTION){
						System.out.println("Kết nối gọi không thành công！");
					}
				}else{
					try{
						String source=info.source+"  ("+format.format(new Date())+")  Đã gửi tin nhắn:"+"\n";
						insertMessage(source,sourceAttribute);
						insertMessage((StyledDocument)info.content);
					}catch(BadLocationException e){
						System.err.println(e.getMessage());
					}
				}
				
			}
		}
//		}else if(o==udpClient){
//			Information info=(Information)object;
//			try{
//				String source=format.format(new Date())+": Bạn đã gửi tin nhắn cho "+info.source+newline;
//				insertMessage(source,sourceAttribute);
//				insertMessage((StyledDocument)info.content);
//			}catch(BadLocationException e){
//				System.err.println(e.getMessage());
//			}
//		}
		else if (o == udpClient) {
		    Information info = (Information) object;
		    try {
		        // Chỉ hiển thị thông báo "Bạn đã gửi tin nhắn" nếu info.type là MESSAGE
		        if (info.type == Information.MESSAGE) {
		            String source = format.format(new Date()) + ": Bạn đã gửi tin nhắn cho " + info.source + newline;
		            insertMessage(source, sourceAttribute);
		            insertMessage((StyledDocument) info.content);
		        } 
		        // Nếu info.type là FILE thì không hiển thị thông báo
		        else if (info.type == Information.FILE) {
		            String source = format.format(new Date()) + ": Bạn đã gửi tệp cho " + info.source + newline;
		            insertMessage(source, sourceAttribute); // Có thể hiển thị thông báo khác nếu cần
		           
	                SimpleAttributeSet serverAttribute = new SimpleAttributeSet();
	                StyleConstants.setForeground(serverAttribute, Color.BLUE); // Ví dụ: đổi màu chữ
	                insertMessage("Tệp: " + (String) info.content + "\n", serverAttribute);
		        }
		    } catch (BadLocationException e) {
		        System.err.println(e.getMessage());
		    }
		}
		
	}
		
		protected class CellRenderer extends DefaultListCellRenderer{
		@Override
		public Component getListCellRendererComponent(JList list,
													Object value,
													int index,
													boolean isSelected,
													boolean cellHasFocus){
			super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
			if(value.equals(name))
				setForeground(Color.red);
			return this;												
		}
	}
}
