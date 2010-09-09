package vista.db.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import vista.set.DataReference;
import vista.set.DataSet;
import vista.set.IrregularTimeSeries;

/**
 * A reference that uses a relational table to store a time series data set
 * @author psandhu
 *
 */
public class JDBCDataReference extends DataReference {

	private static final String DRIVER_CLASSNAME = "driver class name here";
	private static final String DRIVER_URL = "driver url here";
	private String id;

	public JDBCDataReference(String id){
		this.id=id;
	}
	@Override
	protected DataReference createClone() {
		return new JDBCDataReference(this.id);
	}

	@Override
	public DataSet getData() {
		double[] x = new double[1];
		double[] y = new double[1];
		//TODO: Retrieve time values from table here
		// select x_column, y_column from table_named_id
		//
		try{
		Class.forName(DRIVER_CLASSNAME);
		} catch(ClassNotFoundException ex){
			ex.printStackTrace();
			return null;
		}
		try {
			Driver driver = DriverManager.getDriver(DRIVER_URL);
			String url = "jdbc:pgsql:tsdata";
			Properties info = new Properties();
			Connection connect = driver.connect(url, info);
			String sql = "select x,y,flag from timeseries_"+id;
			PreparedStatement prepareStatement = connect.prepareStatement(sql);
			ResultSet rs = prepareStatement.executeQuery();
			int index=0;
			while(rs.next()){
				float x1 = rs.getFloat(0);
				float y1 = rs.getFloat(1);
				int flag = rs.getInt(2);
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return new IrregularTimeSeries(this.id, x, y);
	}

	@Override
	public void reloadData() {
		// non-operation for now
	}

}
