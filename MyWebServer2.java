//Multithreaded webserver that can handle .htm/html, .jpeg/jpg , .gif,/.class files
import java.util.*;
import java.io.*;
import java.net.*;

// A webserver waits for clients to connect, then starts a separate thread to handle the request
public class MyWebServer2{
   private static ServerSocket serverSocket;

   public static void main(String[] args) throws IOException{
	serverSocket=new ServerSocket(8000); // Start, listen on port 8000
	while(true){
		try{
			Socket s=serverSocket.accept(); // wait for client to connect
			new ClientHandler(s); // Handle the client in a separeted thread
		
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	
   }
}


//A ClientHandler reads an HTTP request and responds
class ClientHandler extends Thread{
	private Socket socket; //The accepted socket from the webserver
	
	//Start the thread in the constructor
	public ClientHandler(Socket s){
		socket=s;
		start();
	}

	//Read the HTTP request,respond,and close the connection
	public void run(){
	  try{
		BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintStream out=new PrintStream(new BufferedOutputStream(socket.getOutputStream()));

		String s= in.readLine();
		System.out.println(s);

		String filename="";
		StringTokenizer st=new StringTokenizer(s);
		try{
			if(st.hasMoreElements() && st.nextToken().equalsIgnoreCase("GET") && st.hasMoreElements())
			    filename=st.nextToken();
			else
			  throw new FileNotFoundException();

			if(filename.endsWith("/"))
			   filename+="index.html";
			while(filename.indexOf("/")==0)
                	  filename=filename.substring(1);

			filename=filename.replace('/', File.separator.charAt(0));

			//check for illegal characters to prevent access to superdirectories
			if (filename.indexOf("..")>=0 || filename.indexOf(':')>=0 || filename.indexOf('|')>=0)
				throw new FileNotFoundException();

			//If a directory is requested and the trailing / is missing, send the client an HTTP request to append it.
			//(This is necessary for relative links to work correctly in the client).
		if (new File(filename).isDirectory()) { 
			filename=filename.replace('\\','/');
			out.print("HTTP/1.0 301 Moved Permanently\r\n" + "Location: /"+filename+"/\r\n\r\n");
			out.close();
			return;
		}

			//Open the file (may throw FileNotFoundException)
			InputStream f=new FileInputStream(filename);

			//Determine the MIME type and print HTTP header
			String mimeType="text/plain";
			if (filename.endsWith(".html") || filename.endsWith(".htm"))
				mimeType="text/html";
			else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg"))
				mimeType="image/jpeg";
			else if (filename.endsWith(".gif"))
				mimeType="image/gif";
			else if (filename.endsWith(".class"))
				mimeType="application/octet-stream";
			out.print("HTTP/1.0 200 OK\r\n"+"Content-type: "+mimeType+"\r\n\r\n");

				//Send file contents to client, then close the connection
				byte[] a=new byte[4096];
				int n;
				while ((n=f.read(a))>0)
					out.write(a, 0, n);
				out.close();
			}
			catch (FileNotFoundException x) {
				out.println("HTTP/1.0 404 Not Found \r\n"+"Content-type: text/html\r\n\r\n"+"<html><head></head><body>"+filename+" not found</body></html>\n");
				out.close();
			}
		}
		catch (IOException x) {
			System.out.println(x);
		}
	}
}

