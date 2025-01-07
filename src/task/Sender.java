package task;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;


public class Sender extends Thread {

	private BlockingQueue queue;
	private ObjectOutputStream out;

	/**
	 * Method SendServicer
	 *
	 *
	 */
	public Sender(Socket socket,BlockingQueue queue) 
										throws java.io.IOException {
		this.queue=queue;
		out=new ObjectOutputStream(socket.getOutputStream());
	}

	/**
	 * Method run
	 *
	 *
	 */
	public void run(){
		while(true){
			try{
				Object object=queue.take();
				out.writeObject(object);
			}catch(InterruptedException e){
				System.err.println(e.getMessage());
			}catch(java.io.IOException e){
				break;
			}
		}
	}

}

