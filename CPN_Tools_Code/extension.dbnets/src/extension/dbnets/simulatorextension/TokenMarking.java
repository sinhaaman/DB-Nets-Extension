package extension.dbnets.simulatorextension;

import java.util.List;

/**
 * A class for a token containing the amount and the data
 * @author Aman Sinha
 *
 */
public class TokenMarking {
	private long tokenQuantity;
	private String tokenData;
	
	/**
	 * Constructor
	 */
	TokenMarking(){
		
	}
	/**
	 * Constructor
	 * @param quan The numeric quantity of the token 
	 * @param data The string containing the data
	 */
	TokenMarking(int quan,String data){
		setTokenData(data);
		setTokenQuantity(quan);
	}
	
	/**
	 * Getter function for the token quantity
	 * @return Returns the token quantity
	 */
	public long getTokenQuantity(){
		return this.tokenQuantity;
	}
	
	/**
	 * Getter function for the token data
	 * @return Returns the token data
	 */
	public String getTokenData(){
		return this.tokenData;
	}
	
	/**
	 * Setter function to set the token quantity 
	 * @param quan Quantity for the token
	 */
	public void setTokenQuantity(long quan){
		 this.tokenQuantity = quan;
	}
	
	/**
	 * Setter function to set the token data
	 * @param data Data for the token
	 */
	public void setTokenData(String data){
		 this.tokenData = data;
	}
	
	/**
	 * Calculates the numeric quantity for all the list of tokens
	 * @param tokens A list containing token markings  
	 * @return Total count of the markings
	 */
	public static int totalMarkings(List<TokenMarking> tokens) {
		int to_return = 0;
		for(TokenMarking t : tokens){
			to_return += t.getTokenQuantity();
		}
		return to_return;
	}
	

}
