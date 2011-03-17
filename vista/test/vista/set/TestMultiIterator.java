package vista.set;

import junit.framework.TestCase;

public class TestMultiIterator extends TestCase{
	public void testITS(){
		double[] x = new double[]{ 500000, 500015, 500033};
		double[] y = new double[] { -1, 22, 342};
		IrregularTimeSeries its1 = new IrregularTimeSeries("its1", x, y);
		x = new double[]{ 500001, 500017, 500045};
		y = new double[] { -10, 220, 3420};
		IrregularTimeSeries its2 = new IrregularTimeSeries("its2", x, y);
		MultiIterator iterator = new MultiIterator(new TimeSeries[]{its1, its2});
		assertTrue(iterator.atStart());
		assertFalse(iterator.atEnd());
		DataSetElement e = iterator.getElement();
		assertApproxEquals(500000, e.getX());
		assertApproxEquals(-1, e.getX(1));
		assertTrue(Double.isNaN(e.getX(2)));
		iterator.advance();
		assertFalse(iterator.atStart());
		e=iterator.getElement();
		assertApproxEquals(500001,e.getX());
		assertTrue(Double.isNaN(e.getX(1)));
		assertApproxEquals(-10, e.getX(2));
		while(!iterator.atEnd()){
			System.out.println(iterator.getElement());
			iterator.advance();
		}
	}
	
	public void assertApproxEquals(double expected, double actual){
		assertTrue(Math.abs(expected-actual) < 1e-8);
	}
}
