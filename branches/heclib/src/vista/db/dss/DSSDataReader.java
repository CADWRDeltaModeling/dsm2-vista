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

import hec.heclib.util.HecTime;
import hec.heclib.util.Heclib;
import hec.heclib.util.stringContainer;
import vista.set.DataType;

/**
 * 
 * The class with native function calls to the HEC-DSS library. Only limited
 * functionality of retriving data is available at this time. Storing data
 * options will be added later.
 * 
 * Does not allow multithreaded access. However multi-user access is allowed.
 * 
 * @author Nicky Sandhu
 * @version $Id: DSSDataReader.java,v 1.1 2003/10/02 20:48:44 redwood Exp $
 */
public class DSSDataReader {
	/**
   *
   */
	public DSSDataReader() {
	}

	/**
	 * generates a catalog for this dss file
	 */
	public void generateCatalog(String dssFile) {
		int[] ifltab = DSSUtil.openDSSFile(dssFile);
		int[] numberFound = new int[] { -1 };// way to indicate to create new
												// catalog
		int[] catalogUnit = new int[1];
		Heclib.makedsscatalog(dssFile, ifltab, "NEW COND", numberFound,
				catalogUnit);
		Heclib.closescratchdsscatalog(catalogUnit);
		DSSUtil.closeDSSFile(ifltab);
	}

	/**
	 * returns the type of record. Constant returned is defined in DSSUtil
	 */
	public int recordType(String dssFile, String pathname) {
		return getRecordType(dssFile, pathname);
	}

	/**
	 * Given a dssfile, pathname, starting and ending time in julian minutes
	 * since base date of Dec 31, 1899 2400 and a flag to retrieve data the data
	 * is retrieved from the data base.
	 * 
	 * @returns an DSSData object
	 */
	public DSSData getData(String dssFile, String pathname, long startJulmin,
			long endJulmin, boolean retrieveFlags) {
		try {
			DSSData data = new DSSData();
			int recType = recordType(dssFile, pathname);
			switch (recType) {
			case DataType.REGULAR_TIME_SERIES:
				data = getTimeSeriesData(dssFile, pathname, startJulmin,
						endJulmin, retrieveFlags);
				break;
			case DataType.IRREGULAR_TIME_SERIES:
				data = getIrregularTimeSeriesData(dssFile, pathname,
						startJulmin, endJulmin, retrieveFlags);
				break;
			case DataType.PAIRED:
				data = getPairedData(dssFile, pathname);
				break;
			// case DataType.TEXT:
			// data =
			// getTextData(dssFile, pathname);
			// break;
			default:
				data = null;
			}
			if (data != null)
				data._dataType = recType;
			return data;
		} catch (Exception e) {
			throw new RuntimeException(
					"Exception in reading from dss native methods: "
							+ e.getMessage());
		}
	}

	/**
	 * time series data
	 */
	private DSSData getTimeSeriesData(String dssFile, String pathname,
			long startJulmin, long endJulmin, boolean retrieveFlags) {
		DSSData data = new DSSData();
		if (DEBUG)
			System.out.println("Retrieveing time series");
		int status = retrieveRegularTimeSeries(dssFile, pathname, startJulmin,
				endJulmin, retrieveFlags, data);
		if (status >= 5)
			return null;
		return data;
	}

	/**
   *
   */
	private DSSData getIrregularTimeSeriesData(String dssFile, String pathname,
			long startJulmin, long endJulmin, boolean retrieveFlags) {
		DSSData data = new DSSData();
		int status = retrieveIrregularTimeSeries(dssFile, pathname,
				startJulmin, endJulmin, retrieveFlags, data);
		if (status != 0)
			return null;
		return data;
	}

	/**
   *
   */
	private DSSData getPairedData(String dssFile, String pathname) {
		DSSData data = new DSSData();
		int status = retrievePairedData(dssFile, pathname, data);
		if (status != 0)
			return null;
		return data;
	}

	/**
	 * 
	 private DSSData getTextData(String dssFile, String pathname){ TextData
	 * data = new TextData(); int status = retrieveTextData( dssFile, pathname,
	 * data); if (status != 0) return null; return data; }
	 */
	/**
	 * returns an integer for the type of record contained in the pathname
	 */
	private synchronized int getRecordType(String dssFile, String pathname) {
		int[] ifltab = DSSUtil.openDSSFile(dssFile);
		int[] checkedNumber = new int[] { 0 };
		stringContainer type = new stringContainer();
		int[] dataType = new int[] { 0 };
		int[] existsInt = new int[] { 0 };
		Heclib.zdtype(ifltab, pathname, checkedNumber, existsInt, type,
				dataType);
		if (existsInt[0] != 0) {
			throw new RuntimeException(" ** The pathname: " + pathname
					+ " does not exist in file: " + dssFile);
		}
		DSSUtil.closeDSSFile(ifltab);
		return dataType[0];
	}

