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

import hec.heclib.util.Heclib;
import hec.heclib.util.stringContainer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import vista.gui.VistaUtils;
import vista.set.DataReference;
import vista.set.DataRetrievalException;
import vista.set.DataSet;
import vista.set.DataSetAttr;
import vista.set.DataSetElement;
import vista.set.DataSetIterator;
import vista.set.DataType;
import vista.set.DefaultDataSet;
import vista.set.Group;
import vista.set.IrregularTimeSeries;
import vista.set.Pathname;
import vista.set.RegularTimeSeries;
import vista.set.Session;
import vista.set.TimeSeries;
import vista.time.Time;
import vista.time.TimeFactory;
import vista.time.TimeFormat;
import vista.time.TimeInterval;
import vista.time.TimeWindow;

/**
 * A class with utility functions
 * 
 * @author Nicky Sandhu
 * @version $Id: DSSUtil.java,v 1.1 2003/10/02 20:48:46 redwood Exp $
 */
public class DSSUtil {
	private static final boolean DEBUG = false;
	private static String _defaultServer, _defaultDir;
	private static boolean _retrieveFlags, _localAccess;
	private static int _portNumber = 1099; // default port for server access
	/**
	 * Undefined
	 */
	public static final int UNDEFINED = 0;
	/**
	 * Regular - Interval Time Series Data
	 */
	public static final int REGULAR_TIME_SERIES = 100;
	/**
	 * Irregular - Interval Time Series Data
	 */
	public static final int IRREGULAR_TIME_SERIES = 110;
	/**
	 * Paired Data
	 */
	public static final int PAIRED = 200;
	/**
	 * Text Data
	 */
	public static final int TEXT = 300;
	/**
	 * The standard DSS extension
	 */
	public final static String DSS_EXTENSION = ".dss";
	/**
	 * The standard catalog extension
	 */
	public final static String CATALOG_EXTENSION = ".dsd";

	/**
	 * creates a data reference with given server,file and pathname and a data
	 * set. If the dataset is null a reference is created which will retrieve
	 * the data from the given input parameters
	 */
	public static DataReference createDataReference(String server,
			String filename, String pathname, DataSet ds) {
		if (ds == null) {
			return DSSUtil.createDataReference(server, filename, Pathname
					.createPathname(pathname));
		}
		if (ds instanceof RegularTimeSeries)
			return createTimeSeriesReference(server, filename, pathname,
					(RegularTimeSeries) ds);
		else if (ds instanceof IrregularTimeSeries)
			return createTimeSeriesReference(server, filename, pathname,
					(IrregularTimeSeries) ds);
		else if (ds instanceof DefaultDataSet)
			return createDefaultReference(server, filename, pathname,
					(DefaultDataSet) ds);
		else
			throw new IllegalArgumentException("Type of dataset: "
					+ ds.getClass() + " not known");
	}

	/**
	 * creates a time series reference
	 */
	static DataReference createTimeSeriesReference(String server,
			String filename, String pathname, RegularTimeSeries ts) {
		return new DSSDataReference(server, filename, Pathname
				.createPathname(pathname), ts);
	}

	/**
	 * creates an irregular time series reference
	 */
	static DataReference createTimeSeriesReference(String server,
			String filename, String pathname, IrregularTimeSeries ts) {
		return new DSSDataReference(server, filename, Pathname
				.createPathname(pathname), ts);
	}

	/**
	 * creates a default reference ( paired data)
	 */
	static DataReference createDefaultReference(String server, String filename,
			String pathname, DefaultDataSet ds) {
		return new DSSDataReference(server, filename, Pathname
				.createPathname(pathname), ds);
	}

