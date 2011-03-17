package application;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.CharBuffer;

public class SocketClient {
	
	private static boolean readyToSend = false;
	private static String buff = "";
	private static String address = "";
	private static String toAddress;
	private static int windowSize = 7;
	private static int unACKEDFrames = 0;
	private static int expectedACK = 0;
	
	//private static int expectedFrame = 0;
	
	private static Socket clientSocket = null;
	private static PrintStream os = null;
	
	private static PipedOutputStream cTocOS = new PipedOutputStream();
	private static PipedInputStream cTocIS ;
	private static PrintStream cTocDOS = new PrintStream(cTocOS);
	private static DataInputStream cTocDIS ;
	
	private static PipedOutputStream sTocOS = new PipedOutputStream();
	private static PipedInputStream sTocIS ;
	private static PrintStream sTocDOS = new PrintStream(sTocOS);
	private static DataInputStream sTocDIS ;
	
	private static DataInputStream is;
	
	public static void main(String[] args) {
		String ip = "localhost";
		int port = 9999;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			PipedInputStream cTocIS = new PipedInputStream(cTocOS);
			cTocDIS = new DataInputStream(cTocIS);
			
			PipedInputStream sTocIS = new PipedInputStream(sTocOS);
			sTocDIS = new DataInputStream(sTocIS);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		Connect(ip,port);
		
		SocketIOManager sm = new SocketIOManager();
		sm.start();
		
		ServerMessageListener outListener = new ServerMessageListener();
		outListener.start();
		
		ClientMessageListener inListener = new ClientMessageListener();
		inListener.start();
		
		while (true){
			try {
				boolean isAddressSet = false;
				while (!isAddressSet)
				{
					try
					{
						System.out.println("Who do you want to send to?");
						toAddress = br.readLine();
						toAddress = Integer.toBinaryString(Integer.parseInt(toAddress));
						for (int i = 0; toAddress.length()<8;i++)
						{
							toAddress = "0" + toAddress;
						}
						isAddressSet = true;
					}
					catch (NumberFormatException ex)
					{
						System.out.println("Invalid address...");
					}
				}
				System.out.println("Enter Message:");
				buff = buff + "\n" + br.readLine();
				if (buff != "")
				{
					readyToSend = true;
				}
				else
				{
					readyToSend = false;
				}			
			} catch (IOException e) {
				System.out.println("Failed to read user input.");
			}
		}
	}
	
	private static void Connect(String ip,int port)
	{
		clientSocket = null;
		os = null;
		is = null;
		try {
			clientSocket = new Socket(ip, port);
			os = new PrintStream(clientSocket.getOutputStream());
			BufferedInputStream bis = new BufferedInputStream(clientSocket.getInputStream());
		 	is = new DataInputStream(bis);
		} 
		catch (UnknownHostException e) {
			System.err.println("Don't know about host: hostname");
			} 
		catch (IOException e) {
				System.err.println("Couldn't get I/O for the connection to: hostname");
		}
		
		boolean notConnected = true;
		System.out.println("Attempting to connect to primary...");
		os.println("request-to-connect");
		while(notConnected)
		{
			try {
				String line = is.readLine();
				//System.out.println("line: " + line);
				if (line.equalsIgnoreCase("connection-accepted"))
				{
					line = is.readLine();
					address = line;
					address = Integer.toBinaryString(Integer.parseInt(address));
					for (int i = 0; address.length()<8;i++)
					{
						address = "0" + address;
					}
					
					notConnected = false;
					System.out.println("Connected to server");
				}
			} catch (IOException e) {
				System.out.println("I/O Exception!");
			}
		}
	}
	
