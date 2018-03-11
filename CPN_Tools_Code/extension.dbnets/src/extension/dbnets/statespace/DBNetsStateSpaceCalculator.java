package extension.dbnets.statespace;

import java.io.IOException;
import java.sql.Savepoint;
import java.util.List;
import java.util.Map;

import org.cpntools.accesscpn.engine.highlevel.HighLevelSimulator;
import org.cpntools.accesscpn.engine.highlevel.PacketGenerator;
import org.cpntools.accesscpn.engine.highlevel.instance.Binding;
import org.cpntools.accesscpn.engine.highlevel.instance.Instance;
import org.cpntools.accesscpn.engine.highlevel.instance.InstanceFactory;
import org.cpntools.accesscpn.engine.highlevel.instance.State;
import org.cpntools.accesscpn.engine.highlevel.instance.adapter.ModelInstance;
import org.cpntools.accesscpn.engine.protocol.Packet;
import org.cpntools.accesscpn.model.Code;
import org.cpntools.accesscpn.model.PetriNet;
import org.cpntools.accesscpn.model.Place;
import org.cpntools.accesscpn.model.Transition;
import org.cpntools.simulator.extensions.Channel;

import extension.dbnets.simulatorextension.PacketCreator;

public class DBNetsStateSpaceCalculator {
	private HighLevelSimulator simulator;
	private ViewPlaceHandler viewPlaceHandler;
	private PetriNet pNet;
	private ModelInstance modelInstance;
	private StateSpaceGraph stateGraph;
	private int savePoints = 0;
	private Channel channel;
	private StateSpaceUtilityFunctions utilManager;
	public DBNetsStateSpaceCalculator(){
		
	}
	
	public DBNetsStateSpaceCalculator(HighLevelSimulator simulator, ViewPlaceHandler viewPlaceHandler, PetriNet pNet,
			ModelInstance mi, Channel channel){
		setSimulator(simulator);
		//simulator.setSimulationOptions(false, true, false, true, true, true, true, null, null, null, null, null, true, null, null);
		setViewPlaceHandler(viewPlaceHandler);
		setpNet(pNet);
		setModelInstance(mi);
		setChannel(channel);
		setUtilManager(new StateSpaceUtilityFunctions(getChannel(),getpNet(),getSimulator()));
	}
	
	public void calculateStateSpace() throws Exception{
		if(!viewPlaceHandler.getDbHandler().getConnectionStatus()){
			return;
		}
		getSimulator().initializeSyntaxCheck();
		getSimulator().initialiseSimulationScheduler();
		setViewPlaceMarking();
		
		//State initialState = getSimulator().getMarking();
		State initialState = getUtilManager().getMarking();
		viewPlaceHandler.getDbHandler().getConnection().setAutoCommit(false);
		stateGraph = new StateSpaceGraph(initialState);
		runRecursiveDFS(initialState);
		getUtilManager().setMarking(initialState);
		
	}
	