	/**
	 * reads in data from a DSS database
	 * 
	 * @param filename
	 *            name of dss data base
	 * @param pathname
	 *            name of data to be read
	 * @param retrieveFlags
	 *            true if flags are to be read
	 * @return the data set containing the values
	 * @see DataSet, RegularTimeSeries, IrregularTimeSeries, DefaultDataSet
	 */
	public static DataSet readData(String filename, String pathname,
			boolean retrieveFlags) {
		DSSDataReader reader = new DSSDataReader();
		int recType = reader.recordType(filename, pathname);
		Pathname path = Pathname.createPathname(pathname);
		if (recType == DSSUtil.REGULAR_TIME_SERIES || recType == DSSUtil.REGULAR_TIME_SERIES+5) {
			TimeInterval ti = DSSUtil.createTimeInterval(path);
			TimeWindow tw = DSSUtil.createTimeWindow(path);
			int st = (int) tw.getStartTime().getTimeInMinutes();
			int et = (int) tw.getEndTime().getTimeInMinutes();
			DSSData data = reader.getData(filename, pathname, st, et,
					retrieveFlags);
			int nread = (data == null) ? 0 : data._numberRead;
			if (nread <= 0)
				throw new IllegalArgumentException("Data " + filename + "::"
						+ pathname + " is empty");
			DataSetAttr attr = new DataSetAttr(DataType.REGULAR_TIME_SERIES,
					"TIME", data._yUnits, "", data._yType);
			Time stime = tw.getStartTime();
			return new RegularTimeSeries(filename + "::" + pathname, stime, ti,
					data._yValues, data._flags, attr);
		} else if (recType == DSSUtil.IRREGULAR_TIME_SERIES || recType == DSSUtil.IRREGULAR_TIME_SERIES+5) {
			TimeInterval ti = DSSUtil.createTimeInterval(path);
			TimeWindow tw = DSSUtil.createTimeWindow(path);
			int st = (int) tw.getStartTime().getTimeInMinutes();
			int et = (int) tw.getEndTime().getTimeInMinutes();
			DSSData data = reader.getData(filename, pathname, st, et,
					retrieveFlags);
			int nread = (data == null) ? 0 : data._numberRead;
			if (nread <= 0)
				throw new IllegalArgumentException("Data " + filename + "::"
						+ pathname + " is empty");
			DataSetAttr attr = new DataSetAttr(DataType.IRREGULAR_TIME_SERIES,
					"TIME", data._yUnits, "", data._yType);
			return new IrregularTimeSeries(filename + "::" + pathname,
					data._xValues, data._yValues, data._flags, attr);
		} else if (recType == DSSUtil.PAIRED) {
			DSSData data = reader.getData(filename, pathname, 0, 0,
					retrieveFlags);
			int nread = (data == null) ? 0 : data._numberRead;
			if (nread <= 0)
				throw new IllegalArgumentException("Data " + filename + "::"
						+ pathname + " is empty?");
			DataSetAttr attr = new DataSetAttr(DataType.PAIRED, data._xUnits,
					data._yUnits, data._xType, data._yType);
			return new DefaultDataSet(filename + "::" + pathname,
					data._xValues, data._yValues, data._flags, attr);
		} else {
			throw new IllegalArgumentException("Record for " + filename + "::"
					+ pathname + " is not recognized or is missing");
		}
	}

	/**
	 * writes out data to dss data base
	 * 
	 * @param filename
	 *            name of dss database
	 * @param pathname
	 *            name of data
	 * @param ds
	 *            data set
	 * @see Pathname
	 * @see DataSet
	 */
	public static void writeData(String filename, String pathname, DataSet ds) {

		long smin = 0;
		long emin = 0;
		if (ds instanceof TimeSeries) {
			TimeSeries ts = (TimeSeries) ds;
			smin = ts.getStartTime().getTimeInMinutes();
			emin = ts.getEndTime().getTimeInMinutes();
		}
		// convert to uppercase
		Pathname p = Pathname.createPathname(pathname);
		// set e part to correct interval for regular time series
		if (ds instanceof RegularTimeSeries) {
			TimeInterval ti = ((RegularTimeSeries) ds).getTimeInterval();
			p.setPart(Pathname.E_PART, ti.toString());
		}
		//
		new DSSDataWriter().storeData(filename, p.toString(), smin, emin, ds,
				ds.isFlagged());
	}

