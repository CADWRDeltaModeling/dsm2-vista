/*
    Copyright (C) 1996, 1997, 1998 State of California, Department of 
    Water Resources.

    VISTA : A VISualization Tool and Analyzer. 
	Version 1.0beta
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
package vista.app;

import java.lang.reflect.Constructor;

import vista.graph.Curve;
import vista.graph.CurveAttr;
import vista.graph.CurveDataModel;
import vista.graph.FlaggedCurve;
import vista.graph.GraphUtils;
import vista.graph.ReferenceCurve;
import vista.set.DataReference;
import vista.set.DataRetrievalException;
import vista.set.DataSet;
import vista.set.DataSetAttr;
import vista.set.DefaultDataSet;
import vista.set.IrregularTimeSeries;
import vista.set.RegularTimeSeries;

/**
 * 
 * 
 * @author Nicky Sandhu
 * @version $Id: CurveFactory.java,v 1.1 2003/10/02 20:48:25 redwood Exp $
 */
public class CurveFactory {
	public static int PLAIN = 1;
	public static int DOTTED = 1;
	public static int SHORT_DASHED = 1;
	public static int LONG_DASHED = 1;
	public static int SHORT_LONG_DASHED = 1;

	/**
   *
   */
	public static void setDashedAttribute(Curve crv, int type) {
		CurveAttr attr = (CurveAttr) crv.getAttributes();
		if (type == PLAIN) {
			attr._dashArray = new float[] { 1 };
		} else if (type == DOTTED) {
			attr._dashArray = new float[] { 2, 2 };
		} else if (type == SHORT_DASHED) {
			attr._dashArray = new float[] { 4, 4 };
		} else if (type == LONG_DASHED) {
			attr._dashArray = new float[] { 8, 8 };
		} else if (type == SHORT_LONG_DASHED) {
			attr._dashArray = new float[] { 4, 4, 8, 4 };
		} else {
			attr._dashArray = new float[] { 1 };
		}

	}

	/**
   *
   */
	public static Curve createFlaggedCurve(DataReference ref, int xAxisPos,
			int yAxisPos, String legend) {
		try {
			Curve curve = createFlaggedCurve(ref.getData(), xAxisPos, yAxisPos,
					legend);
			curve.getModel().setReferenceObject(ref);
			return curve;
		} catch (DataRetrievalException dre) {
			throw new IllegalArgumentException(dre.getMessage());
		}
	}

	/**
   *
   */
	public static Curve createCurve(DataReference ref, int xAxisPos,
			int yAxisPos, String legend) {
		if (GraphUtils.isJDK2()) {
			_enhancedGraphics = true;
		} else {
			_enhancedGraphics = false;
		}
		try {
			Curve curve = createFlaggedCurve(ref.getData(), xAxisPos, yAxisPos,
					legend);
			curve.getModel().setReferenceObject(ref);
			return curve;
		} catch (DataRetrievalException dre) {
			throw new IllegalArgumentException(dre.getMessage());
		}
	}

	/**
   *
   */
	private static Curve createFlaggedCurve(DataSet ds, int xAxisPos,
			int yAxisPos, String legend) {
		CurveDataModel cdm = null;
		if (ds instanceof RegularTimeSeries) {
			DataSetAttr attr = ds.getAttributes();
			if (attr == null || attr.getYType().indexOf("PER") >= 0) {
				cdm = new PerValFlaggedCurveModel((RegularTimeSeries) ds,
						AppUtils.getCurrentCurveFilter(), xAxisPos, yAxisPos,
						legend);
			} else {
				cdm = new InstValFlaggedCurveModel(ds, AppUtils
						.getCurrentCurveFilter(), xAxisPos, yAxisPos, legend);
			}
		} else {
			cdm = new InstValFlaggedCurveModel(ds, AppUtils
					.getCurrentCurveFilter(), xAxisPos, yAxisPos, legend);
		}
		if (_enhancedGraphics) {
			try {
				Class cl = Class.forName("vista.graph.FlaggedCurve2D");
				Class[] params = { CurveAttr.class, CurveDataModel.class };
				Constructor cst = cl.getDeclaredConstructor(params);
				return (Curve) cst.newInstance(new Object[] {
						AppUtils.getNextCurveAttr(ds), cdm });
			} catch (Exception exc) {
				exc.printStackTrace(System.err);
				throw new RuntimeException(exc.getMessage());
			}
			// return new FlaggedCurve2D(AppUtils.getNextCurveAttr(ds),cdm);
			// return new FlaggedCurve(AppUtils.getNextCurveAttr(ds),cdm);
		} else {
			return new FlaggedCurve(AppUtils.getNextCurveAttr(ds), cdm);
		}
	}

	/**
   *
   */
	private static Curve createCurve(DataSet ds, int xAxisPos, int yAxisPos,
			String legend) {
		if (ds instanceof RegularTimeSeries)
			return createCurve((RegularTimeSeries) ds, xAxisPos, yAxisPos,
					legend);
		else if (ds instanceof IrregularTimeSeries)
			return createCurve((IrregularTimeSeries) ds, xAxisPos, yAxisPos,
					legend);
		else if (ds instanceof DefaultDataSet)
			return createCurve((DefaultDataSet) ds, xAxisPos, yAxisPos, legend);
		else
			throw new IllegalArgumentException(
					"Class of data set is not recognized " + ds.getClass());
	}

