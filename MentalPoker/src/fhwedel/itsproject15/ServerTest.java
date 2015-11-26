package fhwedel.itsproject15;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;

import org.bouncycastle.crypto.tls.CertificateRequest;
import org.bouncycastle.crypto.tls.DefaultTlsClient;
import org.bouncycastle.crypto.tls.TlsAuthentication;
import org.bouncycastle.crypto.tls.TlsClientProtocol;
import org.bouncycastle.crypto.tls.TlsCredentials;
import org.bouncycastle.crypto.tls.TlsServerProtocol;
import org.bouncycastle.crypto.tls.TlsSignerCredentials;

import fhwedel.itsproject15.network.server.Server;
import fhwedel.itsproject15.network.server.ServerUsingTLS;

public class ServerTest {
	
	private static Server server = null; 
	private static int PORT = 9409;

	public static void waitForInput(BufferedReader br) throws IOException {
		String line = br.readLine();
		if (line != null) {
			if (line.startsWith("STOP ")) {
				server.stop(); 
			} else if (line.startsWith("SEND ")) {
				final String message = line.substring(5).trim();
			} else {
				System.err.println("Command could not be recognized. Start with either STOP (to stop the server) or SEND (to send a message to all Clients)"); 
			}
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out
				.println("Welcome to ITSPROJECT15 Server.\n"); 
		
		//server = new ServerUsingTLS();
		//System.out.println("Starting the server..."); 
		//server.start(PORT);

		//waitForInput(br); 
		//server.stop(); 
		
		// Reference: http://boredwookie.net/index.php/blog/how-to-use-bouncy-castle-lightweight-api-s-tlsclient/
	    //            bcprov-jdk15on-153.tar\src\org\bouncycastle\crypto\tls\test\TlsClientTest.java
	    
	    /*    java.security.SecureRandom secureRandom = new java.security.SecureRandom();
	        Socket socket = new Socket(java.net.InetAddress.getByName("www.google.com"), 443);
	        TlsClientProtocol protocol = new TlsClientProtocol(socket.getInputStream(), socket.getOutputStream(),secureRandom);
	        DefaultTlsClient client = new DefaultTlsClient() {
	            public TlsAuthentication getAuthentication() throws IOException {
	                TlsAuthentication auth = new TlsAuthentication() {
	                    // Capture the server certificate information!
	                    public void notifyServerCertificate(org.bouncycastle.crypto.tls.Certificate serverCertificate) throws IOException {
	                    }

	                    public TlsCredentials getClientCredentials(CertificateRequest certificateRequest) throws IOException {
	                        return null;
	                    }
	                };
	                return auth;
	            }
	        };
	        protocol.connect(client);
	        
	        java.io.OutputStream output = protocol.getOutputStream();
	        output.write("GET / HTTP/1.1\r\n".getBytes("UTF-8"));
	        output.write("Host: www.google.com\r\n".getBytes("UTF-8"));
	        output.write("Connection: close\r\n".getBytes("UTF-8")); // So the server will close socket immediately.
	        output.write("\r\n".getBytes("UTF-8")); // HTTP1.1 requirement: last line must be empty line.
	        output.flush();

	        java.io.InputStream input = protocol.getInputStream();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
	        String line;
	        while ((line = reader.readLine()) != null)
	        {
	            System.out.println(line);
	        }
	        socket.close(); 		*/
	}
}
