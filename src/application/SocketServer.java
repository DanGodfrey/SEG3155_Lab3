package application;

import java.io.*;
import java.net.*;

public class SocketServer {
	public static void main(String args[]) {
		// declaration section:
		// declare a server socket and a client socket for the server
		// declare an input and an output stream
		
		ServerSocket echoServer = null;
		String line;
		DataInputStream is;
		PrintStream os;
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
				line = is.readLine();
                                if(line.equalsIgnoreCase("request-to-send"))
                                {
                                    os.println("clear-to-send");
                                    line = is.readLine();
                                    System.out.println((line));
                                }
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
	}