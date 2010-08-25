package vista.db.hdf5;

import java.util.ArrayList;

class Reservoir {
	private String name;
	private ArrayList<Integer> nodes;
	private ArrayList<Integer> gateNodes;
	
	public Reservoir(String name){
		this.name = name;
		nodes = new ArrayList<Integer>();
		gateNodes = new ArrayList<Integer>();
	}
	
	public void addNode(int nodeId){
		nodes.add(new Integer(nodeId));
	}
	
	public void addGateNode(int nodeId){
		gateNodes.add(new Integer(nodeId));
	}
	
	public int getNumberOfConnections(){
		return nodes.size() + gateNodes.size();
	}
	
	public Integer[] getNodes(){
		Integer[] nodeArray = new Integer[nodes.size()];
		return nodes.toArray(nodeArray);
	}
	
	public Integer[] getGateNodes(){
		Integer[] gateNodeArray = new Integer[gateNodes.size()];
		return gateNodes.toArray(gateNodeArray);
	}

	//FIXME: Assumption is that the index refers to nodes first then gate nodes
	public int getConnection(int index){
		if (index > nodes.size()){
			return gateNodes.get(index-nodes.size());
		} else {
			return nodes.get(index);
		}
	}
}