	/**
	 * retrieves regular time series data for given dss file, pathname and
	 * beginning and ending minutes since Midnight Dec 31, 1899.
	 * 
	 * @return error code
	 */
	private synchronized int retrieveRegularTimeSeries(String dssFile,
			String pathname, long startJulmin, long endJulmin,
			boolean retrieveFlags, DSSData data) {
		int[] ifltab = DSSUtil.openDSSFile(dssFile);
		int nvals = getNumberOfValuesInInterval(startJulmin, endJulmin,
				pathname);
		int idate = (int) startJulmin / 1440;
		int itime = (int) startJulmin % 1440;
		String cdate = Heclib.juldat(idate, 104);
		stringContainer ctime = new stringContainer();
		itime = Heclib.m2ihm(itime, ctime);
		float[] values = new float[nvals];
		int[] flags = new int[0];
		if (retrieveFlags) {
			flags = new int[nvals];
		}
		int readFlags = retrieveFlags ? 1 : 0;
		int[] flagsWereRead = new int[1];

		stringContainer units = new stringContainer();
		stringContainer type = new stringContainer();
		// FIXME: it doesn't look like HEC uses this function call in their java
		// code. really this should have been
		// similar to their doublearrayContainer but instead I have to guess at
		// the max size of the header array
		int maxUserHead = 100;
		int[] userHead = new int[100];
		int[] numberHeadRead = new int[1];
		int[] offset = new int[1];
		int[] compression = new int[1];
		int[] istat = { 0 };
		Heclib.zrrtsx(ifltab, pathname, cdate, ctime.toString(), nvals, values,
				flags, readFlags, flagsWereRead, units, type, userHead,
				maxUserHead, numberHeadRead, offset, compression, istat);
		DSSUtil.closeDSSFile(ifltab);
		if (istat[0] <= 5) {
			data._dataType = DSSUtil.REGULAR_TIME_SERIES;
			data._numberRead = nvals;
			data._offset = offset[0];
			data._flags = flags;
			data._yValues = new double[nvals];
			for(int i=0; i < nvals; i++){
				data._yValues[i] = values[i];
			}
			data._yUnits = units.toString();
			data._yType = type.toString();
			return istat[0];
		} else if (istat[0] > 10) {
			throw new RuntimeException(" A fatal error occurred in file: "
					+ dssFile + " for pathname: " + pathname);
		} else {
			throw new RuntimeException(" An unknown error code: " + istat[0]
					+ " occurred when reading " + dssFile + " for pathname: "
					+ pathname);
		}
	}

	private int getNumberOfValuesInInterval(long startJulmin, long endJulmin,
			String pathname) {
		String[] pathParts = pathname.split("/");
		String ePart = pathParts[5];
		int[] interval = new int[1];
		int[] status = {1}; //1 => get integer interval from E part
		Heclib.zgintl(interval, ePart, new int[1], status);
		if (status[0] != 0){
			if (status[0] == 1){
				throw new RuntimeException("Irregular time E part: "+ePart);
			} else {
				throw new RuntimeException("Non-time series E part: "+ePart);
			}
		}
	     int startJulian = (int) startJulmin/1440;
	     int startTime = (int) startJulmin%1440;
	     int endJulian = (int) endJulmin/1440;
	     int endTime = (int) endJulmin%1440;
	     int nvals = HecTime.nopers(interval[0], startJulian, startTime, endJulian, endTime);
	     return nvals+1;// by 1 to include end of interval
	}

	/**
   *
   */
	private synchronized int retrieveIrregularTimeSeries(String dssFile,
			String pathname, long startJulmin, long endJulmin,
			boolean retrieveFlags, DSSData data) {
		throw new RuntimeException("Not yet implemented");
	}

	/**
   *
   */
	private synchronized int retrievePairedData(String dssFile,
			String pathname, DSSData data) {
		throw new RuntimeException("Not yet implemented!");
	}

	private final static boolean DEBUG = false;
}
