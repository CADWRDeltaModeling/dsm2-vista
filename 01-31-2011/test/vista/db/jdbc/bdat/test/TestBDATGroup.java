package vista.db.jdbc.bdat.test;

import vista.app.GroupFrame;
import vista.db.jdbc.bdat.BDATGroup;
import junit.framework.TestCase;

public class TestBDATGroup extends TestCase{
	public void testGetGroup(){
		BDATGroup g = new BDATGroup();
		int count = g.getNumberOfDataReferences();
		GroupFrame gf = new GroupFrame(g);
		assertTrue(count > 1000);
	}
}
