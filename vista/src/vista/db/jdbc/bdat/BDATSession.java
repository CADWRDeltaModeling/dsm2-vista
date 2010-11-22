package vista.db.jdbc.bdat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import oracle.jdbc.pool.OracleDataSource;

public class BDATSession {
	
	private String user;
	private String serverName;
	private int portNumber;
	private String password;
	private String databaseName;

	public BDATSession(){
		//jdbc:oracle:thin:@grsbldbe00308.np.water.ca.gov:1522:orcl
		Properties props = new Properties();
		String propsFile = System.getProperty("user.home")+System.getProperty("file.separator")+"vista.jdbc.properties";
		try{
			props.load(new FileInputStream(propsFile));
		}catch(Exception ex){
			System.err.println("Could not load connection info from "+propsFile+" ; using defaults");
		}
		user = props.getProperty("db.user","user");
		password = props.getProperty("db.password","password");
		serverName = props.getProperty("db.servername","server");
		portNumber = Integer.parseInt(props.getProperty("db.portnumber","1000"));
		databaseName = props.getProperty("db.databaseName","orcl");
	}
	
	public Connection getConnection() throws SQLException{
		OracleDataSource ds = new OracleDataSource();
		ds.setUser(user);
		ds.setPassword(password);
		ds.setServerName(serverName);
		ds.setPortNumber(portNumber);
		ds.setDatabaseName(databaseName);
		ds.setURL("jdbc:oracle:thin:@"+serverName+":"+portNumber+":"+databaseName);
		return ds.getConnection();
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}
}
