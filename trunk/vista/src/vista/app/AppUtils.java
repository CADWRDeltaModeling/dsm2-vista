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

import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;

import javax.swing.table.TableModel;

import vista.graph.Axis;
import vista.graph.Curve;
import vista.graph.CurveAttr;
import vista.graph.CurveDataModel;
import vista.graph.GEContainer;
import vista.graph.GraphProperties;
import vista.graph.GraphicElement;
import vista.graph.Symbol;
import vista.graph.SymbolFactory;
import vista.set.CompositeFilter;
import vista.set.DataSet;
import vista.set.ElementFilter;
import vista.set.FlagUtils;
import vista.set.MultiValueFilter;
import vista.set.TimeSeries;

/**
 * 
 * 
 * @author Nicky Sandhu
 * @version $Id: AppUtils.java,v 1.1 2003/10/02 20:48:24 redwood Exp $
 */
public class AppUtils {
	/**
   *
   */
	private static NumberFormat _df;
	static {
		_df = new DecimalFormat();
		_df.setMaximumFractionDigits(2);
	}

	/**
   *
   */
	public static Format getFormatter(Axis axis) {
		Format f = axis.getTickGenerator().getFormatter();
		if (f == null)
			f = _df;
		return f;
	}

	private static int _colorTableIndex = 0, _symbolTableIndex = 0;
	static Color[] _colorTable = { Color.red, Color.green, Color.blue,
			Color.pink, Color.yellow, Color.cyan, Color.orange, Color.magenta,
			Color.gray, new Color(0, 206, 209), // Dark Turquoise
			new Color(85, 107, 47), // Dark Olive Green
			new Color(176, 48, 96), // maroon
			new Color(95, 158, 160), // Cadet Blue
			new Color(218, 112, 214), // orchid
			new Color(160, 32, 240) // purple
	};
	static Symbol[] _symbolTable = {
			SymbolFactory.createCircle(false, Color.black, 2),
			SymbolFactory.createTriangle(false, Color.black, 2),
			SymbolFactory.createSquare(false, Color.black, 2),
			SymbolFactory.createCross(false, Color.black, 2),
			SymbolFactory.createButterfly(false, Color.black, 2),
			SymbolFactory.createHourGlass(false, Color.black, 2),
			SymbolFactory.createCircle(true, Color.black, 2),
			SymbolFactory.createTriangle(true, Color.black, 2),
			SymbolFactory.createSquare(true, Color.black, 2),
			SymbolFactory.createCross(true, Color.black, 2),
			SymbolFactory.createButterfly(true, Color.black, 2),
			SymbolFactory.createHourGlass(true, Color.black, 2),
			SymbolFactory.createCircle(false, Color.black, 4),
			SymbolFactory.createTriangle(false, Color.black, 4),
			SymbolFactory.createSquare(false, Color.black, 4),
			SymbolFactory.createCross(false, Color.black, 4),
			SymbolFactory.createButterfly(false, Color.black, 4),
			SymbolFactory.createHourGlass(false, Color.black, 4) };

	/**
   *
   */
	public static CurveAttr getNextCurveAttr(DataSet ds) {
		CurveAttr ca = new CurveAttr();
		ca._foregroundColor = _colorTable[_colorTableIndex % _colorTable.length];
		if (!(ds instanceof TimeSeries)) {
			ca._drawLines = false;
			ca._drawSymbol = true;
			ca._dataPerSymbol = 1;
			ca._symbol = _symbolTable[_symbolTableIndex % _symbolTable.length];
			ca._symbol.getAttributes()._foregroundColor = ca._foregroundColor;
		}
		_symbolTableIndex++;
		_colorTableIndex++;
		return ca;
	}

	/**
   *
   */
	public static void setCurveFilter(GEContainer ge, ElementFilter filter) {
		int count = ge.getElementCount();
		for (int i = 0; i < count; i++) {
			GraphicElement leaf = ge.getElement(i);
			if (leaf instanceof GEContainer)
				AppUtils.setCurveFilter((GEContainer) leaf, filter);
			if (leaf instanceof Curve) {
				Curve c = (Curve) leaf;
				CurveDataModel cdm = c.getModel();
				if (cdm instanceof InstValCurveModel) {
					((InstValCurveModel) cdm).setFilter(filter);
				} else if (cdm instanceof PerValCurveModel) {
					((PerValCurveModel) cdm).setFilter(filter);
				} else if (cdm instanceof PerValFlaggedCurveModel) {
					((PerValFlaggedCurveModel) cdm).setFilter(filter);
				} else if (cdm instanceof InstValFlaggedCurveModel) {
					((InstValFlaggedCurveModel) cdm).setFilter(filter);
				} else {
				}
				// c.setModel(cdm);
			}
		}
	}

