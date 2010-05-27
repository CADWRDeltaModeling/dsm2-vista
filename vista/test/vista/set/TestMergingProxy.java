package vista.set;


public class TestMergingProxy extends BaseTestCase{
	public void testMergeSimple(){
		
	}
	
	public void testMergeNonOverlapping(){
		DataReference[] refsToBeMerged = new DataReference[]{ref, ref_non_overlapping};
		MergingProxy proxy = new MergingProxy(refsToBeMerged);
		DataSet ds = proxy.getData();
		assertNotNull(ds);
		assertTrue(ds instanceof RegularTimeSeries);
		RegularTimeSeries rtsMerged = (RegularTimeSeries) ds;
		assertEquals(((TimeSeries)ref.getData()).getStartTime(), rtsMerged.getStartTime());
		assertEquals(((TimeSeries)ref_non_overlapping.getData()).getEndTime(), rtsMerged.getEndTime());
	}
	
	public void testMergeWithFlags(){
		
	}
	
	public void testMergeSortOrderPriority(){
		
	}
}
