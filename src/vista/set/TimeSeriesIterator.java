package vista.set;

import vista.time.Time;

public interface TimeSeriesIterator extends DataSetIterator {

	/**
	 * positions iterator at index
	 */
	public void positionAtTime(Time tm);
}
