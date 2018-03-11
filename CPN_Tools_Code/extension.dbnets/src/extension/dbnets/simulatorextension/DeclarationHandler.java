package extension.dbnets.simulatorextension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Class for keeping track of declarations in CPN Tools
 * @author Aman Sinha
 *
 */
public class DeclarationHandler {
	/**
	 * Keeps track of the sort of the variables used
	 */
	private Map<String, List<String>> variableSort;
	
	/**
	 * Keeps track of the basic colour sets in CPN Tools
	 */
	private Map<String, List<String>> basicCSSort;
	
	/**
	 * Keeps track of the customised colour set, the user created in CPN Tools
	 */
	private Map<String, List<String>> colorSet;
	
	/**
	 * Constructor for the Declaration Handler class
	 */
	public DeclarationHandler()
	{
		setVariableSort(new HashMap<String,List<String>>());
		setBasicCSSort(new HashMap<String,List<String>>());
		setColorSet(new HashMap<String,List<String>>());
		populateCsMap();
	}
	
	/**
	 * Populate the map with the basic color set.
	 */
	private void populateCsMap(){
		getBasicCSSort().put("UNIT", new ArrayList<String>(Arrays.asList("unit")));
		getBasicCSSort().put("BOOL", new ArrayList<String>(Arrays.asList("bool")));
		getBasicCSSort().put("INT", new ArrayList<String>(Arrays.asList("int")));
		getBasicCSSort().put("INTINF", new ArrayList<String>(Arrays.asList("intinf")));
		getBasicCSSort().put("TIME", new ArrayList<String>(Arrays.asList("time")));
		getBasicCSSort().put("REAL", new ArrayList<String>(Arrays.asList("real")));
		getBasicCSSort().put("STRING", new ArrayList<String>(Arrays.asList("string")));
	}
	
	/**
	 * Function to get all the color sets
	 * @return Returns all the color set in one map
	 */
	public Map<String, List<String>> getColorSets(){
		Map<String,List<String>> to_return = new HashMap<String,List<String>>();
		to_return.putAll(getBasicCSSort());
		to_return.putAll(getColorSet());
		return to_return;
	}
	
	/**
	 * Function to get basic sort types
	 * @return Returns the map of basic sort types
	 */
	public Map<String, List<String>> getBasicSort(){
		return this.getBasicCSSort();
	}
	
	/**
	 * Function to get the map of variable sorts
	 * @return Returns a map containing the variable sorts
	 */
	public Map<String,List<String>> getVariableSort(){
		return this.variableSort;
	}
	
	/**
	 * Adds the derived color set
	 * @param key A color set
	 * @param list A list representing the break down of complex sorts
	 */
	public void addColorSet(String key, List<String> list){
		if(getColorSet().containsKey(key)){
			return;
		}
		List<String> basic_var = new ArrayList<String>();
		for(String var:list){
			normalizeSort(var, basic_var);
		}
		getColorSet().put(key,basic_var);
	}
	
	/**
	 * Using the basic color set and the previous color set, tries to normalize the new color set
	 * @param var variable for which color set needs to be defined 
	 * @param l List to be edited corresponding the parameter var
	 */
	public void normalizeSort(String var, List<String> l){
		if(getColorSet().containsKey(var)){
			l.addAll(getColorSet().get(var));
			return;
		}
		if(getBasicCSSort().containsKey(var)){
			l.addAll(getBasicCSSort().get(var));
			return;
		}
		System.out.println("Sort Resolving Error");
	}
	
	/**
	 * Adds the variable sort to the map
	 * @param key Sort of the new variable
	 * @param list List representing type of the variable
	 */
	public void addVariable(String key, List<String> list){
		if(getVariableSort().containsKey(key)){
			return;
		}
		List<String> basic_var = new ArrayList<String>();
		for(String var:list){
			normalizeSort(var,basic_var);
		}
		getVariableSort().put(key,basic_var);
		
	}
	
	/**
	 * Setter for setting variable sort
	 * @param variableSort Map to set the variableSort
	 */
	public void setVariableSort(Map<String, List<String>> variableSort) {
		this.variableSort = variableSort;
	}
	
	/**
	 * Getter for basic color set
	 * @return Map containing the basic color-set
	 */
	public Map<String, List<String>> getBasicCSSort() {
		return this.basicCSSort;
	}
	
	/**
	 * Setter for basic color set sort
	 * @param basicCSSort A map to set the basic color set
	 */
	public void setBasicCSSort(Map<String, List<String>> basicCSSort) {
		this.basicCSSort = basicCSSort;
	}
	
	/**
	 * Getter for user defined color-sets
	 * @return A map which contains user defined colorsets
	 */
	public Map<String, List<String>> getColorSet(){
		return this.colorSet;
	}

	/**
	 * Setter for user defined color-sets
	 * @param colorSet the colorSet to set
	 */
	public void setColorSet(Map<String, List<String>> colorSet) {
		this.colorSet = colorSet;
	}

}
