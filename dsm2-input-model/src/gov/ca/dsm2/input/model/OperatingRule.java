package gov.ca.dsm2.input.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class OperatingRule implements Serializable {
	public String name;
	public String action;
	public String trigger;

	public String getGateName() {
		int gateIndex = action.indexOf("gate=");
		if (gateIndex < 0) {
			return "";
		}
		int gateNameEndIndex = gateIndex + 5;
		int commaIndex = action.indexOf(",", gateIndex + 5);
		int parenIndex = action.indexOf(")", gateIndex + 5);
		if (commaIndex < 0) {
			gateNameEndIndex = parenIndex;
		} else {
			if ((parenIndex > 0) && (parenIndex < commaIndex)) {
				gateNameEndIndex = parenIndex;
			} else {
				gateNameEndIndex = commaIndex;
			}
		}
		return action.substring(gateIndex + 5, gateNameEndIndex);
	}
}
