import java.util.Random;

public class ClientRequestMessage//Generates the Request Message to be sent to the Server
{
	private String requestMessage;
	private static int requestMessageID;
	private static int measurementID;
	private static String checksum;
	public ClientRequestMessage() throws Exception
	{
		requestMessageID = requestMessageIDGenerator();//Calling Method to generate Random Request ID
		measurementID = measurementIDGenerator();//Calling Method to generate Random Measurement ID
		requestMessage = ("<request><id>"+requestMessageID+"</id><measurement>"+measurementID+"</measurement></request>");//Message without Checksum
		checksum = getchecksum(requestMessage);//Calling Method to generate Checksum
		requestMessage = ("<request><id>"+requestMessageID+"</id><measurement>"+measurementID+"</measurement></request>"+checksum);//Message with Checksum
	}//Constructor ClientRequestMessage
	
	public int requestMessageIDGenerator()//Randomly generate positive number as Request ID which is "less than 65536". 
	{
		Random rand = new Random();
		int  randomRequestNumber = rand.nextInt(65536);
		return randomRequestNumber;
	}//Method requestMessageIDGenerator
	
	public int measurementIDGenerator() throws Exception //Randomly generate Measurement number which is in the "data.txt" file. 
	{
		Random rand = new Random();
		DataLookup dataFile = new DataLookup();//The object of the Class which is reading the "data.txt" file 
		int numOfRecordsIndDataFile = dataFile.numberOfRecords();//Returns number of records in the "data.txt" file.
		int  randomIndex = rand.nextInt(numOfRecordsIndDataFile);//Returns a Random Number less than Number of Records in the "data.txt" file.
		int randomMeasurementID = dataFile.getMeasurementID(randomIndex);//Returns the Measurement ID corresponding to that "Random Number less than Number of Records in the "data.txt" file"
		return randomMeasurementID;//Generated Measurement ID
	}//Method measurementIDGenerator
	
	public static String getchecksum(String requestMessageWOChecksum)
	{
		checksum = IntegrityCheck.integrityCheckMethod(requestMessageWOChecksum);
		return checksum;
	}//Method getchecksum
	
	public static int getRequestMessageID()
	{
		return requestMessageID;
	}//Method getRequestMessageID
	
	public static int getMeasurementID()
	{
		return measurementID;
	}//Method getMeasurementID
	
	public String toString()
	{
		String print = requestMessage;
		return print;
	}//Method toString
}//Class
