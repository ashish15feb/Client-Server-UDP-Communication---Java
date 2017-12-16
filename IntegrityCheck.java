

import java.util.Arrays;

public class IntegrityCheck
{
	public static String integrityCheckMethod (String input)
	{
		byte[] local = input.getBytes();
		int l=local.length;
		
		if(l%2 != 0)
		{
			l++;
			local = Arrays.copyOf(local, l);
			local[l-1] = 48;
		}
		
		short shrt[] = new short[l/2];
		for (int i=0; i<l/2;i++)
		{
			shrt[i]= (short) ((local[2*i]<<8) | local[2*i+1]);
		}
		
		int S = 0, C=7919, index=0;
		int D = 65536;
		
		for (int i=0;i<shrt.length;i++)
		{
			index = (S ^ shrt[i]);
			S = ((C*index)%D);
		}
		String checksum = String.valueOf(S);
		return checksum;
	}
}
