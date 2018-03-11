package extension.dbnets.simulatorextension;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JOptionPane;

/**
 * Class to establish connection with the database
 * @author Aman Sinha
 *
 */
public class DButilityFunctions {
	/**
	 * A connection type variable
	 */
	private Connection connection;
	public boolean connectionStatus = false;
	/**
	 * Constructor
	 */
	public DButilityFunctions()
	{
		setConnection(null);
		setConnectionStatus(false);
	}
	private String dbName = "CPN_DB";
	private String dbURL = "jdbc:postgresql://localhost:5432/" + getDbName();
	private String dbUserName = "postgres";
	private String dbPassword = "tiger";
	
	/**
	 * Function to set up connection
	 * @param db_name Database Name
	 * @param db_url Database URL
	 * @param user_name Database Username
	 * @param password Database Password
	 */
	public void setConnectionParameters(String db_name, String db_url, String user_name, String password){
		setDbName(db_name);
		setDbURL(db_url + getDbName());
		setDbUserName(user_name);
		setDbPassword(password);
	}
	
	/**
	 * Sets up a database connection and returns the result
	 * @return A boolean specifying if the connection is successful
	 */
	public boolean setupDBConnection(){
		try {
			//currently supported only for POSTGRES SQL.
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("Check your jar file for your DBMS");
			return false;
		}
		try {
			setConnection(DriverManager.getConnection(getDbURL(), getDbUserName(), getDbPassword()));
			connectionStatus = true;
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
				    "Database connection unsuccessful, please check parameters",
				    "Database connection error",
				    JOptionPane.ERROR_MESSAGE);
			//System.out.println("Could not connect to the Database");
			return false;
		}
		return true;
	}
	
	/**
	 * Closes the connection the existing DB connection
	 * @return Returns if the connection is successfully closed
	 */
	public boolean closeConnection(){
		boolean success = false;
		if(getConnection()!=null){
			try {
				getConnection().close();
				success = true;
				connectionStatus = false;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return success;
	}
	
	/**
	 * Executes query to the DB
	 * @param query Query to be executed
	 * @return Returns the resultset of the query
	 */
	public ResultSet queryExecute(String query){
		ResultSet rs = null;
		Statement stmt;
		try {
			stmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			if(query.toLowerCase().contains("insert") || query.toLowerCase().contains("update") || query.toLowerCase().contains("delete")){
				stmt.executeUpdate(query);
			}
			else{
				rs = stmt.executeQuery(query);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;	
	}
	
	/*
	 * This function is never called. One could modify this
	 */
	/**
	 * checks whether there exists an entry to the database table
	 * @param schemaName Schema Name to be queried
	 * @param tableName Table Name to be queried
	 * @param columnName Select Column Name corresponding to the table
	 * @param key The column name to be searched for 
	 * @param value The value to be searched for
	 * @return Returns the result ser after executing the query
	 */
	public ResultSet entryExist(String schemaName, String tableName, String columnName, String key, String value){
		ResultSet rs = null;
		try {
			Statement stmt = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			StringBuilder query = new StringBuilder("SELECT " +
					columnName + " FROM "+
					 schemaName + "." + tableName
					 +" WHERE " + key + " = " + value +";");
			rs = stmt.executeQuery(query.toString());
			//rs.first();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
		
	}
	
	/**
	 * Function to get the connection status
	 * @return Returns the connection status with the database
	 */
	public boolean getConnectionStatus(){
		return connectionStatus;
	}
	
	/**
	 * Function to set the connection status
	 * @param val Value to be set for the connection status
	 */
	public void setConnectionStatus(boolean val){
		connectionStatus = val;
	}

	/**
	 * Getter for the connection
	 * @return the connection
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * Setter for the connection
	 * @param connection the connection to set
	 */
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	/**
	 * Getter for the database name
	 * @return the dbName
	 */
	public String getDbName() {
		return dbName;
	}

	/**
	 * Setter for the database name
	 * @param dbName the dbName to set
	 */
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	/**
	 * Getter for the database URL
	 * @return the dbURL
	 */
	public String getDbURL() {
		return dbURL;
	}

	/**
	 * Setter for the database URL
	 * @param dbUrl the dbURL to set
	 */
	public void setDbURL(String dbUrl) {
		this.dbURL = dbUrl;
	}

	/**
	 * Getter for the database username
	 * @return the dbUserName
	 */
	public String getDbUserName() {
		return dbUserName;
	}

	/**
	 * Setter for the database username
	 * @param dbUserName the dbUserName to set
	 */
	public void setDbUserName(String dbUserName) {
		this.dbUserName = dbUserName;
	}

	/**
	 * Getter for the database password
	 * @return the dbPassword
	 */
	public String getDbPassword() {
		//One can use encryption here and handle things accordingly.
		return dbPassword;
	}

	/**
	 * Setter for the database password
	 * @param dbPassword the dbPassword to set
	 */
	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}
}
