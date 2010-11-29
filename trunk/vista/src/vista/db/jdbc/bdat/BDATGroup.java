package vista.db.jdbc.bdat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import vista.set.Group;
import vista.set.GroupProxy;

@SuppressWarnings("serial")
public class BDATGroup extends GroupProxy {
	private BDATSession manager;
	public BDATGroup(){
		manager = new BDATSession();
		setName("BDAT::"+manager.getDatabaseName()+"@"+manager.getServerName()+":"+manager.getPortNumber()+" as "+manager.getUser());
	}
	@Override
	protected Group getInitializedGroup() {
		Group g = new Group();
		Connection connection = null;
		try {
			// FIXME: get password should be on manager ui
			connection = manager.getConnection();
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement
					.executeQuery("select * from emp_cms.result_detail_view where result_id in (select distinct(result_id) from emp_cms.result_detail_view)");
			while (resultSet.next()) {
				int resultId = resultSet.getInt("result_id");
				String abbreviation = resultSet.getString("abbreviation");
				String constituentName = resultSet.getString("constituent_name");
				String aggregateName = resultSet.getString("aggregate_name");
				String intervalName = resultSet.getString("interval_name");
				String readingTypeName = resultSet.getString("reading_type_name");
				String rankName = resultSet.getString("rank_name");
				String probeDepth = resultSet.getString("probe_depth");
				Date startDate = resultSet.getDate("start_date");
				Date endDate = resultSet.getDate("end_date");
				//System.out.println("Creating ref : "+resultId+","+abbreviation+","+constituentName+","+startDate+"-"+endDate);
				g.addDataReference(new BDATDataReference(resultId, abbreviation, constituentName, aggregateName, intervalName, readingTypeName, rankName, probeDepth, startDate, endDate));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return g;
	}

}
