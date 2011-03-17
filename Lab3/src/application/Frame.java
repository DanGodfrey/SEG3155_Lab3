package application;

public class Frame {
	
	private static String flag = "01111110";
	private String frame;
	
	public Frame(String binaryString)
	{
		this.frame = binaryString;
	}
	
	public Frame(String addressBinary, String controlBinary, String informationBinary)
	{
		this.frame = addressBinary + controlBinary + informationBinary;
		this.frame = flag + frame + generateFCS(frame) + flag;
	}
	
	public static String getFlag()
	{
		return flag;
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

	
	public int getPorFbitFromFrame()
	{
		return Integer.parseInt(frame.substring(20,21));
	}
	
	public  String getSourceAddressBinaryFromFrame()
	{
		return this.frame.substring(16,24);
	}
	public  String getDestinationAddressBinaryFromFrame()
	{
		return this.frame.substring(8,16);
	}
	
	public  String getCommandNameFromFrame()
	{
		String stringBinaryFrame = this.frame;
		String commandName = "";
		char frameType = this.getFrameType();
		if(frameType == 'U') //Unnumbered Frame
		{
			if((stringBinaryFrame.substring(18, 20)).contains("00") && (stringBinaryFrame.substring(21, 24)).contains("110"))
				commandName = "UA"; //Unnumbered acknowledgment
			else if((stringBinaryFrame.substring(18, 20)).contains("00") && (stringBinaryFrame.substring(21, 24)).contains("100"))
				commandName = "SIM"; //Set initialization mode
			else if((stringBinaryFrame.substring(18, 20)).contains("00") && (stringBinaryFrame.substring(21, 24)).contains("001"))
				commandName = "SNRM"; //Set normal response 
			else if((stringBinaryFrame.substring(18, 20)).contains("01") && (stringBinaryFrame.substring(21, 24)).contains("001"))
				commandName = "RIM"; //Request initialization mode
		} 
		else if (frameType == 'I') //Information Frame
		{
			
		}
		else if (frameType == 'S') //Supervisory Frame
		{
			if((stringBinaryFrame.substring(18, 20)).contains("00")) //RR
				commandName = "RR";
			else if((stringBinaryFrame.substring(18, 20)).contains("01")) //RNR
				commandName = "RNR";
			else if((stringBinaryFrame.substring(18, 20)).contains("10")) //REJ
				commandName = "REJ";
			else if((stringBinaryFrame.substring(18, 20)).contains("11")) //SREJ
				commandName = "SREJ";
		}

		return commandName;
	}
	public char getFrameType()
	{
		char frameType;
		String stringBinaryFrame = this.frame;
		//Reads first two digits of control field(16th bit and 17th bit) and returns frame type in char
		if((stringBinaryFrame.substring(16, 18)).contains("10"))
			frameType = 'S';
		else if((stringBinaryFrame.substring(16, 18)).contains("11"))
			frameType = 'U';
		else 
			frameType = 'I';
		
		return frameType;
	}
	
	public int getNSFromFrame()
	{
		int NS = 0;
		String stringBinaryFrame = this.frame;
		if(this.getFrameType() == 'I')
		{
			NS = Integer.parseInt(stringBinaryFrame.substring(25, 28),2);
		} else
			try {
				throw new Exception("Incompatible type of frame to get N(S).");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return NS;
	}
	public int getNRFromFrame()
	{
		int NR = 0;
		String stringBinaryFrame = this.frame;
		if(this.getFrameType() == 'I' || this.getFrameType() == 'S')
		{
			NR = Integer.parseInt(stringBinaryFrame.substring(29, 32),2);
		} else
			try {
				throw new Exception("Incompatible type of frame.");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return NR;
		
	}
	
	
	//Converts to a binary string and pads to the given size.
	public String convertDecimalToBinaryString(int number, int size)
	{
		String binaryString = Integer.toBinaryString(number);
		int numberString = Integer.parseInt(binaryString);
		binaryString = String.format("%0"+size+"d", numberString);
		return binaryString;
	}

	public String getDecodedInfo() {
		
		String info = this.frame.substring(32,frame.length() - 24);
		
		return info;
	}
}