	/**
	 * gets the DSS type of the data
	 * 
	 * @see DSSUtil.PAIRED, DSSUtil.REGULAR_TIME_SERIES,
	 *      DSSUtil.IRREGULAR_TIME_SERIES
	 */
	public static int getDataType(DataReference ref) {
		Pathname path = ref.getPathname();
		String epart = path.getPart(Pathname.E_PART);
		if (epart.equals(""))
			return DSSUtil.PAIRED;
		if (epart.indexOf("IR-") >= 0)
			return DSSUtil.IRREGULAR_TIME_SERIES;
		if (DSSUtil.createTimeInterval(path) != null)
			return DSSUtil.REGULAR_TIME_SERIES;
		throw new IllegalArgumentException(ref + " is of unknown type");
	}

	/**
	 * generates a catalog for the given dss file
	 */
	public static void generateCatalog(String dssfile) {
		DSSDataReader reader = new DSSDataReader();
		reader.generateCatalog(dssfile);
	}

	/**
	 * true if name satisfies dss extension name
	 */
	public static boolean isValidDSSFile(String dssfile) {
		return (dssfile.toLowerCase().endsWith(DSS_EXTENSION));
	}

	/**
	 * true if name satisfies catalog extension name
	 */
	public static boolean isValidCatalogFile(String dsdfile) {
		return (dsdfile.toLowerCase().endsWith(CATALOG_EXTENSION));
	}

	/**
	 * given a dss extension replaces it with catalog extension
	 */
	public static String getCatalogFilename(String dssfile) {
		// replace with case sensitivity DSS_EXTENSION with CATLOG_EXTENSION
		char[] str = dssfile.toCharArray();
		str[str.length - 1] = 'd';// Character.isLowerCase(str[str.length-1]) ?
									// 'd': 'D';
		return new String(str);
	}

	/**
	 * returns a string with .dsd replaced by .dss
	 */
	public static String getDSSFilename(String dsdfile) {
		// replace with case sensitivity DSS_EXTENSION with CATLOG_EXTENSION
		char[] str = dsdfile.toCharArray();
		str[str.length - 1] = 's';// Character.isLowerCase(str[str.length-1]) ?
									// 's': 'S';
		return new String(str);
	}

	/**
	 * creates a data reference from a servername, filename, pathname. For
	 * calculating the timewindow it uses the pathnames D PART. The format for
	 * the D PART should be ddMMMyyyy HHmm - ddMMMyyyy HHmm or just ddMMMyyyy
	 * HHmm
	 */
	public static DataReference createDataReference(String serverName,
			String filename, Pathname path) {
		if (serverName == null || filename == null || path == null)
			throw new IllegalArgumentException("One of the arguments is null");
		return new DSSDataReference(serverName, filename, path);
	}

	/**
	 * creates a group from a server name, port number and filename
	 */
	public static Group createGroup(String serverName, String dssFilename) {
		// check dssFilename and servername
		if (dssFilename == null || serverName == null)
			throw new IllegalArgumentException("One of the arguments is null");
		if (!DSSUtil.isValidDSSFile(dssFilename))
			throw new IllegalArgumentException("Invalid dss filename "
					+ dssFilename);
		return new DSSGroup(serverName, dssFilename);
	}

	/**
	 * create a session of groups corresponding to the dssfilename. This is the
	 * default configuration where each group corresponds to each dssfilename.
	 * Subsequent groups created by the user are stored else where...
	 */
	public static Session createSession(String serverName, String directory) {
		return new DSSSession(serverName, directory);
	}

	/**
	 * set port number on which to contact the server
	 */
	public static void setClientPortNumber(int n) {
		_portNumber = n;
	}

	/**
	 * set port number on which to contact the server
	 */
	public static int getClientPortNumber() {
		return _portNumber;
	}

