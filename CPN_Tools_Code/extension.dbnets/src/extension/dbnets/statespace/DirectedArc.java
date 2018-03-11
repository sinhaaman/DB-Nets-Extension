package extension.dbnets.statespace;

import org.cpntools.accesscpn.engine.highlevel.instance.Instance;
import org.cpntools.accesscpn.engine.highlevel.instance.State;
import org.cpntools.accesscpn.model.Transition;

public class DirectedArc{
	private Instance<? extends Transition> arcLabel;		
	private State sourceNode;
	private State targetNode;
	public DirectedArc(){
		
	}
	
	public DirectedArc(State sourceNode, State targetNode, Instance<? extends Transition> arcLabel){
		setSourceNode(sourceNode);
		setTargetNode(targetNode);
		setArcLabel(arcLabel);
	}
	
	public Instance<? extends Transition> getArcLabel() {
		return arcLabel;
	}
	/**
	 * @param arcLabel2 the arcLabel to set
	 */
	public void setArcLabel(Instance<? extends Transition> arcLabel2) {
		this.arcLabel = arcLabel2;
	}
	/**
	 * @return the sourceNode
	 */
	public State getSourceNode() {
		return sourceNode;
	}
	/**
	 * @param sourceNode the sourceNode to set
	 */
	public void setSourceNode(State sourceNode) {
		this.sourceNode = sourceNode;
	}
	/**
	 * @return the targetNode
	 */
	public State getTargetNode() {
		return targetNode;
	}
	/**
	 * @param targetNode the targetNode to set
	 */
	public void setTargetNode(State targetNode) {
		this.targetNode = targetNode;
	}
}
