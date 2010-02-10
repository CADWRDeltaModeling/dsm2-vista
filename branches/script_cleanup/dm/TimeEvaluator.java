/*
    Copyright (C) 1996-2000 State of California, Department of 
    Water Resources.

    VISTA : A VISualization Tool and Analyzer. 
	Version 1.0
	by Nicky Sandhu
    California Dept. of Water Resources
    Division of Planning, Delta Modeling Section
    1416 Ninth Street
    Sacramento, CA 95814
    (916)-653-7552
    nsandhu@water.ca.gov

    Send bug reports to nsandhu@water.ca.gov

    This program is licensed to you under the terms of the GNU General
    Public License, version 2, as published by the Free Software
    Foundation.

    You should have received a copy of the GNU General Public License
    along with this program; if not, contact Dr. Francis Chung, below,
    or the Free Software Foundation, 675 Mass Ave, Cambridge, MA
    02139, USA.

    THIS SOFTWARE AND DOCUMENTATION ARE PROVIDED BY THE CALIFORNIA
    DEPARTMENT OF WATER RESOURCES AND CONTRIBUTORS "AS IS" AND ANY
    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
    PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE CALIFORNIA
    DEPARTMENT OF WATER RESOURCES OR ITS CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
    OR SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA OR PROFITS; OR
    BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
    USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
    DAMAGE.

    For more information about VISTA, contact:

    Dr. Francis Chung
    California Dept. of Water Resources
    Division of Planning, Delta Modeling Section
    1416 Ninth Street
    Sacramento, CA  95814
    916-653-5601
    chung@water.ca.gov

    or see our home page: http://wwwdelmod.water.ca.gov/

    Send bug reports to nsandhu@water.ca.gov or call (916)-653-7552

*/
package vista.dm;
import vista.set.*;
import vista.time.*;
import vista.dm.visitor.Visitor;
import vista.dm.syntaxtree.*;
import java.util.*;
import java.io.StringReader;
/**
  * This evaluator runs thro' the given expression to calculate the common time window
  * of the regular time series in the expression and their time interval's.
  * <p>
  * @author Nicky Sandhu
  * @version $Id: TimeEvaluator.java,v 1.4 2000/03/21 18:16:24 nsandhu Exp $
  */
public class TimeEvaluator implements Visitor{
  private static boolean DEBUG = false;
  private DerivedTimeSeries _dts;
  private TimeWindow _tw;
  private TimeInterval _ti;
  private Node _root;
  static DTSExpression _parser = DTSExpressionEvaluator._parser;
  /**
   *
   */
  public TimeEvaluator(DerivedTimeSeries dts, Node root){
    init(dts,root);
  }
  /**
   *
   */
  public void init(DerivedTimeSeries dts, Node root){
    _dts = dts;
    if ( root == null ){
      StringReader reader = new StringReader(dts.getExpression());
      if ( _parser == null )
	_parser = new DTSExpression(reader);
      else
	_parser.ReInit(reader);
      try {
	_root = _parser.one_line();
      }catch(ParseException pe){
	throw new RuntimeException("Parse Error: " + pe.getMessage());
      }finally{
	reader.close();
      }
    } else {
      _root = root;
    }
    evaluate();
  }
  /**
   *
   */
  void evaluate(){
    _tw = null;
    _ti = null;
    // calculate the time window and time interval
    _root.accept(this);
    // check if a regular time series was specified in the expression.
    /*
    if ( _tw == null || _ti == null ){
      throw new RuntimeException("No regular time series specified in the expression");
    }
    */
  }
  /**
   *
   */
  public TimeWindow getTimeWindow(){
    return _tw;
  }
  /**
   *
   */
  public TimeInterval getTimeInterval(){
    return _ti;
  }
  //
  // Auto class visitors--probably don't need to be overridden.
  //
  public void visit(NodeList n) {
    for ( Enumeration e = n.elements(); e.hasMoreElements(); )
      ((Node)e.nextElement()).accept(this);
  }
  
  public void visit(NodeListOptional n) {
    if ( n.present() )
      for ( Enumeration e = n.elements(); e.hasMoreElements(); )
	((Node)e.nextElement()).accept(this);
  }

  public void visit(NodeOptional n) {
    if ( n.present() )
      n.node.accept(this);
  }

