package vista.db.jdbc;

import vista.set.DataReference;
import vista.set.DataSet;
import vista.set.IrregularTimeSeries;

/**
 * A reference that uses a relational table to store a time series data set
 * @author psandhu
 *
 */
public class JDBCDataReference extends DataReference {

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
		//
		
		return new IrregularTimeSeries(this.id, x, y);
	}

	@Override
	public void reloadData() {
		// non-operation for now
	}

}
