package edu.umd.grouptwo.DataTransferApplication;

public class DataPacket extends Packet {

	public String sequenceNumber;		//Fields of the DATA Packet
	public String payload;
	
	public DataPacket() {			//Constructor initializes the packetType field and integrityCheck field
		this.packetType = "00"+Integer.toBinaryString(Integer.valueOf("33", 16));
		this.integrityCheck = "0000000000000000";
	}
	
	/*	+-----------------------------------------------------------------------------------------------------+
	 *  | Method Name: 		composePacket																	|
	 *  | Method Purpose: 	Composes the packet to be sent, in other words concatenates the different fields	|
	 *  | Input: 			--																				|
	 *  | Output: 			String: Packet Content															|
	 *  | Throws: 			--																				|
	 *  +-----------------------------------------------------------------------------------------------------+
	 */
	
	public String composePacket() {
		return super.packetType + sequenceNumber + payload + super.integrityCheck;
	}
	
}
