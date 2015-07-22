package gov.ca.dsm2.input.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Transfer implements Serializable {
	public String name;
	public String fromObject;
	public String fromIdentifier;
	public String toObject;
	public String toIdentifier;
}
