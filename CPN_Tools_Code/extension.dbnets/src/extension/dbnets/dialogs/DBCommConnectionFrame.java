package extension.dbnets.dialogs;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Observable;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

import extension.dbnets.javacpn.PollServer;
import extension.dbnets.simulatorextension.DButilityFunctions;

import com.jgoodies.forms.layout.FormSpecs;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;

/**
 * Contains a frame for entering for setting up Comms/CPN and DB connection. This frame extends observable
 * and the simulation gets notified once the connection to the database is established or revoked. The
 * extension populates view place corresponding to each connection state.
 * @author Aman Sinha
 *
 */

/*
 * This frame needs to be refactored to view to properly, or try netbeans to open this frame.
 */
public class DBCommConnectionFrame extends Observable{
	private ConnectionFrame frame;
	
	/**
	 * Constructor to create a new frame
	 * @wbp.parser.entryPoint
	 */
	public DBCommConnectionFrame(){
		setFrame(new ConnectionFrame());
	}
	
	/**
	 * Getter for the connection parameters frame
	 * @return the frame
	 */
	public ConnectionFrame getFrame() {
		return frame;
	}

	/**
	 * Setter for the connection parameters frame
	 * @param frame the frame to set
	 */
	public void setFrame(ConnectionFrame frame) {
		this.frame = frame;
	}

	/**
	 * ConnectionFrame 
	 * @author Aman Sinha
	 *
	 */
	public class ConnectionFrame extends JFrame {

		private static final long serialVersionUID = 1L;

		private JPanel contentPane;
	
		private JPasswordField textFieldDBPassword;
		private JTextField textFieldDBURL;
		private JTextField textFieldDBUserName;
		private JTextField textFieldDBName;
		private JLabel lblDbName;
		private JLabel lblDbUrl;
		private JLabel lblDbUsername;
		private JLabel lblDbPassword;
		JButton btnConnectDB;
		private JLabel lblDBConnectionSuccessful;
	
		private JLabel lblCpnPort;
		private JTextField textFieldPort;
		private JButton btnConnectComm;
		private JLabel lblCommConnectionSuccessful;
	
		public PollServer pollServer = null;
		public DButilityFunctions frameDBHandler = null;

	/**
	 * Create the frame.
	 */
		public ConnectionFrame() {
			setTitle("DB Marriage Extension Properties");
			setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			setBounds(100, 100, 573, 442);
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			contentPane.setLayout(new BorderLayout(0, 0));
			setContentPane(contentPane);
		
			JPanel DBpanel = new JPanel();
			contentPane.add(DBpanel, BorderLayout.NORTH);
			DBpanel.setLayout(new FormLayout(new ColumnSpec[] {
					ColumnSpec.decode("61px"),
					ColumnSpec.decode("52px"),
					FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("61px"),
					FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("50px:grow"),
					FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("19px"),
					FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("17px"),
					FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("94px"),
					FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("17px"),
					FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("77px"),},
					new RowSpec[] {
							FormSpecs.LINE_GAP_ROWSPEC,
							RowSpec.decode("22px"),
							FormSpecs.LINE_GAP_ROWSPEC,
							RowSpec.decode("25px"),
							FormSpecs.RELATED_GAP_ROWSPEC,
					FormSpecs.DEFAULT_ROWSPEC,
					FormSpecs.RELATED_GAP_ROWSPEC,
					FormSpecs.DEFAULT_ROWSPEC,
					FormSpecs.LINE_GAP_ROWSPEC,
					RowSpec.decode("16px"),
					FormSpecs.RELATED_GAP_ROWSPEC,
					FormSpecs.DEFAULT_ROWSPEC,
					FormSpecs.RELATED_GAP_ROWSPEC,
					FormSpecs.DEFAULT_ROWSPEC,
					FormSpecs.RELATED_GAP_ROWSPEC,
					FormSpecs.DEFAULT_ROWSPEC,
					FormSpecs.RELATED_GAP_ROWSPEC,
					FormSpecs.DEFAULT_ROWSPEC,
					FormSpecs.RELATED_GAP_ROWSPEC,
					FormSpecs.DEFAULT_ROWSPEC,
					FormSpecs.RELATED_GAP_ROWSPEC,
					FormSpecs.DEFAULT_ROWSPEC,
					FormSpecs.RELATED_GAP_ROWSPEC,
					FormSpecs.DEFAULT_ROWSPEC,
					FormSpecs.RELATED_GAP_ROWSPEC,
					FormSpecs.DEFAULT_ROWSPEC,}));
		
		
			lblDbName = new JLabel("DB Repository Name");
			DBpanel.add(lblDbName, "2, 4, 3, 1, left, center");
		
			textFieldDBName = new JTextField();
			DBpanel.add(textFieldDBName, "6, 4, 9, 1, default, top");
			textFieldDBName.setColumns(10);
		
			lblDbUrl = new JLabel("DB URL");
			DBpanel.add(lblDbUrl, "2, 6, left, center");
		
			textFieldDBURL = new JTextField("jdbc:postgresql://localhost:5432/");
			DBpanel.add(textFieldDBURL, "6, 6, 9, 1, default, top");
			textFieldDBURL.setColumns(10);
		
			lblDbUsername = new JLabel("DB Username");
			DBpanel.add(lblDbUsername, "2, 8, 3, 1, left, center");
		
			textFieldDBUserName = new JTextField();
			DBpanel.add(textFieldDBUserName, "6, 8, 9, 1, default, center");
			textFieldDBUserName.setColumns(10);
			
			lblDbPassword = new JLabel("DB Password");
			DBpanel.add(lblDbPassword, "2, 10, 3, 1, left, center");
		
			textFieldDBPassword = new JPasswordField();
			DBpanel.add(textFieldDBPassword, "6, 10, 9, 2, default, center");
			textFieldDBPassword.setColumns(10);
		
		
			btnConnectDB = new JButton("Connect DB");
			btnConnectDB.addActionListener(new ConnectDBListener());
			DBpanel.add(btnConnectDB, "6, 14, default, top");
		
			lblDBConnectionSuccessful = new JLabel("Connection Successful");
			lblDBConnectionSuccessful.setForeground(new Color(0, 128, 0));
			lblDBConnectionSuccessful.setVisible(false);
			DBpanel.add(lblDBConnectionSuccessful, "8, 14, 5, 1, center, center");
		
			lblCpnPort = new JLabel("CPN/COMM Port");
			DBpanel.add(lblCpnPort, "2, 20, 3, 1");
		
			textFieldPort = new JTextField();
			DBpanel.add(textFieldPort, "6, 20, 9, 1, fill, default");
			textFieldPort.setColumns(10);
		
			btnConnectComm = new JButton("Connect CPN/COMMS");
			btnConnectComm.setFont(new Font("Tahoma", Font.PLAIN, 11));
			btnConnectComm.addActionListener(new ConnectCommListener());
			DBpanel.add(btnConnectComm, "6, 24, 3, 1");
		
			lblCommConnectionSuccessful = new JLabel("Connection Successful");
			lblCommConnectionSuccessful.setForeground(new Color(0, 128, 0));
			lblCommConnectionSuccessful.setVisible(false);
			DBpanel.add(lblCommConnectionSuccessful, "10, 24, 5, 1, center, center");
		}
		
		
		/**
		 * Function to get the connection status of the database
		 * @return Returns the connection status of the database
		 */
		public boolean getDBConnectionStatus(){
			if(getFrameDBHandler() == null){
				return false;
			}
			return getFrameDBHandler().getConnectionStatus();
		}
		
