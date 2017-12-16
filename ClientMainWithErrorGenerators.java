import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class ClientMainWithErrorGenerators//The Class is same as "ClientMessageTransmission" EXCEPT it covers the Error test scenarios
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
		MasterLoop:
		while(true)
		{
			ClientRequestMessage testObject = new ClientRequestMessage();
			String messageToSend = testObject.toString();
			int lengthOfMessage = messageToSend.length();
			
			int typeOfErrorYouWantToGenerate=0;
			while(true)
			{
				Scanner testError = new Scanner(System.in);
				System.out.println("Please Enter 0 to exit, 1/2/3 to generate the Error, OR anyother number for Normal Operation: ");
				try
				{
					typeOfErrorYouWantToGenerate = testError.nextInt();
					break;
				}
				catch (java.util.InputMismatchException numberExpected)
				{
					System.out.println("Please Enter a Number");
				}
			}
			switch (typeOfErrorYouWantToGenerate) 
			{
				case 0: break MasterLoop;
				case 1: messageToSend=modifyCharForIntegrityFail(messageToSend);//The Method flips one bit of the Request ID to make the Checksum Invalid
					    break;
	            case 2:  messageToSend=messageToSend.replaceAll("request", "Error12");
	                     break;
	            case 3:	 messageToSend="<request><id>51086</id><measurement>2255</measurement></request>65240";
	                     break;
	           
	            default: break;
			}
			lengthOfMessage = messageToSend.length();
			byte[] messageInBytes=new byte[2*lengthOfMessage];
			messageInBytes = messageToSend.getBytes();
			System.out.println("Message is "+messageToSend+" and Length is "+lengthOfMessage);
				
			//Define Server IP Address and creating InetAddress object
			InetAddress localHost = InetAddress.getLocalHost();
			String serverIpAddress = localHost.getHostAddress();
			
			InetAddress hostName = InetAddress.getByName(serverIpAddress);
			
			//defining server port number
			int serverPort = 9999;
			
			
			//Creating DatagramPacket Object
			DatagramPacket packetToServer = new DatagramPacket(messageInBytes, messageInBytes.length, hostName, serverPort);
			
			//Creating DatagramSocket Object
			DatagramSocket socketToServer = new DatagramSocket();
			
			
			//creating Byte Receiver Array
			byte[] messageFromServerInBytes=new byte[1024];
			
			//creating Packet Object to receive Datagram Packet = length of Random Variable
			DatagramPacket packetFromServer = new DatagramPacket(messageFromServerInBytes, 1024);
			System.out.println("Sending the request to the server...");
			//setting timeout timer
			
			ResponseCodeLoop:
			while(true)
			{
				timeOutValue = 1000;
				numOfTimesTried=1;
			
				RequestRetryLoop:
				while(numOfTimesTried<=4)
				{
					//Sending the packet to server
					socketToServer.send(packetToServer);
					
					socketToServer.setSoTimeout(timeOutValue);
					System.out.println("Receiving the response from the server...");
					//Socket receiving packet sent by the server
					try
					{
					socketToServer.receive(packetFromServer);
					break RequestRetryLoop;
					}
					catch(SocketTimeoutException timeout)
					{
						timeOutValue=2*timeOutValue;
						numOfTimesTried++;
						if(numOfTimesTried>4)
						{
							System.out.println("Communication Failure");
							
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
				
				messageFromServerInBytes=null;
				ParseServerMessage(messageFromServer);
				if(serverIntegrityCheck==true)
				{
					
				}
				else
				{
					System.out.println("Integrity Check Failed, RETRY.");
					break ResponseCodeLoop;
				}
							
				if(responseCode==0)
				{
					System.out.println("The Temperature corresponds to the Measurement ID "+serverMeasurementID +" is "+temperature);
					break ResponseCodeLoop;
				}
				else
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
						break ResponseCodeLoop;
					}
					System.out.println("Do you want to RETRY sending the message (Y/N): ");
					Scanner choice = new Scanner(System.in);
					String yesNo = choice.next();
					if((yesNo.toUpperCase()).equals("N"))
					{
						break ResponseCodeLoop;
					}
					System.out.println("You have requested RETRY");
				}
			}//ResponseCodeLoop
		}//Super While
	}//Main
		
	public static void ParseServerMessage(String receivedString)
	{
		DecodeServerResponse decodeResponse = new DecodeServerResponse(receivedString);
		serverRequestID = decodeResponse.requestMessageID();
		serverMeasurementID = decodeResponse.measurementID();
		serverIntegrityCheck = decodeResponse.integrityCheck();
		temperature =  decodeResponse.temperatureValue();
		responseCode = decodeResponse.responseCode();
	}
	
	public static String modifyCharForIntegrityFail(String str)//This method flips one bit of the Request ID to make the Checksum Invalid
	{
	    char[] chars = str.toCharArray();//Dissect the Message into Character Array
	    final int INDEX = 13;//Take the first bit of Request ID
	    int temp = Character.getNumericValue(chars[INDEX]);//Converting Character to Integer
	    if(temp < 9)//if the first bit of Request ID is  less than NINE then increase it by ONE
	    {
	    	temp++;
	    }
	    else
	    {
	    	temp=1;//if the first bit of Request ID is  equal to NINE then make it  ONE
	    }
	    chars[INDEX]=(char) (temp+'0');//Converting Integer to Character
	    return String.valueOf(chars);       
	}
}//Class
