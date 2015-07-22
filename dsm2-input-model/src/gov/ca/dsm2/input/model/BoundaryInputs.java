/**
 *    Copyright (C) 2009, 2010 
 *    State of California,
 *    Department of Water Resources.
 *    This file is part of DSM2 Grid Map
 *    The DSM2 Grid Map is free software: 
 *    you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *    DSM2 Grid Map is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details. [http://www.gnu.org/licenses]
 *    
 *    @author Nicky Sandhu
 *    
 */
package gov.ca.dsm2.input.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("serial")
public class BoundaryInputs implements Serializable {
	public ArrayList<BoundaryInput> stageInputs;
	public ArrayList<BoundaryInput> flowInputs;
	public ArrayList<BoundaryInput> sourceFlowInputs;
	private HashMap<String, ArrayList<BoundaryInput>> inputsNodeMap;

	public BoundaryInputs() {
		stageInputs = new ArrayList<BoundaryInput>();
		flowInputs = new ArrayList<BoundaryInput>();
		sourceFlowInputs = new ArrayList<BoundaryInput>();
		inputsNodeMap = new HashMap<String, ArrayList<BoundaryInput>>();
	}

	public void addFlow(BoundaryInput input) {
		flowInputs.add(input);
		updateNodeMapForAdd(input);
	}

	public void addStage(BoundaryInput input) {
		stageInputs.add(input);
		updateNodeMapForAdd(input);
	}

	public void addSourceFlow(BoundaryInput input) {
		sourceFlowInputs.add(input);
		updateNodeMapForAdd(input);
	}

	public List<BoundaryInput> getFlowInputs() {
		return flowInputs;
	}

	public List<BoundaryInput> getStageInputs() {
		return stageInputs;
	}

	public List<BoundaryInput> getSourceFlowInputs() {
		return sourceFlowInputs;
	}

	private void updateNodeMapForAdd(BoundaryInput input) {
		if (inputsNodeMap.containsKey(input.nodeId)) {
			ArrayList<BoundaryInput> inputsOnNode = inputsNodeMap
					.get(input.nodeId);
			inputsOnNode.add(input);
		} else {
			ArrayList<BoundaryInput> inputsOnNode = new ArrayList<BoundaryInput>();
			inputsOnNode.add(input);
			inputsNodeMap.put(input.nodeId, inputsOnNode);
		}
	}

	private void updateNodeMapForRemove(BoundaryInput input) {
		ArrayList<BoundaryInput> inputsOnNode = inputsNodeMap.get(input.nodeId);
		if (inputsOnNode == null) {
			return;
		}
		inputsOnNode.remove(input);
		if (inputsOnNode.size() == 0) {
			inputsNodeMap.remove(input.nodeId);
		}
	}

	public void updateNodeId(String newValue, String previousValue) {
		ArrayList<BoundaryInput> inputsOnNode = inputsNodeMap
				.get(previousValue);
		if (inputsOnNode == null) {
			return;
		}
		for (BoundaryInput input : inputsOnNode) {
			input.nodeId = newValue;
		}
		inputsNodeMap.remove(previousValue);
		inputsNodeMap.put(newValue, inputsOnNode);
	}
}
