public class DecodeClientMessage
{
	private String receivedMessageWOChecksum, receivedMessageWOWhiteSpaces;
	private int requestMessageID=-1;
	private int measurementID=-1;
	private int checksuminMessage=-1, calculatedChecksum;
	private int start, stop;
	
	public DecodeClientMessage(String receivedMessage) throws Exception
	{
		receivedMessageWOWhiteSpaces = (receivedMessage.replaceAll("\\s+","").trim());//Remove white spaces from the Message
		
		
		//Find Request ID
		start = receivedMessageWOWhiteSpaces.indexOf("<request><id>");
		stop = receivedMessageWOWhiteSpaces.indexOf("</id>");
		try
		{
			requestMessageID = Integer.valueOf(receivedMessageWOWhiteSpaces.substring(start+13, stop));
		}
		catch (NumberFormatException cantFindNumber)
		{
			//System.out.println("Can not find Request ID.");
		}
		
		//Find Measurement ID
		start = receivedMessageWOWhiteSpaces.indexOf("<measurement>");
		stop = receivedMessageWOWhiteSpaces.indexOf("</measurement>");
		measurementID = Integer.valueOf(receivedMessageWOWhiteSpaces.substring(start+13, stop));
	
		//Find Checksum from the Request Message
		start = receivedMessageWOWhiteSpaces.indexOf("</request>");
		stop = receivedMessageWOWhiteSpaces.length();
		try
		{
			checksuminMessage = Integer.valueOf(receivedMessageWOWhiteSpaces.substring(start+10, stop));
		}
		catch (NumberFormatException NumberExpected)
		{
			//System.out.println("Number Expected for Checksum Value");
		}
	
		//Create a new String from Request Message without Checksum, to calculate the Checksum and compare the Calculated Checksum with the Checksum transmitted by Client
		start = receivedMessageWOWhiteSpaces.indexOf("<request>");
		stop = receivedMessageWOWhiteSpaces.indexOf("</request>");
		try
		{
			receivedMessageWOChecksum = receivedMessageWOWhiteSpaces.substring(start, stop+10);
		}
		catch (StringIndexOutOfBoundsException cantDefine)
		{
			//When the Format of "Request Message" is not as per Definition the message can not be decoded and this Exception will occur because of Default value of Either Request ID, Measurement ID or Checksum
			//System.out.println("Error in Message Format");
		}
		try
		{
			calculatedChecksum = Integer.valueOf(IntegrityCheck.integrityCheckMethod(receivedMessageWOChecksum));
		}
		catch (NullPointerException cantDefine)
		{
			//Can Not Calculate Checksum When Message Format is Not Correct
			//System.out.println("Checksum can not be calculate because of 'Error in Message Format'");
		}
	}//Constructor DecodeClientMessage

	public int measurementID()//Return Measurement ID
	{
		return measurementID;
	}//Method - measurementID
	
	public int requestMessageID()//Return Request ID
	{
		return requestMessageID;
	}//Method - requestMessageID
	
	public boolean integrityCheck()//Compare the Checksum Sent by Client in the Message with the Calculated Checksum, "Response Code ONE" generation depends upon the Output of this Method
	{
		if(calculatedChecksum==checksuminMessage)
		{
			return true;
		}
		else
		{
			return false;
		}
	}//Method - integrityCheck
	
	public boolean MessageSanity()//"Response Code TWO" generation depends upon the Output of this Method
	{
		String sanityCheckMessage = ("<request><id>"+requestMessageID+"</id><measurement>"+measurementID+"</measurement></request>");
		if(receivedMessageWOChecksum.equals(sanityCheckMessage))//The method checks whether the Message is formated as per predefined structure and keywords.
		{
			return true;
		}
		else
		{
			return false;
		}
			
	}//Method - MessageSanity
}//Class
