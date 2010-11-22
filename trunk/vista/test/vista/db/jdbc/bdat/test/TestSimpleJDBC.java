package vista.db.jdbc.bdat.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class TestSimpleJDBC extends TestCase {
	Connection conn;
	private String DRIVER_CLASSNAME = "oracle.jdbc.pool.OracleDataSource";
	private String DRIVER_URL = "jdbc:oracle:thin:@grsbldbe00308.np.water.ca.gov:1522:orcl";
	public void setUp() throws Exception {
		Class.forName(DRIVER_CLASSNAME);
		conn = DriverManager.getConnection(DRIVER_URL, "sandhu", "User123");
	}

	public void tearDown() throws Exception {
		if (conn != null) {
			conn.close();
		}
	}
	
	public void testRetrieveGroup() throws SQLException{
		String sql = "select * from emp_cms.result_detail_view where result_id in (select distinct(result_id) from emp_cms.result_detail_view)";
		ResultSet resultSet = conn.createStatement().executeQuery(sql);
		assertNotNull(resultSet);
		boolean hasSomeData = false;
		while(resultSet.next()){
			hasSomeData = true;
			break;
		}
		assertTrue(hasSomeData);
	}
	
	public static void main(String[] args){
		TestRunner.run(TestSimpleJDBC.class);
	}
}
