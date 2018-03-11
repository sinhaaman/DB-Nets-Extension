package extension.dbnets.statespace;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cpntools.accesscpn.model.Place;

import extension.dbnets.simulatorextension.DButilityFunctions;
import extension.dbnets.simulatorextension.DeclarationHandler;
import extension.dbnets.simulatorextension.UtilityFunctions;

public class ViewPlaceHandler {
	private DButilityFunctions dbHandler;
	private Map<String, Place> viewPlaceMap;
	private Map<String, ArrayList<String>> viewPlaceQueryMap;
	private DeclarationHandler declHandler;
	private Map<String, Place> pnamePlaceMap;
	private Map<String,String> placeNamePidMap;
	private Map<String,String> pidPnameMap;
	private Map<Place, List<String>> placeSortMap;
	public ViewPlaceHandler(){
		
	}
	
	public ViewPlaceHandler(Map<String, Place> viewPlaceMap, Map<String, Place> pnamePlaceMap,
			Map<String, ArrayList<String>> placeQueryMap, Map<String,String> placeNamePidMap, Map<String,String> pidPnameMap,
			Map<Place, List<String>> placeSortMap, DButilityFunctions dbHandler){
		setDbHandler(dbHandler);
		setViewPlaceMap(viewPlaceMap);
		setPnamePlaceMap(pnamePlaceMap);
		setPlaceQueryMap(placeQueryMap);
		setPlaceNamePidMap(placeNamePidMap);
		setPidPnameMap(pidPnameMap);
		setPlaceSortMap(placeSortMap);
		
	}
	
	public Map<Place, String> getViewPlaceMarkings() throws Exception {
//		if(getDbCommFrame() == null && mode!= RESET){
//			setDbCommFrame(new DBCommConnectionFrame());
//			getDbCommFrame().getFrame().setVisible(true);
//			getDbCommFrame().addObserver(this);
//		}
		//else if(dbCommFrame.frame.getDBConnectionStatus()){
		Map<Place, String> viewPlaceMarkings = new HashMap<Place, String>();
		for(String place_name : getPlaceQueryMap().keySet()){
			String p_id = getPlaceNamePidMap().get(place_name);
			String marking = getViewPlaceMarking(p_id);
			viewPlaceMarkings.put(getPnamePlaceMap().get(place_name), marking);
		}
		return viewPlaceMarkings;
		//}
	}

	private String getViewPlaceMarking(String p_id) {
		// TODO Auto-generated method stub
		String marking = "";
		String place_name = getPidPnameMap().get(p_id);
		ArrayList<String> queries = getPlaceQueryMap().get(place_name);
		for(String query:queries){
			if(getDbHandler().getConnectionStatus())
			{
				ResultSet rs = getDbHandler().queryExecute(query+";");
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
	 * @return the dbHandler
	 */
	public DButilityFunctions getDbHandler() {
		return dbHandler;
	}

	/**
	 * @param dbHandler the dbHandler to set
	 */
	public void setDbHandler(DButilityFunctions dbHandler) {
		this.dbHandler = dbHandler;
	}

	/**
	 * @return the viewPlaceMap
	 */
	public Map<String, Place> getViewPlaceMap() {
		return viewPlaceMap;
	}

	/**
	 * @param viewPlaceMap the viewPlaceMap to set
	 */
	public void setViewPlaceMap(Map<String, Place> viewPlaceMap) {
		this.viewPlaceMap = viewPlaceMap;
	}

	/**
	 * @return the placeQueryMap
	 */
	public Map<String, ArrayList<String>> getPlaceQueryMap() {
		return viewPlaceQueryMap;
	}

	/**
	 * @param placeQueryMap the placeQueryMap to set
	 */
	public void setPlaceQueryMap(Map<String, ArrayList<String>> placeQueryMap) {
		this.viewPlaceQueryMap = placeQueryMap;
	}

	/**
	 * @return the declHandler
	 */
	public DeclarationHandler getDeclHandler() {
		return declHandler;
	}

	/**
	 * @param declHandler the declHandler to set
	 */
	public void setDeclHandler(DeclarationHandler declHandler) {
		this.declHandler = declHandler;
	}

	/**
	 * @return the pnamePlaceMap
	 */
	public Map<String, Place> getPnamePlaceMap() {
		return pnamePlaceMap;
	}

	/**
	 * @param pnamePlaceMap the pnamePlaceMap to set
	 */
	public void setPnamePlaceMap(Map<String, Place> pnamePlaceMap) {
		this.pnamePlaceMap = pnamePlaceMap;
	}

	/**
	 * @return the placeNamePidMap
	 */
	public Map<String,String> getPlaceNamePidMap() {
		return placeNamePidMap;
	}

	/**
	 * @param placeNamePidMap the placeNamePidMap to set
	 */
	public void setPlaceNamePidMap(Map<String,String> placeNamePidMap) {
		this.placeNamePidMap = placeNamePidMap;
	}

	/**
	 * @return the pidPnameMap
	 */
	public Map<String,String> getPidPnameMap() {
		return pidPnameMap;
	}

	/**
	 * @param pidPnameMap the pidPnameMap to set
	 */
	public void setPidPnameMap(Map<String,String> pidPnameMap) {
		this.pidPnameMap = pidPnameMap;
	}

	/**
	 * @return the placeSortMap
	 */
	public Map<Place, List<String>> getPlaceSortMap() {
		return placeSortMap;
	}

	/**
	 * @param placeSortMap the placeSortMap to set
	 */
	public void setPlaceSortMap(Map<Place, List<String>> placeSortMap) {
		this.placeSortMap = placeSortMap;
	}
	

}
