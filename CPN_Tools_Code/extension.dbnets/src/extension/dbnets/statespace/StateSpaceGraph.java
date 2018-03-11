package extension.dbnets.statespace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cpntools.accesscpn.engine.highlevel.instance.State;

public class StateSpaceGraph {
	public Map<State,Boolean> stateMap;
	public List<DirectedArc> arcList;
	public State initial;
	
	public StateSpaceGraph(){
		initialize();
	}
	
	public StateSpaceGraph(State initState){
		initialize();
		setInitial(initState);
	}
	
	public void initialize(){
		setStateMap(new HashMap<State,Boolean>());
		setArcList(new ArrayList<DirectedArc>());
	}
	/**
	 * @return the stateMap
	 */
	public Map<State,Boolean> getStateMap() {
		return stateMap;
	}
	/**
	 * @param stateMap the stateMap to set
	 */
	public void setStateMap(Map<State,Boolean> stateMap) {
		this.stateMap = stateMap;
	}
	/**
	 * @return the arcList
	 */
	public List<DirectedArc> getArcList() {
		return arcList;
	}
	/**
	 * @param arcList the arcList to set
	 */
	public void setArcList(List<DirectedArc> arcList) {
		this.arcList = arcList;
	}

	/**
	 * @return the initial
	 */
	public State getInitial() {
		return initial;
	}

	/**
	 * @param initial the initial to set
	 */
	public void setInitial(State initial) {
		this.initial = initial;
	}

}
