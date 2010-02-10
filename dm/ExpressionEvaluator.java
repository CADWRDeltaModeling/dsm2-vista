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
import vista.dm.visitor.Visitor;
import vista.dm.syntaxtree.*;
import java.util.*;
import java.io.StringReader;
/**
  * An expression evaluator that evaluates given an array of values.
  *
  * @author Nicky Sandhu
  * @version $Id: ExpressionEvaluator.java,v 1.3 2000/03/21 18:16:20 nsandhu Exp $
  */
public class ExpressionEvaluator implements Visitor{
  private static boolean DEBUG = false;
  private Hashtable _valTable = new Hashtable();
  private Hashtable _funcEvalTable = new Hashtable();
  private double [] _stack = new double[50];
  private int _top = -1;
  private int _pvalIndex;
  private Node _root;
  private double [] _values;
  private double [] _pvalues;
  private static DTSExpression _parser = DTSExpressionEvaluator._parser;
  /**
    *
    */
  public ExpressionEvaluator(String expr){
    StringReader reader = new StringReader(expr);
    if ( _parser == null )
      _parser = new DTSExpression(reader);
    else
      _parser.ReInit(reader);
    try {
      Node root = _parser.one_line();
      _root = root;
    }catch(ParseException pe){
      throw new RuntimeException("Parse Error: " + pe.getMessage());
    }finally{
      reader.close();
    }
  }
  /**
    * an expression evaluator given the parse tree of the expression.
    */
  public ExpressionEvaluator(Node root){
    _root = root;
  }
  /**
   * set the array to be used as the input values for the expression.
   */
  public void setValueArray(double [] values){
    _values = values;
  }
  /**
   *
   */
  public void setPeriodValueArray(double [] pvals){
    _pvalues = pvals;
  }
  /**
    *
    */
  public double evaluate(){
    _top = -1;
    _root.accept(this);
    _pvalIndex = 0;
    if ( _top != 0 ) 
      throw new RuntimeException("Problem evaluating expression: Not at top of stack");
    return _stack[_top]; 
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
    if ( _top != 0 ){
      throw new RuntimeException("Malformed expression");
    }
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
	int plus = 0;
	ns.elementAt(1).accept(this);
	double val2 = _stack[_top--];
	double val1 = _stack[_top--];
	// do desired operation
	if ( nc.which == 0 ){
	  if (DEBUG) System.out.println("An and...");
	  _stack[++_top] = val1 != 0 && val2 != 0 ? 1: 0;
	} else if ( nc.which == 1){
	  if (DEBUG) System.out.println("A or...");
	  _stack[++_top] = val1 != 0 || val2 != 0? 1: 0;
	} else if ( nc.which == 2){
	  if (DEBUG) System.out.println("A xor...");
	  _stack[++_top] = val1 != 0 && val2 == 0 || val2 != 0 && val1 == 0 ? 1: 0;
	} else {
	  throw new RuntimeException("Unknown symbol in logical operation");
	}
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
      NodeOptional nopt = (NodeOptional) n.f1;
      if ( nopt.present() ){
	NodeSequence ns = (NodeSequence) nopt.node;
	NodeChoice nc = (NodeChoice) ns.elementAt(0);
	ns.accept(this);
	double val2 = _stack[_top--];
	double val1 = _stack[_top--];
	boolean compare = false;
	if ( nc.which == 0 ){
	  compare = val1 < val2;
	} else if ( nc.which == 1){
	  compare = val1 > val2;
	} else if ( nc.which == 2){
	  compare = val1 >= val2;
	} else if ( nc.which == 3){
	  compare = val1 <= val2;
	} else if ( nc.which == 4){
	  compare = val1 == val2;
	} else if ( nc.which == 5){
	  compare = val1 != val2;
	} else {
	  throw new RuntimeException("Unknown symbol in comparision operation");
	}
	_stack[++_top] = compare ? 1: 0;
      }
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
	double val2 = _stack[_top--];
	double val1 = _stack[_top--];
	if ( nc.which == 0 ){
	  if (DEBUG) System.out.println("A plus...");
	  _stack[++_top]=val1+val2;
	} else if ( nc.which == 1){
	  if (DEBUG) System.out.println("A minus...");
	  _stack[++_top]=val1-val2;
	} else {
	  throw new Error();
	}
	count++;
      }
    }
    if (DEBUG) System.out.println("A sum expression: " + _stack[_top]);
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
	double val2 = _stack[_top--];
	double val1 = _stack[_top--];
	if ( nc.which == 0 ){
	  if (DEBUG) System.out.println("A multiply...");
	  _stack[++_top] = val1*val2;
	} else if ( nc.which == 1){
	  if (DEBUG) System.out.println("A divide...");
	  _stack[++_top] = val1/val2;
	} else {
	  throw new Error();
	}
	count++;
      }
    }
    if (DEBUG) System.out.println("A unary expression: " + _stack[_top]);
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
	double val2 = _stack[_top--];
	double val1 = _stack[_top--];
	if (DEBUG) System.out.println("An exponent...");
	_stack[++_top] = Math.pow(val1,val2);
	count++;
      }
    }
    if (DEBUG) System.out.println("A unary expression: " + _stack[_top]);
  }
  /**
    * <PRE>
    * f0 -> &lt;MINUS&gt; element()
    *       | element()
    * </PRE>
    */
  public void visit(unary n){
    n.f0.accept(this);
    if ( n.f0.which == 0 ){
      _stack[_top] = -_stack[_top];
    }
    if (DEBUG) System.out.println("A unary expression: " + _stack[_top]);
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
    if ( n.f0.which == 0 ){ // a constant
      String name = ((NodeToken) n.f0.choice).tokenImage;
      if (DEBUG) System.out.println("Its a constant! " + name);
      // save this value of string -> double conversion on the node of the tree...
      Object obj = _valTable.get(name);
      if ( obj == null ) 
	_valTable.put(name,obj = new Double(name));
      _stack[++_top]= ((Double)obj).doubleValue();
    } else if (n.f0.which == 1 ){ // a relative reference to values array
      // save this name conversion -> indexing conversion on the node of the tree...
      String name = ((NodeToken) n.f0.choice).tokenImage;
      if (DEBUG) System.out.println("Its a variable! " + name);
      Object obj = _valTable.get(name);
      if ( obj == null ) 
	_valTable.put(name,obj = new Integer( name.substring(1,name.length())) );
      int rowIndex =((Integer) obj).intValue() -1;
      if ( rowIndex < 0 || rowIndex > _values.length-1 ) 
	throw new RuntimeException("relative reference to $"+(rowIndex+1)+" is out of range");
      _stack[++_top] = _values[rowIndex];
    } else if (n.f0.which == 2 ){ // a function
      // should this not be converted in the first pass
      n.f0.choice.accept(this);
    } else if (n.f0.which == 3 ){
      // more statements....
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
    // if period spanning this should have been evaluated...
    if ( func.isPeriodSpanning() ){
	_stack[++_top] = _pvalues[_pvalIndex];
	_pvalIndex++;
    } else { // if not period spanning evaluate it here.
      int nargs = func.getNumberOfArguments();
      NodeOptional nopt = (NodeOptional) n.f2;
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
      // pop off the values from the stack...
      double [] args = new double[nargs];
      for(int i=0; i < args.length; i++) {
	args[i] = _stack[_top--];
	System.out.println("Args["+i+"] = "+args[i]);
      }
      // call the function
      _stack[++_top] = func.execute(args);
    }
  }
}