	/**
	 * these are the user names recognized by vista to allow for flagging of
	 * data. Make only additions to this array. Any deletion will result in
	 * mismatches with the older ids that were in use.
	 */
	public static final String[] USERS = { "psandhu", "rfinch", "kkao", "jamiea",
			"eli", "mmierzwa", "tara", "kle", "datachecker" };
	/**
	 * users with access to setting flags and writing to data base
	 */
	private static Hashtable _userIds;
	private static String[] _userIdsInverse;
	static {
		_userIds = new Hashtable();
		_userIdsInverse = new String[USERS.length + 1];
		_userIdsInverse[0] = null;
		for (int i = 0; i < USERS.length; i++) {
			_userIds.put(USERS[i], new Integer(i + 1));
			_userIdsInverse[i + 1] = USERS[i];
		}
	}

	/**
	 * checks if user with username has access to update database
	 */
	public static boolean isAuthorizedUser(String username) {
		return _userIds.get(username) == null ? false : true;
	}

	/**
	 * get user name for given id
	 */
	public static String getUserName(int id) {
		return _userIdsInverse[id];
	}

	/**
   *
   */
	public static int getUserId(String username) {
		Integer id = (Integer) _userIds.get(username);
		if (id == null)
			return 0;
		else
			return id.intValue();
	}

	/**
	 * gets user id for this user.
	 */
	public static int getUserId() {
		return getUserId(System.getProperty("user.name"));
	}

	private static String _passwd = null;

	/**
	 * updates database for the given reference and storing flags if storeFlags
	 * is true
	 */
	public static void updateData(DataReference ref, boolean storeFlags)
			throws Exception {
		String userId = System.getProperty("user.name");
		String server = ref.getServername();
		String filename = ref.getFilename();
		String path = ref.getPathname().toString();
		long startJulmin = 0, endJulmin = 0;
		TimeWindow tw = ref.getTimeWindow();
		if (tw != null) {
			startJulmin = tw.getStartTime().getTimeInMinutes();
			endJulmin = tw.getEndTime().getTimeInMinutes();
		}
		DataSet ds = ref.getData();
		DSSRemoteClient client = createRemoteClient(server,
				getClientPortNumber());
		if (_passwd == null)
			_passwd = VistaUtils.getPasswordFromDialog();

		try {
			client.storeData(ds, filename, path, startJulmin, endJulmin,
					storeFlags, userId, _passwd);
		} catch (Exception pe) {
			if (pe.getMessage().indexOf("password") >= 0) {
				JOptionPane.showMessageDialog(null,
						"Incorrect password: Try again");
				_passwd = VistaUtils.getPasswordFromDialog();
			} else {
				throw pe;
			}
			try {
				client.storeData(ds, filename, path, startJulmin, endJulmin,
						storeFlags, userId, _passwd);
			} catch (Exception e) {
				VistaUtils.displayException(null, e);
			}
		}
	}

	/**
	 * true if rmi server is being used
	 */
	static boolean useRMI = true;

	/**
	 * creates an object mirroring a remote client on given server and port
	 */
	static DSSRemoteClient createRemoteClient(String server, int port)
			throws InstantiationException {
		DSSRemoteClient client;
		if (server.equals("local")) { // && isLocalAccessEnabled() ) { this just
										// confuses
			try {
				client = new DSSRemoteClientImpl();
				return client;
			} catch (Exception e) {
				e.printStackTrace(System.err);
				throw new InstantiationException(e.getMessage());
			}
		} else {
			try {
				if (port != 1099)
					client = (DSSRemoteClient) java.rmi.Naming.lookup("//"
							+ server + ":" + port + "/DSSRemoteClientServer");
				else
					client = (DSSRemoteClient) java.rmi.Naming.lookup("//"
							+ server + "/DSSRemoteClientServer");
			} catch (Exception e) {
				e.printStackTrace(System.err);
				throw new InstantiationException(e.getMessage());
			}
		}
		return client;
	}