  public void visit(NodeSequence n) {
    for ( Enumeration e = n.elements(); e.hasMoreElements(); )
      ((Node)e.nextElement()).accept(this);
  }

  public void visit(NodeToken n) { }

  /**
   * <PRE>
   * f0 -> logical()
   * f1 -> &lt;EOF&gt;
   * </PRE>
   */
  public void visit(one_line n){
    n.f0.accept(this);
  }
  /**
   * <PRE>
   * f0 -> relational()
   * f1 -> ( ( &lt;AND&gt; | &lt;OR&gt; | &lt;XOR&gt; ) relational() )*
   * </PRE>
   */
  public void visit(logical n){
    n.f0.accept(this);
    if ( n.f1.present() ){
      int count = 0;
      while( count < n.f1.size() ){
	NodeSequence ns = (NodeSequence) n.f1.elementAt(count);
	NodeChoice nc = (NodeChoice) ns.elementAt(0);
	ns.elementAt(1).accept(this);
	count++;
      }
    }
  }
  /**
   * <PRE>
   * f0 -> sum()
   * f1 -> ( ( &lt;LT&gt; | &lt;GT&gt; | &lt;GE&gt; | &lt;LE&gt; | &lt;EQ&gt; | &lt;NE&gt; ) sum() )?
   * </PRE>
   */
  public void visit(relational n) {
    n.f0.accept(this);
    n.f1.accept(this);
  }
  /**
   * <PRE>
   * f0 -> term()
   * f1 -> ( ( &lt;PLUS&gt; | &lt;MINUS&gt; ) term() )*
   * </PRE>
   */
  public void visit(sum n){
    n.f0.accept(this);
    if ( n.f1.present() ){
      int count = 0;
      while ( count < n.f1.size() ){
	NodeSequence ns = (NodeSequence) n.f1.elementAt(count);
	NodeChoice nc = (NodeChoice) ns.elementAt(0);
	int plus = 0;
	ns.elementAt(1).accept(this);
	count++;
      }
    }
    if (DEBUG) System.out.println("A sum expression: ");
  }
  /**
   * <PRE>
   * f0 -> exp()
   * f1 -> ( ( &lt;MULTIPLY&gt; | &lt;DIVIDE&gt; ) exp() )*
   * </PRE>
   */
  public void visit(term n){
    n.f0.accept(this);
    if ( n.f1.present() ){
      int count = 0;
      while ( count < n.f1.size() ){
	NodeSequence ns = (NodeSequence) n.f1.elementAt(count);
	NodeChoice nc = (NodeChoice) ns.elementAt(0);
	ns.elementAt(1).accept(this);
	count++;
      }
    }
    if (DEBUG) System.out.println("A unary expression: ");
  }
  /**
   * <PRE>
   * f0 -> unary()
   * f1 -> ( &lt;EXP&gt; exp() )*
   * </PRE>
   */
  public void visit(exp n){
    n.f0.accept(this);
    if ( n.f1.present() ){
      int count = 0;
      while ( count < n.f1.size() ){
	NodeSequence ns = (NodeSequence) n.f1.elementAt(count);
	NodeChoice nc = (NodeChoice) ns.elementAt(0);
	ns.elementAt(1).accept(this);
	count++;
      }
    }
    if (DEBUG) System.out.println("A unary expression: ");
  }
  /**
   * <PRE>
   * f0 -> &lt;MINUS&gt; element()
   *       | element()
   * </PRE>
   */
  public void visit(unary n){
    n.f0.accept(this);
    if (DEBUG) System.out.println("A unary expression: ");
  }
  /**
   * <PRE>
   * f0 -> &lt;CONSTANT&gt;
   *       | &lt;VARIABLE&gt;
   *       | function()
   *       | "(" logical() ")"
   * </PRE>
   */
  public void visit(element n){
    if ( n.f0.which == 0 ){ // nothing to do here...
      if (DEBUG) System.out.println("Its a constant! ");
    } else if (n.f0.which == 1 ){
      String name = ((NodeToken) n.f0.choice).tokenImage;
      if (DEBUG) System.out.println("Its a variable! " + name);
      int rowIndex = new Integer(name.substring(1,name.length())).intValue() -1;
      MultipleTimeSeries mts = _dts.getMTS();
      DataReference ref = _dts.getStudy().getMatchingReference(mts.getRowAt(rowIndex).getPathParts());
      checkTimeParameters(ref);
    } else if (n.f0.which == 2 ){
      n.f0.choice.accept(this);
    } else if (n.f0.which == 3 ){
      NodeSequence ns = (NodeSequence) n.f0.choice;
      ns.accept(this);
    } else {
      throw new RuntimeException("Could not recognize choice?");
    }
  }
  /**
   * <PRE>
   * f0 -> &lt;ID&gt;
   * f1 -> "("
   * f2 -> [ logical() ( "," logical() )* ]
   * f3 -> ")"
   * </PRE>
   */
  public void visit(function n){
    // 1. check the function name & see if it exists in the registry
    String funcName = n.f0.tokenImage; 
    if (! FunctionRegistry.isRegisteredFunction(funcName)) 
      throw new RuntimeException("Unrecognized function: " + funcName);
    System.out.println("Checking function: " + funcName);
    Function func = FunctionRegistry.getFunction(funcName);
    int nargs = func.getNumberOfArguments();
    NodeOptional nopt = (NodeOptional) n.f2;
    // 2. call the function if it is period spanning...
    if ( func.isPeriodSpanning() ){
      System.out.println("Function "+funcName+" is period spanning!");
      // if argument list is present...
      if ( nopt.present() ){
	System.out.println("arg list is present");
	NodeSequence ns = (NodeSequence) nopt.node;
	// check # of args
	if ( ns.size() != nargs ) 
	  throw new RuntimeException("Incorrect # of arguments: Expected " + 
				     nargs + " got " + ns.size() );
	Vector twarray = new Vector();
	// loop over arguments and check for token representing interval...
	for(int i=0; i < ns.size(); i++){
	  Node nd = ns.elementAt(i);
	  System.out.println("arg["+i+"]="+nd);
	  // if interval check for time interval...
	  if ( nd instanceof NodeToken ) {
	    NodeToken nt = (NodeToken) nd;
	    if ( nt.tokenImage.equals("1mon") ||
		 nt.tokenImage.equals("1day") ||
		 nt.tokenImage.equals("1hour") 
		 ){
	      System.out.println("Got interval: " + nt.tokenImage);
	      if ( _ti == null ){
		_ti = TimeFactory.getInstance().createTimeInterval(nt.tokenImage);
	      } else {
		if( ! _ti.equals(TimeFactory.getInstance().createTimeInterval(nt.tokenImage)) )
		  throw new RuntimeException("Period function " + 
					     funcName + " has incompatible time interval = " + 
					     nt.tokenImage);
	      }
	    }
	  } else { // if logicial 
	    nd.accept(this);
	  }
	}
      }
    } else { // if not period spanning...
      if ( nopt.present() ){
	NodeSequence ns = (NodeSequence) nopt.node;
	    if ( ns.size() != nargs ) 
	      throw new RuntimeException("Incorrect # of arguments: Expected " + 
					 nargs + " got " + ns.size() );
	    for(int i=0; i < ns.size(); i++){
	      Node nd = ns.elementAt(i);
	      nd.accept(this);
	    }
      }
    }
    return;
  }
  /**
   *
   */
  private void checkTimeParameters( DataReference ref ){
    if ( ref.getTimeWindow() == null ||
	 ref.getTimeInterval() == null ) 
      throw new RuntimeException("Reference : " + ref + " is not a regular time series");
    if ( _tw == null ){
      _tw =  ref.getTimeWindow();
    } else {
      _tw = _tw.intersection(ref.getTimeWindow());
      if ( _tw == null ) throw new RuntimeException("No common time window available");
    }
    if ( _ti == null ){
      _ti =  ref.getTimeInterval();
    } else {
      if ( _ti.compare(ref.getTimeInterval()) != 0 )
	throw new RuntimeException("Time intervals must be consistent");
    }
  }
  /**
   *
   */
  private void checkTimeParameters( TimeWindow tw, TimeInterval ti){
    if ( tw == null ||
	 ti == null ) 
      throw new RuntimeException("TimeWindow or TimeInterval is null");
    if ( _tw == null ){
      _tw =  tw;
    } else {
      _tw = _tw.intersection(tw);
      if ( _tw == null ) throw new RuntimeException("No common time window available");
    }
    if ( _ti == null ){
      _ti =  ti;
    } else {
      if ( _ti.compare(ti) != 0 )
	throw new RuntimeException("Time intervals must be consistent");
    }
  }
}
