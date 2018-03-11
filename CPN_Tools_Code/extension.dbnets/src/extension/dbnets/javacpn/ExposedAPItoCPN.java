package extension.dbnets.javacpn;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import extension.dbnets.simulatorextension.DButilityFunctions;
import extension.dbnets.simulatorextension.UtilityFunctions;
/**
 * This class contains the APIs which are exposed to CPN Tools
 * @author Aman Sinha
 *
 */

public class ExposedAPItoCPN {

	/**
	 * Generates a random with three different flavours, randomString, randomTime, randomInteger
	 * @param params The parameters passed from the CPN Tools
	 * @return Returns a string depending on the called API
	 */
	public static String handleRandomGen(String[] params) {
		String result = "";
		if(params[1].toLowerCase().equals("randomstring")){
			result+=UtilityFunctions.genRandomString(Integer.parseInt(params[2]));
		}
		else if(params[1].toLowerCase().equals("randomtime")){
			result+=UtilityFunctions.genRandomTime();
		}
		else if(params[1].toLowerCase().equals("randomint")){
			result+=UtilityFunctions.genRandomNumeric(Integer.parseInt(params[2]));
		}
		return result;
	}

	/**
	 * Function which handles the execution of a query
	 * @param params Parameters received from the CPN Tools
	 * @param db_handler The database handler through which the query needs to passed
	 * @return Returns a String which fetches the value from result set
	 */
	public static String handleExecuteQuery(String[] params, DButilityFunctions db_handler) {
		if(db_handler == null || !db_handler.getConnectionStatus()){
			showDBConnErrorDialog();
			return null;
		}
		String query = params[1];
		ResultSet rs = db_handler.queryExecute(query);
		if(rs == null){
			return null;
		}
		String result ="";
		try {
			rs.beforeFirst();
			while(rs.next()){
				result = rs.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;	
	}

	/**
	 * Function to handles database specific functions such as to get the unique identifier for a particular column
	   of a table
	 * @param params The parameters from CPN Tools
	 * @param db_handler The database handler through which the queries needs to be passed
	 * @return Returns the unique identifier for a table
	 */
	/*
	 * Currently, supported APIs to CPN Tools is "genUniqueID", there can be numerous such APIs
	 */
	public static String handleDBFunctions(String[] params, DButilityFunctions db_handler) {
		// TODO Auto-generated method stub
		String result = "";
		if(db_handler == null || !db_handler.getConnectionStatus()){
			showDBConnErrorDialog();
			return result;
		}
		String funcAPI = params[1];
		String table_name = params[2];
		String c_name = params[3];
		String length = params[4];
		if(funcAPI.equals("genUniqueID")){
			String query = UtilityFunctions.genUniqueIDQuery(table_name, c_name, Integer.parseInt(length));
			ResultSet rs = db_handler.queryExecute(query);
			try {
				rs.beforeFirst();
				while(rs.next()){
					result = rs.getString(1);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		}
		return result;
	}

	/**
	 * Function to show an error dialog box related to database
	 */
	private static void showDBConnErrorDialog(){
		JOptionPane.showMessageDialog(null,
				"Not connected to database, check database connection first",
				"Datase Connection Error",
				JOptionPane.ERROR_MESSAGE);
	}

}
