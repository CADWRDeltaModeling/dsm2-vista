package vista.dm.test;
import vista.dm.*;
import vista.set.*;
import junit.framework.*;
import java.io.*;
import javax.swing.tree.*;
import com.sun.xml.tree.XmlDocument;
import com.sun.xml.tree.TreeWalker;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
public class TestStudy extends TestCommon{
  boolean DEBUG = false;
  //
  public TestStudy(String name){
    super(name);
  }
  // 
  public void testName(){
    if (DEBUG) System.out.println("study name: " + study.getName());
    assert("TEST STUDY".equals(study.getName()));
  }
  //
  public void testFilename(){
    assert("sample.sty".equals(study.getFilename()));
  }
  //
  public void testDSS(){
    study.addDSSFile("../../testdata/BlueRiver.DSS");
    String [] dssfiles = study.getDSSFiles();
    assert(dssfiles.length == 2);
    assert(dssfiles[0].equals("../../testdata/sample.dss"));
    assert(dssfiles[1].equals("../../testdata/BlueRiver.DSS"));
    //
    study.removeAllDSSFiles();
    assert(study.getDSSFiles() == null);
    //
    study.addDSSFile("../../testdata/BlueRiver.DSS");
    study.addDSSFile("../../testdata/sample.dss");
    dssfiles = study.getDSSFiles();
    assert(dssfiles.length == 2);
    assert(dssfiles[0].equals("../../testdata/BlueRiver.DSS"));
    assert(dssfiles[1].equals("../../testdata/sample.dss"));
    //
    study.removeDSSFile("../../testdata/sample.dss");
    assert(study.getDSSFiles().length == 1);
    assert(study.getDSSFiles()[0].equals("../../testdata/BlueRiver.DSS"));
    //
    study.insertDSSFile("../../testdata/sample.dss",0);
    assert(study.getDSSFiles().length == 2);
    assert(study.getDSSFiles()[0].equals("../../testdata/sample.dss"));
    assert(study.getDSSFiles()[1].equals("../../testdata/BlueRiver.DSS"));
    //
    assert(study.isModified());
  }
  //
  public void testReference(){
    study.MULTIPLE_MATCH_ERROR = false;
    if(DEBUG) System.out.println("osname " + System.getProperty("os.name"));
    if(DEBUG) System.out.println("vista.home" + System.getProperty("vista.home"));
    // check match
    DataReference ref = study.getMatchingReference(new String [] { "","RIVERSIDE","","","",""});
    if(DEBUG) System.out.println("RIVERSIDE MATCH = " + ref.toString());
    assert(ref.getPathname().getPart(Pathname.B_PART).equals("RIVERSIDE"));
    // check exact match
    ref = study.getMatchingReference(new String [] { "","RIVER","","","",""});
    assert(ref == null);
    //
    study.MULTIPLE_MATCH_ERROR = true;
    try {
      ref = study.getMatchingReference(new String [] { "","RIVERSIDE","","","",""});
    }catch(RuntimeException e){
      assert(e.getMessage().indexOf("Multiple matches") >= 0);
    }
  }
  //
  public void testLoadSave() throws IOException,FileNotFoundException{
    study.setFilename("study1.sty");
    assert(study.isModified());
    study.save();
    assert(!study.isModified());
    study.save("study2.sty");
    assert(study.getFilename().equals("study2.sty"));
    Study sty1 = new Study("new study");
    sty1.load("study1.sty");
    assert(sty1.getFilename().equals("study1.sty"));
    assert(sty1.getName().equals(study.getName()));
    String [] dssfiles = sty1.getDSSFiles();
    for(int i=0; i < dssfiles.length; i++)
      assert(dssfiles[i].equals(study.getDSSFiles()[i]));
    assert(sty1.getDTSPortfolio().equals(study.getDTSPortfolio()));
    assert(sty1.getMTSPortfolio().equals(study.getMTSPortfolio()));
  }
}
