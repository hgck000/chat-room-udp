package task;

import java.sql.*;

public class DBBean {
	
	
     String DBDrive =  "com.mysql.jdbc.Driver"; 
     String DBUrl = "jdbc:mysql://localhost:3306/user_info"; 
     String DBUser = "root";
     String DBPassword="";
     ResultSet rs = null;
     Connection conn = null;
     Statement stmt = null;
     
     public boolean init(String driveName,String sqlUrl,String sqlUser,String sqlPass){
    	 DBDrive = driveName;
    	 DBUrl = sqlUrl;
    	 DBUser = sqlUser;
    	 DBPassword = sqlPass;
    	 return init();
    	 
     }
     public boolean init(){
    	 try{
    	     Class.forName(DBDrive);
    		 return true;
    	 }catch(Exception e){
    		 System.out.println("Khởi tạo không thành công！");
    		 e.printStackTrace();
    		 return false;
    	 }
     }
     
     public ResultSet executeQuery(String sql){
    	 rs = null;
    	 try {
			conn = DriverManager.getConnection(DBUrl, DBUser, DBPassword);
			stmt = conn.createStatement();
			rs=stmt.executeQuery(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Không thể kết nối đến cơ sở dữ liệu");
			e.printStackTrace();
		}
    	 return rs;
     }
     
     public boolean executeinsert(String sql){
    	 try {
			conn = DriverManager.getConnection(DBUrl, DBUser, DBPassword);
			stmt = conn.createStatement();
			stmt.execute(sql);
			return true;
		} catch (SQLException e) {
			System.out.println("Đã xảy ra lỗi khi thực hiện câu lệnh cập nhật");
			e.printStackTrace();
			return false;
		}			
    	
     }
       
     public boolean executeUpdate(String sql){
    	 try {
 			conn = DriverManager.getConnection(DBUrl, DBUser, DBPassword);
 			stmt = conn.createStatement();
 			stmt.executeUpdate(sql);
 			return true;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			System.out.println("Đã xảy ra lỗi khi thực hiện câu lệnh cập nhật");
 			e.printStackTrace();
 			return false;
 		}
 		
     }
       
     public boolean colse(){
    	 try{
    		 if(rs!=null){
    			 rs.close();
    		 }
    		 if(stmt!=null){
    		     stmt.close();
    		 }
    		 if(conn!=null){
    			 conn.close();
    		 }
    		 return true;
    	 }catch(SQLException e){
    		 System.out.println("Đóng không thành công");
    		 e.printStackTrace();
    		 return false;
    	 }
     }
}
