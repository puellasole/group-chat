import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	
	private Socket socket;
	private BufferedWriter bw;
	private BufferedReader br;
	private String username;
	
	public Client(Socket socket, String username){
		try {
			this.socket = socket;
			this.bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.username = username;
		} catch (IOException e) {
			closeEverything(socket, bw, br);
		}
	}
	
	public void sendMessage() {
		try {
			bw.write(username);
			bw.newLine();
			bw.flush();
			
			Scanner sc = new Scanner(System.in);
			while(socket.isConnected()) {
				String msgToSend = sc.nextLine();
				bw.write(username + ": " + msgToSend);
				bw.newLine();
				bw.flush();
			}
		} catch (IOException e) {
			closeEverything(socket, bw, br);
		}
	}
	
	public void listenForMsg() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String msgFromGroupChat;
				
				while(socket.isConnected()) {
					try {
						msgFromGroupChat = br.readLine();
						System.out.println(msgFromGroupChat);
					} catch (IOException e) {
						closeEverything(socket, bw, br);
					}
				}
			}
		}).start();
	}
	
	public void closeEverything(Socket s, BufferedWriter bw, BufferedReader br) {
		try {
			if(bw != null) bw.close();
			if(br != null) br.close();
			if(socket != null) socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter your username for the group chat");
		String username = sc.nextLine();
		Socket s = new Socket("localhost", 8080);
		Client client = new Client(s, username);
		client.listenForMsg();
		client.sendMessage();
	}

}
