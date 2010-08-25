package vista.dm.test;
import vista.dm.*;
import junit.framework.*;
import java.io.*;
import javax.swing.tree.*;
import com.sun.xml.tree.XmlDocument;
import com.sun.xml.tree.TreeWalker;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class TestCommon extends TestCase{
  //
  protected Portfolio portfolio;
  protected DerivedTimeSeries dts1,dts2;
  protected MultipleTimeSeries mts1,mts2;
  protected Study study;
  protected Project project;
  //
  public TestCommon(String name){
    super(name);
  }
  //
  protected MTSRow make_row(String [] parts){
    MTSRow row = new MTSRow(); 
    for(int i=0; i < parts.length; i++) 
      row.setPathPart(parts[i],i);
    return row;
  }
  //
  protected DerivedTimeSeries make_dts(Study study, String name, 
				     MultipleTimeSeries mts, String expr){
    DerivedTimeSeries dts = new DerivedTimeSeries(study);
    dts.setName(name);
    dts.importMTS(mts);
    dts.setExpression(expr);
    dts.setPathname(new String[]{"MY OWN", dts.getName(), "FLOW", "", "", "CALCULATED"});
    return dts;
  }
  //
  protected MultipleTimeSeries make_mts(Study study){
    MultipleTimeSeries mts = new MultipleTimeSeries(study);
    mts.setName("my first mts");
    MTSRow row = 
      make_row(new String[] {"MY BASIN", "RIVERSIDE", "FLOW", "", "1HOUR", "OBS"});
    mts.add(row);
    row = 
      make_row(new String[] {"MY BASIN", "RIVERSIDE", "FLOW", "", "1HOUR", "TEST"});
    mts.add(row);
    return mts;
  }
  //
  protected Study make_study(){
    Study study = new Study("test study");
    study.setFilename("sample.sty");
    study.addDSSFile("../../testdata/sample.dss");
    MultipleTimeSeries mts = make_mts(study);
    //
    portfolio = new Portfolio(make_dts(study,"",make_mts(study),""));
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) portfolio.getRoot();
    portfolio.addFolder("my flows",root,0);
    DefaultMutableTreeNode my_flows = (DefaultMutableTreeNode) portfolio.getChild(root,0);
    portfolio.addFolder("riverside",my_flows,0);
    DefaultMutableTreeNode riverside = (DefaultMutableTreeNode) portfolio.getChild(my_flows,0);
    //
    portfolio.addInFolder(make_dts(study,"dts add",mts,"$1+$2"),my_flows,0);
    portfolio.addInFolder(make_dts(study,"dts sub",mts,"$1- $2"),my_flows,1);
    portfolio.addInFolder(make_dts(study,"dts mul",mts,"$1*$2"),my_flows,2);
    portfolio.addInFolder(make_dts(study,"dts div",mts,"$1/$2"),my_flows,3);
    // mixed rts and scalar arithmetic expressions
    portfolio.addInFolder(make_dts(study,"dts mixed add",mts,"$1+ 5000.5"),riverside,0);
    portfolio.addInFolder(make_dts(study,"dts mixed sub",mts,"$2- 5000"),riverside,1);
    portfolio.addInFolder(make_dts(study,"dts mixed mul",mts,"$1*1.05"),riverside,2);
    portfolio.addInFolder(make_dts(study,"dts mixed div",mts,"$2/1110.33"),riverside,3);
    portfolio.addInFolder(make_dts(study,"dts mixed reverse add",mts,"5000.5+$1"),riverside,4);
    portfolio.addInFolder(make_dts(study,"dts mixed reverse sub",mts,"5000-$2"),riverside,5);
    portfolio.addInFolder(make_dts(study,"dts mixed reverse mul",mts,"1.05*$1"),riverside,6);
    portfolio.addInFolder(make_dts(study,"dts mixed reverse div",mts,"1110.33/$2"),riverside,7);
    // mixed rts and scalar arithmetic expressions
    portfolio.addInFolder(make_dts(study,"dts per avg function",mts,"peravg($1,'1mon')"),riverside,8);
    portfolio.addInFolder(make_dts(study,"dts per max function",mts,"permax($1+$2,'1day')"),
			  riverside,9);
    portfolio.addInFolder(make_dts(study,"dts per min function",mts,"permin($1,'1mon)"),riverside,10);
    //
    study.setDTSPortfolio(portfolio);
    return study;
  }
  //
  protected void setUp(){
    study = make_study();
    dts1 = new DerivedTimeSeries(study);
    dts1.setName("My dts");
    dts1.setPathname(new String[]{"A","B","C","","","F"});
    dts2 = new DerivedTimeSeries(study);
    dts2.setName("xml/test");
    dts2.setExpression("$1-$2*$3");
    dts2.importMTS( make_mts(study));
    //
    project = new Project();
    project.setName("my test project");
  }
  //
  protected void tearDown(){
  }
}
