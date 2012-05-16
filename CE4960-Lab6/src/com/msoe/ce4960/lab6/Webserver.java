package com.msoe.ce4960.lab6;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Acts as a basic webserver
 *
 * Experiences:
 * This lab was extremely straightforward.  Thanks to the discussions and topics
 * reviewed in class, parsing the headers and data was a fairly easy task.  Once
 * there all it required was writing output.
 * 
 * Design:
 * The server starts listening on port 8888.  When a client connects, it spins
 * off another thread to handle the request.  This thread parses the request.
 * If the request is a GET request, the file is fetched, if it is available.  If
 * it is not available, a 404 error is returned.  If it is a POST request, the 
 * thread processes the post data and echos fields that it expected back to the
 * user.
 *
 * Deficiencies:
 * The main deficiency in this program is its lack of flexibility.  Right now, it
 * can only serve raw files and parse 1 type of POST request.  Ideally, the server
 * would be able to look at where the file was POSTed to and react differently.
 * That would be a relatively simple task, but is out of the scope for this lab.
 *
 * Ideas for Improvement:
 * Make this a more defined lab.  Maybe have it act more like a webserver where
 * multiple forms have to be processed.  I would also add a requirement to serve
 * images.
 *
 * Usage:
 * Start the server and go to {@link http://localhost:8888}.  Fill out the form
 * (which was retrieved via a GET request) and submit it.  The POST data will
 * be processed and your data will be echoed back to you.
 * 
 * Note that this has only been tested with Firefox 12
 *
 * @author Erik Sommer
 */

/**
 * Main class.  Starts the webserver
 * @author Erik Sommer
 *
 */
public class Webserver {

	/**
	 * The port the server should listen on
	 */
	private static final int SERVER_PORT = 8888;

	/**
	 * Main method.  Starts the webserver
	 * @param args	ignored
	 */
	public static void main(String[] args){

		// Socket to listen for connections on
		ServerSocket socket;
		try {

			// Create the socket to listen for connections on
			socket = new ServerSocket(SERVER_PORT);

			// Listen for connections
			while(true){

				// Accept a client connection
				Socket clientSocket = socket.accept();

				// Spin off a new thread;
				new Thread(new ClientHandler(clientSocket)).start();
			}

		} catch (IOException e) {
			System.err.println("An I/O exception occured");
			e.printStackTrace();
		}
	}
}
