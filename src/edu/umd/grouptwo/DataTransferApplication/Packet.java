package edu.umd.grouptwo.DataTransferApplication;

public abstract class Packet {
	
	public String packetType;		//These two fields are common in INIT, IACK, DATA, DACK, hence part of the abstract class.
	public String integrityCheck;
	
	public abstract String composePacket();		//This method will be implemented by individual classes (packets)
	
}
