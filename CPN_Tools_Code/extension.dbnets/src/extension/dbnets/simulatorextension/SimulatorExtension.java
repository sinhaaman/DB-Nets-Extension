package extension.dbnets.simulatorextension;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import org.cpntools.accesscpn.engine.highlevel.instance.Instance;
import org.cpntools.accesscpn.engine.highlevel.instance.adapter.ModelInstance;
import org.cpntools.accesscpn.engine.highlevel.instance.adapter.ModelInstanceAdapterFactory;
import org.cpntools.accesscpn.engine.protocol.Packet;
import org.cpntools.accesscpn.model.Arc;
import org.cpntools.accesscpn.model.Page;
import org.cpntools.accesscpn.model.PetriNet;
import org.cpntools.accesscpn.model.Place;
import org.cpntools.accesscpn.model.Transition;
import org.cpntools.accesscpn.model.auxgraphics.impl.TextImpl;
import org.cpntools.simulator.extensions.AbstractExtension;
import org.cpntools.simulator.extensions.Channel;
import org.cpntools.simulator.extensions.Command;
import org.cpntools.simulator.extensions.Extension;
import org.cpntools.simulator.extensions.Instrument;

import extension.dbnets.dialogs.DBCommConnectionFrame;

/**
 * The Simulator Extension class responsible for view place marking, model handling and connection with comm and database
 * The extension runs on the event subscription mechanism and handles those events which are passed by the CPN Tools.
 * Upon the receiving the events, extension decides what to do with the request and handles it.
 * View places are populated using this extension.
 * @author Aman Sinha
 *
 */
public class SimulatorExtension extends AbstractExtension implements Observer {
	private ModelHandler modelHandler;
	private DeclarationHandler declHandler;
	private ModelInstance modelInstance;
	private Map<String,Place> pidPlaceMap;
	private Map<Place,String> placePidMap;
	private Map<String,String> placeNamePidMap;
	private Map<String,String> pidPnameMap;
	private Map<String,Place> pnamePlaceMap;
	private DBCommConnectionFrame dbCommFrame;

	private Map<String,Place> viewPlaces;
	private Map<Place,String> invViewPlaces;
	private Map<String, ArrayList<String>> placeQueriesMap;
	private Map<Place, List<String>> placeSortMap;
	private PetriNet petriNet;
	private boolean placeSortDone;
	private boolean debug;
	private boolean RESET = true;
	private boolean NON_RESET = false;
	@Override
	/*
	 * (non-Javadoc)
	 * @see org.cpntools.simulator.extensions.Extension#getName()
	 */
	public String getName() {
		return "DB-Nets Extension";
	}

	/*
	 * returns the id of the extension. This has to be unique(non-Javadoc)
	 * @see org.cpntools.simulator.extensions.SubscriptionHandler#getIdentifier()
	 */
	/**
	 * Returns the identifier of the extension
	 */
	public int getIdentifier() {
		return Extension.TESTING;
	}

	/**
	 * Constructor for the extension class
	 */
	public SimulatorExtension(){
		//Subscribing to Simulation(500), compile declaration(300) and misc. commands(200)
		addSubscription(new Command(500, Command.ANY));
		addSubscription(new Command(200, Command.ANY));
		addSubscription(new Command(300,1));
		setDebug(false);
		initialize();

		//Adding an instrument in order to show the dialog box to the user
		//Try to think of a default target, here the default target is Page.
		addInstrument(new Instrument(Instrument.ToolBoxes.SIMULATION, "DB-Nets", "DBN",
				"DB-Nets", Instrument.Target.PAGE));
	}
	
	/**
	 * Initializes all the maps
	 */
	private void initialize() {
		setDeclHandler(new DeclarationHandler());
		setPidPlaceMap(new HashMap<String, Place>());
		setPlacePidMap(new HashMap<Place,String>());
		setPlaceNamePidMap(new HashMap<String,String>());
		setPidPnameMap(new HashMap<String,String>());
		setPnamePlaceMap(new HashMap<String, Place>());
		setViewPlaces(new HashMap<String,Place>());
		setInvViewPlaces(new HashMap<Place,String>());	
		setPlaceQueriesMap(new HashMap<String,ArrayList<String>>());
		setPlaceSortMap(new HashMap<Place,List<String>>());
	}

