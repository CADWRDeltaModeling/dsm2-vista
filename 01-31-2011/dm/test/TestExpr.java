package vista.dm.test;
import vista.dm.*;
import junit.framework.*;

public class TestExpr extends TestCase{
  //
  String [] exprs, complex_exprs, incorrect_exprs;
  double [] [] vals;
  double [] [] ans;
  //
  public TestExpr(String name){
    super(name);
  }
  //
  protected void setUp(){
    exprs = new String [] {
      "$1+$2","$1/$2","$1-$2","$1*$2","$1 > 1 & $2 > 2 "
    };
    complex_exprs = new String []{"$1/($1+$2)","($1 > 1.0)*($2 > 2)"};
    incorrect_exprs = new String []{"($1*$3)*$2","$1/$0"};
    vals = new double[][]{
      new double[]{1.5,2.5},
      new double[]{1,1},
      new double[]{1,0},
      new double[]{1,-1}
    };
  }
  //
  protected void tearDown(){
  }
  // 
  public void testSimple(){
    for(int i=0; i < exprs.length; i++){
      ExpressionEvaluator el = new ExpressionEvaluator(exprs[i]);
      for(int j=0; j < vals.length; j++){
	double [] val = vals[j];
	el.setValueArray(val);
	switch(i){
	case 0:
	  assert(el.evaluate() == val[0]+val[1]);
	  break;
	case 1:
	  assert(el.evaluate() == val[0]/val[1]);
	  break;
	case 2:
	  assert(el.evaluate() == val[0]-val[1]);
	  break;
	case 3:
	  assert(el.evaluate() == val[0]*val[1]);
	  break;
	case 4:
	  if ( val[0] > 1 && val[1] > 2 )
	    assert(el.evaluate() == 1);
	  else
	    assert(el.evaluate() == 0);
	  break;
	default: break;
	}
	//System.out.println("Value of "+exprs[i]+ " is "+el.evaluate());
      }
    }
  }
  //
  public void testComplex(){
    for(int i=0; i < complex_exprs.length; i++){
      ExpressionEvaluator el = new ExpressionEvaluator(complex_exprs[i]);
      for(int j=0; j < vals.length; j++){
	double [] val = vals[j];
	el.setValueArray(val);
	switch(i){
	case 0:
	  assert(el.evaluate() == val[0]/(val[0]+val[1]));
	  break;
	case 1:
	  double i1, i2;
	  if ( val[0] > 1.0 )
	    i1 = 1;
	  else
	    i1 = 0;
	  if ( val[1] > 2 )
	    i2 = 1;
	  else
	    i2 = 0;
	  assert(el.evaluate() == i1*i2);
	  break;
	default: break;
	}
	//System.out.println("Value of "+complex_exprs[i]+ " is "+el.evaluate());
      }
    }
  }
}
