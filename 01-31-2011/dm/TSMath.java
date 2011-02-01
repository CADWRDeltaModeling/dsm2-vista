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
/**
  * A class that handles logical and arithmetic operations between time series or scalars.
  *
  * @author Nicky Sandhu
  * @version $Id: TSMath.java,v 1.2 2000/03/21 18:16:24 nsandhu Exp $
  */
public class TSMath{
  public static ElementFilter _filter = Constants.DEFAULT_FILTER;
  public static final int AND_OP=100, OR_OP=200, XOR_OP=300;
  public static final int ADD_OP=1, SUB_OP=2, MUL_OP=3, DIV_OP=4, EXP_OP=5, NEG_OP=6;
  /**
   * The heart of the operation functionality.
   */
  public static double doOperation(double y1, double y2, int opId){
    if ( opId == ADD_OP )
      return y1+y2;
    else if ( opId == SUB_OP )
      return y1-y2;
    else if ( opId == MUL_OP )
      return y1*y2;
    else if ( opId == DIV_OP )
      return y1/y2;
    else if ( opId == EXP_OP )
      return Math.pow(y1,y2);
    else if ( opId == AND_OP )
      return (((int) y1==1 && (int)y2==1) ? 1 : 0);
    else if ( opId == OR_OP )
      return (((int) y1==1 || (int)y2==1) ? 1 : 0);
    else if ( opId == XOR_OP )
      return ( (((int) y1==1 && (int)y2==0) || ( (int) y1 == 0 && (int) y2 == 1)) ? 1 : 0);
    else 
      throw new IllegalArgumentException("Unknown operation, Operation Id: " + opId);
  }
  /**
    * This method handles both Double and TimeSeries objects and resolves the operations
    * in terms of an operation on two double values.
    */
  public static Object doOperation(Object op1, Object op2, int opId){
    boolean op1reg = op1 instanceof RegularTimeSeries || op1 instanceof Double;
    if ( !op1reg ) throw new RuntimeException("First operand "+op1+" is not a regular time series or number");
    boolean op2reg = op2 instanceof RegularTimeSeries || op2 instanceof Double;
    if ( !op2reg ) throw new RuntimeException("Second operand " +op2+" is not a regular time series or number");
    RegularTimeSeries d1=null,d2=null;
    Double dd1=null, dd2=null;
    if ( op1 instanceof RegularTimeSeries )
      d1 = (RegularTimeSeries) op1;
    else 
      dd1 = (Double) op1;
    if ( op2 instanceof RegularTimeSeries )
      d2 = (RegularTimeSeries) op2;
    else 
      dd2 = (Double) op2;
    if ( d1 != null && d2 != null ){
      if ( d1.getTimeInterval().compare(d2.getTimeInterval() ) != 0 )
	throw new RuntimeException("operation between time series of different intervals");
      if ( ! d1.getTimeWindow().isSameAs(d2.getTimeWindow()) ){
	TimeWindow tw1 = d1.getTimeWindow();
	TimeWindow tw2 = d2.getTimeWindow();
	TimeWindow tw = tw1.intersection(tw2);
	if ( tw == null )
	  throw new RuntimeException("operation between time series of non-intersecting time windows");
	if ( ! tw1.equals(tw) ) d1 = (RegularTimeSeries) d1.createSlice(tw);
	if ( ! tw2.equals(tw) ) d2 = (RegularTimeSeries) d2.createSlice(tw);
      }
    }
    // get the iterator and put the data set that is a regular time series as the
    // data set to be returned
    DataSetIterator dsi1 = d1 == null ? null : d1.getIterator();
    DataSetIterator dsi2 = d2 == null ? null : d2.getIterator();
    DataSetIterator dsi = dsi1 == null ? dsi2 : dsi1; 
    DataSet ds = d1 == null ? d2 : d1;
    if ( dsi == null ){ // both objects are numbers
      double y1 = dd1.doubleValue();
      double y2 = dd2.doubleValue();
      return new Double( doOperation(y1,y2,opId) );
    }
    while( !dsi.atEnd() ){
      DataSetElement dse = dsi.getElement();
      DataSetElement dse1 = dsi1 == null ? null : dsi1.getElement();
      DataSetElement dse2 = dsi2 == null ? null : dsi2.getElement();
      boolean oneNumber = false;
      if ( dse1 == null || dse2 == null ) oneNumber = true;
      double y = 0.0;
      if ( (!oneNumber && ( _filter.isAcceptable(dse1) && _filter.isAcceptable(dse2))) ||
	   (oneNumber && ( _filter.isAcceptable(dse))) ){ 
	double y1 = dse1 == null ? dd1.doubleValue(): dse1.getY(); 
	double y2 = dse2 == null ? dd2.doubleValue(): dse2.getY();
	y = doOperation(y1,y2,opId);
	dse.setY(y);
	dsi.putElement(dse);
      }else {
	dse.setY(Constants.MISSING_VALUE);
	dsi.putElement(dse);
      }
      if ( dsi1 != null ) dsi1.advance(); 
      if ( dsi2 != null ) dsi2.advance(); 
    }
    boolean unknown = d1.getAttributes().getYUnits().equals("UNKNOWN") ;
    boolean muldivop = opId == TimeSeriesMath.DIV ||
      opId == TimeSeriesMath.MUL;
    //    
    if ( unknown && !muldivop){
      d1.setAttributes(d2.getAttributes());
    } else if (!unknown && muldivop){
      d1.getAttributes().setYUnits("NONE");
    }
    return d1;
  }
  /**
    * @see doOperation
    */
  public static Object doAndOperation(Object op1, Object op2){
    return doOperation(op1,op2,AND_OP);
  }
  /**
    * @see doOperation
    */
  public static Object doOrOperation(Object op1, Object op2){
    return doOperation(op1,op2,OR_OP);
  }
  /**
    * @see doOperation
    */
  public static Object doXorOperation(Object op1, Object op2){
    return doOperation(op1,op2,XOR_OP);
  }
  /**
    * @see doOperation
    */
  public static Object doAddOperation(Object op1, Object op2){
    return doOperation(op1,op2,ADD_OP);
  }
  /**
    * @see doOperation
    */
  public static Object doSubtractOperation(Object op1, Object op2){
    return doOperation(op1,op2,SUB_OP);
  }
  /**
    * @see doOperation
    */
  public static Object doMultiplyOperation(Object op1, Object op2){
    return doOperation(op1,op2,MUL_OP);
  }
  /**
    * @see doOperation
    */
  public static Object doDivideOperation(Object op1, Object op2){
    return doOperation(op1,op2,DIV_OP);
  }
  /**
    * @see doOperation
    */
  public static Object doExponentOperation(Object op1, Object op2){
    return doOperation(op1,op2,EXP_OP);
  }
  /**
    * @see doOperation
    */
  public static Object doNegateOperation(Object op1){
    return doOperation(op1,new Double(-1),MUL_OP);
  }
  /**
   * creates a copy of rts and returns it.
   */
  public static TimeSeries createCopy(TimeSeries rts){
    return rts.createSlice(rts.getTimeWindow());
  }
}