	/**
   *
   */
	static DSSCatalogReader createCatalogReader(String serverName,
			String filename, boolean redoCatalog) {
		DSSCatalogReader reader = null;
		try {
			DSSRemoteClient client = DSSUtil.createRemoteClient(serverName,
					getClientPortNumber());
			reader = new DSSCatalogReader(client.getCatalog(filename,
					redoCatalog));
		} catch (InstantiationException ie) {
			ie.printStackTrace(System.err);
			throw new RuntimeException("Nested Exception: " + ie);
		} catch (java.rmi.RemoteException re) {
			re.printStackTrace(System.err);
			throw new RuntimeException("Nested Exception: " + re);
		}
		return reader;
	}

	/**
	 * returns the instance of time factory used for this database
	 */
	public static TimeFactory getTimeFactory() {
		return _tf;
	}

	/**
   *
   */
	static TimeFactory _tf = TimeFactory.getInstance();
	/**
   *
   */
	static TimeFormat _dtf = _tf.getTimeFormatInstance().create("ddMMMyyyy");

	/**
	 * creates a time window from given pathname with given time interval format<br>
	 * ddMMMyyyy HHmm - ddMMMyyyy HHmm ; e.g. 01JAN1990 0100 - 01MAY1993 1400 <br>
	 * or<br>
	 * ddMMMyyyy HHmm
	 */
	public static TimeWindow createTimeWindow(Pathname path) {
		Time startTime = null, endTime = null;
		TimeInterval tipath = createTimeInterval(path);
		// check pathname validatity for creating time window
		String epart = path.getPart(Pathname.E_PART);
		String dpart = path.getPart(Pathname.D_PART);
		if (epart == null || dpart == null)
			return null;
		if (epart.equals(""))
			return null;
		if (tipath == null)
			return null;
		// create a time window from dpart ddMMMYYYY HHmm [- ddMMMYYYY HHmm]
		StringTokenizer st = new StringTokenizer(dpart, "-");
		if (st.countTokens() == 0 || st.countTokens() > 2)
			return null;
		String token = null;
		try {
			token = st.nextToken().trim();
			startTime = _tf.createTime(token, "ddMMMyyyy", _dtf);
			// startTime.incrementBy( createTimeInterval(path) ); // one more
			// increment
			if (st.hasMoreTokens()) {
				token = st.nextToken().trim();
				endTime = _tf.createTime(token, "ddMMMyyyy", _dtf);
			} else {
				// handle case of only one token
				endTime = startTime.create(startTime);
			}
		} catch (IllegalArgumentException pe) {
			System.out.println("Error getting time from " + token);
			return null;
		}
		// create time interval from epart ( not same as time interval of data
		// rather
		// it is the amount the actual end of time window is from given end
		// time.
		String incr = "";
		if (epart.indexOf(TimeInterval.MIN_INTERVAL_STR) > 0) {
			if (tipath.compare(_tf.createTimeInterval("15MIN")) >= 0)
				incr = 1 + TimeInterval.MONTH_INTERVAL_STR; // stored in month
															// blocks
			else
				incr = 1 + TimeInterval.DAY_INTERVAL_STR; // stored in day
															// blocks
		} else if (epart.indexOf(TimeInterval.HOUR_INTERVAL_STR) > 0) {
			incr = 1 + TimeInterval.MONTH_INTERVAL_STR; // stored in month
														// blocks
		} else if (epart.indexOf(TimeInterval.DAY_INTERVAL_STR) > 0) {
			incr = 1 + TimeInterval.YEAR_INTERVAL_STR; // stored in year blocks
		} else if (epart.indexOf(TimeInterval.WEEK_INTERVAL_STR) > 0
				|| epart.indexOf(TimeInterval.MONTH_INTERVAL_STR) > 0) {
			incr = 1 + TimeInterval.DECADE_INTERVAL_STR; // stored in decade
															// blocks
		} else if (epart.indexOf(TimeInterval.YEAR_INTERVAL_STR) > 0
				|| epart.indexOf(TimeInterval.DECADE_INTERVAL_STR) > 0
				|| epart.indexOf(TimeInterval.CENTURY_INTERVAL_STR) > 0) {
			incr = 1 + TimeInterval.CENTURY_INTERVAL_STR;
		} else {
			return null;
		}
		// get time interval
		TimeInterval ti = _tf.createTimeInterval(incr);
		// endTime.incrementBy(tipath.createByMultiplying(-1)); // decrement by
		// 1 interval
		if (epart.indexOf("IR") < 0) {
			startTime.incrementBy(tipath);
			// end Time = end time from d part + time interval from e part
			if (ti != null)
				endTime.incrementBy(ti); // regular block size varies with
											// interval
		} else {
			endTime.incrementBy(tipath); // irregular block size is same as
											// interval
		}

		TimeWindow tw = _tf.createTimeWindow(startTime, endTime);
		return tw;
	}

