package de.fhwedel.itsproject1516.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLSocket;

public class CommunicationServer implements Runnable{

	private TLSServer server; 
	
	/** The reader for the socket to the server/client. */
	BufferedReader reader;
	
	/** the other side of the socket */
	private int client; 

	public CommunicationServer(TLSServer someone, SSLSocket s, int aClient) {
		this.server = someone;
		this.client = aClient;
		try {
			final BufferedInputStream in = new BufferedInputStream(s.getInputStream());
			final InputStreamReader inputstreamreader = new InputStreamReader(in);
			this.reader = new BufferedReader(inputstreamreader);
		} catch (IOException e) {
			this.server.clientLeft(aClient);
		}
	}
	
	@Override
	public void run() {
		boolean notEndOfStream = true;
		while (notEndOfStream) {
			try {
				final String line = reader.readLine();
				if (line == null) {
					this.server.clientLeft(this.client);
					notEndOfStream = false;
				} else {
					this.server.received(line);
				}
			} catch (IOException e) {
				this.server.clientLeft(this.client);
				notEndOfStream = false;
			}
		} 
	}

}
