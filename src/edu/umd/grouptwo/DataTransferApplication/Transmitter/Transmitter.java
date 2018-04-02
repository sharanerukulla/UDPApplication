package edu.umd.grouptwo.DataTransferApplication.Transmitter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Random;

import edu.umd.grouptwo.DataTransferApplication.DataPacket;
import edu.umd.grouptwo.DataTransferApplication.InitialPacket;
import edu.umd.grouptwo.DataTransferApplication.IntegrityCheck;

public class Transmitter {
	
	public static int timeoutCounter = 0;
	private static final int NUM_DATA_PACKETS = 10;
	private static final int NUM_PAYLOAD_BYTES = 10 * 300;
	
	public static void main(String[] args) {
	
		try {
			
			byte[] serverIPAddress = new byte[] {127, 0, 0, 1};		//Byte array to store the IP address of the receiver (In this case its the same system)
			//byte[] serverIPAddress = new byte[] {(byte)192, (byte)168, (byte)137, (byte)192};		//Byte array to store the IP address of the receiver (In this case its a different system connected by Wifi)
			//byte[] serverIPAddress = new byte[] {(byte)169, (byte)254, (byte)237, (byte)138};		//Byte array to store the IP address of the receiver (In this case its a different system connected by Ethernet)
			
			InetAddress server = InetAddress.getByAddress(serverIPAddress);		//Deriving INET address of the receiver
			int serverPort = 9999;		//Receiver Port number on which the 
			int timeout = 1000;			//Session Timeout  (in milliseconds)
			
			DatagramSocket clientDatagramSocket = new DatagramSocket();
			
			long startTime;			//Variable to store the time when the corresponding packet transmission starts
			long[] roundTripTime = new long[10];			//Array to store the round trip time's ( in milliseconds) of individual data packets
			Random randomNumber = new Random();
			String dackFromReceiver, prevSequenceNumber = "", dataPayload;
			byte[] initPacketContent, ackPacketContent = new byte[5], dataToBeTransmitted = new byte[305];
			//Data to be transmitted (Payload 1)
			//String dataToBeSent = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaacaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaadaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaeaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaafaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaagaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaahaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaiaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaajaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
			//Data to be transmitted (Payload 2)
			String dataToBeSent = "a$%^&*#()-_+=!;:<.>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaab$%^&*#()-_+=!;:<.>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaac$%^&*#()-_+=!;:<.>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaad$%^&*#()-_+=!;:<.>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaae$%^&*#()-_+=!;:<.>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaf$%^&*#()-_+=!;:<.>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaag$%^&*#()-_+=!;:<.>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaah$%^&*#()-_+=!;:<.>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaai$%^&*#()-_+=!;:<.>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaj$%^&*#()-_+=!;:<.>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
			InitialPacket initPacket = new InitialPacket();
			DataPacket dataPacket = new DataPacket();
			
			initPacket.initSequenceNumber = decimalToBinaryString(Integer.toBinaryString(randomNumber.nextInt(65535)));			//Generating a 2 byte random sequence number and converting it to a 16-bit binary string
			initPacket.numDataPackets = decimalToBinaryString(Integer.toBinaryString(NUM_DATA_PACKETS));			//Converting NUM_DATA_PACKETS to a 16-bit binary string
			initPacket.numPayloadBytes = decimalToBinaryString(Integer.toBinaryString(NUM_PAYLOAD_BYTES));			//Converting NUM_PAYLOAD_BYTES to a 16-bit binary string
			initPacket.integrityCheck = IntegrityCheck.calculateAtTransmitter(initPacket.composePacket());			//Calculating the integrity check value
			initPacketContent = stringToBytes(initPacket.composePacket());
			
			DatagramPacket init = new DatagramPacket(initPacketContent, initPacketContent.length, server, serverPort);		//Creating Datagram packet for INIT Packet
			DatagramPacket iack = new DatagramPacket(ackPacketContent, ackPacketContent.length);			//Creating Datagram packet for IACK Packet
			
			clientDatagramSocket.connect(server, serverPort);
			//Sending INIT Packet and receiving IACK packet
			if(clientDatagramSocket.isConnected()) {
				try {
					iack = sendInitReceiveIack(clientDatagramSocket, init, timeout, iack);
					System.out.println("INIT Packet sent to the server.");
					timeoutCounter = 0;
				} catch (IOException e) {
					System.err.println("Oops! IO Exception Occured!");
				}
				System.out.println("IACK received from the server.");
				ackPacketContent = iack.getData();
				String iackFromReceiver = bytesToString(ackPacketContent);
				//Performing Integrity check on the IACK packet received from the receiver
				if (Integer.valueOf(IntegrityCheck.calculateAtReceiver(iackFromReceiver), 2) == Integer.valueOf("0000000000000000")) {
					//Checking packet Type and sequence number of the IACK packet
					if ( Integer.valueOf(iackFromReceiver.substring(0, 8), 2) == 170 && Integer.valueOf(iackFromReceiver.substring(8, 24), 2) == 1 + Integer.valueOf(initPacket.initSequenceNumber, 2) ) {
						System.out.println("Initial Handshake completed.");			//End of Initial Handshake phase.
					} else {
						System.err.println("Wrong packet.");
						retransmitInitPacket(clientDatagramSocket, init, timeout, iack, initPacket.initSequenceNumber);		//Retransmit the packet if the packet is corrupted or type doesn't match or sequence number is out of order
					}
				} else {
					retransmitInitPacket(clientDatagramSocket, init, timeout, iack, initPacket.initSequenceNumber);			//Retransmit the packet if the integrity check fails
				}
			}
			
			DatagramPacket dack = new DatagramPacket(ackPacketContent, ackPacketContent.length);			//Creating Datagram packet for DACK Packet
			for(int i = 1; i <= NUM_DATA_PACKETS; i++ ) {
				//Calculating the sequence number for the DACK packet
				if (i == 1) {
					dataPacket.sequenceNumber = decimalToBinaryString(Integer.toBinaryString((Integer.valueOf(initPacket.initSequenceNumber, 2) + 1) % 65535));
					prevSequenceNumber = dataPacket.sequenceNumber;
				} else {
					dataPacket.sequenceNumber = decimalToBinaryString(Integer.toBinaryString((Integer.valueOf(prevSequenceNumber, 2) + 300 ) % 65535));
					prevSequenceNumber = dataPacket.sequenceNumber;
				}
				dataPayload = dataToBeSent.substring(300*(i-1), 300*i);			//Extracting 300 Bytes of data for each DATA packet
				dataPacket.payload = stringToBit(dataPayload);
				dataPacket.integrityCheck = IntegrityCheck.calculateAtTransmitter(dataPacket.composePacket());
				dataToBeTransmitted = stringToBytes(dataPacket.composePacket());
				DatagramPacket dataPacketWithPayload = new DatagramPacket(dataToBeTransmitted, dataToBeTransmitted.length, server, serverPort); //creating UDP packet for DATA
				//Transmit DATA packet and receive DACK packet
				if(clientDatagramSocket.isConnected()) {
					try {
						startTime = System.currentTimeMillis();
						dack = sendDataReceiveDack(clientDatagramSocket, dataPacketWithPayload, timeout, dack, startTime, roundTripTime, i);
						System.out.println("Data Packet [ " + i + " ] sent to the server.");
						timeoutCounter = 0;
					} catch (IOException e) {
						System.err.println("Oops! IO Exception Occupred!");
					}
					System.out.println("DACK for Data Packet [ " + i + " ] received from the server.");
					ackPacketContent= dack.getData();
					dackFromReceiver = bytesToString(ackPacketContent);
					if (Integer.valueOf(IntegrityCheck.calculateAtReceiver(dackFromReceiver), 2) == Integer.valueOf("0000000000000000")) {
						if (Integer.valueOf(dackFromReceiver.substring(0, 8), 2) == 204 && Integer.valueOf(dackFromReceiver.substring(8, 24), 2) == Integer.valueOf(prevSequenceNumber, 2) + 300) {
							System.out.println("Data packet [ " + i + " ] sent and correct acknowledgement received.");
						} else {
							System.err.println("Wrong packet received, will be retransmitting Data Packet [ " + i + " ].");
							startTime = System.currentTimeMillis();
							retransmitDataPacket(clientDatagramSocket, dataPacketWithPayload, timeout, dack, startTime, roundTripTime, i, prevSequenceNumber);			//Retransmit the packet if the packet is corrupted or type doesn't match or sequence number is out of order
						}	
					} else {
						startTime = System.currentTimeMillis();
						retransmitDataPacket(clientDatagramSocket, dataPacketWithPayload, timeout, dack, startTime, roundTripTime, i, prevSequenceNumber);			//Retransmit the packet if the integrity check fails
					}
				}
				
			}
			long min = roundTripTime[0], max = 0, sum = 0;
			for (int k = 0 ; k < roundTripTime.length; k++) {
				System.out.println("Round Trip Time for Data Packet No. - " + (k+1) + " is " + roundTripTime[k] + " ms.");
				//Finding minimum Round Trip Time among all the packets sent
				if (roundTripTime[k] < min)
					min = roundTripTime[k];
				//Finding maximum Round Trip Time among all the packets sent
				if (roundTripTime[k] > max)
					max = roundTripTime[k];
				sum += roundTripTime[k];
			}
			long avg = sum/10;			//Calculating Average Round Trip Time
			System.out.println("Minimum Round Trip Time = " + min + " ms, Maximum Round Trip Time = " + max + " ms, Average Round Trip Time = " + avg + " ms.");
			

			clientDatagramSocket.disconnect();		//Disconnect and close the socket once the intended 300*10 Bytes of data has been transmitted.
			clientDatagramSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	/*	+-------------------------------------------------------------------------------------------------+
	 *  | Method Name: 		sendDataReceiveDack															|
	 *  | Method Purpose: 	Transmit DATA packet, receive DACK packet and calculate 						|
	 *  |					Round Trip Time																|
	 *  | Input: 			1. Datagram Socket: Client Datagram socket for transmitting/receiving	packets	|
	 *  |					2. Datagram Packet: DATA packet to be transmitted								|
	 *  |					3. Integer: Timeout value (in milli seconds)									|
	 *  |					4. Datagram Packet: DACK packet to be received								|
	 *  |					5. Long: Store's the time when transmission starts							|
	 *  |					6. Long Array: Store's the round trip time for each data packet				|
	 *  |					7. Integer: Store's the data packet number									|
	 *  | Output: 			DatagramPacket - DACK Packet													|
	 *  | Throws: 			IOException																	|
	 *  +-------------------------------------------------------------------------------------------------+
	 */
	
	private static DatagramPacket sendDataReceiveDack(DatagramSocket clientDatagramSocket, DatagramPacket dataPacket, int timeout, DatagramPacket dack, long startTime, long[] RTT, int i) throws IOException {
		clientDatagramSocket.send(dataPacket);
		clientDatagramSocket.setSoTimeout(timeout);
		try {
			clientDatagramSocket.receive(dack);
			long endTime = System.currentTimeMillis();
			RTT[i-1] = endTime - startTime;
		} catch (SocketTimeoutException e) {
			timeoutCounter++;		//If timeout happens increment the counter
			if (timeoutCounter < 4) {
				System.out.println("Timeout. Will be retransmitting the packet.");
				startTime = System.currentTimeMillis();
				dack = sendDataReceiveDack(clientDatagramSocket, dataPacket, (2*timeout), dack, startTime, RTT, i);
			} else
			{
				System.err.println("Communication failure. (4th Timeout)");		//Re-send the packet, this time with double the previous timeout interval
				System.exit(0);
			}
		}
		return dack;
	}
	
	/*	+---------------------------------------------------------------------------------------------+
	 *  | Method Name: 		retransmitDataPacket														|
	 *  | Method Purpose: 	Retransmit DATA packet and receive DACK packet							|
	 *  | Input: 			1. Datagram Socket: Client Datagram socket for transmitting/receiving		|
	 *  |					2. Datagram Packet: DATA packet to be transmitted							|
	 *  |					3. Integer: Store's timeout value (in milliseconds)						|
	 *  |					4. Datagram Packet: DACK packet to be received							|
	 *  |					5. Long: Store the time when transmission starts							|
	 *  |					6. Long Array: Store's the round trip time for each data packet			|
	 *  |					7. Integer: Store's the data packet number								|
	 *  |					8. String:  Previous sequence number										|
	 *  | Output: 			--																		|
	 *  | Throws: 			--																		|
	 *  +---------------------------------------------------------------------------------------------+
	 */
	
	private static void retransmitDataPacket(DatagramSocket clientDatagramSocket, DatagramPacket dataPacket, int timeout,
			DatagramPacket dack, long startTime, long[] RTT, int i, String prevSequenceNumber) {
		try {
			dack = sendDataReceiveDack(clientDatagramSocket, dataPacket, timeout, dack, startTime, RTT, i);		//Start the sending process again
			timeoutCounter = 0;		//Reset the counter to 0
		} catch (IOException e) {
			System.err.println("Oops! IO Exception Occupred!");
		}
		byte[] ackPacketContent = dack.getData();
		String dackFromReceiver = bytesToString(ackPacketContent);
		if (Integer.valueOf(IntegrityCheck.calculateAtReceiver(dackFromReceiver), 2) == Integer.valueOf("0000000000000000")) {
			if (Integer.valueOf(dackFromReceiver.substring(0, 8), 2) == 204 && Integer.valueOf(dackFromReceiver.substring(8, 24), 2) == Integer.valueOf(prevSequenceNumber, 2) + 300) {
				System.out.println("Data packet [ " + i + " ] sent and correct acknowledgement received.");
			} else {
				System.err.println("Wrong packet received, will be retransmitting Data Packet [ " + i + " ].");
				startTime = System.currentTimeMillis();
				retransmitDataPacket(clientDatagramSocket, dataPacket, timeout, dack, startTime, RTT, i, prevSequenceNumber);			//Retransmit the packet if the packet is corrupted or type doesn't match or sequence number is out of order
			}	
		} else {
			startTime = System.currentTimeMillis();
			retransmitDataPacket(clientDatagramSocket, dataPacket, timeout, dack, startTime, RTT, i, prevSequenceNumber);			//Retransmit the packet if the integrity check fails
		}
	}
	
	/*	+---------------------------------------------------------------------------------+
	 *  | Method Name: 		sendInitReceiveIack											|
	 *  | Method Purpose: 	Sending INIT to and receiving IACK from the receiver			|
	 *  | Input: 			1. Datagram Socket: Client socket for transmitting/receiving	|
	 *  |					2. Datagram Packet: INIT packet to be transmitted				|
	 *  |					3. Integer: Store's timeout value (in milliseconds)			|
	 *  |					4. Datagram Packet: IACK packet to be received				|
	 *  | Output: 			DatagramPacket - IACK Packet									|
	 *  | Throws: 			IOException													|
	 *  +---------------------------------------------------------------------------------+
	 */
	
	private static DatagramPacket sendInitReceiveIack(DatagramSocket clientDatagramSocket, DatagramPacket init, int timeout, DatagramPacket iack) throws IOException {
		clientDatagramSocket.send(init);
		clientDatagramSocket.setSoTimeout(timeout);
		try {
			clientDatagramSocket.receive(iack);
		} catch (SocketTimeoutException e) {
			timeoutCounter++;		//If timeout happens increment the counter
			if (timeoutCounter < 4) {
				System.out.println("Timeout. Will be retransmitting the packet.");
				iack = sendInitReceiveIack(clientDatagramSocket, init, (2*timeout), iack);		//Re-send the packet, this time with double the previous timeout interval
			} else
			{
				System.err.println("Communication failure. (4th Timeout)");
				System.exit(0);
			}
		}
		return iack;
	}
	
	/*	+---------------------------------------------------------------------------------+
	 *  | Method Name: 		retransmitInitPacket											|
	 *  | Method Purpose: 	Retransmit INIT packet and receive IACK packet				|
	 *  | Input: 			1. Datagram Socket: client socket for transmitting/receiving	|
	 *  |					2. Datagram Packet: INIT packet to be transmitted				|
	 *  |					3. Integer: for storing timeout value						|
	 *  |					4. Datagram Packet: IACK packet to be received				|
	 *  |					5. String:  Previous sequence number							|		
	 *  | Output: 			IACK Packet													|
	 *  | Throws: 			--															|
	 *  +---------------------------------------------------------------------------------+
	 */
	
	private static void retransmitInitPacket(DatagramSocket clientDatagramSocket, DatagramPacket init, int timeout,
			DatagramPacket iack, String sequenceNumber) {
		try {
			iack = sendInitReceiveIack(clientDatagramSocket, init, timeout, iack);			//Re-send the packet, this time with double the previous timeout interval
			timeoutCounter = 0;		//Reset the counter to 0
		} catch (IOException e) {
			System.err.println("Oops! IO Exception Occured!");
		}
		byte[] ackPacketContent = iack.getData();
		String iackFromReceiver = bytesToString(ackPacketContent);
		//Performing integrity check on the IACK packet received from the receiver
		if (Integer.valueOf(IntegrityCheck.calculateAtReceiver(iackFromReceiver), 2) == Integer.valueOf("0000000000000000")) {
			//Checking packet type and sequence number of the IACK packet
			if ( Integer.valueOf(iackFromReceiver.substring(0, 8), 2) == 170 && Integer.valueOf(iackFromReceiver.substring(8, 24), 2) == 1 + Integer.valueOf(sequenceNumber, 2) ) {
				System.out.println("Initial Handshake completed.");			//End of Initial Handshake phase.
			} else {
				System.err.println("Wrong packet.");
				retransmitInitPacket(clientDatagramSocket, init, timeout, iack, sequenceNumber);		//Retransmit the packet if the packet is corrupted or type doesn't match or sequence number is out of order
			}
		} else {
			retransmitInitPacket(clientDatagramSocket, init, timeout, iack, sequenceNumber);			//Retransmit the packet if the integrity check fails
		}
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
		int a;
		for( int i = 0; i < packetData.length()-1; i = i+8) {
			a = Integer.valueOf(packetData.substring(i, i+8), 2);
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
	
	private static String bytesToString (byte[] byteArray) {
		String data = "";
		int a;
		for( int j = 0; j < byteArray.length; j++) {
			a = byteArray[j] & 0xFF;
			data = data + decimalTo8BitString(Integer.toBinaryString(a));
		}
		return data;
	}

	/*	+---------------------------------------------------------------------------------------------+
	 *  | Method Name: 		stringToBit																|
	 *  | Method Purpose: 	Convert a string to a string with bits representing characters			|
	 *  | Input: 			String - Data															|
	 *  | Output: 			String - Data in bit format												|
	 *  | Throws: 			--																		|
	 *  +---------------------------------------------------------------------------------------------+
	 */
	
	private static String stringToBit(String dataPayload) {
		String dataInBits = "";
		byte[] dataPayloadInBytes = dataPayload.getBytes();
		for (byte b : dataPayloadInBytes) {
			dataInBits += decimalTo8BitString(Integer.toBinaryString(b));
		}	
		return dataInBits;
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
		while(number.length() != 16 ) {
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
		while(number.length() != 8 ) {
			number = "0" + number;
		}
		return number;
	}

}