	@Override
	/*
	 * (non-Javadoc) Handles the subscribed packet based on its command and subcommand
	 * @see org.cpntools.simulator.extensions.AbstractExtension#handle(org.cpntools.accesscpn.engine.protocol.Packet, org.cpntools.accesscpn.engine.protocol.Packet)
	 */
	/**
	 * Handles the forwarded packets for the subscribed commands
	 */
	public Packet handle(Packet p, Packet r){
		p.reset();
		r.reset();
		int cmd = p.getInteger();	//retrieves the command
		int subcmd = p.getInteger();//retrieves the sub command
		switch (cmd){
		case 200:{
			switch (subcmd){
			case 9:{
				//get and set model location
				String model_name = p.getString();
				String model_loc = p.getString();
				setModelHandler(new ModelHandler(model_name,(model_loc.replace("/cygdrive/", "") + "/").replaceFirst("/", ":/")));
				try {
					//Flushes once the model is saved and recalculates everything
					try{
						//First remove the marking of the places
						populateViewPlaces(RESET);
					}catch(Exception e){
						//If some properties of the place is changed it is caught as an exception.
						if(isDebug()){
							System.out.println("Failed to clear the markings of the view place");
						}
					}
					flush();
					loadPetriNet();
					populatePlaceMaps();
					populateSortMap();
					if(isPlaceSortDone()){
						populateViewPlaces(NON_RESET);						
					}
				} catch (Exception e) {
					if(isDebug()){
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				}
				p.reset();
				break;
			}
			default:{
				break;
			}
			}
			break;
		}

		case 300:{
			switch(subcmd){
			case 1:
			{
				//declaration of the variables and color sets
				setPlaceSortDone(false);
				try{
					int i_val = p.getInteger();
					p.getString(); //Popping the ID
					switch(i_val){
					case 9:{
						int no_var = p.getInteger();
						List<String> vars = new ArrayList<String>();
						for(int i=0;i<no_var;++i){
							vars.add(p.getString());
						}
						getDeclHandler().addColorSet(p.getString(), vars);
						p.reset();
						break;
					}
					case 15:{
						List<String> vars = new ArrayList<String>();
						vars.add(p.getString());
						getDeclHandler().addColorSet(p.getString(), vars);
						p.reset();
						break;
					}
					case 20:{
						String type = p.getString();
						int num_var = p.getInteger();
						for(int i=0;i<num_var;++i){
							getDeclHandler().addVariable(p.getString(), new ArrayList<String>(Arrays.asList(type)));
						}
						p.reset();
						break;
					}
					default:{
						break;
					}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				p.reset();
				break;
			}
			default:{
				break;
			}
			}
			break;
		}

		case 500:{
			switch (subcmd){
			case 2:{
				//TODO Rewind is called, one can close the connection, refresh all the view places
				try {
					populateViewPlaces(NON_RESET);
				} catch (Exception e) {
					if(isDebug()){
						e.printStackTrace();
					}
				}
				break;
			}
			case 3:{
				//The nodes are instantiated and the markings are to be set
				String p_id = p.getString();
				String p_name = getPidPnameMap().get(p_id);
				if(getPlaceQueriesMap().containsKey(p_name)){
					start_comm_handler();
					//populateViewPlaces();
				}
				break;
			}
			
			case 5:{
				//Initialize a simulator scheduler
				if(!isPlaceSortDone()){
					clearViewPlacesMap();
					populateSortMap();
					setPlaceSortDone(true);
				}
				break;
			}
			case 12:{
				//Transition has fired
				//This is in case of the simulation with many steps involved
				try {
					populateViewPlaces(NON_RESET);
				} catch (Exception e) {
					if(isDebug()){
						e.printStackTrace();
					}
				}
				break;
			}
			case 15:{
				//This is in the case of single step simulation with manual binding
				try {
					populateViewPlaces(NON_RESET);
				} catch (Exception e) {
					if(isDebug()){
						e.printStackTrace();
					}
				}
				break;
			}
			case 35:{
				//populateViewPlaces();
				break;
			}
			default:{
				break;
			}
			}
			break;
		}
		default:{
			break;
		}
		}
		if(isDebug()){
			System.out.println(p.toString());
			System.out.println(r.toString());
		}
		return null;
	}

	/*
	 * Sets up the channel through which the communication can take place (non-Javadoc)
	 * @see org.cpntools.simulator.extensions.AbstractExtension#setChannel(org.cpntools.simulator.extensions.Channel)
	 */
	public void setChannel(final Channel c){
		super.setChannel(c);
		addObserver(new SimExtObserver());
	}

	@Override
	/*
	 * called when there is successful connection with the database(non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable source, Object value) {
		if (source.getClass().equals(DBCommConnectionFrame.class)){
			//if(dbCommFrame.frame.getDBConnectionStatus()){
			try {
				populateViewPlaces(NON_RESET);
			} catch (Exception e) {
				if(isDebug()){
					e.printStackTrace();
				}
			}
			//}
		}
	}


	public class SimExtObserver implements Observer{
		@Override
		/*
		 * called when extension instrument is clickin in CPN Tools (non-Javadoc)
		 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
		 */
		public void update(Observable arg0, Object arg1) {
			getDbCommFrame().getFrame().setVisible(true);	
		}	
	}

	/**
	 * Populates all the view place
	 * @throws IOException 
	 */
	private void populateViewPlaces(boolean mode) throws Exception {
		if(getDbCommFrame() == null && mode!= RESET){
			setDbCommFrame(new DBCommConnectionFrame());
			getDbCommFrame().getFrame().setVisible(true);
			getDbCommFrame().addObserver(this);
		}
		//else if(dbCommFrame.frame.getDBConnectionStatus()){
		for(String place_name : getPlaceQueriesMap().keySet()){
			String p_id = getPlaceNamePidMap().get(place_name);
			try{
				String marking = populateViewPlaces(p_id);
				if(mode == RESET){
					marking = "";
				}
				if(setMarking(p_id,marking)){
					if(isDebug()){
						System.out.println("Set Marking Success");
					}
					ArrayList<String> marking_list = new ArrayList<String>();
					marking_list.add(marking);
					List<Arc> arcs = getPidPlaceMap().get(p_id).getSourceArc();
					List<Instance<Transition>> trans_inst = new ArrayList<Instance<Transition>>();
					for(Arc a: arcs){
						trans_inst.addAll(getModelInstance().getAllInstances(a.getTransition()));
					}
					Packet p = PacketCreator.instance.simulatorChange(getModelInstance().getAllInstances(getPidPlaceMap().get(p_id)),trans_inst, marking_list, this.channel);
					marking_list.clear();
					try {
						this.channel.send(p);
					} catch (IOException e) {
						System.out.println("Problem in sending the message");
						e.printStackTrace();
					}
				}else{
					if(isDebug()){
						System.out.println("Set Marking Failure");
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	/**
	 * Flushes all the maps. Usually called when a PN is saved
	 */
	private void flush() {
		setPetriNet(null);
		setModelInstance(null);
		getPidPlaceMap().clear();
		getPlacePidMap().clear();
		getPlaceNamePidMap().clear();
		getPidPnameMap().clear();
		getPnamePlaceMap().clear();
		getViewPlaces().clear();
		getInvViewPlaces().clear();
		getPlaceQueriesMap().clear();
		getPlaceSortMap().clear();
	}

	/**
	 * Populates the Sort Map for the the view place queries
	 */
	private void populateSortMap() {
		for(String p_name : getPlaceQueriesMap().keySet()){
			if(getPnamePlaceMap().containsKey(p_name)){	//if it is in the net
				if(getViewPlaces().containsKey(p_name)){
					System.out.println("Two Conflicting Places");
				}
				else{
					getViewPlaces().put(p_name,getPnamePlaceMap().get(p_name));
				}
				if(getInvViewPlaces().containsKey(getPnamePlaceMap().get(p_name))){
					if(isDebug()){
						System.out.println("Place already exists");
					}
				}
				else{
					getInvViewPlaces().put(getPnamePlaceMap().get(p_name), p_name);
				}
				getPlaceSortMap().put(getPnamePlaceMap().get(p_name), getDeclHandler().getColorSets().get(getPnamePlaceMap().get(p_name).getSort().getText()));
			}
		}
	}

	/**
	 * clears view place map
	 */
	private void clearViewPlacesMap(){
		getViewPlaces().clear();
		getInvViewPlaces().clear();
	}

	/**
	 * sets the marking of the petri net. Sends packet via the channel
	 * @param p_id representing the id of the place
	 * @param marking representing the marking of the corresponding place
	 * @return Returns true if the marking can be successfully set to the place, otherwise returns false
	 * @throws IOException 
	 */
	private boolean setMarking(String p_id, String marking) throws IOException {
		boolean success = false;
		Place place = getPidPlaceMap().get(p_id);
		for(Instance<Place> place_inst:getModelInstance().getAllInstances(place)){
			if (checkPlaceMarking(place_inst.getNode(), marking)){
				Packet p = PacketCreator.instance.constructSetMarking(place_inst.getNode().getId(), place_inst.getInstanceNumber(), false);
				try {
					this.channel.send(p);
					success = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else{
				return false;
			}
		}
		return success;
	}

	/**
	 * Checks whether the marking for a particular place is compatible with it's place color type
	 * @param node A Place node for the corresponding marking neets to be checked
	 * @param marking The marking of the place
	 * @return Returns true if the marking is compatible with the place, otherwise returns false.
	 */
	private boolean checkPlaceMarking(Place node, String marking) throws IOException{
		String type = node.getSort().getText();
		Packet p = PacketCreator.instance.constructCheckMarking(type, marking);//.getBoolean();
		try {
			Packet r = this.channel.send(p);
			return r.getBoolean();
		} catch (IOException e){
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * The view places are assigned the marking which is retrieved by executing the assigned queries
	 * @param p_id Place id of the place
	 * @return Returns a string representing the marking of the view place
	 */
	private String populateViewPlaces(String p_id) throws Exception{
		String marking = "";
		String place_name = getPidPnameMap().get(p_id);
		ArrayList<String> queries = getPlaceQueriesMap().get(place_name);
		//ArrayList<String> queries = place_queries.get(place_name);
		for(String query:queries){
			if(getDbCommFrame().getFrame().getDBConnectionStatus())
			{
				ResultSet rs = getDbCommFrame().getFrame().getFrameDBHandler().queryExecute(query+";");
				Place place = getPnamePlaceMap().get(place_name);
				List<String> temp = getPlaceSortMap().get(place);
				if(temp != null){
					marking = UtilityFunctions.getViewPlaceMarking(rs, temp);
				}
			}
		}
		return marking;
	}

	/**
	 * populates the place maps and all other related maps for faster retrieval
	 */
	private void populatePlaceMaps(){
		setModelInstance((ModelInstance) ModelInstanceAdapterFactory.getInstance().adapt(
				getPetriNet(), ModelInstance.class));
		List<Page> list = getPetriNet().getPage();
		for(Page p : list) {			
			for (Place place : p.place()) {
				Collection<Instance<Place>> p_inst = getModelInstance().getAllInstances(place);
				for(Instance<Place> p_instance : p_inst){
					String p_name = p_instance.toString();
					String p_id = place.getId();
					if(getPnamePlaceMap().containsKey(p_name)){
						System.out.println("Place Instance Error");
					}
					else{
						getPnamePlaceMap().put(p_name, place);
					}
					if(getPlacePidMap().containsKey(place)){
						System.out.println("Place already exists");
					}
					else{
						getPlacePidMap().put(place, p_id);
					}
					if(getPidPlaceMap().containsKey(p_id)){
						System.out.println("Place ID already exists");
					}
					else{
						getPidPlaceMap().put(p_id, place);
					}
					if(getPlaceNamePidMap().containsKey(p_name)){
						System.out.println("Place Name already exists");
					}
					else{
						getPlaceNamePidMap().put(p_name, p_id);
					}
					if(getPidPnameMap().containsKey(p_id)){
						System.out.println("Place ID already exists");
					}
					else{
						getPidPnameMap().put(p_id, p_name);
					}
				}

			}
			//populate place query map
			populatePlaceQueryMap(p);
		}

	}


	/**
	 * populates the place query maps which is read in the text box in the CPN Tools
	 * @param p Page for which the Place Query Map is checked
	 */
	private void populatePlaceQueryMap(Page p) {
		for (Object obj : p.getObject()){
			Class<? extends Object> cls = obj.getClass();
			//String name = cls.getName();
			String s_name = cls.getSimpleName();
			if(s_name.equals("TextImpl")){
				TextImpl text_obj = (TextImpl) cls.cast(obj);
				String text = text_obj.getText();
				String[] text_array = text.split("\n");
				for(String temp_string:text_array){
					temp_string = temp_string.replaceAll(" :", ":");
					temp_string = temp_string.replaceAll(": ", ":");
					String temp_string_array[] = temp_string.split(":");
					//CPN Tools replaces all spaces and newlines in the name of the places/transitions with "_"
					if(temp_string_array.length >= 3 && (temp_string_array[0].equals("view_place") || temp_string_array[0].equals("relational_place")) && getPnamePlaceMap().containsKey(temp_string_array[1].replaceAll(" ","_"))){
						List<String> q = new ArrayList<String>(Arrays.asList(temp_string_array[2].split(";")));
						if(q.size() == 1){
							q.get(0).replace(";", "");
						}
						temp_string_array[1] = temp_string_array[1].replaceAll(" ","_");
						if(getPlaceQueriesMap().containsKey(temp_string_array[1])){
							getPlaceQueriesMap().get(temp_string_array[1]).addAll(q);
						}
						else{
							ArrayList<String> l = new ArrayList<String>();
							l.addAll(q);
							getPlaceQueriesMap().put(temp_string_array[1], l);
						}
					}
				}
				if(isDebug()){
					System.out.println(text);
				}
			}
			if(isDebug()){
				System.out.println(s_name);
			}

		}
	}

	/**
	 * loads the Petri net from the model handler
	 * @throws Exception when the Petri net couldn't be retrieved.
	 */
	private void loadPetriNet() throws Exception{
		setPetriNet(getModelHandler().getPetriNet());
	}

	/**
	 * starts the Comms/CPN handler
	 */
	private void start_comm_handler(){
		if(getDbCommFrame() == null){
			setDbCommFrame(new DBCommConnectionFrame());
			getDbCommFrame().getFrame().setVisible(true);
			getDbCommFrame().addObserver(this);
		}
	}

	/**
	 * Getter for Model Handler
	 * @return the modelHandler
	 */
	public ModelHandler getModelHandler() {
		return modelHandler;
	}

	/**
	 * Setter for Model Handler
	 * @param modelHandler the modelHandler to set
	 */
	public void setModelHandler(ModelHandler modelHandler) {
		this.modelHandler = modelHandler;
	}

	/**
	 * Getter for Declaration Handler
	 * @return the declHandler
	 */
	public DeclarationHandler getDeclHandler() {
		return declHandler;
	}

	/**
	 * Setter for Declaration Handler
	 * @param declHandler the declHandler to set
	 */
	public void setDeclHandler(DeclarationHandler declHandler) {
		this.declHandler = declHandler;
	}

	/**
	 * Setter for model instance of the loaded petri net
	 * @return the modelInstance
	 */
	public ModelInstance getModelInstance() {
		return modelInstance;
	}

	/**
	 * Setter for model instance of the loaded petri net
	 * @param modelInstance the modelInstance to set
	 */
	public void setModelInstance(ModelInstance modelInstance) {
		this.modelInstance = modelInstance;
	}

	/**
	 * Getter for pidPlaceMap
	 * @return the pidPlaceMap
	 */
	public Map<String,Place> getPidPlaceMap() {
		return pidPlaceMap;
	}

	/**
	 * Setter for pidPlaceMap
	 * @param pidPlaceMap the pidPlaceMap to set
	 */
	public void setPidPlaceMap(Map<String,Place> pidPlaceMap) {
		this.pidPlaceMap = pidPlaceMap;
	}

	/**
	 * Getter for placePidMap
	 * @return the placePidMap
	 */
	public Map<Place,String> getPlacePidMap() {
		return placePidMap;
	}

	/**
	 * Setter for placePidMap
	 * @param placePidMap the placePidMap to set
	 */
	public void setPlacePidMap(Map<Place,String> placePidMap) {
		this.placePidMap = placePidMap;
	}

	/**
	 * Getter for placeNamePidMap
	 * @return the placeNamePidMap
	 */
	public Map<String,String> getPlaceNamePidMap() {
		return placeNamePidMap;
	}

	/**
	 * Setter for placeNamePidMap
	 * @param placeNamePidMap the placeNamePidMap to set
	 */
	public void setPlaceNamePidMap(Map<String,String> placeNamePidMap) {
		this.placeNamePidMap = placeNamePidMap;
	}

	/**
	 * Getter for pidPnameMap
	 * @return the pidPnameMap
	 */
	public Map<String,String> getPidPnameMap() {
		return pidPnameMap;
	}

	/**
	 * Setter for pidPnameMap
	 * @param pidPnameMap the pidPnameMap to set
	 */
	public void setPidPnameMap(Map<String,String> pidPnameMap) {
		this.pidPnameMap = pidPnameMap;
	}

	/**
	 * Getter for pnamePlaceMap
	 * @return the pnamePlaceMap
	 */
	public Map<String,Place> getPnamePlaceMap() {
		return pnamePlaceMap;
	}

	/**
	 * Setter for pnamePlaceMap
	 * @param pnamePlaceMap the pnamePlaceMap to set
	 */
	public void setPnamePlaceMap(Map<String,Place> pnamePlaceMap) {
		this.pnamePlaceMap = pnamePlaceMap;
	}

	/**
	 * Getter for connection frame dialog box
	 * @return the dbCommFrame
	 */
	public DBCommConnectionFrame getDbCommFrame() {
		return dbCommFrame;
	}

	/**
	 * Setter for connection frame dialog box
	 * @param dbCommFrame the dbCommFrame to set
	 */
	public void setDbCommFrame(DBCommConnectionFrame dbCommFrame) {
		this.dbCommFrame = dbCommFrame;
	}

	/**
	 * Getter for viewPlaces Map
	 * @return the viewPlaces map
	 */
	public Map<String,Place> getViewPlaces() {
		return viewPlaces;
	}

	/**
	 * Setter for viewPlaces Map
	 * @param viewPlaces the viewPlaces map to set
	 */
	public void setViewPlaces(Map<String,Place> viewPlaces) {
		this.viewPlaces = viewPlaces;
	}

	/**
	 * Getter for invViewPlaces map
	 * @return the invViewPlaces map
	 */
	public Map<Place,String> getInvViewPlaces() {
		return invViewPlaces;
	}

	/**
	 * Setter for invViewPlaces map
	 * @param invViewPlaces the invViewPlaces to set map
	 */
	public void setInvViewPlaces(Map<Place,String> invViewPlaces) {
		this.invViewPlaces = invViewPlaces;
	}

	/**
	 * Getter for placeQueriesMap map
	 * @return the placeQueriesMap map
	 */
	public Map<String, ArrayList<String>> getPlaceQueriesMap() {
		return placeQueriesMap;
	}

	/**
	 * Setter for placeQueriesMap map
	 * @param placeQueriesMap the placeQueriesMap to set
	 */
	public void setPlaceQueriesMap(Map<String, ArrayList<String>> placeQueriesMap) {
		this.placeQueriesMap = placeQueriesMap;
	}

	/**
	 * Getter for placeSortMap
	 * @return the placeSort map
	 */
	public Map<Place, List<String>> getPlaceSortMap() {
		return placeSortMap;
	}

	/**
	 * Setter for placeSortMap
	 * @param placeSort the placeSort to set
	 */
	public void setPlaceSortMap(Map<Place, List<String>> placeSort) {
		this.placeSortMap = placeSort;
	}

	/**
	 * Getter for petriNet of the extension
	 * @return the petriNet
	 */
	public PetriNet getPetriNet() {
		return petriNet;
	}

	/**
	 * Setter for petriNet of the extension
	 * @param petriNet the petriNet to set
	 */
	public void setPetriNet(PetriNet petriNet) {
		this.petriNet = petriNet;
	}

	/**
	 * Getter for placeSortDone
	 * @return the True if place sort is performed
	 */
	public boolean isPlaceSortDone() {
		return placeSortDone;
	}

	/**
	 * Setter for placeSortDone
	 * @param placeSortDone the boolean value to set
	 */
	public void setPlaceSortDone(boolean placeSortDone) {
		this.placeSortDone = placeSortDone;
	}

	/**
	 * Getter for the debug parameter
	 * @return the debug
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * Setter for the debug parameter
	 * @param debug the debug to set
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
