package extension.dbnets.javacpn;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import extension.dbnets.simulatorextension.DButilityFunctions;

/**
 * Class for creating a poll server which runs as a thread and listens to a particular specified port
 * @author Aman Sinha
 *
 */
public class PollServer extends Thread{
	private String hostName;
	private int port;
	private DButilityFunctions dbHandler;
	private JavaCPN javaCPN;
	private boolean isRunning = false;
	private JTextField textFieldPort = null;
	private JButton btnConnectComm = null;
	private JLabel lblCommConnectionSuccessful = null;
	
	/**
	 * Constructor for POllServer
	 * @param port_number Port number to listen at
	 * @param host_name Host name, in case of remote connection
	 * @param db_hand The database handler through which it can pass its queries and actions
	 * @param textFldPort The text field in the form which specifies the port to listen at
	 * @param btnConnComm The button which confirms the selected port
	 * @param lbl Label representing if the connection is successful or not
	 */
	public PollServer(int port_number, String host_name, DButilityFunctions db_hand, JTextField textFldPort, JButton btnConnComm, JLabel lbl){
		super("pollCPN");
		setTextFieldPort(textFldPort);
		setBtnConnectComm(btnConnComm);
		setLblCommConnectionSuccessful(lbl);
		setPort(port_number);
		setHostName(host_name);
		setDBHandler(db_hand);
		setJavaCPN(new JavaCPN());
	}
	
	public PollServer(){
		
	}
	
	/**
	 * Function for the thread run
	 */
	public void run(){
		try {
			getJavaCPN().connect(getHostName(), getPort());
			System.out.println("Connection Successful CPN/COMMS");
			setRunning(true);
			getBtnConnectComm().setText("Disconnect CPN/COMMS");
			getLblCommConnectionSuccessful().setVisible(true);
			getTextFieldPort().setEditable(false);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			while(isRunning()){
				String fromCPN = EncodeDecode.decodeString(getJavaCPN().receive());
				String[] params = fromCPN.split("\\?");
				String func = params[0];
				//Reflection in JAVA can also be used to check which function to call.
				if(func.equals("getRandom")){
					String to_send = ExposedAPItoCPN.handleRandomGen(params);
					getJavaCPN().send(EncodeDecode.encode(to_send));
				}
				else if(func.equals("getFromDB")){
					String to_send = ExposedAPItoCPN.handleDBFunctions(params, getDBHandler());
					getJavaCPN().send(EncodeDecode.encode(to_send));
				}
				else if(func.equals("exQuery")){
					String to_send = ExposedAPItoCPN.handleExecuteQuery(params, getDBHandler());
					if(to_send != null){
						getJavaCPN().send(EncodeDecode.encode(to_send));
					}
				}
			}
		}
		catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Function to check the running status of the thread
	 * @return Returns true if the thread is running, else returns false
	 */
	public boolean getRunningStatus() {
		// TODO Auto-generated method stub
		return isRunning();
	}
	
	/**
	 * Function to close the connection
	 * @throws IOException throws IOException if there is problem in closing the connection 
	 */
	public void closeConnection() throws IOException{
		getJavaCPN().disconnect();
		setRunning(false);
		getBtnConnectComm().setText("Connect CPN/COMMS");
		getLblCommConnectionSuccessful().setVisible(false);
		getTextFieldPort().setEditable(true);
	}
	
	/**
	 * Getter for the connection host name
	 * @return the hostName
	 */
	public String getHostName() {
		return hostName;
	}
	
	/**
	 * Setter for the connection host name
	 * @param hostName the hostName to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	
	/**
	 * Getter for the connection port
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * Setter for the connection port
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
	/**
	 * Function to get the database handler
	 * @return Returns the database handler
	 */
	public DButilityFunctions getDBHandler() {
		return dbHandler;
	}
	
	/**
	 * Function to set the database handler
	 * @param db_handler The database handler
	 */
	public void setDBHandler(DButilityFunctions db_handler) {
		this.dbHandler = db_handler;
	}
	
	/**
	 * Getter for JAVA/CPN
	 * @return the JAVA CPN
	 */
	public JavaCPN getJavaCPN() {
		return javaCPN;
	}
	
	/**
	 * Setter for JAVA/CPN
	 * @param j_cpn the JAVA CPN to set
	 */
	public void setJavaCPN(JavaCPN j_cpn) {
		this.javaCPN = j_cpn;
	}
	
	/**
	 * Getter to get the running status of the PollServer
	 * @return the running status of the PollServer
	 */
	public boolean isRunning() {
		return isRunning;
	}
	
	/**
	 * Setter to set the running status of the PollServer
	 * @param isRunning the isRunning to set
	 */
	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	
	/**
	 * Getter for Port text field
	 * @return the Port text field
	 */
	public JTextField getTextFieldPort() {
		return textFieldPort;
	}
	
	/**
	 * Setter for Port text field
	 * @param textFieldPort the Port text field to set
	 */
	public void setTextFieldPort(JTextField textFieldPort) {
		this.textFieldPort = textFieldPort;
	}
	
	/**
	 * Getter for the connection button
	 * @return the connection button
	 */
	public JButton getBtnConnectComm() {
		return btnConnectComm;
	}
	
	/**
	 * Setter for the connection button
	 * @param btnConnectComm the connection button to set
	 */
	public void setBtnConnectComm(JButton btnConnectComm) {
		this.btnConnectComm = btnConnectComm;
	}
	
	/**
	 * Getter for the connection label
	 * @return  the label of connection
	 */
	public JLabel getLblCommConnectionSuccessful() {
		return lblCommConnectionSuccessful;
	}
	
	/**
	 * Setter for the connection label
	 * @param lblCommConnectionSuccessful the label of connection to set
	 */
	public void setLblCommConnectionSuccessful(JLabel lblCommConnectionSuccessful) {
		this.lblCommConnectionSuccessful = lblCommConnectionSuccessful;
	}

}
