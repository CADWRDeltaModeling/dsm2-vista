package vista.db.jdbc.bdat;

import java.lang.ref.SoftReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import vista.set.DataReference;
import vista.set.DataSet;
import vista.set.DataSetAttr;
import vista.set.DataType;
import vista.set.IrregularTimeSeries;
import vista.set.Pathname;
import vista.set.RegularTimeSeries;
import vista.time.Time;
import vista.time.TimeFactory;
import vista.time.TimeWindow;

/**
 * A reference that uses a relational table to store a time series data set
 * 
 * @author psandhu
 * 
 */
@SuppressWarnings("serial")
public class BDATDataReference extends DataReference {
	private static TimeFactory TF = TimeFactory.getInstance();
	private int resultId;
	private String abbreviation;
	private String constituentName;
	private String aggregateName;
	private String intervalName;
	private String readingTypeName;
	private String rankName;
	private String probeDepth;
	private Date startDate;
	private Date endDate;
	private String units;
	/**
	 * The data set contained by this reference. Don't save data on serializing
	 */
	private transient SoftReference<DataSet> dataset;
	private BDATConnection connection;

	public BDATDataReference(BDATConnection connection, int requestId) {
		this.connection=connection;
		this.resultId = requestId;
	}

	public BDATDataReference(BDATConnection connection, int resultId, String abbreviation,
			String constituentName, String aggregateName, String intervalName,
			String readingTypeName, String rankName, String probeDepth,
			Date startDate, Date endDate) {
		this.connection=connection;
		this.resultId = resultId;
		this.setAbbreviation(abbreviation);
		this.constituentName = constituentName;
		this.aggregateName = aggregateName;
		this.intervalName = intervalName;
		if (this.intervalName.equalsIgnoreCase("Visit")) {
			setTimeInterval(null);
		} else {
			String interval = this.intervalName.replace(" ", "");
			setTimeInterval(TF.createTimeInterval(interval));
		}
		this.readingTypeName = readingTypeName;
		this.rankName = rankName;
		this.probeDepth = probeDepth;
		this.startDate = startDate;
		if (startDate == null) {
			startDate = new Date();
		}
		if (endDate == null) {
			endDate = new Date();
		}
		this.endDate = endDate;
		Time st = TF.createTime(startDate);
		Time et = TF.createTime(endDate);
		TimeWindow timeWindow = null;
		if (st.compare(et) <= 0) {
			timeWindow = TF.createTimeWindow(st, et);
		}
		setTimeWindow(timeWindow);
		setFilename(abbreviation);
		setServername("bdat");
		Pathname pathname = Pathname.createPathname(new String[] {
				"BDAT: " + this.resultId,
				this.abbreviation,
				this.constituentName,
				"",
				(getTimeInterval() == null ? "IR-DAY" : getTimeInterval()
						.toString()),
				this.aggregateName + "_" + this.readingTypeName + "_"
						+ this.rankName + "_" + this.probeDepth });
		setPathname(pathname);
		Pattern p = Pattern.compile("\\(.*?\\)");
		Matcher m = p.matcher(this.constituentName);
		if (m.find()){
			units = m.group();
		}else{
			units="?";
		}
	}

	@Override
	protected DataReference createClone() {
		return new BDATDataReference(this.connection, this.resultId);
	}

	@Override
	public DataSet getData() {
		if (dataset == null || dataset.get() == null) {
			loadData();
		}
		return dataset.get();
	}

	private double[] resize(double[] x) {
		if (x == null) {
			return null;
		}
		int l = x.length + 100000;
		double[] nx = new double[l];
		System.arraycopy(x, 0, nx, 0, x.length);
		return nx;
	}

	private int[] resize(int[] x) {
		if (x == null) {
			return null;
		}
		int l = x.length + 100000;
		int[] nx = new int[l];
		System.arraycopy(x, 0, nx, 0, x.length);
		return nx;
	}
	@Override
	public void reloadData() {
		if (dataset != null) {
			dataset.clear();
			dataset = null;
		}
		getData();
	}
	
	public void loadData() {
		DataSet data = null;
		double[] x = null;
		if (getTimeInterval() == null) {
			x = new double[100000];
		}
		double[] y = new double[100000];
		int[] flags = new int[100000];
		Connection connection = null;
		try {
			connection=this.connection.getConnection();
			PreparedStatement preparedStatement = connection
					.prepareStatement("select * from emp_cms.emp_raw_result "
							+ " where result_id=? order by time asc");
			preparedStatement.setInt(1, this.resultId);
			ResultSet rs = preparedStatement.executeQuery();
			int index = 0;
			while (rs.next()) {
				if (getTimeInterval() == null) {
					// TODO: could this more efficient?
					x[index] = TF.createTime(rs.getTime("time"))
							.getTimeInMinutes();
				}
				y[index] = rs.getDouble("value");
				flags[index]=0;
				// FIXME: flag[index] = rs.getString("QAQC_FLAGID").. convert to
				// MISSING, UNSCREENED, etc.
				index++;
				if (index >= y.length) {
					x = resize(x);
					y = resize(y);
					flags=resize(flags);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		if (getTimeInterval() == null) {
			DataSetAttr attr = new DataSetAttr(DataType.IRREGULAR_TIME_SERIES, "TIME", units, "", "INST-VAL");
			data = new IrregularTimeSeries(getName(), x, y, flags, attr);
		} else {
			DataSetAttr attr = new DataSetAttr(DataType.REGULAR_TIME_SERIES, "TIME", units, "", "INST-VAL");
			data = new RegularTimeSeries(getName(), getTimeWindow()
					.getStartTime(), getTimeInterval(), y, flags, attr);
		}
		dataset = new SoftReference<DataSet>(data);
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public int getResultId() {
		return resultId;
	}

	public void setResultId(int resultId) {
		this.resultId = resultId;
	}

	public String getConstituentName() {
		return constituentName;
	}

	public void setConstituentName(String constituentName) {
		this.constituentName = constituentName;
	}

	public String getAggregateName() {
		return aggregateName;
	}

	public void setAggregateName(String aggregateName) {
		this.aggregateName = aggregateName;
	}

	public String getIntervalName() {
		return intervalName;
	}

	public void setIntervalName(String intervalName) {
		this.intervalName = intervalName;
	}

	public String getReadingTypeName() {
		return readingTypeName;
	}

	public void setReadingTypeName(String readingTypeName) {
		this.readingTypeName = readingTypeName;
	}

	public String getRankName() {
		return rankName;
	}

	public void setRankName(String rankName) {
		this.rankName = rankName;
	}

	public String getProbeDepth() {
		return probeDepth;
	}

	public void setProbeDepth(String probeDepth) {
		this.probeDepth = probeDepth;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

}
