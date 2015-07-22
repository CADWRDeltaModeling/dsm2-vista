package gov.ca.dsm2.input.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class GateDevice implements Serializable {
	public String gateName;
	public String device;
	public int numberOfDuplicates;
	public double elevation;
	public double coefficientFromNode;
	public double coefficientToNode;
	public String defaultOperation;
}
