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


class Client extends Thread
{
	public Socket s;
	public ObjectInputStream in;
	public ObjectOutputStream out;
	public String username;
	public boolean isPlaying;
	public Client(Socket s) throws Exception{
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
	
	public String getUsername(){
		return username;
	}
	public void setUsername(String s){
		username = s;
	}
	public void run() {
		try{
			while(true){
				Message m = (Message) in.readObject();
				if (m == null) continue;
				System.out.println("server receives message: " + m);

				switch(m.getType()){
				case to_server_send_message:
					if (!Server.isPlaying(this))
						continue;
					String destUser = Server.isPlayingWith(getUsername());
					m.setDestUsername(destUser);
					m.setType(Message.MsgType.to_client_send_message);
					m.setMessageText(getUsername() + ": " + m.getMessageText());
					Server.sendChat(getUsername(), destUser, m);
					break;
				case to_server_send_username:
					if(Server.existsClient(m.getMessageText())){
						m.setType(Message.MsgType.to_client_send_username_taken);
						Server.sendMessage(this, m);
					} else {
						setUsername(m.getMessageText());
						Server.addClient(this);
					}
					break;
				case to_server_invite_player:
					String dest = m.getDestUsername();
					if (!Server.existsClient(dest))
						continue;
					if (Server.isPlaying(dest)){
						// player already playing
						m.setType(Message.MsgType.to_client_user_already_playing);
						Server.sendMessage(dest, m);
					} else {
						// player free , ask him to play
						m.setType(Message.MsgType.to_client_send_invitation);
						Server.sendMessage(dest, m);
					}
					break;
				case to_server_resign:
					break;
				case to_server_propose_draw:
					break;
				case to_server_confirm_invitation:
					m.setType(Message.MsgType.to_client_send_response_accepted_to_invitation);
					Server.sendMessage(m.getDestUsername(), m);
					Server.addGame(m.getSourceUsername(),m.getDestUsername() );
					break;
				case to_server_decline_invitation:
					m.setType(Message.MsgType.to_client_send_response_declined_to_invitation);
					Server.sendMessage(m.getDestUsername(), m);
					break;
				case to_server_confirm_draw:
					break;
				case to_server_decline_draw:
					break;
				case to_server_move:
					break;
				default:
					break;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
}
class Game {
	String player1, player2;
	public Game(String player1, String player2){
		this.player1 = player1;
		this.player2 = player2;
	}
	public String getPlayer1() {
		return player1;
	}

	public void setPlayer1(String player1) {
		this.player1 = player1;
	}

	public String getPlayer2() {
		return player2;
	}

	public void setPlayer2(String player2) {
		this.player2 = player2;
	}
	
	public boolean equals(Object other){
		if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof Game))return false;
	    other = (Game)other;
	    return this.player1.equals(((Game) other).getPlayer1()) &&
	    		this.player2.equals(((Game) other).getPlayer2());
	}
}

public class Server extends JFrame{

		public enum PieceType { pawn, queen, king, rook, bishop, knight };
	
		static public ArrayList<Client> clients = new ArrayList<>();
		static public HashMap<String, Client> usernames = new HashMap<>();
		static public HashSet<Game> games = new HashSet<>();
		
		public static void main(String[] args) throws Exception{
			
			
			ServerSocket ss = new ServerSocket(9090);
			while(true){
				Socket s = ss.accept();
				Client c = new Client(s);
				//addClient(c);
				c.start();
			}
						
		}
		
		public static synchronized boolean addClient(Client c){
			if (usernames.containsKey(c.getUsername()))
				return false;
			clients.add(c);
			usernames.put(c.getUsername(), c);
			return true;
		}
		public static synchronized boolean existsClient(String username){
			return usernames.containsKey(username);
		}
		public static synchronized boolean existsClient(Client c){
			return usernames.containsValue(c);
		}
		public static synchronized boolean addGame(String p1, String p2){
			return games.add(new Game(p1,p2));
		}
		public static synchronized boolean isPlaying(String p){
			for(Game g : games){
				if (g.getPlayer1().equals(p) || g.getPlayer2().equals(p))
					return  true;
			}
			return false;
		}
		
		public static synchronized boolean isPlaying(Client c){
			return isPlaying(c.getUsername());
		}
		public static synchronized String isPlayingWith(String p){
			for(Game g : games){
				if (g.getPlayer1().equals(p) || g.getPlayer2().equals(p))
					return  g.getPlayer1().equals(p) ? g.getPlayer2() : g.getPlayer1();
			}
			return null;
		}
		/*public static synchronized void sendInvitationFromTo(String source, String dest) throws Exception{
			Client c = usernames.get(dest);
			Message m  = new Message(source, Message.MsgType.send_invitation);
			c.getOut().writeObject(m);
		}*/
		public static synchronized void sendChat(String source, String dest, Message m) throws Exception{
			Client sourceC = usernames.get(source);
			Client destC = usernames.get(dest);
			sourceC.getOut().writeObject(m);
			destC.getOut().writeObject(m);
		}
		public static synchronized void sendMessage(String source, Message m) throws Exception{
			Client sourceC = usernames.get(source);
			sourceC.getOut().writeObject(m);
		}
		public static synchronized void sendMessage(Client source, Message m) throws Exception{
			source.getOut().writeObject(m);
		}
}
