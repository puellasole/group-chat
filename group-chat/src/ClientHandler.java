import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
	
	private Socket socket;
	private BufferedWriter bufferedWriter;
	private BufferedReader bufferedReader;
	private String clientUsername;
	
	public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
	
	public ClientHandler(Socket socket){
		try {
			this.socket = socket;
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.clientUsername = bufferedReader.readLine();
			clientHandlers.add(this);
			broadcastMessage("SERVER: " + clientUsername + " has entered the chat!");
		} catch (IOException e) {
			closeEverything(socket, bufferedWriter, bufferedReader);
		}
	}

	@Override
	public void run() {
		String messageFromClient;
		
		while(socket.isConnected()) {
			try {
				messageFromClient = bufferedReader.readLine();
				broadcastMessage(messageFromClient);
			} catch (IOException e) {
				closeEverything(socket, bufferedWriter, bufferedReader);
				break;
			}
		}
	}
	
	public void broadcastMessage(String messageToSend) {
		for(ClientHandler clientConnection : clientHandlers) {
			try {
				if(!clientConnection.clientUsername.equals(clientUsername)) {
					clientConnection.bufferedWriter.write(messageToSend);
					clientConnection.bufferedWriter.newLine();
					clientConnection.bufferedWriter.flush();
				}
			} catch(IOException e) {
				closeEverything(socket, bufferedWriter, bufferedReader);
			}
		}
	}
	
	public void removeClientConnection() {
		clientHandlers.remove(this);
		broadcastMessage("SERVER: " + clientUsername + " has left the chat!");
	}
	
	public void closeEverything(Socket socket, BufferedWriter bw, BufferedReader br) {
		removeClientConnection();
		try {
			if(bw != null) bw.close();
			if(br != null) br.close();
			if(socket != null) socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