	/**
	 * creates a time interval from the given path. The time interval is
	 * determined from the E PART of the pathname
	 */
	public static TimeInterval createTimeInterval(Pathname path) {
		String epart = path.getPart(Pathname.E_PART);
		String interval = epart;
		if (interval.trim().equals(""))
			return null;
		if (epart.indexOf("IR-") >= 0) {
			interval = 1 + epart.substring(3, epart.length());
		}
		return _tf.createTimeInterval(interval);
	}

	/**
	 * the default server for the dss files
	 */
	public static String getDefaultServer() {
		return _defaultServer;
	}

	/**
	 * the default directory for the database
	 */
	public static String getDefaultDirectory() {
		return _defaultDir;
	}

	/**
	 * true if flags are to be retrieved
	 */
	public static boolean areFlagsRetrieved() {
		return _retrieveFlags;
	}

	/**
	 * true if local access is enabled. This means the server "local" is not
	 * interpreted as a machine name
	 */
	public static boolean isLocalAccessEnabled() {
		return _localAccess;
	}

	/**
	 * sets the access properties from the given properties object. The
	 * properties set are default.server, default.dir, default.port dss.flags,
	 * dss.localAccess
	 */
	public static void setAccessProperties(Properties props) {
		String str = props.getProperty("default.server");
		if (str != null)
			_defaultServer = str;
		str = props.getProperty("default.dir");
		if (str != null)
			_defaultDir = str;
		str = props.getProperty("default.port");
		if (str != null)
			_portNumber = new Integer(str).intValue();
		str = props.getProperty("dss.flags");
		if (str != null) {
			if (str.indexOf("true") >= 0)
				_retrieveFlags = true;
			else
				_retrieveFlags = false;
		}
		str = props.getProperty("dss.localAccess");
		if (str != null) {
			if (str.indexOf("true") >= 0)
				_localAccess = true;
			else
				_localAccess = false;
		}
	}

