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
	 * load the dss library
	 */
	static {
		DSSUtil.loadDSSLibrary();
	}

	/**
   *
   */
	public DSSDataReader() {
	}

	/**
	 * generates a catalog for this dss file
	 */
	public native void generateCatalog(String dssFile);

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
			DSSData data = null;
			long MAXVAL = 9000L;
			int recType = recordType(dssFile, pathname);
			long interval, nvals, ntry, st, et;
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
		if (status != 0)
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
	private synchronized native int getRecordType(String dssFile,
			String pathname);

	/**
	 * retrieves regular time series data for given dss file, pathname and
	 * beginning and ending minutes since Midnight Dec 31, 1899.
	 * 
	 * @return error code
	 */
	private synchronized native int retrieveRegularTimeSeries(String dssFile,
			String pathname, long startJulmin, long endJulmin,
			boolean retrieveFlags, DSSData data);

	/**
   *
   */
	private synchronized native int retrieveIrregularTimeSeries(String dssFile,
			String pathname, long startJulmin, long endJulmin,
			boolean retrieveFlags, DSSData data);

	/**
   *
   */
	private synchronized native int retrievePairedData(String dssFile,
			String pathname, DSSData data);

	private final static boolean DEBUG = false;
}
