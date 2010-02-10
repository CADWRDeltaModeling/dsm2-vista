package vista.dm.test;
import vista.dm.*;
import vista.set.*;
import vista.time.*;
import junit.framework.*;
import java.io.*;
import javax.swing.tree.*;
import com.sun.xml.tree.XmlDocument;
import com.sun.xml.tree.TreeWalker;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class TestDTS extends TestCommon{
  public static boolean DEBUG = false;
  //
  public TestDTS(String name){
    super(name);
  }
  // 
  public void testName(){
    // should change to upper case
    assert("MY DTS".equals(dts1.getName()));
    assert(!dts2.equals(dts1));
  }
  //
  public void testClone() throws IOException{
    DerivedTimeSeries dts = (DerivedTimeSeries) dts1.createClone();
    assert(dts.getName().equals(dts1.getName()));
    assert(dts.getStudy() == dts1.getStudy());
    if (DEBUG){
      XmlDocument xdoc = new XmlDocument();
      xdoc.appendChild(xdoc.createElement("root"));
      dts.toXml(xdoc,xdoc.getDocumentElement());
      dts1.toXml(xdoc,xdoc.getDocumentElement());
      xdoc.write(System.out);
      System.out.println("dts mts: " + dts.getMTS());
      System.out.println("dts1 mts: " + dts1.getMTS());
    }
    assert(dts.getMTS().getStudy()==dts1.getMTS().getStudy());
    assert(dts.getMTS().equals(dts1.getMTS()));
    assert(dts.getExpression().equals(dts1.getExpression()));
    assert(dts.getPathname().equals(dts1.getPathname()));
    assert(dts.equals(dts1));
    dts = new DerivedTimeSeries(dts1);
    assert(dts.equals(dts1));
  }
  //
  public void testEquals(){
    DerivedTimeSeries dts = new DerivedTimeSeries(dts1);
    assert(dts.getName().equals(dts1.getName()));
    assert(dts.getStudy() == dts1.getStudy());
    //    System.out.println("dts mts name " + dts.getMTS().getName());
    //    System.out.println("dts1 mts name " + dts1.getMTS().getName());
    assert(dts.getMTS().getName().equals(dts1.getMTS().getName()));
    assert(dts.getExpression().equals(dts1.getExpression()));
    assert(dts.getPathname().equals(dts1.getPathname()));
  }
  // 
  public void testImportMTS(){
    DerivedTimeSeries dts = new DerivedTimeSeries(dts1.getStudy());
    dts.importMTS(dts1.getMTS());
    assert(dts.getMTS() != dts1.getMTS());
    assert(dts.getMTS().equals(dts1.getMTS()));
  }
  //
  public void testToString(){
    DerivedTimeSeries dts = new DerivedTimeSeries(dts1.getStudy());
    dts.setName("/DTS/XYZ/KKK/JJJ");
    assert(dts.toString().equals("JJJ"));
    dts.setName("/DTS/XYZ/KKK/JJJ/");
    assert(!dts.toString().equals("JJJ"));
    assert(dts2.toString().equals("TEST"));
  }
  //
  public void testXml() throws IOException, FileNotFoundException, SAXException{
    // write to xml
    XmlDocument doc = new XmlDocument();
    doc.appendChild(doc.createElement("root"));
    dts1.toXml(doc,doc.getDocumentElement());
    dts2.toXml(doc,doc.getDocumentElement());
    FileWriter wr = new FileWriter("test.xml");
    doc.write(wr);
    wr.close();
    //
    FileInputStream fis = new FileInputStream("test.xml");
    XmlDocument xdoc2 = XmlDocument.createXmlDocument(fis,false);
    TreeWalker tw = new TreeWalker(xdoc2.getDocumentElement());
    // element 1
    Element el = tw.getNextElement(dts1.getXmlTagName());
    assert(el!=null);
    DerivedTimeSeries dtsxml1 = new DerivedTimeSeries(dts1.getStudy());
    dtsxml1.fromXml(el);
    // element 2
    el = tw.getNextElement(dts1.getXmlTagName());
    assert(el!=null);
    DerivedTimeSeries dtsxml2 = new DerivedTimeSeries(dts1.getStudy());
    dtsxml2.fromXml(el);
    //
    XmlDocument doc2 = new XmlDocument();
    doc2.appendChild(doc2.createElement("root"));
    dtsxml1.toXml(doc2,doc2.getDocumentElement());
    dtsxml2.toXml(doc2,doc2.getDocumentElement());
    FileWriter wr2 = new FileWriter("test2.xml");
    doc2.write(wr2);
    wr2.close();
    //
    assert(dtsxml1.equals(dts1));
    assert(dtsxml2.equals(dts2));
  }
  //
  public void testCalc(){
    Portfolio po = study.getDTSPortfolio();
    DataReference ref1 = study.getMatchingReference(new String[] {"MY BASIN", "RIVERSIDE", "FLOW", "", "1HOUR", "OBS"});
    DataReference ref2 = study.getMatchingReference(new String[] {"MY BASIN", "RIVERSIDE", "FLOW", "", "1HOUR", "TEST"});
    RegularTimeSeries ds1 = (RegularTimeSeries) ref1.getData(); 
    RegularTimeSeries ds2 = (RegularTimeSeries) ref2.getData();
    //
    String [] names = new String []{
      "/DTS/MY FLOWS/DTS ADD",
	"/DTS/MY FLOWS/DTS SUB",
	"/DTS/MY FLOWS/DTS MUL",
	"/DTS/MY FLOWS/DTS DIV",
	"/DTS/MY FLOWS/RIVERSIDE/DTS MIXED ADD",
	"/DTS/MY FLOWS/RIVERSIDE/DTS MIXED SUB",
	"/DTS/MY FLOWS/RIVERSIDE/DTS MIXED MUL",
	"/DTS/MY FLOWS/RIVERSIDE/DTS MIXED DIV",
	"/DTS/MY FLOWS/RIVERSIDE/DTS MIXED REVERSE ADD",
	"/DTS/MY FLOWS/RIVERSIDE/DTS MIXED REVERSE SUB",
	"/DTS/MY FLOWS/RIVERSIDE/DTS MIXED REVERSE MUL",
	"/DTS/MY FLOWS/RIVERSIDE/DTS MIXED REVERSE DIV",
	"/DTS/MY FLOWS/RIVERSIDE/DTS PER AVG FUNCTION",
	"/DTS/MY FLOWS/RIVERSIDE/DTS PER MAX FUNCTION",
	"/DTS/MY FLOWS/RIVERSIDE/DTS PER MIN FUNCTION"
	};
    for(int i=0; i < names.length; i++){
      DerivedTimeSeries dts = (DerivedTimeSeries) po.getNamed(names[i]);
      RegularTimeSeries rts = (RegularTimeSeries) dts.getData();
      //
      switch(i){
      case 0: ;
      case 1: ;
      case 2: ;
      case 3: ;
	TimeWindow tw = ds1.getTimeWindow().intersection(ds2.getTimeWindow());
	assert(tw.equals(rts.getTimeWindow()));
	break;
      case 4:
      case 6:
	assert(rts.getTimeWindow().equals(ds1.getTimeWindow()));
	break;
      case 5:
      case 7:
	assert(rts.getTimeWindow().equals(ds2.getTimeWindow()));
	break;
      case 12:
      case 14:
	assert(rts.getTimeWindow().equals(ds1.getTimeWindow()));
	assert(rts.getTimeInterval().equals(TimeFactory.getInstance().createTimeInterval("1mon")));
	break;
      case 13:
	tw = ds1.getTimeWindow().intersection(ds2.getTimeWindow());
	assert(tw.equals(rts.getTimeWindow()));
	assert(rts.getTimeInterval().equals(TimeFactory.getInstance().createTimeInterval("1day")));
	break;
      }
      //
      TimeSeries [] ts = new TimeSeries[3];
      ts[0] = (TimeSeries) ds1;
      ts[1] = (TimeSeries) ds2;
      ts[2] = (TimeSeries) rts;
      MultiIterator mi = new MultiIterator(ts,Constants.DEFAULT_FLAG_FILTER);
      while(!mi.atEnd()){
	DataSetElement el = mi.getElement();
	double y1 = el.getX(1);
	double y2 = el.getX(2);
	double y3 = el.getX(3);
	//System.out.println("el: " + el);
	if( SetUtils.isGoodValue(y1) && SetUtils.isGoodValue(y2) && SetUtils.isGoodValue(y3) ){
	  switch(i){
	  case 0:
	    assert( (y1+y2) == y3);
	    break;
	  case 1:
	    assert( (y1-y2) == y3);
	    break;
	  case 2:
	    assert( (y1*y2) == y3);
	    break;
	  case 3:
	    assert( (y1/y2) == y3);
	    break;
	  case 4:
	    assert( (y1+5000.5) == y3);
	    break;
	  case 5:
	    assert( (y2-5000) == y3);
	    break;
	  case 6:
	    assert( (y1*1.05) == y3);
	    break;
	  case 7:
	    assert( (y2/1110.33) == y3);
	    break;
	  case 8:
	    assert( (5000.5+y1) == y3);
	    break;
	  case 9:
	    assert( (5000-y2) == y3);
	    break;
	  case 10:
	    assert( (1.05*y1) == y3);
	    break;
	  case 11:
	    assert( (1110.33/y2) == y3);
	    break;
	  case 12:
	    assert(true);
	    break;
	  case 13:
	    assert(true);
	    break;
	  case 14:
	    assert(true);
	    break;
	  default:
	    break;
	  }
	}
	mi.advance();
      }
    }
  }
}
