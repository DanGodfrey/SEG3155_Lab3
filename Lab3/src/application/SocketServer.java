package application;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class SocketServer {
	
	private static ArrayList<UserThread> connections = new ArrayList<UserThread>();
	private static ServerSocket echoServer = null;
	private static int nextToSend = 0;
	
	public static void main(String args[]) {
		
		
		
			
			
			
	// Try to open a server socket on port 9999
	// Note that we can't choose a port less than 1023 if we are not
	// privileged users (root)

			try {
				echoServer = new ServerSocket(9999);
				}
			catch (IOException e) {
				System.out.println(e);
				}
	// Create a socket object from the ServerSocket to listen and accept
	// connections.
	// Open input and output streams
			try {
				System.out.println("Server Waiting for Request.....");
				
	// As long as we receive data, echo that data back to the client.
				while (true) {				
						DataInputStream is = null;
                    	Socket clientSocket = echoServer.accept();
                    	is = new DataInputStream(clientSocket.getInputStream());
                    	if (is.readLine().equalsIgnoreCase("request-to-connect"))
                    	{
	                    	System.out.println("client accepted");
	                        UserThread newThread = new UserThread(clientSocket,connections);
	                        connections.add(newThread);
	                        newThread.start();
                    	}
                       
					//line = is.readUTF();
				
					}
				}
			catch (IOException e) {
				System.out.println(e);
				}
	}
		
	private static class UserThread extends Thread
	{
		private DataInputStream is = null;
		private PrintStream os = null;
		private Socket clientSocket = null;
		private ArrayList<UserThread> clientThreads;
		private int address;
		
		public UserThread (Socket clientSocket, ArrayList<UserThread> t)
		{
			this.clientSocket = clientSocket;
			this.clientThreads = t;	
			this.address = -1;
		}
		
		public void run()
		{
			String line;
			
			try {
				is = new DataInputStream(clientSocket.getInputStream());
				os = new PrintStream(clientSocket.getOutputStream());
				
				os.println("connection-accepted");
				this.address = (clientThreads.size()-1);
				os.println(address);
				
				System.out.println("--User: " + (clientThreads.size()-1) + " has connected--");
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				while(true)
				{
					
					if (address == 0)
					{
						Thread.sleep(2000);
					}
					if(nextToSend == this.address)
					{
						os.println("request-for-you-to-send");
						boolean isFinished = false;
						//System.out.println("User " + address + " can send");
						while (!isFinished)
						{
							if (is.available() > 0)
							{
								line = is.readLine();
								if (line.equalsIgnoreCase("want-to-send"))
								{
									System.out.println("User " + address + " wants to send");
								}
								else if (line.startsWith(Frame.getFlag()))
								{
									System.out.println("User " + address + " sends frame Legth-" + line.length() + " : " + line);
									for (int i = 0; i < connections.size();i++)
									{
										if(i != address)
										{
											connections.get(i).os.println(line);
										}
									}
								}
								else if (line.equalsIgnoreCase("finished-send"))
								{
									System.out.println("User " + address + " finished sending");
									isFinished = true;
								}
								else if (line.equalsIgnoreCase("dont-want-to-send"))
								{
									//System.out.println("User " + address + " doesn't want to send");
									isFinished = true;
								}
							}
						}
						
							
						
						nextToSend++;
						if (nextToSend >= connections.size())
						{
							nextToSend = 0;
						}
						//System.out.println("Next to Send = " + nextToSend);
						line = "";
					}
				}
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

		
}
	
