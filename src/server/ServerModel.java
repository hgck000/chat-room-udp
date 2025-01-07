package server;

import task.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import java.util.concurrent.*;
import javax.swing.text.*;
import javax.swing.table.AbstractTableModel;


public class ServerModel implements Runnable {

	protected Logger logger = Logger.getLogger("server.model");
	protected boolean running;
	protected int port;	protected int mostConnect = 50;
	protected AbstractTableModel userTableModel;
	private ServerSocket server;
	private BlockingQueue queue;


	private List<Socket> socketList;
	private Map<Socket, BlockingQueue> socket_queue_map;
	private Map<Socket, String> socket_name_map;
	private Map<Socket, Date> socket_date_map;

	/**
	 * Method main
	 * 
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		
		new ServerModel();
	}

	/**
	 * Method ServerModel
	 */
	public ServerModel() {
		
		queue = new LinkedBlockingQueue();
		socketList = new LinkedList<Socket>();
		socket_queue_map = new HashMap<Socket, BlockingQueue>();
		socket_name_map = new HashMap<Socket, String>();
		socket_date_map = new HashMap<Socket, Date>();
	}



	public void startOn(int port) throws IOException {
		this.port = port;
		server = new ServerSocket(port);
		logger.info("starting server on port:" + port + "\n");
		running = true;
		new Thread(this).start();
		transact();
	}



	public void stop() throws IOException {
		server.close();
		running = false;
	}



	public boolean isRunning() {
		return running;
	}



	public int getConnectNumber() {
		return socketList.size();
	}



	public void setMostConnect(int mostConnect) {
		this.mostConnect = mostConnect;
	}



	public AbstractTableModel getUserTableModel() {
		if (userTableModel == null) {
			userTableModel = new AbstractTableModel() {
				protected String[] columnNames = { "Tên người dùng", "ip", "Cổng", "Thời gian đăng nhập" };
				public int getRowCount() {
					return socketList.size();
				}

				public int getColumnCount() {
					return columnNames.length;
				}

				public String getColumnName(int column) {
					return columnNames[column];
				}

				public Object getValueAt(int row, int column) {
					switch (column) {
					case 0:
						return socket_name_map.get(socketList.get(row));
					case 1:
						return socketList.get(row).getInetAddress();
					case 2:
						return socketList.get(row).getPort();
					case 3:
						return socket_date_map.get(socketList.get(row));
					default:
						return null;
					}
				}

				public boolean isCellEditable(int row, int column) {
					return false;
				}
			};
		}
		return userTableModel;
	}


	public void removeUser(int index) throws IOException {
		Socket socket = socketList.get(index);
		socket.close();
	}



	public void sendMessage(Information info) {
		try {
			queue.put(info);
			logger.info(info.content.toString());
		} catch (InterruptedException e) {
			logger.warning(e.getMessage());
		}

	}



	public void run() {
		while (!server.isClosed()) {
			try {
				Socket socket = server.accept();
				if (socket_queue_map.size() >= mostConnect) {
					socket.close();
				} else if (testName(socket)) {
					logger.info(socket_name_map.get(socket)
							+ socket.getRemoteSocketAddress() + " Đã kết nối..." + "\n");
					sendNames(socket);
					new Sender(socket, socket_queue_map.get(socket)).start();
					new Receiver(socket, queue).start();
				}
			} catch (IOException e) {
				logger.warning(e.getMessage());
			}
		}
	}



	private boolean testName(Socket socket) throws IOException {

		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		String name = dis.readUTF();				
		Collection<String> names = socket_name_map.values();
		boolean valid = Setting.isValidName(name) && !names.contains(name);
		dos.writeBoolean(valid);
		if (valid) {
			Information info = new Information(Information.ENTER, name,
					socket.getRemoteSocketAddress());
			try {
				queue.put(info);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
			addUser(socket, name);
		} else {
			dos.close();
			dis.close();
			socket.close();
		}
		return valid;
	}



	protected void addUser(Socket socket, String name) {
		socketList.add(socket);
		socket_queue_map.put(socket, new SynchronousQueue());
		socket_name_map.put(socket, name);
		socket_date_map.put(socket, new Date());
		if (userTableModel != null)
			userTableModel.fireTableDataChanged();
	}



	protected void removeUser(Socket socket) {
		socketList.remove(socket);
		socket_queue_map.remove(socket);
		socket_name_map.remove(socket);
		socket_date_map.remove(socket);
		if (userTableModel != null)
			userTableModel.fireTableDataChanged();
	}



	private void sendNames(Socket socket) throws IOException {
		ObjectOutputStream dos = new ObjectOutputStream(socket.getOutputStream());

		int size = socketList.size();
		dos.writeInt(size);
		for (int i = 0; i < size; i++) {
			Socket s = socketList.get(i);
			String name = socket_name_map.get(s);
			dos.writeUTF(name);
			
			dos.writeObject(s.getRemoteSocketAddress());
		}
	}



	private void transact() {
		new Thread(new Runnable() {
			public void run() {
				while (running) {
					Information info;
					try {
						Object object = queue.take();
						if (object instanceof Information) {
							
							info = (Information) object;
							if (info.type == Information.MESSAGE) {
								if (info.content instanceof StyledDocument) {
									StyledDocument doc = (StyledDocument) info.content;
									try {
										logger.log(
												Level.INFO,
												doc.getText(0, doc.getLength()),
												info.source);
									} catch (BadLocationException be) {
										logger.warning(be.getMessage());
									}
								}
							}
						} else if (object instanceof Socket) {
							Socket socket = (Socket) object;
							logger.info(socket_name_map.get(socket)
									+ socket.getRemoteSocketAddress() + " Đã thoát..." +"\n");
//							logger.info(socket.getRemoteSocketAddress()+ " Đã thoát...");
							String name = socket_name_map.get(socket);
							info = new Information(Information.EXIT, name, null);
							removeUser(socket);
						} else {
							continue;
						}

						
						Iterator<Map.Entry<Socket, BlockingQueue>> i = socket_queue_map
								.entrySet().iterator();
						while (i.hasNext()) {
							Map.Entry<Socket, BlockingQueue> entry = i.next();
							entry.getValue().put(info);
						}
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
}
