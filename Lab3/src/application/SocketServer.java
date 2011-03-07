package application;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class SocketServer {
	
	private static ArrayList<UserThread> connections = new ArrayList<UserThread>();
	private static ServerSocket echoServer = null;
	private static PrintStream os = null;
	private static DataInputStream is = null;
	
	
	public static void main(String args[]) {
		
		
			// declaration section:
			// declare a server socket and a client socket for the server
			// declare an input and an output stream
			
			String line;
		
			
			Socket serverSocket = null;
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
	                        serverSocket = echoServer.accept();
				is = new DataInputStream(serverSocket.getInputStream());
				os = new PrintStream(serverSocket.getOutputStream());
	// As long as we receive data, echo that data back to the client.
				while (true) {				
					
                    	Socket clientSocket = echoServer.accept();
                    	System.out.println("client accepted");
                        UserThread newThread = new UserThread(clientSocket,connections);
                        connections.add(newThread);
                        newThread.run();
                        line = is.readLine();
                        System.out.println((line));
                    
					//line = is.readUTF();
									
					if (line.indexOf("CLOSE") != -1) {				
						break;
						}
					}
				
				is.close();
				os.close();
				serverSocket.close();
				echoServer.close();
				System.out.println("Server close");
				}
			catch (IOException e) {
				System.out.println(e);
				}
	}
		
	private static class UserThread extends Thread
	{
		DataInputStream is = null;
		PrintStream os = null;
		Socket clientSocket = null;
		ArrayList<UserThread> clientThreads;
		
		public UserThread (Socket clientSocket, ArrayList<UserThread> t)
		{
			this.clientSocket = clientSocket;
			this.clientThreads = t;		
		}
		
		public void run()
		{
			String line;
			String name;
			
			try {
				is = new DataInputStream(clientSocket.getInputStream());
				os = new PrintStream(clientSocket.getOutputStream());
				
				os.println("connection-accepted");
				os.println(clientThreads.size()-1);
				
				System.out.println("--User: " + (clientThreads.size()-1) + " has connected--");
				
				while(true)
				{
					
				}
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
		
		
}
	
