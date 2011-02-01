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
  * An evaluator that works with the parse tree of an expresssion to evaluate
  * it based on the given data sets in an associated derived time serires.
  * Note: The strategy is to create a single copy of the time series of the 
  * required size.
  * <p>
  * This evaluator runs thro' the given expression to see if a function is present
  * that requires period spanning operations ( e.g. moving averages, period averages etcetra)
  * If such a function is present it is then evaluated using another DTSExpression Evaluator.
  * The value retured by this evaluator is then stored in a temp array and is attached to the
  * function's node by means of a hash table.
  * <p>
  * Once this evaluator is done cleaning up the given expression of period spanning functions
  * it then sets up a loop and invokes ExpressionEvaluator using its root of the parse tree. The 
  * values are then sent along with any period spanning functions values in a value array. The
  * evaluated value is then stored in a time series of an appropriate size which is then returned
  * by call to this evaluator's evaluate method.
  * <p>
  * @author Nicky Sandhu
  * @version $Id: DTSExpressionEvaluator.java,v 1.3 2000/03/21 18:16:18 nsandhu Exp $
  */
public class DTSExpressionEvaluator implements Visitor{
  private static boolean DEBUG = false;
  private Vector _periodRTS;
  private TimeWindow _tw;
  private TimeInterval _ti;
  private RegularTimeSeries [] _dataSets;
  private Node _root;
  private DerivedTimeSeries _dts;
  private RegularTimeSeries _rts;
  static DTSExpression _parser;
  /**
   * A constructor to 
   */
  public DTSExpressionEvaluator(DerivedTimeSeries dts){
    init(dts,null);
  }
  /**
   *
   */
  public DTSExpressionEvaluator(DerivedTimeSeries dts, Node root){
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
      _dataSets = new RegularTimeSeries[dts.getMTS().getCount()];
  }
  /**
   *
   */
  public DataSet evaluate(){
    // null out old references..
    for(int i=0; i < _dataSets.length; i++) _dataSets[i] = null;
    // evaluates for time window.
    // get the time window of interest. If null throw exception
    // check the time interval of the data sets specified for operation
    TimeEvaluator te = new TimeEvaluator(_dts,_root);
    _tw = te.getTimeWindow();
    _ti = te.getTimeInterval();
    if ( _tw == null || _ti == null ){
      throw new RuntimeException("No regular time series specified in the expression");
    }
    // evaluates functions that are period spanning
    // evaluate period spanning function calls by recursive calls to this evaluator
    // and store the resulting time series in a hash table.
    // store the required time series in an array of the appropriate size.
    // check if atleast one time series is available
    _periodRTS = new Vector();
    _root.accept(this);
    // get all the required iterators to the data sets needed
    DataSetIterator [] dsiarray = new DataSetIterator[_dataSets.length];
    double [] vals = new double[_dataSets.length];
    _rts = null;
    DataSetIterator dsi1 = null;
    for(int i=0; i < dsiarray.length; i++) {
      if ( _dataSets[i] != null ) {
	dsiarray[i] = _dataSets[i].getIterator();
	if ( dsi1 == null )
	  dsi1 = dsiarray[i];
	if ( _rts == null )
	  _rts = (RegularTimeSeries) _dataSets[i].createSlice(_dataSets[i].getTimeWindow());
      } else{
	dsiarray[i] = null;
      }
    }
    // get all the required iterators to the data sets needed
    DataSetIterator [] parray = null;
    if ( _periodRTS.size() > 0 ){
      parray = new DataSetIterator[_periodRTS.size()];
      for(int i=0; i < parray.length; i++) {
	RegularTimeSeries rts = (RegularTimeSeries) ((RegularTimeSeries)_periodRTS.elementAt(i)).createSlice(_tw);
	parray[i] = rts.getIterator();
      }
    }
    double [] pvals = null;
    if ( parray != null ) pvals = new double[parray.length];
    // set up an expression evaluator that evaluates the given expression
    // this evaluator is fed the values of the time series at the current time
    // along with the resulting time series from period operations.
    DataSetIterator dsi = _rts.getIterator();
    ExpressionEvaluator expEval = new ExpressionEvaluator(_root);
    while( !dsi1.atEnd() ){
      for(int i=0; i < dsiarray.length; i++){
	if ( dsiarray[i] == null ) continue;
	vals[i] = dsiarray[i].getElement().getY();
	dsiarray[i].advance();
      }
      expEval.setValueArray(vals);
      if ( parray != null ){
	for(int i=0; i < parray.length; i++){
	  pvals[i] = parray[i].getElement().getY();
	  parray[i].advance();
	}
	expEval.setPeriodValueArray(pvals);
      }
      DataSetElement dse = dsi.getElement();
      dse.setY(expEval.evaluate());
      dsi.putElement(dse);
      dsi.advance();
    }
    // position them at the start time
    // create an array of values = max no. of relative refs
    // create an regular time series of the appropriate time window.
    // set a loop ( perhaps a multi iterator ) till the end
    // fill the value array ( and the period spanning function hashtable if any )
    // invoke ExpressionEvaluator with this root and 
    _rts.setName(_dts.getPathname().toString());
    return _rts;
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
      if ( _dataSets[rowIndex] == null ) {
	MultipleTimeSeries mts = _dts.getMTS();
	DataReference ref = _dts.getStudy().getMatchingReference(mts.getRowAt(rowIndex).getPathParts());
	ref = DataReference.create(ref,_tw);
	_dataSets[rowIndex]  = (RegularTimeSeries) ref.getData();
      }
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
    String funcName = n.f0.tokenImage;
    if (! FunctionRegistry.isRegisteredFunction(funcName)) 
      throw new RuntimeException("Unrecognized function: " + funcName);
    Function func = FunctionRegistry.getFunction(funcName);
    int nargs = func.getNumberOfArguments();
    NodeOptional nopt = (NodeOptional) n.f2;
    // call the function if it is period spanning...
    if ( func.isPeriodSpanning() ){
      if ( nopt.present() ){
	NodeSequence ns = (NodeSequence) nopt.node;
	if ( ns.size() != nargs ) 
	  throw new RuntimeException("Incorrect # of arguments: Expected " + 
				     nargs + " got " + ns.size() );
	Object [] dsarray = new DataSet[ns.size()];
	for(int i=0; i < ns.size(); i++){
	  Node nd = ns.elementAt(i);
	  dsarray[i] = new DTSExpressionEvaluator(_dts,nd).evaluate();
	}
	RegularTimeSeries ds = (RegularTimeSeries) func.execute(dsarray);
	_periodRTS.addElement(ds);
      } else {
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
    }
  }
}
