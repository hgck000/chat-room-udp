package client;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;

import task.VoiceChat;
import static javax.swing.text.StyledEditorKit.*;
import java.awt.event.FocusListener;
import java.util.EventListener;



public class EditToolBar extends JToolBar implements ActionListener, CaretListener, FocusListener{

	JTextPane editor;
	protected JFileChooser fileChooser;
	protected AbstractButton bold,italic,colors,pictures,underline;
	protected JComboBox fonts,sizes;
	private Container ancestor; 
	protected static String BOLD="In đậm";
	protected static String ITALIC="In nghiêng";
	protected static String UNDERLINE="Gạch chân";
	protected static String FONT="Font chữ";
	protected static String SIZE="Cỡ chữ";
	protected static String PICTURE="Chèn ảnh";
	protected static String COLOR="Đổi màu chữ";
	
	/**
	 * Method EditToolBar
	 *
	 *
	 */
	public EditToolBar(JTextPane editor) {
		
		super(HORIZONTAL);
		this.editor=editor;
//		this.setOpaque(false);
		createAndAddItems();                        
		editor.addCaretListener(this);					
//		editor.addFocusListener(this);
		fileChooser=new JFileChooser();
		fileChooser.addChoosableFileFilter(new PictureFilter());
	}
	
	protected void createAndAddItems(){
		//font chữ
		fonts=new JComboBox(getAllFonts());
		fonts.setFocusable(false);
		fonts.setMaximumSize(new Dimension(120,20));
		fonts.setMinimumSize(new Dimension(120,20));
		fonts.setPreferredSize(new Dimension(120,20));
		fonts.addActionListener(this);
		fonts.setToolTipText(FONT);
		add(fonts);
		//cỡ chữ
		sizes=new JComboBox(getFontSizeArray());
		sizes.setFocusable(false);
		sizes.setMaximumSize(new Dimension(40,20));
		sizes.setMinimumSize(new Dimension(40,20));
		sizes.addActionListener(this);
		sizes.setToolTipText(SIZE);
		add(sizes);

		//In đậm
				Action action=new StyledAction("",new BoldAction(),editor);
				bold=new JToggleButton(action);
				bold.setIcon(createIcon("B.png"));
				bold.setFocusable(false);
				bold.setToolTipText(BOLD);
				add(bold);
				//In nghiêng
				action=new StyledAction("",new ItalicAction(),editor);
				italic=new JToggleButton(action);
				italic.setIcon(createIcon("I.png"));
				italic.setFocusable(false);
				italic.setToolTipText(ITALIC);
				add(italic);
				//Gạch chân
				action=new StyledAction("",new UnderlineAction(),editor);
				underline=new JToggleButton(action);
				underline.setIcon(createIcon("U.png"));
				underline.setFocusable(false);
				underline.setToolTipText(UNDERLINE);
				add(underline);
				//Màu
				colors=new JButton(createIcon("setting.png"));
				colors.setFocusable(false);
				colors.addActionListener(this);
				colors.setToolTipText(COLOR);
				add(colors);
				//Chèn ảnh	
				pictures=new JButton(createIcon("pic.png"));
				pictures.setFocusable(false);
				pictures.addActionListener(this);
				pictures.setToolTipText(PICTURE);
				add(pictures);
				updateItems(editor.getInputAttributes());
				
	}
	//Lấy font chữ
	protected String[] getAllFonts(){
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	}
	//Lấy cỡ chữ
	protected Integer[] getFontSizeArray(){
		int length=50;
		Integer[] array=new Integer[length];
		for(int i=0;i<length;i++){
			array[i]=6+i;
		}
		return array;
	}
	
//	protected Icon createPictureIcon(File file)throws Exception{
//		Icon icon=null;
//		try{
//			java.net.URL url=file.toURL();
//			icon=new ImageIcon(url);
//		}catch(OutOfMemoryError er){
//			throw new Exception(er.getMessage());
//		}
//		if(icon.getIconWidth()<=0||icon.getIconHeight()<=0)
//			throw new Exception("File không hợp lệ，vui lòng chọn đúng định dạng！");
//		return icon;
//	}
	protected Icon createPictureIcon(File file) throws Exception {
	    // Đặt kích thước cố định cho hình ảnh
	    int targetWidth = 100;  // Chiều rộng cố định (bạn có thể chỉnh sửa giá trị này)
	    int targetHeight = 100; // Chiều cao cố định (bạn có thể chỉnh sửa giá trị này)
	    ImageIcon icon = new ImageIcon(file.getAbsolutePath());
	    
	    // Kiểm tra xem ảnh có hợp lệ không
	    if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
	        throw new Exception("File không hợp lệ，vui lòng chọn đúng định dạng！");
	    }