	/**
   *
   */

	private static Curve createCurve(RegularTimeSeries rts, int xAxisPos,
			int yAxisPos, String legend) {
		DataSetAttr attr = rts.getAttributes();
		CurveDataModel cdm = null;
		if (attr != null) {
			if (attr.getYType().indexOf("PER") >= 0) {
				cdm = new PerValCurveModel(rts, null, xAxisPos, yAxisPos, legend);
			}
		}
		if (cdm==null){
			 cdm = new InstValCurveModel(rts, null, xAxisPos, yAxisPos, legend);
		}
		CurveDataModel cdmf = createFlaggedCurveDataModel(rts, xAxisPos,
				yAxisPos, legend);
		return new ReferenceCurve(AppUtils.getNextCurveAttr(rts), cdm, cdmf);
	}

	/**
   *
   */
	private static Curve createCurve(IrregularTimeSeries rts, int xAxisPos,
			int yAxisPos, String legend) {
		DataSetAttr attr = rts.getAttributes();
		CurveDataModel cdm = new InstValCurveModel(rts, AppUtils
				.getCurrentCurveFilter(), xAxisPos, yAxisPos, legend);
		CurveDataModel cdmf = createFlaggedCurveDataModel(rts, xAxisPos,
				yAxisPos, legend);
		return new ReferenceCurve(AppUtils.getNextCurveAttr(rts), cdm, cdmf);
	}

	/**
   *
   */
	private static Curve createCurve(DefaultDataSet rts, int xAxisPos,
			int yAxisPos, String legend) {
		DataSetAttr attr = rts.getAttributes();
		CurveDataModel cdm = new InstValCurveModel(rts, AppUtils
				.getCurrentCurveFilter(), xAxisPos, yAxisPos, legend);
		CurveDataModel cdmf = createFlaggedCurveDataModel(rts, xAxisPos,
				yAxisPos, legend);
		return new ReferenceCurve(AppUtils.getNextCurveAttr(rts), cdm, cdmf);
	}

	/**
   *
   */
	public static CurveDataModel createCurveDataModel(RegularTimeSeries rts,
			int xAxisPos, int yAxisPos, String legend) {
		DataSetAttr attr = rts.getAttributes();
		CurveDataModel cdm = new InstValCurveModel(rts, AppUtils
				.getCurrentCurveFilter(), xAxisPos, yAxisPos, legend);
		if (attr != null) {
			if (attr.getYType().indexOf("PER") >= 0) {
				cdm = new PerValCurveModel(rts, AppUtils
						.getCurrentCurveFilter(), xAxisPos, yAxisPos, legend);
			}
		}
		return cdm;
	}

	/**
   *
   */
	public static CurveDataModel createCurveDataModel(IrregularTimeSeries rts,
			int xAxisPos, int yAxisPos, String legend) {
		DataSetAttr attr = rts.getAttributes();
		CurveDataModel cdm = new InstValCurveModel(rts, AppUtils
				.getCurrentCurveFilter(), xAxisPos, yAxisPos, legend);
		return cdm;
	}

	/**
   *
   */
	public static CurveDataModel createCurveDataModel(DefaultDataSet rts,
			int xAxisPos, int yAxisPos, String legend) {
		DataSetAttr attr = rts.getAttributes();
		CurveDataModel cdm = new InstValCurveModel(rts, AppUtils
				.getCurrentCurveFilter(), xAxisPos, yAxisPos, legend);
		return cdm;
	}

	/**
   *
   */
	public static CurveDataModel createFlaggedCurveDataModel(DataReference ref,
			int xAxisPos, int yAxisPos, String legend) {
		DataSet ds = null;
		try {
			ds = ref.getData();
		} catch (Exception e) {
			return null;
		}
		return createFlaggedCurveDataModel(ds, xAxisPos, yAxisPos, legend);

	}

	/**
   *
   */
	public static CurveDataModel createFlaggedCurveDataModel(DataSet ds,
			int xAxisPos, int yAxisPos, String legend) {
		CurveDataModel cdm = null;
		if (ds instanceof RegularTimeSeries) {
			DataSetAttr attr = ds.getAttributes();
			if (attr == null || attr.getYType().indexOf("PER") >= 0) {
				cdm = new PerValFlaggedCurveModel((RegularTimeSeries) ds, AppUtils.getCurrentCurveFilter(),
						xAxisPos, yAxisPos, legend);
			} else {
				cdm = new InstValFlaggedCurveModel(ds, AppUtils.getCurrentCurveFilter(), xAxisPos,
						yAxisPos, legend);
			}
		} else {
			cdm = new InstValFlaggedCurveModel(ds, AppUtils.getCurrentCurveFilter(), xAxisPos, yAxisPos,
					legend);
		}
		return cdm;
	}

	/**
   *
   */
	private static boolean _enhancedGraphics = false;
}
