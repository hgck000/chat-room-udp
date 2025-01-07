package task;

import java.io.Serializable;


public class Information implements Serializable {
	
	public int type;
	public String source;
	public Object content;
	public static final int ENTER=1;
	public static final int EXIT=2;
	public static final int MESSAGE=3;
	public static final int FILE=4;
	public static final int VOICE=5;

	public Information(int type,String source,Object content) {		
		this.type=type;
		this.source=source;
		this.content=content;
	}
}
