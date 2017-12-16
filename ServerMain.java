
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerMain
{
	private static int clientPort;
	private static int clientRequestID;
	private static int clientMeasurementID;
	private static int responseCode;
	private static boolean integrityCheck;
	private static double temperature;
	private static final int SOCKETNUMBER =9999;
		
	public static void main(String[] args) throws Exception
	{
		//Create Datagram Socket with same Port as defined in the client 
		DatagramSocket socket = new DatagramSocket(SOCKETNUMBER);//The same Socket Number which was defined at the Client Side while Packet was generated.
		
		while(true)//Infinite While Loop waiting for the Request Message from Client
		{
			//define received byte
			byte[] receivedByte=new byte[1024];// The size is defined based on Max. MTU
			
			//define Object to Receive Data Packet
			DatagramPacket receivedPacket = new DatagramPacket(receivedByte,1024);
			
			System.out.println("Receiving the request from the client...");
			socket.receive(receivedPacket);
			
			ServerMain.ExtractClientInfo(receivedPacket);//ExtractClientInfo Extracts the IP Address and Port# of Client
						
			String receivedString = new String(receivedByte, 0);
			System.out.println(receivedString);
			receivedByte=null;
			
			ServerMain.ParseClientMessage(receivedString);//ParseClientMessage Dissect the Received Message String and populate the variables Request ID, Measurement ID, Checksum.
			
			String messageToTransmit = MessagetoSend();//MessagetoSend Generates the Response to Client Request
			
			//Convert the Response Message String to Byte Array
			int lengthOfMessage = messageToTransmit.length();
			byte[] messageToTransmitInBytes = new byte[2*lengthOfMessage];
			messageToTransmitInBytes = messageToTransmit.getBytes();
			System.out.println("Message in String is: \n"+messageToTransmit+" and Length is: "+lengthOfMessage);
						
			//Prepare Data Packet to be sent
			InetAddress clientObject = receivedPacket.getAddress();
			DatagramPacket sendPacket = new DatagramPacket(messageToTransmitInBytes, messageToTransmitInBytes.length, clientObject, clientPort);
			System.out.println("Sending the response to the client...");
			socket.send(sendPacket);
			
		}//Infinite While Loop

	}//Main
	
	public static void ExtractClientInfo(DatagramPacket receivedPacket)//Extracts the IP Address and Port# of Client
	{
		//Finding IP and Port of Sender
		InetAddress client = receivedPacket.getAddress();
		clientPort = receivedPacket.getPort();
	}//Method - ExtractClientInfo
	
	public static void ParseClientMessage(String receivedString) throws Exception//The Method Parse "Request Message" with the help of "DecodeClientMessage" Class and populate various Variables. These Variables are being used to generate "Response Message"
	{
		DecodeClientMessage decodeClientRequest = new DecodeClientMessage(receivedString);
		clientRequestID = decodeClientRequest.requestMessageID();//Identify Client Request ID
		clientMeasurementID = decodeClientRequest.measurementID();//Identify the Requested Measurement ID
		integrityCheck = decodeClientRequest.integrityCheck();//Checking Integrity of Received Message by comparing the calculated checksum and transmitted checksum
		DataLookup demo = new DataLookup();
		temperature =  demo.getTemperatureValue(clientMeasurementID);//Get Temperature Value from "Class DataLookup" corresponding to the Measurement ID as defined in the "Request Message"
		responseCode = ResponseCode(receivedString);//Calling Response Code Method to generate a Response Code
	}//Method - ParseClientMessage
	
	public static String MessagetoSend()//Prepare the Response Message String
	{
		//String without Checksum
		String messageToBeSent = ("<response><id>"+clientRequestID+"</id><code>"+responseCode+"</code><measurement>"+clientMeasurementID+"</measurement><value>"+temperature+"</value></response>");
		//Calling IntegrityCheck Class Method to generate Checksum
		String MessageToBeSentChecksum = IntegrityCheck.integrityCheckMethod(messageToBeSent);
		
		//String with Checksum
		messageToBeSent = ("<response><id>"+clientRequestID+"</id><code>"+responseCode+"</code><measurement>"+clientMeasurementID+"</measurement><value>"+temperature+"</value></response>"+MessageToBeSentChecksum);
		return messageToBeSent;
	}//Method - MessagetoSend
	
	public static int ResponseCode(String receivedString) throws Exception //Define Response Codes based on Variable States or Function Return values
	{
		DecodeClientMessage decodeClientRequest = new DecodeClientMessage(receivedString);
		if(clientRequestID==-1 || clientMeasurementID==-1 || (decodeClientRequest.MessageSanity()==false))//if clientMeasurementID or clientRequestID have DEFAULT Values even after Decoding the Request Message, or the Return Value of "MessageSanity Function of decodeClientRequest Class" is FALSE
		{
			return 2;
		}
		else if(integrityCheck==false)//if IntegrityCheck is failed (FALSE)
		{
			return 1;
		}
		
		else if(Double.isNaN(temperature)==true)//if Temperature Value is not found and has DEFAULT Value (NaN)
		{
			System.out.println("Error: non-existent measurement. The measurement with the requested measurement ID does not exist.");
			return 3;
		}
		else
		{
			return 0;
		}
		
	}//Method - ResponseCode
}//Class