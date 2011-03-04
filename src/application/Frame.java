package application;

public class Frame {
	
	private String flag = "01111110";
	private String frame;
	
	public Frame(String binaryString)
	{
		this.frame = binaryString;
	}
	
	public Frame(String addressBinary, String controlBinary, String informationBinary) throws Exception
	{
		this.frame = addressBinary + controlBinary + informationBinary;
		this.frame = flag + frame + generateFCS(frame) + flag;
	}
	
	
	private String generateFCS(String frame)
	{
		 int crc = 0xFFFF;          // initial value
	        int polynomial = 0x1021;   // 0001 0000 0010 0001  (0, 5, 12) 

	        // byte[] testBytes = "123456789".getBytes("ASCII");
	        byte[] bytes = new byte[frame.length()]; 
	        for(int i =0; i <frame.length();i++)
	        {
	        	bytes[i] = (byte)frame.charAt(i);
	        }
	       

	        for (byte b : bytes) {
	            for (int i = 0; i < 8; i++) {
	                boolean bit = ((b   >> (7-i) & 1) == 1);
	                boolean c15 = ((crc >> 15    & 1) == 1);
	                crc <<= 1;
	                if (c15 ^ bit) crc ^= polynomial;
	             }
	        }

	        crc &= 0xffff;
	        return Integer.toBinaryString(crc);
	}

	public String toString(){
		return frame;
	}
	
	public String getNS()
	{
		return "";
	}
	
	public String getNR()
	{
		return "";
	}
	
	public String getDecodedInfo()
	{
		return "";
	}
}
