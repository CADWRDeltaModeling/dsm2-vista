package vista.db.jdbc.test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class TestSimpleJDBC extends TestCase {
	Connection conn;
	private String DRIVER_CLASSNAME = "org.postgresql.Driver";
	private String DRIVER_URL = "jdbc:postgresql://localhost:5432/tsdata";

	public void setUp() throws Exception {
		Class.forName(DRIVER_CLASSNAME);
		conn = DriverManager.getConnection(DRIVER_URL, "nsandhu", "nsandhu");
	}

	public void tearDown() throws Exception {
		if (conn != null) {
			conn.close();
		}
	}
	
	public void testInsert() throws Exception {
		int size = 10000;
		int batchSize = 100;
		boolean batch = true;
		PreparedStatement prepareStatement = conn
				.prepareStatement("insert into sample_ts1(x,y,flag) values (?,?,?)");
		Date d = new Date(2000, 0, 1);
		long time = d.getTime();
		conn.setAutoCommit(false);
		for (int i = 0; i < size; i++) {
			prepareStatement.setFloat(1, i + time);
			prepareStatement.setFloat(2, (float) Math.sin(i / Math.PI));
			prepareStatement.setInt(3, i);
			if (batch) {
				prepareStatement.addBatch();
				if (i % batchSize == 0) {
					prepareStatement.executeBatch();
					conn.commit();
				}
			} else {
				prepareStatement.execute();
				conn.commit();
			}
		}
		if (batch){
			prepareStatement.executeBatch();
		}
		conn.commit();
	}
	
	public void testRetrieve() throws Exception{
		int size=10000;
		conn.setAutoCommit(false);
		String sql = "select x,y,flag from sample_ts1";
		long ti = System.currentTimeMillis();
		//PreparedStatement stmt = conn.prepareStatement("select x,y,flag from sample_ts1 limit 900000");
		Statement stmt = conn.createStatement();
		stmt.setFetchSize(100);
		ResultSet resultSet = stmt.executeQuery(sql);
		int i=0;
		while(resultSet.next()){
			resultSet.getFloat(1);
			resultSet.getFloat(2);
			resultSet.getInt(3);
			i++;
		}
		stmt.close();
		resultSet.close();
		System.out.println("Time taken: "+(System.currentTimeMillis()-ti));
		System.out.println(i);
	}
	
	public static void main(String[] args){
		TestRunner.run(TestSimpleJDBC.class);
	}
}
