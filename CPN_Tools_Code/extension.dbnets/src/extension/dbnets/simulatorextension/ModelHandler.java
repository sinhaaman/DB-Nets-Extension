package extension.dbnets.simulatorextension;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.parsers.ParserConfigurationException;

import org.cpntools.accesscpn.model.PetriNet;
import org.cpntools.accesscpn.model.importer.DOMParser;
import org.cpntools.accesscpn.model.importer.NetCheckException;
import org.xml.sax.SAXException;

/**
 * Class to handle the loaded CPN model
 * @author Aman Sinha
 *
 */
public class ModelHandler {
	private String modelName;
	private String modelLocation;
	public ModelHandler(){
		
	}
	
	/*
	 * 
	 */
	/**
	 * Constructor for Model Handler
	 * @param model_name Model Name
	 * @param location Model Location
	 */
	public ModelHandler(String model_name, String location){
		setModelName(model_name);
		setModelLocation(location);
	}
	
	/**
	 * Getter for the model name
	 * @return Returns the model name
	 */
	public String getModelName(){
		return modelName;
	}
	
	/**
	 * Getter for the model location
	 * @return Returns the model location
	 */
	public String getModelLocation(){
		return modelLocation;
	}
	
	/**
	 * Setter for the model name
	 * @param model_name Model Name
	 */
	public void setModelName(String model_name){
		modelName = model_name;
	}
	
	/**
	 * Setter for the model location
	 * @param location Model location
	 */
	public void setModelLocation(String location){
		modelLocation = location;
	}
	
	/**
	 * Getter for petri net
	 * @return Returns the petri net for the model
	 * @throws MalformedURLException
	 * @throws NetCheckException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public PetriNet getPetriNet() throws MalformedURLException, NetCheckException, SAXException, IOException, ParserConfigurationException{
		File f = new File(getModelPath());
		return DOMParser.parse(f.toURI().toURL());
	}

	/**
	 * Getter for model path
	 * @return Returns the model path
	 */
	public String getModelPath(){
		String model_path = modelLocation + modelName;
		return (model_path);
	}
}
