package extension.dbnets.simulatorextension;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class contains all the required miscellaneous functions
 * @author Aman Sinha
 *
 */
public class UtilityFunctions {
	
	
	/**
	 * Parses a string and returns the marking in the form of multisets.
	 * @param marking Takes a string marking
	 * @return Returns a list of TokenMarkings
	 */
	public static List<TokenMarking> parseMarking(String marking){
		List<TokenMarking> p_mark_list = new ArrayList<TokenMarking>();
		if(marking.equals("") || marking.equals("empty")){
			return p_mark_list;
		}
		String[] splitted_tokens = marking.split("\\++");
		TokenMarking current_token = null;
		for(String s : splitted_tokens){
			String[] token_val = s.split("`");
			current_token = new TokenMarking(Integer.parseInt(token_val[0].replaceAll("\\s+","")),token_val[1].replaceAll("\\s+",""));
			p_mark_list.add(current_token);
		}
		return p_mark_list;
	}
	
	/**
	 * Generates the random number inclusive of the provided minimum and maximum values
	 * @param min the minimum value
	 * @param max the maximum value
	 * @return A random number inclusive of the range
	 */
	public static int randomNumber(int min, int max){
		return ThreadLocalRandom.current().nextInt(min, max + 1);
		
	}
	
	/**
	 * Generates a random number (64-bit integer) of the given length
	 * @param length The length/digits of the random number to be generated
	 * @return Returns a random number (64-bit integer) of the given length
	 */
	public static long genRandomInt(int length){
		return randomNumber(10^(length-1),10^(length));
	}
	
	/**
	 * Generates a string representing a number of the given length
	 * @param length The length/digits of the random number to be generated
	 * @return Returns a string representing a number of the given length
	 */
	public static String genRandomNumeric(int length){
		String to_return = "";
		for(int i=1;i<=length;++i){
			int res = randomNumber(0,9);
			char c = '0';
			c+=res;
			to_return+=c;
		}
		return to_return;
	}
	
	/**
	 * Generate random string of a given length
	 * @param length The length of the random string to be generated
	 * @return Returns a randomly generated string of the given length
	 */
	public static String genRandomString(int length){
		String to_return = "";
		for(int i=1;i<=length;++i){
			long res = randomNumber(0,25);
			char c = 'A';
			c+=res;
			to_return+=c;
		}
		return to_return;
	}
	
	/**
	 * Generates a random time which is compatible with SQL format.
	 * @return A random string representing random time compatible with SQL format.
	 */
	public static String genRandomTime(){
		String t = "";
		long hh = randomNumber(0,23);
		long mm = randomNumber(0,59);
		long ss = randomNumber(0,59);
		t+=Long.toString(hh) + ":" + Long.toString(mm) + ":" + Long.toString(ss);
		return t;
	}
	
	/**
	 * Generate a current system time which is compatible with SQL format.
	 * @return A string representing the current system time compatible with SQL format.
	 */
	public static String genSystemTime(){
		long MSEC_SINCE_EPOCH = System.currentTimeMillis();
		Date instant = new Date( MSEC_SINCE_EPOCH );
		SimpleDateFormat sdf = new SimpleDateFormat( "hh:mm:ss" );
		String time = sdf.format( instant );
		return time;
	}
	
	/**
	 * Returns a query which helps in generating a unique id of fixed length for a specific primary key of a table
	 * @param table_name Table name for which the unique identifier needs to be generated
	 * @param col_name Column name for which the unique identifier needs to be generated
	 * @param length Length of the unique identifier
	 * @return A String representing the unique identifier of given length, table name and column name
	 */
	public static String genUniqueIDQuery(String table_name, String col_name, int length){
		String lower_limit = "10^" + Integer.toString(length-1);
		String upper_limit = "10^" + Integer.toString(length);
		String query = "SELECT random_num FROM ("
		  + "SELECT FLOOR(RANDOM() * (" + upper_limit + "-" + lower_limit + ") + " + lower_limit + ") AS random_num "
		  + "FROM "+ table_name
		  + " UNION "
		  + "SELECT FLOOR(RANDOM() * (" + upper_limit + "-" + lower_limit + ") + " + lower_limit + ") AS random_num "
		+") AS numbers_mst_plus_1 "
		+"WHERE \"random_num\" NOT IN (SELECT " + col_name +" FROM " + table_name + ")"+
		"LIMIT 1;";
		return query;
	}
	
	/**
	 * Calculates the markings of the view place
	 * @param rs ResultSet of a result of a query
	 * @param list A list containing the type corresponding to the result set
	 * @return A string representing a marking of a place
	 */
	public static String getViewPlaceMarking(ResultSet rs, List<String> list) {
		String result = null;
		Map<String,Integer> marking_map = new HashMap<String,Integer>();
		try {
			rs.beforeFirst();
			while(rs.next()){
				StringBuilder marking = new StringBuilder("(");
				String prefix = "";
				for(int i=0;i<list.size();++i){
					marking.append(prefix);
					prefix = ",";
					String type = list.get(i);
					String temp = rs.getString(i+1);
					if(type.equals("string")){
						marking.append("\"" + temp + "\"");
					}
					else if(type.equals("bool")){
						if(temp.equals("t")){
							marking.append(true);
						}
						else{
							marking.append(false);
						}
					}
					else{
						marking.append(temp);
					}					
				}
				marking.append(")");
				if(marking_map.containsKey(marking.toString())){
					marking_map.put(marking.toString(), marking_map.get(marking.toString()) + 1);
				}
				else
				{
					marking_map.put(marking.toString(), 1);
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		StringBuilder marking = new StringBuilder();
		String prefix = "";
		for(String key:marking_map.keySet()){
			marking.append(prefix + marking_map.get(key) + "`" + key);
			prefix = " ++ ";
		}
		result = marking.toString();
		return result;
	}

}
