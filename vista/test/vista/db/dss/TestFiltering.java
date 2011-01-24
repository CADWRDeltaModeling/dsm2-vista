package vista.db.dss;

import vista.set.DataReference;
import vista.set.Group;
import junit.framework.TestCase;

public class TestFiltering extends TestCase{

	public void testFind(){
		Group group = DSSUtil.createGroup("local", "scripts/testdata/file1.dss");
		DataReference[] matches = group.find(new String[]{"VISTA-EX1"});
		assertNotNull(matches);
		assertEquals(2,matches.length);
		matches = group.find(new String[]{"VISTA-EX1","COS"});
		assertNotNull(matches);
		assertEquals(1,matches.length);
		// these strings can be regular patterns
		matches = group.find(new String[]{".*","COS.*"});
		assertNotNull(matches);
		assertEquals(2,matches.length);
		// you just have to specify upto the pathname parts you need
		matches = group.find(new String[]{".*"});
		assertNotNull(matches);
		assertEquals(4,matches.length);
	}
}
