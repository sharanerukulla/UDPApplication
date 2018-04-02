package edu.umd.grouptwo.DataTransferApplication;

public class InitialPacket extends Packet {

	public String initSequenceNumber;		//Fields of the INIT Packet
	public String numDataPackets;
	public String numPayloadBytes;
	
	public InitialPacket() {			//Constructor initializes the packetType field and integrityCheck field
		this.packetType = "0"+Integer.toBinaryString(Integer.valueOf("55", 16));
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
		return super.packetType + initSequenceNumber + numDataPackets + numPayloadBytes + super.integrityCheck;
	}
	
}
