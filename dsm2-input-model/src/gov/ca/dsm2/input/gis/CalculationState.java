package gov.ca.dsm2.input.gis;

import java.io.Serializable;

/**
 * Contains the state of calculation that had been requested. It uses task queues
 * and atomic updates of counters in a memcache id based on its id to 
 * @author nsandhu
 *
 */
@SuppressWarnings("serial")
public class CalculationState implements Serializable{
	public String id;
	public int numberOfTasks;
	public int numberOfCompletedTasks;
	public double latestValue;
	public long startTimeInMillis;
	public long estimatedTimeLeftInMillis;
}
