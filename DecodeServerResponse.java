

public class DecodeServerResponse //The Class parses Response Message received from Server and Populates the Variables, and validates the Checksum transmitted by Server with the Calculated Checksum 
{
	private String receivedMessageWOChecksum, receivedMessageWOWhiteSpaces;
	private int requestMessageID=-1;
	private int measurementID=-1;
	private int checksuminMessage=-1, calculatedChecksum=-1;
	private int start, stop;
	private int responseCode;
	private double temperatureValue;
	
	public DecodeServerResponse(String receivedMessage)
	{
		receivedMessageWOWhiteSpaces = (receivedMessage.replaceAll("\\s+","").trim());//Remove all white spaces from the message string
		
		//Determine Request ID
		start = receivedMessageWOWhiteSpaces.indexOf("<response><id>");
		stop = receivedMessageWOWhiteSpaces.indexOf("</id>");
		try
		{
			requestMessageID = Integer.valueOf(receivedMessageWOWhiteSpaces.substring(start+14, stop));
		}
		catch( java.lang.NumberFormatException cantFindMsgID)
		{
			
		}
		//Determine Response Code
		start = receivedMessageWOWhiteSpaces.indexOf("<code>");
		stop = receivedMessageWOWhiteSpaces.indexOf("</code>");
		responseCode = Integer.valueOf(receivedMessageWOWhiteSpaces.substring(start+6, stop));
		
		//Determine Measurement ID
		start = receivedMessageWOWhiteSpaces.indexOf("<measurement>");
		stop = receivedMessageWOWhiteSpaces.indexOf("</measurement>");
		measurementID = Integer.valueOf(receivedMessageWOWhiteSpaces.substring(start+13, stop));
		
		//Determine Temperature Value
		start = receivedMessageWOWhiteSpaces.indexOf("<value>");
		stop = receivedMessageWOWhiteSpaces.indexOf("</value>");
		temperatureValue = Double.parseDouble(receivedMessageWOWhiteSpaces.substring(start+7, stop));
		
		//Determine Checksum from the Message String
		start = receivedMessageWOWhiteSpaces.indexOf("</response>");
		stop = receivedMessageWOWhiteSpaces.length();
		checksuminMessage = Integer.valueOf(receivedMessageWOWhiteSpaces.substring(start+11, stop));
		
		//Retrieve Message String without Checksum
		start = receivedMessageWOWhiteSpaces.indexOf("<response>");
		stop = receivedMessageWOWhiteSpaces.indexOf("</response>");
		try
		{
			receivedMessageWOChecksum = receivedMessageWOWhiteSpaces.substring(start, stop+11);
			//Calculate Checksum on "retrieved Message String without Checksum"
			calculatedChecksum = Integer.valueOf(IntegrityCheck.integrityCheckMethod(receivedMessageWOChecksum));
		}
		catch(java.lang.StringIndexOutOfBoundsException cantFindStartOrEndOfString)
		{
			
		}
		
		
		if(requestMessageID==-1 || measurementID==-1 || checksuminMessage==-1)
		{
			responseCode = 2;
		}
		
	}//Constructor DecodeServerResponse

	public int measurementID()
	{
		return measurementID;
	}//Method - measurementID
	
	public int requestMessageID()
	{
		return requestMessageID;
	}//Method - requestMessageID
	
	public int responseCode()
	{
		return responseCode;
	}//Method - responseCode
	
	public double temperatureValue()
	{
		return temperatureValue;
	}//Method - temperatureValue
	
	public boolean integrityCheck()//Method compares the Checksum transmitted by Server with the Checksum Calculated and returns the result
	{
		if(calculatedChecksum!=-1)
		{
			if(calculatedChecksum==checksuminMessage)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		return true;
	}//Method - integrityCheck

}//Class