		/**
		 * Function to get the Comms/CPN listening status to the port
		 * @return Returns the Comms/CPN listening status to the port
		 */
		public boolean getCommConnectionStatus(){
			if(pollServer == null){
				return false;
			}
			return pollServer.getRunningStatus();
		}
		
		/**
		 * Function to get the Frame DB Handler
		 * @return the frameDBHandler
		 */
		public DButilityFunctions getFrameDBHandler() {
			return frameDBHandler;
		}


		/**
		 * Function to set the Frame DB Handler
		 * @param frameDBHandler the frameDBHandler to set
		 */
		public void setFrameDBHandler(DButilityFunctions frameDBHandler) {
			this.frameDBHandler = frameDBHandler;
		}

		/**
		 * ActionListener for connecting/disconnecting database.
		 * @author Aman Sinha
		 *
		 */
		private class ConnectDBListener implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if(getFrameDBHandler() == null || !getFrameDBHandler().getConnectionStatus()){
					setFrameDBHandler(new DButilityFunctions());
					getFrameDBHandler().setConnectionParameters(textFieldDBName.getText(), textFieldDBURL.getText(), textFieldDBUserName.getText(), textFieldDBPassword.getText());
					boolean success = getFrameDBHandler().setupDBConnection();
					if(success){
						if(pollServer != null){
							pollServer.setDBHandler(getFrameDBHandler());
						}
						btnConnectDB.setText("Disconnect DB");
						lblDBConnectionSuccessful.setVisible(true);
						textFieldDBName.setEditable(false);
						textFieldDBURL.setEditable(false);
						textFieldDBUserName.setEditable(false);
						textFieldDBPassword.setEditable(false);
					}
				}
				else{
					boolean success = getFrameDBHandler().closeConnection();
					if(success){
						btnConnectDB.setText("Connect DB");
						lblDBConnectionSuccessful.setVisible(false);
						textFieldDBName.setEditable(true);
						textFieldDBURL.setEditable(true);
						textFieldDBUserName.setEditable(true);
						textFieldDBPassword.setEditable(true);
					}				
				}
				setChanged();
				notifyObservers();
			}
		
		}
		
		/**
		 * ActionListener for connecting/disconnecting COMM/CPN port
		 * @author Aman Sinha
		 *
		 */
		private class ConnectCommListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if(pollServer == null || !pollServer.getRunningStatus()){
					int port = Integer.parseInt(textFieldPort.getText());
					pollServer = new PollServer(port, null, getFrameDBHandler(), textFieldPort, btnConnectComm, lblCommConnectionSuccessful);
					pollServer.start();
				}
				else{
					try {
						pollServer.closeConnection();
					} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					}
				
				}
			
			}
		
		}

	}
}
