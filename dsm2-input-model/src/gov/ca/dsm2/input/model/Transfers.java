package gov.ca.dsm2.input.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("serial")
public class Transfers implements Serializable {
	private ArrayList<Transfer> transfers;
	private HashMap<String, Transfer> transfersMap;

	public Transfers() {
		transfers = new ArrayList<Transfer>();
		transfersMap = new HashMap<String, Transfer>();
	}

	public void addTransfer(Transfer transfer) {
		transfers.add(transfer);
		transfersMap.put(transfer.name, transfer);
	}

	public List<Transfer> getTransfers() {
		return transfers;
	}

	public Transfer getTransfer(String name) {
		return transfersMap.get(name);
	}

}