	private static class ServerMessageListener extends Thread
	{
		public void run()
		{
			while(true)
			{
				try {
					if (sTocDIS.available()>0)
					{
						
						String line = sTocDIS.readLine();
						if (line.equalsIgnoreCase("request-for-you-to-send") && readyToSend)
						{
							//expectedFrame = 0;
							//unACKEDFrames = 0;
							os.println("want-to-send");
							Thread.sleep(200);
							byte[] inBytes = buff.getBytes();
							String info = "";
							
							BigInteger bi = new BigInteger(inBytes);
							info = bi.toString(2);
							while (info.length() % 8 != 0)
							{
								info = 0 + info;
							}
							String infoToSend = "";
							int k = 0;
							for (int i = 0; i < info.length();i++)
							{
								infoToSend = infoToSend + info.charAt(i);
								if (infoToSend.length() == 64*8)
								{	
									try {
										String control = this.buildSendControl(k%8);
										Frame frameToSend = new Frame(toAddress + address,control,infoToSend);
										while (unACKEDFrames >= windowSize)
										{
											Thread.sleep(1000);
										}
										os.println(frameToSend.toString());
										unACKEDFrames++;
										infoToSend = "";
										//Thread.sleep(2000);
									} catch (Exception e) {
										System.out.println("Failed to send frame");
									}
									k++;
								}
							}
							if (infoToSend.length() != 0)
							{
								try {
									String control = this.buildSendControl(k%8);
									Frame frameToSend = new Frame(toAddress + address,control,infoToSend);
									os.println(frameToSend.toString());
									unACKEDFrames++;
									infoToSend = "";
								} catch (Exception e) {
									System.out.println("Failed to send frame");
								}
							}
							buff = "";
							readyToSend = false;
							os.println("finished-send");
						}
						else if (line.equalsIgnoreCase("request-for-you-to-send"))
						{
							os.println("dont-want-to-send");
						}
						else
						{
							System.out.println("Error:Received Frame in Server Monitor Thread. This should not happen...");
						}
					}
					
				} catch (IOException e) {
					System.out.println("I/O Error!");
				} catch (InterruptedException e) {
					System.out.println("Error: This should not be happening!!");
				}
				
			}
		}

		private String buildSendControl(int frameNum) {
			String ns = Integer.toBinaryString(frameNum);
			while (ns.length() < 3)
			{
				ns = "0" + ns;
			}
			return "0" + ns + "1" + "000";
		}
		
	}

	private static class ClientMessageListener extends Thread
	{	
		public void run()
		{
			while(true)
			{
				try {
					if (cTocDIS.available()>0)
					{
						String line = cTocDIS.readLine();
						Frame recFrame = new Frame(line);
						if (recFrame.toString().startsWith(Frame.getFlag()))
						{
							int nr = recFrame.getNRFromFrame();
							int ns = recFrame.getNSFromFrame();
							if (unACKEDFrames > 0) //Frames are being ACKed
							{
								System.out.println("ACK Received. Excpeting Frame " + nr);
								unACKEDFrames = unACKEDFrames - 1;
							}
							else //New message frame
							{
								String sourceClientBin = recFrame.getSourceAddressBinaryFromFrame();
								int sourceClient = Integer.parseInt(sourceClientBin,2);
								System.out.println("User " + sourceClient + " says: " + recFrame.getDecodedInfo());
								this.SendACKFrame(ns,sourceClientBin);
							}
								//TODO: need code to send ACK here	
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		private void SendACKFrame(int ns, String sourceClient) {
			int nr = ns + 1;
			if (nr > windowSize)
			{
				nr = 0;
			}
			String ctrl = this.buildACKControl(nr);
			Frame frameToSend = new Frame(sourceClient + address,ctrl,"");
			os.println(frameToSend.toString());
			os.println("finished-send");
		}

		private String buildACKControl(int frameNumRec) {
			String nr = Integer.toBinaryString(frameNumRec);
			while (nr.length() < 3)
			{
				nr = "0" + nr;
			}
			return "0" + "000" + "1" + nr ;
		
		}
	}

	private static class SocketIOManager extends Thread
	{
		public void run()
		{
			String line = "";
			while(true)
			{
				try {
					line = is.readLine();
					if (line.startsWith(Frame.getFlag()))
					{
						cTocDOS.println(line);
					}
					else
					{
						sTocDOS.println(line);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
