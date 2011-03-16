package application;

import java.io.*;
import java.math.BigInteger;
import java.net.*;

public class SocketClient {
	
	private static boolean readyToSend = false;
	private static String buff = "";
	private static String address = "";
	private static int windowSize = 7;
	private static int unACKEDFrames = 0;
	private static int expectedFrame = 0;
	private static Socket clientSocket = null;
	private static PrintStream os = null;
	private static DataInputStream is = null;
	private static boolean isInListenerBlocked = false;
	
	public static void main(String[] args) {
		String ip = "localhost";
		int port = 9999;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		
		Connect(ip,port);
		
		OutgoingListener outListener = new OutgoingListener();
		outListener.start();
		
		IncomingListener inListener = new IncomingListener();
		inListener.start();
		
		while (true){
			try {
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
			is = new DataInputStream(clientSocket.getInputStream());
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
				System.out.println("line: " + line);
				if (line.equalsIgnoreCase("connection-accepted"))
				{
					line = is.readLine();
				
					address = line;
					notConnected = false;
					System.out.println("Connected to server");
				}
			} catch (IOException e) {
				System.out.println("I/O Exception!");
			}
		}
	}
	
	private static class OutgoingListener extends Thread
	{
		
		
		public OutgoingListener()
		{
			
		}
		
		
		public void run()
		{
			while(true)
			{
				try {
					if (is.available()>0)
					{
						String line = is.readLine();
					
						if (line.equalsIgnoreCase("request-for-you-to-send") && readyToSend)
						{
							expectedFrame = 0;
							unACKEDFrames = 0;
							os.println("want-to-send");
							Thread.sleep(200);
							byte[] inBytes = buff.getBytes();
							String info = "";
							BigInteger bi = new BigInteger(inBytes);
							info = bi.toString(2);
							String infoToSend = "";
							for (int i = 0; i < info.length();i++)
							{
								infoToSend = infoToSend + info.charAt(i);
								if (infoToSend.length() == 64*8)
								{	
									try {
										String control = this.buildSendControl(unACKEDFrames);
										Frame frameToSend = new Frame(address,control,infoToSend);
										while (unACKEDFrames >= windowSize){}
										os.println(frameToSend.toString());
										unACKEDFrames++;
										infoToSend = "";
										Thread.sleep(200);
									} catch (Exception e) {
										System.out.println("Failed to send frame");
									}
								}
							}
							if (info.length() != 0)
							{
								try {
									String control = this.buildSendControl(unACKEDFrames);
									Frame frameToSend = new Frame(address,control,info);
									os.println(frameToSend.toString());
									Thread.sleep(200);
									unACKEDFrames++;
									info = "";
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
							System.out.println("line: " + line);
						}
					}
					
				} catch (IOException e) {
					System.out.println("I/O Error!");
				} catch (InterruptedException e) {
					System.out.println("This should not be happening!!");
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

	private static class IncomingListener extends Thread{
		
		public IncomingListener()
		{
			
		}
		
		public void run()
		{
			while(true)
			{
				try {
					if (is.available()>0)
					{
						is.mark(10000);
						String line = is.readLine();
						if (line.startsWith(Frame.getFlag()))
						{
							Frame recFrame = new Frame(line);
							int ns = recFrame.getNSFromFrame();
							int nr = recFrame.getNRFromFrame();
							if (ns != 0) //message is being delivered
							{
								System.out.println("Received Frame: " + line);
								
								//TODO: need code to send ACK here
							
							}
							else if (ns != 0) //ACK is being delivered
							{
								for(int i = 0; i < ns - expectedFrame;i++)
								{
									unACKEDFrames--; //expand window
								}
								expectedFrame = ns;
							}
						}
						else{
							is.reset();
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		private String buildACKControl(int frameNum) {
			//TODO: Build Control Field for an ACK. should be 00001xxx where xxx is the number of received frames
			return "";
		}
	}
}
