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
package vista.db.dss;

import vista.set.DataSet;
import vista.set.DataSetAttr;
import vista.set.SetUtils;

/**
 * 
 * The class with native function calls to the HEC-DSS library. Only limited
 * functionality of retriving data is available at this time. Storing data
 * options will be added later.
 * 
 * Does not allow multithreaded access. However multi-user access is allowed.
 * 
 * @author Nicky Sandhu
 * @version $Id: DSSDataWriter.java,v 1.1 2003/10/02 20:48:45 redwood Exp $
 */
class DSSDataWriter {
	/**
	 * load the dss library
	 */
	static {
		DSSUtil.loadDSSLibrary();
	}

	/**
   *
   */
	public DSSDataWriter() {
	}

	/**
   *
   */
	public void storeData(String filename, String pathname, long startJulmin,
			long endJulmin, DataSet ds, boolean storeFlags) {
		DataSetAttr attr = ds.getAttributes();
		DSSData data = new DSSData();
		int dataType = attr.getType();
		data._dataType = dataType;
		data._xType = attr.getXType();
		data._yType = attr.getYType();
		data._xUnits = attr.getXUnits();
		data._yUnits = attr.getYUnits();
		data._numberRead = ds.size();
		data._yValues = SetUtils.createYArray(ds);
		if (pathname.length() >= 1800)
			throw new IllegalArgumentException("Pathname: " + pathname
					+ " is too long (>1800chars)");
		if (dataType == DSSUtil.REGULAR_TIME_SERIES) {
			if (storeFlags)
				data._flags = SetUtils.createFlagArray(ds);
			storeTimeSeriesData(filename, pathname, startJulmin, endJulmin,
					data, storeFlags);
		} else if (dataType == DSSUtil.IRREGULAR_TIME_SERIES) {
			data._xValues = SetUtils.createXArray(ds);
			if (storeFlags)
				data._flags = SetUtils.createFlagArray(ds);
			storeIrregularTimeSeriesData(filename, pathname, 0, 0, data,
					storeFlags);
		} else if (dataType == DSSUtil.PAIRED) {
			data._xValues = SetUtils.createXArray(ds);
			storePairedData(filename, pathname, data, storeFlags);
		} else {
			throw new IllegalArgumentException("Data type: " + dataType
					+ " is invalid");
		}
	}

	/**
	 * time series data
	 */
	private synchronized native void storeTimeSeriesData(String dssFile,
			String pathname, long startJulmin, long endJulmin, DSSData data,
			boolean storeFlags);

	/**
	 * store irregular time series
	 */
	private synchronized native void storeIrregularTimeSeriesData(
			String dssFile, String pathname, long startJulmin, long endJulmin,
			DSSData data, boolean storeFlags);

	/**
	 * store paired data
	 */
	private synchronized native void storePairedData(String dssFile,
			String pathname, DSSData data, boolean storeFlags);

	/**
	 * 
	 private DSSData storeTextData(String dssFile, String pathname){ TextData
	 * data = new TextData(); int status = retrieveTextData( dssFile, pathname,
	 * data); if (status != 0) return null; return data; }
	 */
	private static final boolean DEBUG = false;
}
