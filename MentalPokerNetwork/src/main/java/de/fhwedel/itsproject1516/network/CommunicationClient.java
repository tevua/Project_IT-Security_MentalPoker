package de.fhwedel.itsproject1516.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLSocket;


public class CommunicationClient implements Runnable{
	/** The client */
	TLSClient client;

	/** The reader for the socket to the server. */
	BufferedReader reader;

	/**
	 * Construktor.
	 * 
	 * @param aClient
	 *            Client.
	 * @param aSocket
	 *            the socket used for communication
	 **/
	public CommunicationClient(TLSClient aClient, SSLSocket aSocket) {
		this.client = aClient;
		try {
			final BufferedInputStream in = new BufferedInputStream(aSocket.getInputStream());
			final InputStreamReader inputstreamreader = new InputStreamReader(in);
			this.reader = new BufferedReader(inputstreamreader);
		} catch (SocketTimeoutException e) {
			this.client.disconnect();
		} catch (IOException e) {
			this.client.disconnect();
		}
	}

	@Override
	public void run() {
		boolean notEndOfStream = true;
		while (notEndOfStream) {
			try {
				final String line = reader.readLine();
				if (line != null) {
					this.client.received(line);
				}
			} catch (IOException e) {
				notEndOfStream = false;
				this.client.disconnect();
			}
		}
	}
}
