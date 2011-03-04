package application;

import java.io.*;
import java.net.*;

public class SocketClient {
	
	private static boolean readyToSend = false;
	private static String buff = "";
	private static String address = "";
	private static int windowSize = 5;
	private static int unACKEDFrames = 0;
	private static int expectedFrame = 0;
	
	public static void main(String[] args) {
		String ip = "";
		int port = 0;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter server ip: ");
		try {
			ip = br.readLine();
			System.out.println("Enter server port: ");
		} catch (IOException e1) {
			System.out.println("I/O Error!");
		}
		
		Connect(ip,port);
		
		OutgoingListener outListener = new OutgoingListener(ip,port);
		outListener.run();
		
		IncomingListener inListener = new IncomingListener(ip,port);
		inListener.run();
		
		while (true){
			try {
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
		Socket clientSocket = null;
		PrintStream os = null;
		DataInputStream is = null;
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
		
		os.println("reuest=to-connect");
		boolean notConnected = true;
		while(notConnected)
		{
			try {
				String line = is.readLine();
				if (line != "")
				{
					address = line;
					notConnected = false;
				}
			} catch (IOException e) {
				System.out.println("I/O Exception!");
			}
		}
	}
	
	private static class OutgoingListener extends Thread
	{
		
		private Socket clientSocket = null;
		private PrintStream os = null;
		private DataInputStream is = null;
		public OutgoingListener(String ip, int port)
		{
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
		}
		
		public void run()
		{
			while(true)
			{
				try {
					String line = is.readLine();
					if (line.equalsIgnoreCase("request-for-you-to-send") && readyToSend)
					{
						expectedFrame = 0;
						unACKEDFrames = 0;
						os.println("want-to-send");
						Thread.sleep(200);
						byte[] inBytes = buff.getBytes();
						String info = "";
						for (int i = 0; i < inBytes.length;i++)
						{
							info = info + inBytes[i];
							if (info.length() == 64)
							{	
								try {
									String control = this.buildSendControl(unACKEDFrames);
									Frame frameToSend = new Frame(address,control,info);
									while (unACKEDFrames > windowSize){}
									os.println(frameToSend.toString());
									unACKEDFrames++;
									info = "";
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
						os.println("finished-send");
					}
					else
					{
						os.println("dont-want-to-send");
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
		private Socket clientSocket = null;
		private PrintStream os = null;
		private DataInputStream is = null;
		public IncomingListener(String ip, int port)
		{
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
		}
		
		public void run()
		{
			while(true)
			{
				try {
					String line = is.readLine();
					if (line != "")
					{
						Frame recFrame = new Frame(line);
						int ns = Integer.parseInt(recFrame.getNS(), 2);
						int nr = Integer.parseInt(recFrame.getNR(),2);
						if (ns != 0) //message is being delivered
						{
							System.out.println(recFrame.getDecodedInfo());
							
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
				} catch (IOException e) {
					System.out.println("I/O Error!");
				} 
			}
		}

		private String buildACKControl(int frameNum) {
			//TODO: Build Control Field for an ACK. should be 00001xxx where xxx is the number of received frames
			return "";
		}
	}
}
