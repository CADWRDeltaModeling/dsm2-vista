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
public class TestPortfolio extends TestCommon{
  boolean DEBUG = false;
  //
  public TestPortfolio(String name){
    super(name);
  }
  // 
  public void testGet(){
    NamedLeaf leaf = portfolio.getNamed("/DTS/my flows/riVerside/dts mixed div");
    assert(leaf.getName().equals("/DTS/MY FLOWS/RIVERSIDE/DTS MIXED DIV"));
  }
  //
  public void testAdd(){
    DerivedTimeSeries dts = new DerivedTimeSeries(dts1);
    String name = dts.getName();
    dts.setName("/DTS/MY FLOWS/"+name);
    portfolio.addNamedLeaf(dts);
    String fullname = "/DTS/MY FLOWS/"+name.toUpperCase().trim();
    NamedLeaf leaf = portfolio.getNamed(fullname);
    assert(leaf != null);
    assert(leaf.getName().equals(fullname));
    assert(Portfolio.getFolderName(leaf).equals("/DTS/MY FLOWS"));
    assert(Portfolio.getLocalName(leaf).equals(name.toUpperCase().trim()));
    Portfolio.setLocalName(leaf,"XYZ");
    assert(Portfolio.getLocalName(leaf).equals("XYZ"));
  }
  //
  public void testSaveLoad() throws IOException, FileNotFoundException{
    portfolio.save("port.xml");
    try {
      portfolio.load("port.xml");
    }catch(RuntimeException re){
      assert(re.getMessage().indexOf("Portfolio already has")>=0);
    }
    portfolio.OVERRIDE_DEFINES = true;
    try{
      portfolio.load("port.xml");
    }catch(RuntimeException re){
      throw new AssertionFailedError("Exception raised: " + re.getMessage());
    }
    //
    Portfolio po = new Portfolio(portfolio.getList()[0]);
    po.load("port.xml");
    assert(po.equals(portfolio));
  }
}
