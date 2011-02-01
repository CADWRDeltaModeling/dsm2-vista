package vista.app;

import vista.db.dss.DSSUtil;
import vista.set.DataReference;
import vista.set.DataSetAttr;
import vista.set.DataType;
import vista.set.RegularTimeSeries;
import junit.framework.TestCase;

public class TestDataTable extends TestCase{
	public void testRTS(){
		double[] data = new double[100];
		for(int i=0; i < data.length; i++){
			data[i]=i;
		}
		DataSetAttr attr = new DataSetAttr(DataType.REGULAR_TIME_SERIES, "TIME", "CFS", "", "INST-VAL");
		RegularTimeSeries rts = new RegularTimeSeries("/A/B/C//15MIN/F/","01JAN2000 0100", "15MIN", data, null, attr);
		DataReference ref = DSSUtil.createDataReference("", "", "/AP/BP/CP///FP/", rts);
		DataTable dataTable = new DataTable(ref);
		dataTable.show();
	}
}
