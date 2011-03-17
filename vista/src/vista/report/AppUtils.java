package vista.report;import java.awt.Dimension;import java.awt.Toolkit;import java.io.File;import java.util.Vector;import javax.swing.JComboBox;import javax.swing.JDialog;import javax.swing.JFrame;import javax.swing.JOptionPane;import vista.app.DataGraph;import vista.app.DataTableFrame;import vista.app.DefaultGraphBuilder;import vista.app.MultiDataTableFrame;import vista.db.dss.DSSUtil;import vista.graph.Axis;import vista.graph.AxisAttr;import vista.graph.Graph;import vista.graph.MultiPlot;import vista.graph.Plot;import vista.set.DataReference;import vista.set.DataSet;import vista.set.DataSetAttr;import vista.set.DataSetElement;import vista.set.DataSetIterator;import vista.set.Group;import vista.set.PathPartPredicate;import vista.set.Pathname;import vista.set.RegularTimeSeries;import vista.set.SetUtils;import vista.set.TimeSeriesMath;import vista.time.Time;import vista.time.TimeFactory;import vista.time.TimeInterval;import vista.time.TimeWindow;/** *  * contains the common utility functions *  *  *  * @author Nicky Sandhu *  * @version $Id: AppUtils.java,v 1.1 2003/10/02 20:49:16 redwood Exp $ *  *          Last change: AM 21 Jun 2000 5:41 pm */public class AppUtils {	public static boolean DEBUG = true;	/**	 * 	 * true if graph is needed	 */	public static boolean viewGraph = true;	/**
   *
   */	public static boolean viewTable = false;	/**
   *
   */	public static boolean viewMonthlyTable = false;	/**
    *
    */	public static boolean useCFS = true;	/**
    *
    */	public static boolean useStoredUnits = false;	/**
    *
    */	public static boolean plotComparitive = false;	/**
    *
    */	public static boolean plotDifference = false;	/**	 * 	 * true if monthly report shows data by water year	 */	public static boolean _isWaterYear = true;	/**	 * 	 * sort by water year type classification	 */	public static boolean _show60_20_20 = false;	/**	 * 	 * sort by water year type classification	 */	public static boolean _show40_30_30 = false;	/**	 * 	 * the svar string	 */	public static String SVAR = "SVAR";	/**	 * 	 * the dvar string	 */	public static String DVAR = "DVAR";	/**	 * 	 * units string for flow in cubic feet per second	 */	public static String CFS = "CFS";	/**	 * 	 * units string for flow in thousand acre-feet/ month	 */	public static String TAF = "TAF";	/**	 * 	 * the default time window	 */	public static String DEFAULT_TIME_WINDOW = "OCT1921 - SEP1994";	/**	 * 	 * the default sizes for plots	 */	public static Dimension DEFAULT_PLOT_SIZE = new Dimension(750, 650);	public static Dimension DEFAULT_TABLE_SIZE = new Dimension(300, 700);	public static Dimension DEFAULT_MT_SIZE = new Dimension(750, 300);	/**	 * 	 * returns a data reference which exists in a dss file/group with	 * 	 * an exact b part "bpart" and and exact c part "cpart" and	 * 	 * a time window. If no data reference is found a null is	 * 	 * returned.	 * 	 * Pre-conditions:	 * 	 * 1. If either part is null only one part is matched and the	 * 	 * first reference matching is returned.	 * 	 * 2. If both parts are null then null is returned.	 * 	 * 3. If time window is greater then existing time window a	 * 	 * reference with intersecting time window is returned. If	 * 	 * no reference is	 * 	 * 4. If time window is null the default time window in the	 * 	 * group is returned	 * 	 * Post-conditions:	 * 	 * 1. If more than one data reference is matched then a	 * 	 * warning message is printed to the output and the first matching	 * 	 * is returned.	 * 	 * 2. If an error occurs or if the inputs are not valid or if	 * 	 * nothing is found for the given inputs a null reference is	 * 	 * returned.	 * 	 * 	 */	public static DataReference getDataReference(Group dssGroup,	String bpart, String cpart, TimeWindow tw) {		DataReference ref = null;		if (dssGroup == null)			return null;		Group gc = Group.createGroup(dssGroup);		// do mapping		//		if (bpart != null && !bpart.equals(""))			gc.filterBy(new PathPartPredicate("^" + bpart + "$",					Pathname.B_PART), true);		// no bpart found		if (gc.getNumberOfDataReferences() == 0) {			System.err.println("No matching reference in " + dssGroup.getName()			+ " for bpart = " + bpart);			throw new RuntimeException("No matching reference in "					+ dssGroup.getName()					+ " for bpart = " + bpart);			// return null;		}		if (cpart != null && !cpart.equals(""))			gc.filterBy(new PathPartPredicate("^" + cpart + "$",					Pathname.C_PART), true);		if (gc.getNumberOfDataReferences() > 1) {			System.err.println("Warning: " + dssGroup.getName()			+ " has more than one references for bpart = " + bpart			+ " and cpart = " + cpart);			ref = gc.getDataReference(0);		} else if (gc.getNumberOfDataReferences() == 0) {			throw new RuntimeException("No matching reference in "					+ dssGroup.getName()					+ " for cpart = " + cpart + " & bpart = " + bpart);		} else {			ref = gc.getDataReference(0);		}		// set time window		if (ref != null) {			if (tw != null)				ref = DataReference.create(ref, tw);		}		//		return ref;	}	/**	 * 	 * gets the pathnames for the matching parts array and fileType	 * 	 * If any part is null it is ignored for a match. This method	 * 	 * @param parts	 *            The pathname part from A to F	 * 	 * @param fileType	 *            AppUtils.SVAR or AppUtils.DVAR	 * 	 * @return an array of data references or null if no file found	 */	public static DataReference[] getDataReferences(String[] parts,			String fileType) {		/*		 * 		 * Group dssGroup = null;		 * 		 * if ( fileType.equals(SVAR) )		 * 		 * dssGroup = AppUtils.getCurrentProject().getSVGroup();		 * 		 * else		 * 		 * dssGroup = AppUtils.getCurrentProject().getDVGroup();		 * 		 * TimeWindow tw = AppUtils.getCurrentProject().getTimeWindow();		 * 		 * //		 * 		 * if ( dssGroup == null ) return null;		 * 		 * Group gc = Group.createGroup(dssGroup);		 * 		 * for(int i=0; i < parts.length; i++){		 * 		 * String part = parts[i];		 * 		 * if (i == Pathname.D_PART) continue;		 * 		 * if ( parts[i] != null ) part = replace(parts[i],"*",".*?");		 * 		 * if ( part != null && !part.equals("")){		 * 		 * part = "^"+part+"$";		 * 		 * gc.filterBy(true, new PathPartPredicate(part,i));		 * 		 * }		 * 		 * if ( gc.getNumberOfDataReferences() == 0 ){		 * 		 * throw new RuntimeException("No matching reference in " +		 * dssGroup.getName()		 * 		 * + " for part = " + part);		 * 		 * }		 * 		 * }		 * 		 * int count = gc.getNumberOfDataReferences();		 * 		 * DataReference [] refs = new DataReference[count];		 * 		 * for(int i=0; i< count; i++){		 * 		 * DataReference ref = DataReference.create(gc.getDataReference(i),tw);		 * 		 * // some references may be non-time series		 * 		 * refs[i] = ref == null ? gc.getDataReference(i) : ref;		 * 		 * }		 * 		 * return refs;		 */return null;	}	/**	 * 	 * opens a dss file or returns null if it could not open file	 */	public static Group openDSSFile(String dssfile) {		if (dssfile == null)			return null;		try {			return DSSUtil.createGroup("local", dssfile);		} catch (Exception e) {			System.err.println("Exception: " + e.getMessage());			return null;		}	}	/**	 * 	 * plots a single data reference. This is a preliminary style of plotting	 * 	 * based on vista. This can be improved later ??	 */	public static JFrame plot(DataReference ref) {		DefaultGraphBuilder gb = new DefaultGraphBuilder();		gb.addData(ref);		Graph graph[] = gb.createGraphs();		for (int i = 0; i < graph.length; i++) {			MultiPlot mp = null;			if (graph[i].getPlot() instanceof MultiPlot)				mp = (MultiPlot) graph[i].getPlot();			else				mp = null;			Plot[] plots = null;			if (mp != null)				plots = mp.getAllPlots();			else				plots = new Plot[] { graph[i].getPlot() };			for (int j = 0; j < plots.length; j++) {				Axis baxis = plots[j].getAxis(AxisAttr.BOTTOM);				TimeFactory tf = TimeFactory.getInstance();				TimeWindow tw = tf						.createTimeWindow("01OCT1921 0000 - 01OCT1998 0000");				// TimeWindow tw = AppUtils.getCurrentProject().getTimeWindow();				Time stime = tw.getStartTime();				stime = stime.create(stime);				stime.incrementBy(TimeFactory.getInstance().createTimeInterval(						"-1MON"));				Time etime = tw.getEndTime();				baxis.setDCRange(stime.getTimeInMinutes(), etime						.getTimeInMinutes());			}		}		DataGraph dg = new DataGraph(graph[0], "Graph", false);		dg.setSize(DEFAULT_PLOT_SIZE); // set to 8.5x11 for landscape printing.		Toolkit tk = dg.getToolkit();		Dimension screenSize = tk.getScreenSize();		Dimension frameSize = dg.getSize();		dg.setLocation(screenSize.width - frameSize.width,		screenSize.height - frameSize.height);		return dg;	}	/**	 * 	 * plots a single data reference. This is a preliminary style of plotting	 * 	 * based on vista. This can be improved later ??	 */	public static JFrame plot(DataReference[] refs) {		if (refs == null)			return null;		if (refs.length == 1) {			return plot(refs[0]);		}		DefaultGraphBuilder gb = new DefaultGraphBuilder();		for (int i = 0; i < refs.length; i++) {			if (refs[i] != null)				gb.addData(refs[i]);		}		Graph graph[] = gb.createGraphs();		if (graph == null)			return null;		DataGraph dg = new DataGraph(graph[0], "Graph", false);		dg.setSize(DEFAULT_PLOT_SIZE); // set to 8.5x11 for landscape printing.		Toolkit tk = dg.getToolkit();		Dimension screenSize = tk.getScreenSize();		Dimension frameSize = dg.getSize();		dg.setLocation(screenSize.width - frameSize.width,		screenSize.height - frameSize.height);		return dg;	}	/**	 * 	 * tabulates a single data reference based on vista's DataTable	 * 	 * @see vista.app.DataTableFrame	 */	public static JFrame tabulate(DataReference ref) {		if (ref == null)			return null;		JFrame fr = new DataTableFrame(ref, false);		Toolkit tk = fr.getToolkit();		fr.setSize(DEFAULT_TABLE_SIZE);		Dimension screenSize = tk.getScreenSize();		Dimension frameSize = fr.getSize();		fr.setLocation(screenSize.width - frameSize.width,		screenSize.height - frameSize.height);		return fr;	}	/**	 * 	 * tabulates a single data reference based on vista's DataTable	 * 	 * @see vista.app.DataTableFrame	 */	public static JFrame tabulate(DataReference[] refs) {		JFrame fr = null;		if (refs == null)			return fr;		if (refs.length == 1) {			fr = new DataTableFrame(refs[0], false);		}		fr = new MultiDataTableFrame(refs, false);		Toolkit tk = fr.getToolkit();		fr.setSize(DEFAULT_TABLE_SIZE);		Dimension screenSize = tk.getScreenSize();		Dimension frameSize = fr.getSize();		fr.setLocation(screenSize.width - frameSize.width,		screenSize.height - frameSize.height);		return fr;	}	/**
   *
   */	public static JFrame monthlyTable(DataReference ref) {		MonthlyTableDisplay mtd = null;		if (_show60_20_20 && MT_60_20_20 != null) {			mtd = new MonthlyTableDisplay(ref, _isWaterYear, MT_60_20_20);		} else if (_show40_30_30 && MT_40_30_30 != null) {			mtd = new MonthlyTableDisplay(ref, _isWaterYear, MT_40_30_30);		} else {			mtd = new MonthlyTableDisplay(ref, _isWaterYear, null);		}		JFrame fr = new DefaultFrame(mtd);		fr.setSize(DEFAULT_MT_SIZE);		Toolkit tk = fr.getToolkit();		Dimension screenSize = tk.getScreenSize();		Dimension frameSize = fr.getSize();		fr.setLocation(screenSize.width - frameSize.width,		screenSize.height - frameSize.height);		return fr;	}	/**
   *
   */	public static JFrame monthlyTable(DataReference[] refs) {		MonthlyTableDisplay mtd = null;		if (_show60_20_20 && MT_60_20_20 != null) {			mtd = new MonthlyTableDisplay(refs, _isWaterYear, MT_60_20_20);		} else if (_show40_30_30 && MT_40_30_30 != null) {			mtd = new MonthlyTableDisplay(refs, _isWaterYear, MT_40_30_30);		} else {			mtd = new MonthlyTableDisplay(refs, _isWaterYear, null);		}		JFrame fr = new DefaultFrame(mtd);		fr.setSize(DEFAULT_MT_SIZE);		Toolkit tk = fr.getToolkit();		Dimension screenSize = tk.getScreenSize();		Dimension frameSize = fr.getSize();		fr.setLocation(screenSize.width - frameSize.width,		screenSize.height - frameSize.height);		return fr;	}	/**	 * 	 * gets the operation id which is one of the id's defined	 * 	 * in TimeSeriesMath class	 * 	 * @param operationStr	 *            is one of +,-,*,/	 * 	 * @return the operation id or -1 if none is found	 * 	 * @see vista.set.TimeSeriesMath	 */	public static int getOperationId(String operationStr) {		String str = operationStr;		if (str.equals("+")) {			return TimeSeriesMath.ADD;		} else if (str.equals("-")) {			return TimeSeriesMath.SUB;		} else if (str.equals("*")) {			return TimeSeriesMath.MUL;		} else if (str.equals("/")) {			return TimeSeriesMath.DIV;		} else {			return -1;		}	}	/**	 * 	 * gets the operation name for the given operation id	 * 	 * @param operation	 *            id one of TimeSeriesMath.ADD|SUB|MUL|DIV	 * 	 * @see vista.set.TimeSeriesMath	 */	public static String getOperationName(int operationId) {		switch (operationId) {		case TimeSeriesMath.ADD:			return "+";		case TimeSeriesMath.SUB:			return "-";		case TimeSeriesMath.MUL:			return "*";		case TimeSeriesMath.DIV:			return "/";		default:			return "?";		}	}	/**	 * 	 * displays error by writing it out to the error stream.	 * 	 * This can be changed later to display the message to some	 * 	 * console or dialog window. ??	 */	public static void displayError(String msg) {		System.err.println(msg);	}	/**	 * 	 * This method attempts the guess the time window from the	 * 	 * given group by filtering for "FLOW" and then reading upto	 * 	 * five pathnames and taking their intersection	 */	public static TimeWindow guessTimeWindowFromGroup(Group g) {		if (g == null)			return null;		Group gc = Group.createGroup(g);		TimeWindow tw = null;		gc.filterBy("FLOW"); // look at flow pathnames		if (gc.getNumberOfDataReferences() == 0)			return null;		int count = gc.getNumberOfDataReferences();		int max5 = Math.max(count, 5);		tw = gc.getDataReference(0).getTimeWindow();		for (int i = 1; i < max5; i++) {			TimeWindow tw2 = gc.getDataReference(i).getTimeWindow();			tw = tw.intersection(tw2);		}		return tw;	}	private static Group _dv1g, _sv1g, _dv2g, _sv2g;	private static TimeWindow _tw;	public static String[] _bparts = null, _cparts = null;	/**	 * 	 * guesses a unique list of b parts from given group	 * 	 * 	 * 	 * @return a unique list of b parts or a list of exactly one	 * 	 *         empty string if none found	 */	public static String[] guessListOfBparts(Group g) {		if (g == null)			return new String[] { "" };		Vector parts = new Vector();		Group gc = Group.createGroup(g);		int count = gc.getNumberOfDataReferences();		if (count == 0)			return new String[] { "" };		//		for (int i = 0; i < count; i++) {			String str = gc.getDataReference(i).getPathname().getPart(					Pathname.B_PART);			if (!parts.contains(str))				parts.addElement(str);		}		//		if (parts.size() == 0)			return new String[] { "" };		else {			String[] bparts = new String[parts.size()];			parts.copyInto(bparts);			return bparts;		}	}	/**	 * 	 * guesses a unique list of c parts from a given group	 * 	 * @return a unique list of c parts or a list of exactly one	 * 	 *         empty string if none found	 */	public static String[] guessListOfCparts(Group g) {		if (g == null)			return new String[] { "" };		Vector parts = new Vector();		Group gc = Group.createGroup(g);		int count = gc.getNumberOfDataReferences();		if (count == 0)			return new String[] { "" };		//		for (int i = 0; i < count; i++) {			String str = gc.getDataReference(i).getPathname().getPart(					Pathname.C_PART);			if (!parts.contains(str))				parts.addElement(str);		}		//		if (parts.size() == 0)			return new String[] { "" };		else {			String[] cparts = new String[parts.size()];			parts.copyInto(cparts);			if (DEBUG)				System.out.println("In guessCpart, cparts.length="						+ cparts.length);			return cparts;		}	}	/**	 * 	 * returns the filename associated with the given group	 */	public static String getFilename(Group g) {		if (g == null)			return "";		// modify DSSUtil to contain this separator string rather than		// hardwiring it here ??		String gname = g.getName();		if (gname == null)			return "";		int index = gname.indexOf("::");		if (index < 0)			return "";		return gname.substring(index + 2, gname.length());	}	/**	 * 	 * This method queries for a pathname with the given bpart and cpart	 * 	 * in the varType Group.	 * 	 * @param varType	 *            The type of the variable SVAR or DVAR	 * 	 * @param studyNumber	 *            The study #1 or #2	 * 	 * @param bpart	 *            The b part	 * 	 * @param cpart	 *            The c part	 */	public static DataReference getDataReference(int studyNumber,	String bpart, String cpart) {		/*		 * 		 * Project prj = getCurrentProject();		 * 		 * TimeWindow tw = prj.getTimeWindow();		 * 		 * DataReference ref = null;		 * 		 * // get mappings and convert it any		 * 		 * PathPartMapping map = getMapping(studyNumber);		 * 		 * if ( map != null ){		 * 		 * String [] strArray = map.getMap(new String []{bpart,cpart});		 * 		 * if ( strArray != null )		 * 		 * bpart = strArray[0]; cpart= strArray[1];		 * 		 * }		 * 		 * //		 * 		 * if ( studyNumber == 1 ){		 * 		 * //		 * 		 * try {		 * 		 * ref = getDataReference(prj.getDVGroup(), bpart, cpart, tw);		 * 		 * }catch(Exception e){		 * 		 * ref = getDataReference(prj.getSVGroup(), bpart, cpart, tw);		 * 		 * }		 * 		 * } else if ( studyNumber == 2 ){		 * 		 * //		 * 		 * try {		 * 		 * ref = getDataReference(prj.getDV2Group(), bpart, cpart, tw);		 * 		 * }catch(Exception e){		 * 		 * ref = getDataReference(prj.getSV2Group(), bpart, cpart, tw);		 * 		 * }		 * 		 * }		 * 		 * else		 * 		 * throw new RuntimeException("");		 * 		 * return ref;		 */return null;	}	/**	 * 	 * displays data for the given bpart and cpart	 */	public static JFrame[] displayData(String bpart, String cpart) {		// first look in dv group then in sv group		/*		 * 		 * DataReference ref1 = null;		 * 		 * try {		 * 		 * ref1 = getDataReference(1,bpart, cpart);		 * 		 * }catch(RuntimeException e){		 * 		 * ref1 = null;		 * 		 * }		 * 		 * if (ref1 != null) changeToCurrentUnits(ref1);		 * 		 * Project prj = AppUtils.getCurrentProject();		 * 		 * //		 * 		 * if ( ref1 == null )		 * 		 * throw new RuntimeException("No matching data in study 1 found for " +		 * cpart + " at " + bpart);		 * 		 * else {		 * 		 * if ( plotDifference || plotComparitive ){		 * 		 * if ( ! isInComparableState(prj) )		 * 		 * throw new		 * RuntimeException("Cannot compare without loading base and compare files"		 * );		 * 		 * DataReference ref2 = null;		 * 		 * try {		 * 		 * ref2 = getDataReference(2,bpart,cpart);		 * 		 * }catch(RuntimeException re){		 * 		 * ref2 = null;		 * 		 * }		 * 		 * if ( ref2 == null )		 * 		 * throw new RuntimeException("No matching data in study 2 found for " +		 * cpart + " at " + bpart);		 * 		 * if (ref2 != null) changeToCurrentUnits(ref2);		 * 		 * if ( plotDifference ){		 * 		 * return displayData(ref2.__sub__(ref1));		 * 		 * }else		 * 		 * return displayData(new DataReference[]{ref1,ref2});		 * 		 * } else {		 * 		 * return displayData(ref1);		 * 		 * }		 * 		 * }		 */return null;	}	/**
    *
    */	private static void changeUnknownToTAF(DataReference ref) {		// Kludge requested by A. Munevar ????#$!		// Query user if units are unknown		DataSet ds = ref.getData();		DataSetAttr attr = ds.getAttributes();		if (attr.getYUnits().equals("UNKNOWN")) {			String[] possibleValues = { AppUtils.TAF, AppUtils.CFS, "NONE" };			JOptionPane pane = new JOptionPane("Choose Units for "					+ ref.getPathname().toString(),			JOptionPane.INFORMATION_MESSAGE,			JOptionPane.OK_OPTION,			null, null, null);			pane.setWantsInput(true);			pane.setSelectionValues(possibleValues);			pane.setInitialSelectionValue(AppUtils.getCurrentUnits());			// show the dialog			JDialog dialog = pane.createDialog(null, "Choose Units");			pane.selectInitialValue();			JComboBox jcbox = (JComboBox) GuiUtils.getComponent(					JComboBox.class,					dialog.getContentPane());			jcbox.setEditable(true);			dialog.show();			String val = jcbox.getEditor().getItem().toString();			TSMath.setYUnits(ds, val);		}	}	/**
    *
    */	public static void changeToCurrentUnits(DataReference ref) {		// first check for unknown units and prompt user for some units, TAF or		// CFS or NONE		// changeUnknownToTAF(ref);		try {			DataSet ds = ref.getData();			// set new attributes for data if this does not cache original units			if (!(ds.getAttributes() instanceof TSDataAttr))				ds.setAttributes(new TSDataAttr(ds.getAttributes()));			// if stored units are needed use original units to figure out the			// conversions			useStoredUnits = false;			if (useStoredUnits) {				RegularTimeSeries rts = (RegularTimeSeries) ds;				String ounits = ((TSDataAttr) ds.getAttributes())						.getOriginalUnits();				boolean isCFS = ounits.equals("CFS");				boolean isTAF = ounits.equals("TAF");				if (isCFS)					TSMath.taf2cfs(rts);				else if (isTAF)					TSMath.cfs2taf(rts);				else					System.out.println("Not CFS or TAF units...");				return;			}			// convert units from CFS or TAF but do cfs conversion only if not			// storage.			if (ds instanceof RegularTimeSeries) {				RegularTimeSeries rts = (RegularTimeSeries) ds;				boolean isStorage = ds.getAttributes().getTypeName().equals(						"STORAGE");				if (useCFS && !isStorage)					TSMath.taf2cfs(rts);				else					TSMath.cfs2taf(rts);			}		} catch (RuntimeException re) {			System.err.println(re.getMessage());		}	}	/**
   *
   */	public static JFrame[] displayData(DataReference ref) {		JFrame[] frarray = null;		if (ref != null) {			changeToCurrentUnits(ref);			int count = 0;			if (viewGraph)				count++;			if (viewTable)				count++;			if (viewMonthlyTable)				count++;			frarray = new JFrame[count];			count = 0;			if (viewGraph)				frarray[count++] = plot(ref);			if (viewTable)				frarray[count++] = tabulate(ref);			if (viewMonthlyTable)				frarray[count++] = monthlyTable(ref);		}		return frarray;	}	/**
   *
   */	public static JFrame[] displayData(DataReference[] refs) {		JFrame[] frarray = null;		if (refs == null || refs.length == 0)			return null;		if (refs.length == 1)			return displayData(refs[0]);		else {			for (int i = 0; i < refs.length; i++) {				if (refs[i] != null)					changeToCurrentUnits(refs[i]);			}			int count = 0;			if (viewGraph)				count++;			if (viewTable)				count++;			if (viewMonthlyTable)				count++;			frarray = new JFrame[count];			count = 0;			if (viewGraph)				frarray[count++] = plot(refs);			if (viewTable)				frarray[count++] = tabulate(refs);			if (viewMonthlyTable)				frarray[count++] = monthlyTable(refs);		}		return frarray;	}	/**
    *
    */	public static void useStoredUnits() {		useStoredUnits = true;	}	/**
    *
    */	public static void useUnits(String units) {		useStoredUnits = false;		if (units.equals(AppUtils.CFS)) {			useCFS = true;		} else {			useCFS = false;		}	}	/**	 * 	 * @return a new string copy of "toBe" with string "with" in string "orig"	 */	public static String replace(String orig, String toBe, String with) {		return SetUtils.createReplacedString(orig, toBe, with);	}	public static int[] MT_40_30_30 = { 1 };// readMTList("MT40-30-30.table");	public static int[] MT_60_20_20 = { 2 };// readMTList("MT60-20-20.table");	/**
   *
   */	public static DataReference makeOneRef(DataReference ref1) {		RegularTimeSeries rts = (RegularTimeSeries) ref1.getData();		rts = (RegularTimeSeries) rts.createSlice(rts.getTimeWindow());		rts.getAttributes().setYUnits("NONE");		DataSetIterator dsi = rts.getIterator();		while (!dsi.atEnd()) {			DataSetElement dse = dsi.getElement();			dse.setY(1);			dsi.putElement(dse);			dsi.advance();		}		Pathname path = Pathname.createPathname(new String[] { "CALSIM", "1",				"", "", "1MON",				ref1.getPathname().getPart(Pathname.F_PART) });		return DSSUtil.createDataReference("local", "calc.dss",				path.toString(), rts);	}	/**
    *
    */	public static TimeWindow createTimeWindowFromString(String str) {		int dIndex = str.indexOf("-");		if (dIndex < 0)			throw new IllegalArgumentException(					"Invalid string for time window " + str);		String ststr = str.substring(0, dIndex).trim();		String etstr = str.substring(dIndex + 1, str.length()).trim();		// this initializes to beginning of month		TimeFactory tf = TimeFactory.getInstance();		TimeInterval ti = tf.createTimeInterval("1mon");		TimeInterval timin = tf.createTimeInterval("1min");		Time stime = tf.createTime(ststr, "MMMyyyy");		stime = tf.createTime((stime.__add__(timin)).ceiling(ti));		Time etime = tf.createTime(etstr, "MMMyyyy");		etime = tf.createTime((etime.__add__(timin)).ceiling(ti));		return tf.createTimeWindow(stime, etime);	}	/**
   *
   */	public static String getCurrentUnits() {		if (AppUtils.useCFS) {			return AppUtils.CFS;		} else {			return AppUtils.TAF;		}	}	/**
    *
    */	public static void loadProps() {		/*		 * 		 * viewGraph = new		 * Boolean(AppProps.getProperty("AppUtils.viewGraph")).booleanValue();		 * 		 * viewTable = new		 * Boolean(AppProps.getProperty("AppUtils.viewTable")).booleanValue();		 * 		 * viewMonthlyTable = new		 * Boolean(AppProps.getProperty("AppUtils.viewMonthlyTable"		 * )).booleanValue();		 * 		 * //useCFS = new		 * Boolean(AppProps.getProperty("AppUtils.useCFS")).booleanValue();		 * 		 * //useStoredUnits = new		 * Boolean(AppProps.getProperty("AppUtils.useStoredUnits"		 * )).booleanValue();		 * 		 * _isWaterYear = new		 * Boolean(AppProps.getProperty("AppUtils.isWaterYear")).booleanValue();		 * 		 * _show60_20_20 = new		 * Boolean(AppProps.getProperty("AppUtils.show60_20_20"		 * )).booleanValue();		 * 		 * _show40_30_30 = new		 * Boolean(AppProps.getProperty("AppUtils.show40_30_30"		 * )).booleanValue();		 * 		 * DEFAULT_PLOT_SIZE = GraphUtils.parseDimensionProperty		 * 		 * (AppProps.getProperty("AppUtils.DEFAULT_PLOT_SIZE"));		 * 		 * DEFAULT_TABLE_SIZE = GraphUtils.parseDimensionProperty		 * 		 * (AppProps.getProperty("AppUtils.DEFAULT_TABLE_SIZE"));		 * 		 * DEFAULT_MT_SIZE= GraphUtils.parseDimensionProperty		 * 		 * (AppProps.getProperty("AppUtils.DEFAULT_MT_SIZE"));		 */	}	/**
    *
    */	public static void saveProps() {		AppProps.setProperty("AppUtils.viewGraph", new Boolean(viewGraph)				.toString());		AppProps.setProperty("AppUtils.viewTable", new Boolean(viewTable)				.toString());		AppProps.setProperty("AppUtils.viewMonthlyTable", new Boolean(				viewMonthlyTable).toString());		AppProps.setProperty("AppUtils.useCFS", new Boolean(useCFS).toString());		// AppProps.setProperty("AppUtils.useStoredUnits",new		// Boolean(useStoredUnits).toString());		AppProps.setProperty("AppUtils.isWaterYear", new Boolean(_isWaterYear)				.toString());		AppProps.setProperty("AppUtils.show60_20_20",				new Boolean(_show60_20_20).toString());		AppProps.setProperty("AppUtils.show40_30_30",				new Boolean(_show40_30_30).toString());		AppProps.setProperty("AppUtils.DEFAULT_PLOT_SIZE", DEFAULT_PLOT_SIZE				.toString());		AppProps.setProperty("AppUtils.DEFAULT_TABLE_SIZE", DEFAULT_TABLE_SIZE				.toString());		AppProps.setProperty("AppUtils.DEFAULT_MT_SIZE", DEFAULT_MT_SIZE				.toString());		AppProps.save();	}	/**
    *
    */	static {		try {			loadProps();		} catch (Exception e) {			e.printStackTrace(System.err);		}	}	/**
   *
   */	public static boolean needsRecataloging(String dssFile) {		String catalogFile = DSSUtil.getCatalogFilename(dssFile);		File cf = new File(catalogFile);		File df = new File(dssFile);		return (!cf.exists()) || (cf.lastModified() < df.lastModified());	}}