	/**
    *
    */
	public static ElementFilter getCurrentCurveFilter() {
		double[] missingValues = { Float.MIN_VALUE,
				vista.set.Constants.MISSING_VALUE,
				vista.set.Constants.MISSING_RECORD };
		CompositeFilter compFilter = new CompositeFilter(new ElementFilter[] {
				new MultiValueFilter(missingValues), FlagUtils.MISSING_FILTER });
		if (GraphProperties.properties.get("displayGood").equals("false")) {
			compFilter.add(FlagUtils.OK_FILTER);
		}
		if (GraphProperties.properties.get("displayQuestionable").equals(
				"false")) {
			compFilter.add(FlagUtils.QUESTIONABLE_FILTER);
		}
		if (GraphProperties.properties.get("displayReject").equals("false")) {
			compFilter.add(FlagUtils.REJECT_FILTER);
		}
		if (GraphProperties.properties.get("displayUnscreened").equals("false")) {
			compFilter.add(FlagUtils.UNSCREENED_FILTER);
		}
		return compFilter;
	}

	/**
	 * rejects all values that are missing, questionable or rejected
	 */
	public static ElementFilter getQuestionableFilter() {
		double[] missingValues = { Float.MIN_VALUE,
				vista.set.Constants.MISSING_VALUE,
				vista.set.Constants.MISSING_RECORD };
		ElementFilter compFilter = new CompositeFilter(new ElementFilter[] {
				new MultiValueFilter(missingValues), FlagUtils.MISSING_FILTER,
				FlagUtils.QUESTIONABLE_FILTER, FlagUtils.REJECT_FILTER });
		return compFilter;
	}

	/**
	 * rejects all values that are missing or rejected
	 */
	public static ElementFilter getRejectFilter() {
		double[] missingValues = { Float.MIN_VALUE,
				vista.set.Constants.MISSING_VALUE,
				vista.set.Constants.MISSING_RECORD };
		ElementFilter compFilter = new CompositeFilter(new ElementFilter[] {
				new MultiValueFilter(missingValues), FlagUtils.MISSING_FILTER,
				FlagUtils.REJECT_FILTER });
		return compFilter;
	}

	/**
	 * rejects all values that are missing
	 */
	public static ElementFilter getMissingFilter() {
		double[] missingValues = { Float.MIN_VALUE,
				vista.set.Constants.MISSING_VALUE,
				vista.set.Constants.MISSING_RECORD };
		ElementFilter compFilter = new CompositeFilter(new ElementFilter[] {
				new MultiValueFilter(missingValues), FlagUtils.MISSING_FILTER });
		return compFilter;
	}

	static void setGraphics(GraphicElement ge, Graphics g) {
		if (ge instanceof GEContainer) {
			GEContainer gec = (GEContainer) ge;
		} else {
		}
	}

	/**
   *
   */
	public static void dumpToText(TableModel m, String filename) {
		try {
			//
			PrintWriter writer = new PrintWriter(new BufferedWriter(
					new FileWriter(filename)));
			//
			int nc = m.getColumnCount();
			int nr = m.getRowCount();
			StringBuffer buf = new StringBuffer(nc * 30);
			for (int i = 0; i < nc; i++) {
				buf.append("\t").append(m.getColumnName(i));
			}
			writer.println(buf.toString());
			for (int i = 0; i < nr; i++) {
				for (int j = 0; j < nc; j++) {
					writer.print("\t");
					writer.print(m.getValueAt(i, j).toString());
				}
				writer.println();
			}
			writer.close();
		} catch (IOException ioe) {
			System.err.println("Error " + ioe.getMessage()
					+ "dumping table to text");
		}
	}

	/**
    *
    */
	public static String insertEscapeChar(String dir) {
		StringBuffer sb = new StringBuffer(dir);
		char ls = '\\';
		int i = 0;
		while (true) {
			if (i >= sb.length())
				break;
			if (sb.charAt(i) == ls) {
				if (i + 1 < sb.length()) {
					if (sb.charAt(i + 1) == ls) {
						i += 2;
						continue;
					}
				}
				sb.insert(i++, ls);
			}
			i++;
		}
		return sb.toString();
	}
}
