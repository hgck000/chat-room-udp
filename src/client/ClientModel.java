package client;

import task.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.AbstractListModel;


public class ClientModel {
	
	protected Socket socket;
	protected String name;
	protected AbstractListModel listModel;
	
	private BlockingQueue sendQueue,receiveQueue;
	private Vector<String> nameList; 
	private Map<String,InetSocketAddress> name_address_map;
	
	/**
	 * Method ClientModel
	 *
	 *
	 */
	public ClientModel(String ip,int port)
			throws IOException,UnknownHostException {
		
		socket=new Socket(ip,port);
		sendQueue=new LinkedBlockingQueue();
		receiveQueue=new LinkedBlockingQueue();
		nameList=new Vector<String>();
		name_address_map=new HashMap<String,InetSocketAddress>();
	}

	/**
	 *Checkign user
	 */
	
	public boolean validate(String name) throws IOException {
		
		DataOutputStream dos=new DataOutputStream(socket.getOutputStream());
		DataInputStream dis=new DataInputStream(socket.getInputStream());
		dos.writeUTF(name);
		if(dis.readBoolean()){
			this.name=name;
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 *Nhận tên người dùng
	 */
	public String getName(){
		return name;
	}
	

	public Vector<String> getNames() throws Exception{
		Vector<String> v=new Vector<String>();
		ObjectInputStream dis=new ObjectInputStream(socket.getInputStream());
		int size=dis.readInt();
		for(int i=0;i<size;i++){
			String _name=dis.readUTF();
			InetSocketAddress address=(InetSocketAddress)dis.readObject();
			nameList.add(_name);
			name_address_map.put(_name,address);
			v.add(_name);
		}
		v.add(0,v.remove(v.size()-1));
		return v;
	}
	
	
	public void start(){
		try{
			new Receiver(socket,receiveQueue).start();
			new Sender(socket,sendQueue).start();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	public int getLocalPort(){
		return socket.getLocalPort();
	}
	

	public SocketAddress getAddress(String name){
		return name_address_map.get(name);
	}

	public Information getMessage(){
		Information message=null;
		try{
			Object object=receiveQueue.take();
			if(object instanceof Information){
				message=(Information)object;
				if(message.type==message.ENTER){
					if(!nameList.contains(message.source)){
						nameList.add(message.source);
						name_address_map.put(message.source,(InetSocketAddress)message.content);
					}
				}else if(message.type==message.EXIT){
					nameList.remove(message.source);
					name_address_map.remove(message.source);
				}
			}else if(object instanceof Socket){
				System.out.println("Mất kết nối với máy chủ");
				message=new Information(Information.EXIT,Setting.SERVER,null);
			}	
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		return message;
	}
	

	public boolean putMessage(DefaultStyledDocument doc){
		Information info=new Information(Information.MESSAGE,name,doc);
		try{
			sendQueue.put(info);
			return true;
		}catch(InterruptedException e){
			e.printStackTrace();
			return false;
		}
	}
	
	public AbstractListModel getListModel(){
		if(listModel==null){
			listModel=new AbstractListModel(){
				public int getSize(){
					return nameList.size();
				}
				
				public Object getElementAt(int index){
					return nameList.get(index);
				}
			};
		}
		return listModel;
	}
}
