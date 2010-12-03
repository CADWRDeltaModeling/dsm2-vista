package vista.app;
import junit.framework.TestCase;
import vista.app.DataGraph;
import vista.app.DefaultGraphBuilder;
import vista.app.MultiDataTable;
import vista.db.dss.DSSUtil;
import vista.graph.Graph;
import vista.set.DataReference;
import vista.set.DataSetAttr;
import vista.set.DataType;
import vista.set.DefaultReference;
import vista.set.FlagUtils;
import vista.set.RegularTimeSeries;
import vista.set.TimeSeries;
import vista.set.TimeSeriesMergeUtils;

public class TestMerging extends TestCase {
	public RegularTimeSeries createMergeTimeSeries1(){
		double[] values1 = new double[100];
		for (int i = 0; i < values1.length; i++) {
			values1[i] = i;
		}
		int[] flags1 = new int[100];
		for (int i = 0; i < flags1.length; i++) {
			flags1[i] = 0;
		}
		DataSetAttr attr1 = new DataSetAttr(DataType.REGULAR_TIME_SERIES,
				"TIME", "XX", "", "INST-VAL");
		RegularTimeSeries rts1 = new RegularTimeSeries("/TEST/ORIGINAL/FLOW///MERGE/",
				"01JAN1990 0100", "15MIN", values1, flags1, attr1);
		return rts1;
	}
	
	public RegularTimeSeries createMergeTimeSeries2(){
		double[] values2 = new double[100];
		for (int i = 0; i < values2.length; i++) {
			values2[i] = 1000 + i;
		}
		int[] flags2 = new int[100];
		for (int i = 0; i < flags2.length; i++) {
			if (i < 20) {
				flags2[i] = FlagUtils.setFlagTypeAndUserId(flags2[i],
						FlagUtils.OK_FLAG, 0);
			} else if (i < 40) {
				flags2[i] = FlagUtils.setFlagTypeAndUserId(flags2[i],
						FlagUtils.QUESTIONABLE_FLAG, 0);
			} else if (i < 60) {
				flags2[i] = FlagUtils.setFlagTypeAndUserId(flags2[i],
						FlagUtils.REJECT_FLAG, 0);
			} else if (i < 80) {
				flags2[i] = 0;
			}
		}
		DataSetAttr attr2 = new DataSetAttr(DataType.REGULAR_TIME_SERIES,
				"TIME", "XX", "", "INST-VAL");
		RegularTimeSeries rts2 = new RegularTimeSeries("/TEST/REPLACER/FLOW///MERGE/",
				"01JAN1990 0500", "15MIN", values2, flags2, attr2);
		return rts2;
	}
	
	public Graph[] createGraphs(DataReference ... refs){
		DefaultGraphBuilder builder = new DefaultGraphBuilder();
		for(int i=0; i < refs.length; i++){
			builder.addData(refs[i]);
		}
		Graph[] graphs = builder.createGraphs();
		assertNotNull(graphs);
		return graphs;
	}

	public void testMergeRTS() {
		RegularTimeSeries rts1 = createMergeTimeSeries1();
		RegularTimeSeries rts2 = createMergeTimeSeries2();
		Graph[] graphs = createGraphs(DSSUtil.createDataReference("", "", rts1.getName(), rts1),DSSUtil.createDataReference("", "", rts2.getName(), rts2));
		assertNotNull(graphs);
		assertEquals(1, graphs.length);
		DataGraph graph = new DataGraph(graphs[0], true);
		graph.getCanvas();
	}
}
