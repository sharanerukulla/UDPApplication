package edu.umd.grouptwo.DataTransferApplication.Receiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import edu.umd.grouptwo.DataTransferApplication.DataPacketAcknowledgement;
import edu.umd.grouptwo.DataTransferApplication.InitialPacketAcknowledgement;
import edu.umd.grouptwo.DataTransferApplication.IntegrityCheck;

public class Receiver {
	
	private static final int SERVER_PORT = 9999;			//Port on the which the UDP Service is running on the receiver.
	private static final int INIT_PACKET_TYPE = 1;		//Constant to identify INIT Packets
	private static final int DATA_PACKET_TYPE = 2;		//Constant to identify INIT Packets

	public static void main(String[] args) {
		try {
			DatagramSocket serverDatagramSocket = new DatagramSocket(SERVER_PORT);		//Creating a Datagram socket on the specified port on the Receiver
			System.out.println("Server is running!");
			double effectiveDataRate;	//Creating the required variables
			long firstPacketRxTime = 0, lastPacketRxTime = 0, transmissionTime = 0;		//Variables to measure the total time taken to receive the payload
			String data = "", receivedDataInString, prevAckNumber = "", totalReceivedData = "";			//Variables to store the data extracted from the packets
			int clientPort, numPayloadBytes = 0, numDataPackets = 0, numBits, dataPacketCounter = 0, packetType;			//Variables to store the data extracted from the packets
			byte[] initAckContent = new byte[5], dataAck = new byte[5], packetContent = new byte[305];
			DatagramPacket iackPacketToBeTransmitted, dataPacketToBeTransmitted, packetFromClient = new DatagramPacket(packetContent, packetContent.length);		//Datagram packets to send and receive packets from the transmitter
			InetAddress clientAddress;
			
			while(true) {
				System.out.println("Waiting for a packet from the client.");
				serverDatagramSocket.receive(packetFromClient);		//Waiting to receive packets from the transmitter
				System.out.println("Packet received from the client.");
				clientAddress = packetFromClient.getAddress();				//Extracting the IPAddress of the client from the received packet
				clientPort = packetFromClient.getPort();						//Extracting the Port of the client from the received packet
				packetContent = packetFromClient.getData();					//Extracting data from the packet received from the client
				receivedDataInString = bytesToString(packetContent);
				packetType = checkPacketType(receivedDataInString);
				serverDatagramSocket.connect(clientAddress, clientPort);		//Attempting to connect to the client
				if ( packetType == INIT_PACKET_TYPE) {
					receivedDataInString = receivedDataInString.substring(0, 72);		//As INIT packet is 9 Bytes (72 bits) long
					if(checkInitPacket(receivedDataInString)) {
						InitialPacketAcknowledgement initialAck = new InitialPacketAcknowledgement();
						initialAck.acknowledgementNumber = decimalToBinaryString(Integer.toBinaryString( (1 + Integer.valueOf(receivedDataInString.substring(8, 24), 2)) % 65535 ));	//Sending the next expected Sequence number as the acknowledgement number
						initialAck.integrityCheck = IntegrityCheck.calculateAtTransmitter(initialAck.composePacket());	
						initAckContent = stringToBytes(initialAck.composePacket());
						iackPacketToBeTransmitted = new DatagramPacket(initAckContent, initAckContent.length, clientAddress, clientPort);
						numDataPackets = Integer.valueOf(receivedDataInString.substring(24, 40), 2);			//Extracting the total number of data packets the the transmitter will be sending to receiver
						numPayloadBytes = Integer.valueOf(receivedDataInString.substring(40, 56), 2);		//Extracting the total number of payload bytes of data the transmitter will be sending to receiver
						System.out.println("INIT Packet is received from the client.");
						serverDatagramSocket.send(iackPacketToBeTransmitted);			//Sending IACK Packet to the transmitter
						System.out.println("IACK Packet is sent to the client.");
						prevAckNumber = initialAck.acknowledgementNumber;				//Holiding the sent acknowledgement number in memory to check the next packet's that will be received are in-order or not
					} else {
						System.err.println("Wrong packet received, hence discarding.");
					}
				}
				else if ( packetType == DATA_PACKET_TYPE ) {
					dataPacketCounter++;		//Increment the data packet counter when ever a data packet is received from the transmiter
					if(dataPacketCounter == 1)
						firstPacketRxTime = System.currentTimeMillis();		//Record the time when a first data packet is received from the transmitter
					if(dataPacketCounter == numDataPackets)
						lastPacketRxTime = System.currentTimeMillis();		//Record the time when the last data packet is received from the transmitter
					if(checkDataPacket(receivedDataInString, prevAckNumber)) {
						DataPacketAcknowledgement dack = new DataPacketAcknowledgement(); //
						dack.acknowledgementNumber = decimalToBinaryString(Integer.toBinaryString( ( (numPayloadBytes / numDataPackets) + Integer.valueOf(receivedDataInString.substring(8, 24), 2)) % 65535 ));
						//Calculating the acknowledgement number to be sent to the transmitter and wrapping it around if it crosses maximum 16 Bit number
						dack.integrityCheck = IntegrityCheck.calculateAtTransmitter(dack.composePacket());
						String dackPacket = dack.composePacket();
						dataAck = stringToBytes(dackPacket);
						dataPacketToBeTransmitted = new DatagramPacket(dataAck, dataAck.length, clientAddress, clientPort);
						System.out.println("Data Packet [ " + dataPacketCounter + " ] is received from the client.");
						data += receivedDataInString.substring(24, 24+2400);			//Extracting the data from the packet content received from the transmitter
						serverDatagramSocket.send(dataPacketToBeTransmitted);
						System.out.println("DACK Packet is sent to the client.");
						prevAckNumber = dack.acknowledgementNumber;		//Holding the sent acknowledgement number in memory to check the next packet's that will be received are in-order or not
					} else {
						System.err.println("Out-of-order/Wrong packet, hence discarding.");
						dataPacketCounter--;			//Decrement the counter as the packet received in out-of-order
					}
				}
				else
					System.out.println("Invalid packet type, hence discarding.");
				//Converting the received data to characters once all the data from the transmitter is received.
				if(dataPacketCounter == numDataPackets) {
					for( int k = 0; k < data.length(); k = k+8) {
						int b = Integer.valueOf(data.substring(k, k+8), 2);
						totalReceivedData += (char) b;
					}
					
					if (totalReceivedData.length() == numPayloadBytes)
						System.out.println("Data transfer successful and the data received is: ");
					try {
						writeReceivedDataToFile(totalReceivedData);
						System.out.println(totalReceivedData);
					} catch (IOException e) {
						System.err.println("Error writing to the file, printing received data on the console.");
						System.out.println(totalReceivedData);
					}
					numBits = numPayloadBytes * 8;
					transmissionTime = lastPacketRxTime - firstPacketRxTime;
					effectiveDataRate = ( (double) numBits / (double) transmissionTime ) / 1000;
					System.out.println("Effective data rate is " + effectiveDataRate + " MBits/s.");
					dataPacketCounter = 0;
					serverDatagramSocket.disconnect();
					totalReceivedData = "";
					data = "";
				}
			}
		//serverDatagramSocket.disconnect();
		//serverDatagramSocket.close();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*	+---------------------------------------------------------------------------------------------+
	 *  | Method Name: 		writeReceivedDataToFile													|
	 *  | Method Purpose: 	Write the received data to a text file on the file system					|
	 *  | Input: 			Data received from the transmitter in a string 							|
	 *  | Output: 			--																		|
	 *  | Throws: 			IO Exception																|
	 *  +---------------------------------------------------------------------------------------------+
	 */
	
	private static void writeReceivedDataToFile(String totalReceivedData) throws IOException {
		File outputFile = new File("receivedData.txt");
		FileOutputStream outputStream = new FileOutputStream(outputFile);
		outputStream.write(totalReceivedData.getBytes());
		outputStream.flush();
		outputStream.close();
	}
	
	/*	+---------------------------------------------------------------------------------------------+
	 *  | Method Name:		checkPacketType															|
	 *  | Method Purpose:	Check's the type of the packet based on the 1st Byte or 1st 8 Bits		|
	 *  |						- INIT Packet - 55h - 85												|
	 *  |						- DATA Packet - 33h - 51												|
	 *  | Input:				Data received from the transmitter in a string 							|
	 *  | Output:			Integer																	|
	 *  |						- 1, if Packet is INIT Packet										|
	 *  |						- 2, if Packet is DATA Packet										|
	 *  | Throws:			--																		|
	 *  +---------------------------------------------------------------------------------------------+
	 */
	
	private static int checkPacketType(String receivedDataDInString) {
		if (Integer.valueOf(receivedDataDInString.substring(0, 8), 2) == 85)
			return INIT_PACKET_TYPE;
		else if (Integer.valueOf(receivedDataDInString.substring(0, 8), 2) == 51)
			return DATA_PACKET_TYPE;
		else
			return 0;
	}
	
	/*	+---------------------------------------------------------------------------------------------+
	 *  | Method Name: 		checkDataPacket															|
	 *  | Method Purpose:	Check if the received DATA packet is in-order							|
	 *  | Input:				Content of the packet received from the transmitter in a string			|
	 *  | Output:			boolean																	|
	 *  | 						- True: If all of the following conditions are satisfied:				|
	 *  |							1. Integrity check passes										|
	 *  |							2. Correct packet type											|
	 *  |							3. If the sequence number of the received packet matches			| 
	 *  |							   the acknowledgement number sent in the previous DACK Packet	|
	 *  | Throws:			--																		|
	 *  +---------------------------------------------------------------------------------------------+
	 */
	private static boolean checkDataPacket(String receivedDataDInString, String prevAckNumber) {
		int a = Integer.valueOf(receivedDataDInString.substring(8, 24), 2);
		int b = Integer.valueOf(prevAckNumber, 2);
		if (Integer.valueOf(IntegrityCheck.calculateAtReceiver(receivedDataDInString), 2) == Integer.valueOf("0000000000000000") && Integer.valueOf(receivedDataDInString.substring(0, 8), 2) == 51 && a == b)
			return true;	
		else
			return false;
	}
	
	/*	+---------------------------------------------------------------------------------------------+
	 *  | Method Name: 		checkInitPacket															|
	 *  | Method Purpose:	Check if the received INIT packet is in-order							|
	 *  | Input:				Content of the packet received from the transmitter in a string			|
	 *  | Output:			boolean																	|
	 *  | 						- True: If all of the following conditions are satisfied:				|
	 *  |							1. Integrity check passes										|
	 *  |							2. Correct packet type											|
	 *  | Throws:			--																		|
	 *  +---------------------------------------------------------------------------------------------+
	 */
	
	private static boolean checkInitPacket(String receivedDataDInString) {
		if (Integer.valueOf(IntegrityCheck.calculateAtReceiver(receivedDataDInString), 2) == Integer.valueOf("0000000000000000") && Integer.valueOf(receivedDataDInString.substring(0, 8), 2) == 85)
			return true;	
		else
			return false;
	}
	
	/*	+---------------------------------------------------------------------------------------------+
	 *  | Method Name: 		decimalToBinaryString													|
	 *  | Method Purpose: 	Convert a decimal expressed in binary string to 16-Bit Binary String		|
	 *  | Input: 			String - Decimal expressed in binary string								|
	 *  | Output: 			String - Decimal expressed in 16-Bit binary	string						|
	 *  | Throws: 			--																		|
	 *  +---------------------------------------------------------------------------------------------+
	 */
	
	private static String decimalToBinaryString(String number) {
		while(number.length() != 16) {
			number = "0" + number;
		}
		return number;
	}
	
	/*	+---------------------------------------------------------------------------------------------+
	 *  | Method Name: 		decimalTo8BitString														|
	 *  | Method Purpose: 	Convert a decimal expressed in binary string to 8-Bit Binary String		|
	 *  | Input: 			String - Decimal expressed in binary string								|
	 *  | Output: 			String - Decimal expressed in 8-Bit binary	string						|
	 *  | Throws: 			--																		|
	 *  +---------------------------------------------------------------------------------------------+
	 */
	
	private static String decimalTo8BitString(String number) {
		while(number.length() != 8) {
			number = "0" + number;
		}
		return number;
	}
	
	/*	+---------------------------------------------------------------------------------------------+
	 *  | Method Name: 		stringToBytes															|
	 *  | Method Purpose: 	Method to convert a string with bits to bytes							|
	 *  | Input: 			String - Packet Content													|
	 *  | Output: 			Byte Array - Packet Content												|
	 *  | Throws: 			--																		|
	 *  +---------------------------------------------------------------------------------------------+
	 */
	
	private static byte[] stringToBytes(String packetData) {
		byte[] byteArray = new byte[packetData.length()/8];
		for(int i=0; i<packetData.length()-1; i = i+8) {
			int a = Integer.valueOf(packetData.substring(i, i+8), 2);
			byteArray[i/8] = (byte) a;
		}
		return byteArray;
	}
	
	/*	+---------------------------------------------------------------------------------------------+
	 *  | Method Name: 		bytesToString															|
	 *  | Method Purpose: 	Method to convert packet data in bytes to string with bits				|
	 *  | Input: 			Byte Array - Packet Content												|
	 *  | Output: 			String - Packet Content													|
	 *  | Throws: 			--																		|
	 *  +---------------------------------------------------------------------------------------------+
	 */
	
	private static String bytesToString(byte[] packetContent) {
		String data = "";
		for(int j=0; j<packetContent.length; j++) {
			int a = packetContent[j] & 0xFF;
			data = data + decimalTo8BitString(Integer.toBinaryString(a));
		}
		return data;
	}
	
}