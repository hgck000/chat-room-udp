package task;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Receiver extends Thread {
	private Socket socket;
	private BlockingQueue queue;
	private ObjectInputStream in;


	public Receiver(Socket socket,BlockingQueue queue)
											throws IOException{
		this.socket=socket;
		this.queue=queue;
		in=new ObjectInputStream(socket.getInputStream());
	}

	/**
	 * Method run
	 *
	 *
	 */
	public void run(){
		try{
			while(true){
				try{
					Object object=in.readObject();
					queue.put(object);
				}catch(InterruptedException e){
					e.printStackTrace();
				}catch(ClassNotFoundException e){
					e.printStackTrace();
				}
			}
		}catch(IOException e){
		}finally{	
			try{
				in.close();
				queue.put(socket);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}	
}