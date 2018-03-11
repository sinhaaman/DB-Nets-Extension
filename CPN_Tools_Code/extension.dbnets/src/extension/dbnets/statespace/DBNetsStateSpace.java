package extension.dbnets.statespace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cpntools.accesscpn.engine.highlevel.HighLevelSimulator;
import org.cpntools.accesscpn.engine.highlevel.checker.Checker;
import org.cpntools.accesscpn.engine.highlevel.instance.adapter.ModelInstance;
import org.cpntools.accesscpn.model.PetriNet;
import org.cpntools.accesscpn.model.Place;
import org.cpntools.simulator.extensions.Channel;

import extension.dbnets.simulatorextension.DButilityFunctions;


public class DBNetsStateSpace {
	private HighLevelSimulator simulator;
	private PetriNet petriNet;
	private ModelInstance modelInstance;
	private String modelLocation;
	private ViewPlaceHandler viewPlaceHandler;
	private DBNetsStateSpaceCalculator ssCalculator;
	private Channel channel;
	public DBNetsStateSpace(){
		
	}
	
	public DBNetsStateSpace(PetriNet pNet, ModelInstance mi, String modelLocation, Map<String, Place> viewPlaceMap, Map<String, Place> pnamePlaceMap,
			Map<String, ArrayList<String>> placeQueryMap, Map<String,String> placeNamePidMap, Map<String,String> pidPnameMap, Map<Place, List<String>> placeSortMap, DButilityFunctions dbHandler, Channel channel){
		try {
			setPetriNet(pNet);
			setModelPath(modelLocation);
			setModelInstance(mi);
			setSimulator(HighLevelSimulator.getHighLevelSimulator());
			setChannel(channel);
			final Checker checker = new Checker(petriNet, null, getSimulator());
			initializeChecker(checker, this.modelLocation, this.modelLocation);
			setViewPlaceHandler(new ViewPlaceHandler(viewPlaceMap, pnamePlaceMap, placeQueryMap, placeNamePidMap, pidPnameMap, placeSortMap, dbHandler));
			setSsCalculator(new DBNetsStateSpaceCalculator(simulator, viewPlaceHandler, pNet, mi, channel));
			//getSimulator().setTarget((Notifier) getPetriNet());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initializeChecker(Checker checker, String modelPath, String outputPath) {
		// TODO Auto-generated method stub
		try {
			checker.localCheck();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try{
			checker.checkInitializing();
			checker.checkDeclarations();
			checker.generateSerializers();
			checker.checkPages();
			checker.generatePlaceInstances();
			checker.checkMonitors();
			checker.generateNonPlaceInstances();
			checker.initialiseSimulationScheduler();
			checker.instantiateSMLInterface();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void testSimulator() throws Exception{
		ssCalculator.calculateStateSpace();
		StateSpaceGraph t = ssCalculator.getStateGraph();
		System.out.println(t);
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
	 * @return the petriNet
	 */
	public PetriNet getPetriNet() {
		return petriNet;
	}

	/**
	 * @param petriNet the petriNet to set
	 */
	public void setPetriNet(PetriNet petriNet) {
		this.petriNet = petriNet;
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
	 * @return the modelPath
	 */
	public String getModelPath() {
		return modelLocation;
	}

	/**
	 * @param modelPath the modelPath to set
	 */
	public void setModelPath(String modelPath) {
		this.modelLocation = modelPath;
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
	 * @return the ssCalculator
	 */
	public DBNetsStateSpaceCalculator getSsCalculator() {
		return ssCalculator;
	}

	/**
	 * @param ssCalculator the ssCalculator to set
	 */
	public void setSsCalculator(DBNetsStateSpaceCalculator ssCalculator) {
		this.ssCalculator = ssCalculator;
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

}
