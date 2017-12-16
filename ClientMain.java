import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class ClientMain
{
	static int timeOutValue=1000;
	static int numOfTimesTried = 1;
	static int serverRequestID;
	static int serverMeasurementID;
	static boolean serverIntegrityCheck;
	static double temperature;
	static int responseCode;
	public static void main(String[] args) throws Exception
	{
		while(true)//This Loop is called Super Loop and is being used to continuously sending Requests till the Program is Manually Terminated by the User.
		{
			ClientRequestMessage testObject = new ClientRequestMessage();// Creating object of Class "ClientRequestMessage", the class generate Client Request Message
			String meassageToSent = testObject.toString();
			int lengthOfMessage = meassageToSent.length();
			byte[] messageInBytes=new byte[2*lengthOfMessage]; //Define Byte Array of sufficient length to accommodate String Message.
			messageInBytes = meassageToSent.getBytes();//Convert String Message to Byte Array.
			System.out.println(new String(messageInBytes, 0));
	
			//Define Server IP Address and creating InetAddress object
			InetAddress localHost = InetAddress.getLocalHost();//Get the Local Machine's IP Address
			String serverIpAddress = localHost.getHostAddress();
			InetAddress hostName = InetAddress.getByName(serverIpAddress);//Create Object of "InetAddress" class using IP Address of Local Machine, in actual Client Server scenario the Ip Address of Server should be defined.
			
			//defining server port number
			int serverPort = 9999;
			
			//Creating DatagramPacket Object to send packet to Server using Request Message, InetAddress Object and Port Number
			DatagramPacket packetToServer = new DatagramPacket(messageInBytes, messageInBytes.length, hostName, serverPort);
			
			//Creating DatagramSocket Object
			DatagramSocket socketToServer = new DatagramSocket();
						
			//creating Byte Receiver Array
			byte[] messageFromServerInBytes=new byte[1024];
			
			//creating Packet Object to receive Datagram Packet with default length of 1024 bytes
			DatagramPacket packetFromServer = new DatagramPacket(messageFromServerInBytes, 1024);
			System.out.println("Sending the request to the server...");
			
			//First loop to print Response Code and Retry in case of an Error
			ResponseCodeLoop:
			while(true)
			{
				timeOutValue = 1000;
				numOfTimesTried=1;
				
				//Second Loop to Retry in case there is no response from the Server and a Timeout occurs
				RequestRetryLoop:
				while(numOfTimesTried<=4)
				{
					//Sending the packet to server
					socketToServer.send(packetToServer);
					//System.out.println(packetToServer);
					socketToServer.setSoTimeout(timeOutValue);
					System.out.println("Receiving the response from the server...");
					
					//Socket waiting to receive packet sent by the server
					try
					{
					socketToServer.receive(packetFromServer);
					break RequestRetryLoop;//If there is a response from Server then the Third "TimeOut" Loop is no longer required
					}
					catch(SocketTimeoutException timeout)//If No response from server, Timer Value to be doubled after every TimeOut
					{
						timeOutValue=2*timeOutValue;
						numOfTimesTried++;
						if(numOfTimesTried>4)//Re-try up to Maximum 4 Times, after that Communication Failure and abandon to Send the Request, that is Break the First Loop.
						{
							System.out.println("Communication Failure!");
							
							break ResponseCodeLoop;
						}
						else
						{
							System.out.println("Client socket Receive timed out! RETRY "+numOfTimesTried+" Doubling the Timer Value to: "+timeOutValue/1000+" seconds");
						}
					}
				}//Request Retry Loop
					
					//Decoding the Bytes sent by Server
					messageFromServerInBytes = packetFromServer.getData();
					String messageFromServer = new String(messageFromServerInBytes, 0);
					
					messageFromServerInBytes=null;//Reset the Byte Array, so that Next Byte Array don't get garbage carryover
					ParseServerMessage(messageFromServer);//Decode the Message received from Server, populate variables Response Code, Measurement ID, Temperature Value and Checksum.
					if(serverIntegrityCheck==true)// Check whether the Calculated Checksum and Checksum sent by the Server are a match.
					{
						
					}
					else
					{
						System.out.println("Integrity Check Failed, RETRY.");
						break ResponseCodeLoop;
					}
								
				if(responseCode==0)// Response Code Zero means the Response is valid.
				{
					System.out.println("The Temperature corresponds to the Measurement ID "+serverMeasurementID +" is "+temperature);
					break ResponseCodeLoop;
				}
				else//If Response code is Non-Zero, some error has occurred during Transmission or Reception. Need to Retry using First Loop.
				{
					if(responseCode==1)
					{
						System.out.println("Error: integrity check failure. The request has one or more bit errors.");
					}
					else if(responseCode==2)
					{
						System.out.println("Error: malformed request. The syntax of the request message is not correct.");
					}
					else if(responseCode==3)
					{
						System.out.println("Error: non-existent measurement. The measurement with the requested measurement ID does not exist.");
					}
					System.out.println("Do you want to RETRY sending the message (Y/N): ");// Check if User wants to send the Request again.
					Scanner choice = new Scanner(System.in);
					String yesNo = choice.next();
					if((yesNo.toUpperCase()).equals("N"))//If user does not want to Re-try, terminate First Loop.
					{
						break ResponseCodeLoop;
					}
					System.out.println("You have requested RETRY");//If user wants to Re-try, continue First Loop.
				}
			}//ResponseCodeLoop
		}//Super Loop to continuously send the Request till Program is Manually Terminated by User.
	}//Main
	
	
	
	public static void ParseServerMessage(String receivedString)// The Method Parse the Message Received by Client. It populates the variables Response Code, Measurement ID, Temperature and Checksum. The variables are being used by Main Method.
	{
		DecodeServerResponse decodeResponse = new DecodeServerResponse(receivedString);
		serverRequestID = decodeResponse.requestMessageID();
		serverMeasurementID = decodeResponse.measurementID();
		serverIntegrityCheck = decodeResponse.integrityCheck();
		temperature =  decodeResponse.temperatureValue();
		responseCode = decodeResponse.responseCode();
	}
}//Class