	@SuppressWarnings("unchecked")
	private void runRecursiveDFS(State currentState) throws Exception {
		// TODO Auto-generated method stub
		if(stateGraph.stateMap.containsKey(currentState)){
			return;
		}
		stateGraph.stateMap.put(currentState, true);
		List<Instance<Transition>> allTransitions = utilManager.getAllTransitionInstances();
		List<Instance<? extends Transition>> enabledTransitions = utilManager.isEnabled(allTransitions);
		Integer counter = getCounter();
		Savepoint sp = viewPlaceHandler.getDbHandler().getConnection().setSavepoint(counter.toString());
		//try to make a SQL savepoint here
		for(Instance<? extends Transition> ti : enabledTransitions){
			List<Binding> binding_list = simulator.getBindings(ti);
			for(Binding b : binding_list){
			try{
				//Packet p = PacketCreator.instance.constructExecute(ti.getNode().getId(),
				        //ti.getInstanceNumber());
				//Packet r = this.channel.send(p);
				//viewPlaceHandler.getDbHandler().getConnection().commit();
				if (b.getAllAssignments().isEmpty()){
					Packet p = PacketCreator.instance.constructExecute(ti.getNode().getId(),
					        ti.getInstanceNumber());
					Packet r = this.channel.send(p);
					simulator.setMarking(utilManager.getMarking());
				}
				else{
					simulator.execute(b);
					utilManager.setMarking(simulator.getMarking());
				}
				System.out.println(simulator.getMarking());
				setViewPlaceMarking();
				State newState = utilManager.getMarking();
				System.out.println(newState);
				DirectedArc arc = new DirectedArc(currentState, newState, ti);
				stateGraph.arcList.add(arc);
				runRecursiveDFS(newState);
				//setViewPlaceMarking();
				//State newState = simulator.getMarking();
				//DirectedArc arc = new DirectedArc(currentState, newState, b);
				//stateGraph.arcList.add(arc);
			}catch (Exception e){
				e.printStackTrace();
			}finally{
				viewPlaceHandler.getDbHandler().getConnection().rollback(sp);
				utilManager.setMarking(currentState);
			}
			}
			//try to make a SQL rollback here
		}
		viewPlaceHandler.getDbHandler().getConnection().releaseSavepoint(sp);
		//try to delete the savepoint here
		
	}

	private int getCounter() {
		// TODO Auto-generated method stub
		return ++savePoints;
	}

	public void setViewPlaceMarking() throws Exception{
		Map<Place, String> viewPlaceMarkings = getViewPlaceHandler().getViewPlaceMarkings();
		for(Place place : viewPlaceMarkings.keySet()){
			for(Instance<Place> place_inst:getModelInstance().getAllInstances(place)){
				getUtilManager().setMarking(place_inst, viewPlaceMarkings.get(place));
				System.out.println(viewPlaceMarkings.get(place));
				getSimulator().setMarking(place_inst, viewPlaceMarkings.get(place));
				//getSimulator().setMarkingFast(getSimulator().getMarking());
			}
		}
	}

	/**
	 * @return the simulator
	 */
	public HighLevelSimulator getSimulator() {
		return simulator;
	}

	/**
	 * @param simulator the simulator to set
	 */
	public void setSimulator(HighLevelSimulator simulator) {
		this.simulator = simulator;
	}

	/**
	 * @return the viewPlaceHandler
	 */
	public ViewPlaceHandler getViewPlaceHandler() {
		return viewPlaceHandler;
	}

	/**
	 * @param viewPlaceHandler the viewPlaceHandler to set
	 */
	public void setViewPlaceHandler(ViewPlaceHandler viewPlaceHandler) {
		this.viewPlaceHandler = viewPlaceHandler;
	}

	/**
	 * @return the pNet
	 */
	public PetriNet getpNet() {
		return pNet;
	}

	/**
	 * @param pNet the pNet to set
	 */
	public void setpNet(PetriNet pNet) {
		this.pNet = pNet;
	}

	/**
	 * @return the modelInstance
	 */
	public ModelInstance getModelInstance() {
		return modelInstance;
	}

	/**
	 * @param modelInstance the modelInstance to set
	 */
	public void setModelInstance(ModelInstance modelInstance) {
		this.modelInstance = modelInstance;
	}

	/**
	 * @return the stateGraph
	 */
	public StateSpaceGraph getStateGraph() {
		return stateGraph;
	}

	/**
	 * @param stateGraph the stateGraph to set
	 */
	public void setStateGraph(StateSpaceGraph stateGraph) {
		this.stateGraph = stateGraph;
	}

	/**
	 * @return the channel
	 */
	public Channel getChannel() {
		return channel;
	}

	/**
	 * @param channel the channel to set
	 */
	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	/**
	 * @return the utilManager
	 */
	public StateSpaceUtilityFunctions getUtilManager() {
		return utilManager;
	}

	/**
	 * @param utilManager the utilManager to set
	 */
	public void setUtilManager(StateSpaceUtilityFunctions utilManager) {
		this.utilManager = utilManager;
	}
	

}
