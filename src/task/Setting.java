package task;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import javax.swing.text.Element;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.Container;
import java.awt.event.ActionListener;



public class Setting {
	
	/**
	 *UI Color
	 */
	public static java.awt.Color color1=new java.awt.Color(115,186,255);
	
	/**
	 *Color
	 */
	public static java.awt.Color color2=new java.awt.Color(115,255,186);
	
	/**
	 *Color
	 */
	public static java.awt.Color color3=new java.awt.Color(160,188,242); 


	public static final String SERVER="server";
	


	protected static String[] invalidName={
		"","true","false","yes","no","server","client","enter","exit","to"
	};
	


	protected static char[] invalidChar={
		'\\','/','.',':','(',')','[',']','-',
	};
	


	protected static Set<String> invalidNameSet=new HashSet<String>();
	
	static{
		for(int i=0;i<invalidName.length;i++)
			invalidNameSet.add(invalidName[i]);
	}
	
	public static void main(String[] args){
		System.out.println(isValidName("   "));
	}
	


	public static boolean isValidName(String name) {
		for(int i=0;i<invalidChar.length;i++){
			if(name.indexOf(invalidChar[i])!=-1)
				return false;
		}
		return !invalidNameSet.contains(name.trim());	
	}
	


	public static void getAllElements(List<Element> list,Element root){
		if(root.isLeaf()){
			list.add(root);
		}else{
			for(int i=0;i<root.getElementCount();i++)
				getAllElements(list,root.getElement(i));
		}
	}
	


	public static JButton createButton(String text,int mn,String command,
			KeyStroke stroke,Container c,ActionListener als){
		JButton button=new JButton(text);
		button.setMnemonic(mn);
		button.setActionCommand(command);
		button.addActionListener(als);
		button.registerKeyboardAction(als,command,stroke,JComponent.WHEN_IN_FOCUSED_WINDOW);
		button.setFocusable(false);
		c.add(button);
		return button;
	}	
}