	/**
	 * writes in text format dssts or dssits compatible.
	 * 
	 * @param refs
	 *            an array of data references to be written out
	 * @dssFile the name of the dss file which appears in ts or its format
	 * @textFile the name of the file to which the output is written to.
	 */
	public static void writeText(DataReference[] refs, String dssFile,
			String textFile) throws IOException {
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(
				textFile)));
		writer.println(dssFile);
		try {
			for (int i = 0; i < refs.length; i++) {
				int dataType = DSSUtil.getDataType(refs[i]);
				switch (dataType) {
				case DSSUtil.REGULAR_TIME_SERIES:
					writeTS(writer, refs[i], dssFile);
					break;
				case DSSUtil.IRREGULAR_TIME_SERIES:
					writeITS(writer, refs[i], dssFile);
					break;
				case DSSUtil.PAIRED:
					writePaired(writer, refs[i], dssFile);
					break;
				default:
					;
				}
			}
		} catch (IOException ioe) {
			throw new IOException(ioe.getMessage());
		} finally {
			writer.println("FINISH");
			writer.close();
		}
	}

	/**
   *
   */
	static void writeTS(PrintWriter writer, DataReference ref, String dssFile)
			throws IOException {
		writer.println(ref.getPathname());
		DataSet ds = null;
		try {
			ds = ref.getData();
		} catch (DataRetrievalException e) {
			throw new IOException("Exception: " + e.getMessage()
					+ " retrieving data");
		}
		DataSetAttr attr = ds.getAttributes();
		writer.println(attr.getYUnits());
		if (attr.getYType() == null)
			writer.println("INST-VAL");
		else
			writer.println(attr.getYType());
		DataSetIterator dsi = ds.getIterator();
		DataSetElement dse = dsi.getElement();
		Time stime = DSSUtil.getTimeFactory().getTimeInstance().create(
				Math.round(dse.getX()));
		writer.println(stime);
		while (!dsi.atEnd()) {
			writer.println(dsi.getElement().getY());
			dsi.advance();
		}
		writer.println("END");
	}

	/**
   *
   */
	static void writeITS(PrintWriter writer, DataReference ref, String dssFile)
			throws IOException {
		writer.println(ref.getPathname());
		DataSet ds = null;
		try {
			ds = ref.getData();
		} catch (DataRetrievalException e) {
			throw new IOException("Exception: " + e.getMessage()
					+ " retrieving data");
		}
		//
		DataSetAttr attr = ds.getAttributes();
		writer.println(attr.getYUnits());
		if (attr.getYType() == null)
			writer.println("INST-VAL");
		else
			writer.println(attr.getYType());
		DataSetIterator dsi = ds.getIterator();
		Time tp = DSSUtil.getTimeFactory().getTimeInstance();
		Time stime = null;
		while (!dsi.atEnd()) {
			stime = tp.create(Math.round(dsi.getElement().getX()));
			writer.print(stime);
			writer.print(" ");
			writer.println(dsi.getElement().getY());
			dsi.advance();
		}
		writer.println("END");
	}

	/**
   *
   */
	static void writePaired(PrintWriter writer, DataReference ref,
			String dssFile) throws IOException {
		throw new IOException("Not implemented write paired yet");
	}

	/**
    *
    */
	public static String getBlockStart(TimeSeries ts) {
		TimeWindow tw = ts.getTimeWindow();
		TimeInterval ti = null;
		if (ts instanceof RegularTimeSeries) {
			ti = ((RegularTimeSeries) ts).getTimeInterval();
		} else {
			String blockInterval = ((IrregularTimeSeries) ts)
					.getBlockInterval();
			String interval = blockInterval.substring(blockInterval
					.indexOf("-") + 1, blockInterval.length());
			ti = TimeFactory.getInstance().createTimeInterval(interval);
		}
		Time stime = tw.getStartTime().floor(ti);
		SimpleDateFormat sdf1 = new SimpleDateFormat("ddMMMyyyy HHmm");
		SimpleDateFormat sdf2 = new SimpleDateFormat("ddMMMyyyy");
		String blockStart = null;
		try {
			blockStart = sdf2.format(sdf1.parse(stime.toString()));
		} catch (ParseException pe) {
			throw new RuntimeException("Could not parse out start block date!!");
		}
		blockStart = blockStart.toUpperCase();
		return blockStart;
	}

	public static int[] openDSSFile(String dssFile, boolean toWrite){
		Heclib.zset("PROGRAM", "VISTA", 0);
		Heclib.zset("MLEVEL", "", 0);
		Heclib.zset("CCDATE", "ON", 0);
		stringContainer outName = new stringContainer();
		boolean exists = Heclib.zfname(dssFile, outName );
		if (!exists && !toWrite){
			throw new RuntimeException("** The DSS File does not exist: "+dssFile);
		}
		int[] ifltab = new int[600];
		int[] status = new int[1];
		Heclib.zopen(ifltab, dssFile, status);
		if ( status[0] != 0){
			throw new RuntimeException(" *** Error in opening DSS File: "+dssFile+ " Status: "+ status[0]);
		}
		return ifltab;
	}
	
	public static void closeDSSFile(int[] ifltab){
     	Heclib.zclose(ifltab);
	}
}
