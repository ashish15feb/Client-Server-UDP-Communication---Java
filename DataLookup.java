
import java.io.*;
import java.util.Arrays;
public class DataLookup
{
	private double tamperature=Double.NaN;
	//
	private static String fileName = "data.txt" ;
	private String line;
	private Integer[] measurementID = new Integer[1];
	private Double[] tempratureValue = new Double[1];
	private int recordNumber=0, stringIndexOfTAB;
	public DataLookup() throws IOException
	{
		BufferedReader inputStream = new BufferedReader(new FileReader(fileName));
		line = inputStream.readLine();
		while ( line != null )  // continue until end of file
		{
			measurementID = Arrays.copyOf(measurementID, recordNumber+1);
			tempratureValue = Arrays.copyOf(tempratureValue, recordNumber+1);
			stringIndexOfTAB = line.indexOf("\t");
			measurementID[recordNumber] = Integer.valueOf(line.substring(0, stringIndexOfTAB));
			tempratureValue[recordNumber] = Double.valueOf(line.substring(stringIndexOfTAB));
			
			line = inputStream.readLine();
			recordNumber++;
		}
			inputStream.close();
	}
		
	public double getTemperatureValue (int measurementNumber)
	{
		for(int i=0;i<recordNumber;i++)
		{
			if(measurementNumber==measurementID[i])
			{
				tamperature=tempratureValue[i];
				break;
			}
		}
	return	tamperature;
	}//Method - getTemperatureValue
	public int getMeasurementID(int index) 
	{
		return measurementID[index];
	}
	public int numberOfRecords() 
	{
		return measurementID.length;
	}
}//Class