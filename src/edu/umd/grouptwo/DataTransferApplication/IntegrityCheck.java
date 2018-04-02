package edu.umd.grouptwo.DataTransferApplication;

public class IntegrityCheck {
	
	/*	+-----------------------------------------------------------------------------------------------------+
	 *  | Method Name: 		calculateAtTransmitter															|
	 *  | Method Purpose: 	Computes the value of Integrity check at the sender and returns it				|
	 *  | Input: 			--																				|
	 *  | Output: 			String: Integrity Check value													|
	 *  | Throws: 			--																				|
	 *  +-----------------------------------------------------------------------------------------------------+
	 */
	
	public static String calculateAtTransmitter(String data) {
		data = data.substring(0, data.length()-16);
		String integrityCheck = "0000000000000000";
		if(data.length() % 16 != 0)
			data = data + "00000000";
		
		for (int i = 0; i<= data.length() - 16; i+=16) {
			int xorValue = Integer.valueOf(integrityCheck, 2) ^ Integer.valueOf(data.substring(i, i+16), 2);
			integrityCheck = Integer.toBinaryString(xorValue);
			while(integrityCheck.length() != 16) {
				integrityCheck = "0" +  integrityCheck;
			}
		}
		return integrityCheck;
	}
	
	/*	+-----------------------------------------------------------------------------------------------------+
	 *  | Method Name: 		calculateAtReceiver																|
	 *  | Method Purpose: 	Computes the value of Integrity check at the receiver and returns it,				| 
	 *  	|					the receiver then checks if its all 0's ( for a correct packet)					|
	 *  | Input: 			--																				|
	 *  | Output: 			String: Integrity Check value													|
	 *  | Throws: 			--																				|
	 *  +-----------------------------------------------------------------------------------------------------+
	 */
	
	public static String calculateAtReceiver(String data) {
		String integrityCheck = "0000000000000000";
		if(data.length() % 16 != 0)
			data = data.substring(0, data.length()-16) + "00000000" + data.substring(data.length()-16, data.length());
		
		for (int i = 0; i<= data.length() - 16; i+=16) {
			int xorValue = Integer.valueOf(integrityCheck, 2) ^ Integer.valueOf(data.substring(i, i+16), 2);
			integrityCheck = Integer.toBinaryString(xorValue);
			while(integrityCheck.length() != 16) {
				integrityCheck = "0" +  integrityCheck;
			}
		}
		return integrityCheck;
	}
	
}