	    // Chỉnh kích thước ảnh
	    Image scaledImage = icon.getImage().getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
	    return new ImageIcon(scaledImage);
	}
	
	protected static Icon createIcon(String path){
		java.net.URL url=EditToolBar.class.getResource(path);
		if(url!=null){
			ImageIcon icon = new ImageIcon(url);
	        
			Image scaledImage = icon.getImage().getScaledInstance(27, 27, Image.SCALE_SMOOTH);
	        return new ImageIcon(scaledImage);
//			return new ImageIcon(url);
		}else{
			System.err.println("Không tìm thấy file:"+path);
			return null;
		}
	}

	/**
	 * Method actionPerformed
	 *
	 *
	 * @param e
	 *
	 */
	public void actionPerformed(ActionEvent e) {
		
		Object source=e.getSource();
		if(source==colors){
			Color initColor=StyleConstants.getForeground(editor.getInputAttributes());
			Color color=JColorChooser.showDialog(EditToolBar.this,
												"Chọn màu",
												initColor);
			if(color!=null){
				new StyledAction(null,new ForegroundAction("color",color),editor).actionPerformed(e);
			}	
		}else if(source==fonts){
			String fontName=(String)fonts.getSelectedItem();
			new StyledAction(null,new FontFamilyAction(fontName,fontName),editor).actionPerformed(e);
		}else if(source==sizes){
			Integer choicSize=(Integer)sizes.getSelectedItem();
			new StyledAction(null,new FontSizeAction(null,choicSize),editor).actionPerformed(e);
		}else if(source==pictures){
			int returnVal=fileChooser.showOpenDialog(getAncestor());
			if(returnVal==JFileChooser.APPROVE_OPTION){
				try{
//					editor.insertIcon(createPictureIcon(fileChooser.getSelectedFile()));
//					editor.insertComponent(new JLabel());
					Icon pictureIcon = createPictureIcon(fileChooser.getSelectedFile());
		            editor.insertIcon(pictureIcon);
				}catch(Throwable ex){
					System.err.println(ex.getMessage());
					JOptionPane.showMessageDialog(getAncestor(),ex.getMessage());
				}
			}
		}
	}

	/**
	 * Method caretUpdate
	 *
	 *
	 * @param e
	 *
	 */
	public void caretUpdate(CaretEvent e) {
		
		AttributeSet atts=editor.getInputAttributes();
		int offset=editor.getCaretPosition();
//		System.out.println("offset"+offset);
		atts=editor.getStyledDocument().getCharacterElement(offset>0?offset-1:offset).getAttributes();
		updateItems(atts);
	}
	

	protected void updateItems(AttributeSet atts){

		fonts.removeActionListener(this);
		sizes.removeActionListener(this);
		
		bold.setSelected(StyleConstants.isBold(atts));
		italic.setSelected(StyleConstants.isItalic(atts));
		underline.setSelected(StyleConstants.isUnderline(atts));
		fonts.setSelectedItem(StyleConstants.getFontFamily(atts));
		sizes.setSelectedItem(StyleConstants.getFontSize(atts));
		
		fonts.addActionListener(this);
		sizes.addActionListener(this);
	}
	

	protected Container getAncestor(){
		if(ancestor==null){
			ancestor=getParent();
			while(ancestor.getParent()!=null)
				ancestor=ancestor.getParent();
		}
		return ancestor;
	}


	public void focusGained(FocusEvent e) {
		
		bold.setEnabled(true);
		italic.setEnabled(true);
		underline.setEnabled(true);
		colors.setEnabled(true);
		fonts.setEnabled(true);
		sizes.setEnabled(true);
	}

	
	public void focusLost(FocusEvent e) {
		bold.setEnabled(false);
		italic.setEnabled(false);
		underline.setEnabled(false);
		colors.setEnabled(false);
		fonts.setEnabled(false);
		sizes.setEnabled(false);
	}
	

	class StyledAction extends StyledTextAction{
		protected StyledTextAction action;
		protected JEditorPane editor; 
		public StyledAction(String mn,StyledTextAction action,JEditorPane editor){
			super(mn);
			this.action=action;
			this.editor=editor;
		}
		
		public void actionPerformed(ActionEvent e){
			if(getEditor(e)==editor){
				action.actionPerformed(e);
			}
		}
	}
	

	class PictureFilter extends javax.swing.filechooser.FileFilter{
		private java.util.List<String> list;
		private String description="Tệp ảnh"; 
	
		public PictureFilter() {
			list=new java.util.LinkedList<String>();
//			for(String ext:javax.imageio.ImageIO.getReaderFormatNames()){
//				addExt(ext);
//			}
			addExt("gif");
			addExt("jpg");
			addExt("png");
			addExt("jfif");
		}
		
		public void addExt(String ext){
			list.add(ext);
		}

		public boolean accept(File file) {
			if(file.isDirectory())
				return true;
			String fileName=file.getName();
			int index=fileName.lastIndexOf('.');
			if(index>0&&index<fileName.length()-1){
				String extension=fileName.substring(index+1);
				for(String ext:list){
					if(extension.equalsIgnoreCase(ext))
						return true;
				}
			}
			return false;	
		}
	
		public String getDescription(){
			StringBuffer buf=new StringBuffer(description);
			buf.append("(");
			for(String ext:list){
				buf.append("*."+ext+",");
			}
			buf.deleteCharAt(buf.length()-1);
			buf.append(")");
			return buf.toString();	
		}
	}
}
