package chess;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.swing.*;

class Client extends Thread {
	public Socket s;
	public ObjectInputStream in;
	public ObjectOutputStream out;
	public String username;
	public boolean isPlaying;

	public Client(Socket s) throws Exception {
		out = new ObjectOutputStream(s.getOutputStream());
		in = new ObjectInputStream(s.getInputStream());
		out.flush();
	}

	public ObjectInputStream getIn() {
		return in;
	}

	public ObjectOutputStream getOut() {
		return out;
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	public void setPlaying(boolean isPlaying) {
		this.isPlaying = isPlaying;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String s) {
		username = s;
	}

	public void run() {
		try {
			while (true) {
				System.out.println("here");
				// get messages from client and send messages accordingly to
				// other clients and to the sender as well
				Message m = (Message) in.readObject();

				switch (m.getType()) {
				case send_invitation:
					break;
				case send_message:
					m.setStr(m.getStr().toUpperCase());
					out.writeObject(m);
					break;
				case send_username:
					setUsername(m.getStr());
					break;
				case propose_draw:
					break;
				case resign:
					break;
				case send_move:
					break;
				default:
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

public class Server extends JFrame {

	public enum PieceType {
		pawn, queen, king, rook, bishop, knight
	};

	static public ArrayList<Client> clients = new ArrayList<>();
	static public HashMap<String, Client> usernames = new HashMap<>();

	public static void main(String[] args) throws Exception {
		ServerSocket ss = new ServerSocket(9090);
		while (true) {
			Socket s = ss.accept();
			Client c = new Client(s);
			addClient(c);
			c.start();
		}
	}

	public static synchronized boolean addClient(Client c) {
		if (usernames.containsKey(c.getUsername()))
			return false;
		clients.add(c);
		usernames.put(c.getUsername(), c);
		return true;
	}

	public static synchronized boolean existsClient(String username) {
		return usernames.containsKey(username);
	}

	public static synchronized boolean existsClient(Client c) {
		return usernames.containsValue(c);
	}
}
