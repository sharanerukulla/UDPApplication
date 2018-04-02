package edu.umd.grouptwo.DataTransferApplication;

public class DataPacketAcknowledgement extends Packet {
	
	public String acknowledgementNumber;			//Fields of the DACK Packet
	
	public DataPacketAcknowledgement() {			//Constructor initializes the packetType field and integrityCheck field
		this.packetType = Integer.toBinaryString(Integer.valueOf("cc", 16));
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
		return super.packetType + acknowledgementNumber + super.integrityCheck;
	}
	
}
