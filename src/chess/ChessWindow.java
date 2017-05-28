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
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Scanner;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;



class Sender extends Thread{
	public ObjectOutputStream out;
	public Sender(ObjectOutputStream _out){out=_out;}
	
	
	//public static ArrayList<Message> mq = new ArrayList<>(); // message queue
	public  static List<Message> mq = Collections.synchronizedList(new ArrayList<Message>());
	public void run(){
		try{
			Scanner sc = new Scanner(System.in);
			while(ChessWindow.isConnected()){	
				System.out.println(ChessWindow.getMyColor() + "  " + ChessWindow.getTurn());
				
				synchronized(mq){
					synchronized(out){
						if (!mq.isEmpty()){
							
							Message m = mq.get(0);
							
							System.out.println("client sends message: " + m);
							mq.remove(0);
							out.writeObject(m);
						
						}
					}
				}
						
			}
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
		
	}
	public static synchronized void addMessage(Message m){
		mq.add(m);
		if (m == null){
			System.out.println("Am fost null aici");
		}
	}
}
class Receiver extends Thread{
	public static boolean userRegistered = false;
	public ObjectInputStream in;
	
	public ArrayList<Message> mq = new ArrayList<>();
	public Receiver(ObjectInputStream _in){in=_in;}
	public void run(){
		try{
			// asteapta mesaje de la server
			while(ChessWindow.isConnected()){
					Message m = (Message)in.readObject();
					System.out.println("client receives message: " + m);

					if (m==null) continue;
					String fromWhom = m.getSourceUsername();
					String user = m.getDestUsername();
					switch(m.getType()){  
					case to_client_send_invitation:
						{
							int response = JOptionPane.showConfirmDialog(new JFrame(), "User " + fromWhom + " wants to play a game with you", "Invitation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
							m.flipUsers();
							if (response == JOptionPane.YES_OPTION){
								m.setType(Message.MsgType.to_server_confirm_invitation);
								Sender.addMessage(m);
								ChessWindow.setTurn("white");
								ChessWindow.setMyColor("black");
							} else {
								m.setType(Message.MsgType.to_server_decline_invitation);
								Sender.addMessage(m);
								
							}
							break;
						}
						
					case to_client_send_response_declined_to_invitation:
						{	
							JOptionPane.showMessageDialog(new JFrame(), "Invitation declined", "Response", JOptionPane.INFORMATION_MESSAGE);
						}
						break;
					case to_client_send_response_accepted_to_invitation:
					{
						ChessWindow.setTurn("white");
						ChessWindow.setMyColor("white");
						JOptionPane.showMessageDialog(new JFrame(), "Invitation accepted", "Response", JOptionPane.INFORMATION_MESSAGE);	
					}
					break;
					case to_client_send_message:
						ChessWindow.showMessage(m.getMessageText());
						break;
					case to_client_send_username_taken:
						JOptionPane.showMessageDialog(new JFrame(), "Username already taken", "Sign in", JOptionPane.ERROR_MESSAGE);
						ChessWindow.promptAndSendUsername();
						break;
					case to_client_propose_draw:
						break;
					case to_client_resign:
						break;
					case to_client_user_already_playing:
						JOptionPane.showMessageDialog(new JFrame(), "User currently in a game", "Invitation", JOptionPane.ERROR_MESSAGE);
						break;
					case to_client_move:
						System.out.println(m.getStartX()+" "+m.getStartY());
						System.out.println(ChessWindow.board[m.getStartX()][m.getStartY()].getPiece());
						ChessWindow.board[m.getStartX()][m.getStartY()].getPiece().moveTo(ChessWindow.board[m.getDestX()][m.getDestY()]);
						ChessWindow.flipTurn();
						break;
					default:
							break;
					}
			}
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
}


public class ChessWindow extends JFrame{

		public enum PieceType { pawn, queen, king, rook, bishop, knight };
		public static boolean isConnected = false;
		public static  boolean isConnected(){
			return isConnected;
		}
		public static void setConnected(boolean toSet){
			isConnected = toSet;
		}
		public static String username;
		public static String opponentUsername; // null means not playing at the moment
		public static String getUsername() {
			return username;
		}
		public static void setUsername(String user) {
			username = user;
		}
		public static String getOpponentUsername() {
			return opponentUsername;
		}
		public static void setOpponentUsername(String opponentUser) {
			opponentUsername = opponentUser;
		}
		
		public static void promptAndSendUsername(){
			// prompt for a username
			setUsername(JOptionPane.showInputDialog(new JFrame(), "Enter Username:", "Dialog",
			        JOptionPane.OK_OPTION) );
			setOpponentUsername(null);
			Message m = new Message(Message.MsgType.to_server_send_username);
			m.setMessageText(getUsername());
			Sender.addMessage(m);
			if (m == null){
				System.out.println("locul 3");
			}
		}
		
		public static Receiver rec;
		public static Sender sen;
		public static void main(String[] args) throws Exception{
			new ChessWindow();
			
			Socket socket = new Socket("localhost", 9090);
			promptAndSendUsername();
			
			setConnected(true);
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			
			rec = new Receiver(in);
			System.out.println("receive started");
			sen = new Sender(out);
			System.out.println("sender started");
			
			rec.start();
			sen.start();
		}
		
		public static JLabel selectLabel, chatLabel;
		public static JTextArea textArea1;
		public static JTextField textField1, userTextField;
		public static JButton inviteButton, drawButton, resignButton;
		public static JComboBox players;

		public static Cell[][] board;
		 BoardState boardState = new BoardState();
		
		
		public static synchronized void showMessage(String message){
			textArea1.append(message+"\n");
		}
		
		public ChessWindow() throws Exception{
			
			this.setSize(800,600);
			this.setLocationRelativeTo(null);	
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setTitle("Chess engine");
			
			JPanel thePanel = new JPanel(new BorderLayout());
			thePanel.setLayout(new GridBagLayout());
			
			
			JPanel gridPanel = new JPanel(new GridLayout(8,8));
			board = new Cell[8][8];
			
			
			JPanel buttonPanel = new JPanel();
			JPanel selectPanel = new JPanel(new BorderLayout());

			textArea1  = new JTextArea(20,20);
			textField1 = new JTextField(20);
			textField1.requestFocus();
			userTextField = new JTextField(20);
			inviteButton = new JButton("Invite");
			ListenForButton lForButton  = new ListenForButton();
			inviteButton.addActionListener(lForButton);
			drawButton = new JButton("Propose draw");
			resignButton = new JButton("Resign");
			String[] pl = { "mihai" , "ana", "ion" , "andrei", "vasea", "vasile"};
			players = new JComboBox(pl);
			selectLabel = new JLabel("Select player:");
			chatLabel = new JLabel("Chat");
			
			selectPanel.add(selectLabel, BorderLayout.NORTH);
			selectPanel.add(players, BorderLayout.CENTER);
			selectPanel.add(inviteButton, BorderLayout.SOUTH);
				
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
			buttonPanel.add(drawButton);
			buttonPanel.add(resignButton);
			
			JScrollPane scrollbar1 = new JScrollPane(textArea1, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			ListenForAction lForAction = new ListenForAction();
			textField1.addActionListener(lForAction);
			textArea1.setLineWrap(true);;
			textArea1.setWrapStyleWord(true);
			textArea1.setEnabled(false);

			// fill board
			for(int i=0;i<8;i++){
				for(int j=0;j<8;j++){
					board[i][j] = new Cell(i,j, 40);
					if (i == 1 || i == 6){
						String color = (i == 1 ? "black" : "white");
						board[i][j].setPiece(new Pawn(color));
					}
					if (i == 0 || i == 7){
						String color = (i == 0 ? "black" : "white");
						switch(j){
						case 0:
						case 7:
							board[i][j].setPiece(new Rook(color));
							break;
						case 1:
						case 6:
							board[i][j].setPiece(new Knight(color));
							break;
						case 2:
						case 5:
							board[i][j].setPiece(new Bishop(color));
							break;
						case 3:
							board[i][j].setPiece(new Queen(color));
							break;
						case 4:
							board[i][j].setPiece(new King(color));
							break;
						}
					}
					gridPanel.add(board[i][j]);
					board[i][j].addActionListener(board[i][j]);
				}
			}
		
			addComp(thePanel, scrollbar1, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE);
			addComp(thePanel, textField1, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE);
			addComp(thePanel, selectPanel, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE);
			addComp(thePanel,	buttonPanel, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE);
			addComp(thePanel, gridPanel, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE);
						
			this.add(thePanel);
			this.setVisible(true);
			
		}
		private class ListenForAction implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				Message m = new Message(Message.MsgType.to_server_send_message);
				m.setSourceUsername(getUsername());
				m.setMessageText(textField1.getText());
				Sender.addMessage(m);
				if (m == null){
					System.out.println("locul 4");
				}
				textField1.setText(null);
			}

			
			
		}
		private class ListenForButton implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("locul asta");
				Message m = new Message(Message.MsgType.to_server_invite_player);
				m.setSourceUsername(getUsername());
				m.setDestUsername((String)players.getSelectedItem());
				if (m == null){
					System.out.println("locul 5");
				}
				Sender.addMessage(m);
				
			}

			
			
		}
		
		private void addComp(JPanel thePanel, JComponent comp, int xPos, int yPos, int compWidth, int compHeight, int place, int stretch){
			
			GridBagConstraints gridConstraints = new GridBagConstraints();
			
			gridConstraints.gridx = xPos;
			gridConstraints.gridy = yPos;
			gridConstraints.gridwidth = compWidth;
			gridConstraints.gridheight = compHeight;
			gridConstraints.weightx = 1;
			gridConstraints.weighty = 1;
			gridConstraints.insets = new Insets(5,5,5,5);
			gridConstraints.anchor = place;
			gridConstraints.fill = stretch;
			
			thePanel.add(comp, gridConstraints);
			
		}
		
		class Cell extends JButton implements ActionListener{
			public Cell(int x, int y, int size){
				super();
				m_piece = null;
				m_x = x;
				m_y = y;
				m_size = size;
				setPreferredSize(new Dimension(size,size));
				updateBackground();
			}
			private int  m_x, m_y;
			private int m_size;
			private Piece m_piece;
			private String m_color;
			private boolean m_selected;
			
			public void updateBackground(){
				if ((m_x+m_y)%2 == 0){
					setBackground(Color.WHITE);
					setColor("white");
				} else {
					setBackground(Color.GRAY);
					setColor("gray");
				}
			}
			public void setPiece(Piece p){
				m_piece = p;
				if (p==null){
					setIcon(null);
				} else {
					setIcon(p.getIcon());
					p.setPosX(m_x);
					p.setPosY(m_y);
				}
			}
			public Piece getPiece(){
				return m_piece;
			}
			public int getPosX(){
				return m_x;
			}
			public int getPosY(){
				return m_y;
			}
			public boolean isEmpty(){
				return m_piece == null;
			}
			public String getColor(){
				return m_color;
			}
			public void setColor(String color){
				m_color = color;
			}
			public void setSelected(boolean toSet){
				m_selected = toSet;
			}
			public boolean isMoveValid(int dx, int dy){
				if (isEmpty()) return false;
				return getPiece().isMoveValid(dx, dy);
			}
			
			public boolean isUnderAttackBy(String opp){
				for(int i=0;i<8;i++){
					for(int j=0;j<8;j++){
						Piece p = board[i][j].getPiece();
						if (p!=null){
							if (p.getColor().equals(opp) && p.isMoveLegal(m_x, m_y)){
								return true;
							}
						}
					}
				}
				return false;
			}
			return false;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Cell cell = (Cell) e.getSource();
			if (cell != this)
				return;
			if (boardState.isSelected()) {
				// cell is the destination cell
				Cell source = boardState.getSelectedCell();
				Cell dest = this;
				if (source.isMoveValid(getPosX(), getPosY())) {

					Piece piece = source.getPiece();
					piece.moveTo(dest);
					boardState.flipTurn();
					boardState.setSelected(false);
				} else {

					Piece destPiece = dest.getPiece();
					if (destPiece != null) {
						if (destPiece.getColor().equals(boardState.getTurn())) {
							boardState.setSelected(true);
							boardState.setSelectedCell(dest);
							boardState.setSelectedPiece(destPiece);
						} else {
							// nothing here
						}
					}
				}
				return null;
			}
			public void postMoveVerification(){
				// check check positions
				Piece king = boardState.getKing("white");
				Cell cell = board[king.getPosX()][king.getPosY()];
				if (cell.isUnderAttackBy("black")){
					cell.setBackground(Color.RED);
				} else {
					cell.updateBackground();
				}
				king = boardState.getKing("black");
				cell = board[king.getPosX()][king.getPosY()];
				if (cell.isUnderAttackBy("white")){
					cell.setBackground(Color.RED);
				} else {
					cell.updateBackground();
				}
				
				
			}
			
		}
	}

	class Player {
		public String color;

		public Player(String color) {
			this.color = color;
		}
		class Pawn extends Piece{
			public Pawn(String color){
				super(color);
				m_type = PieceType.pawn;
				m_pieceName = "pawn";
				Image img = Toolkit.getDefaultToolkit().getImage("icons/"+color+"_"+m_pieceName+".png");
				img = img.getScaledInstance( 35, 35,  java.awt.Image.SCALE_SMOOTH ) ;
				m_icon = new ImageIcon(img);
			}
			public boolean isMoveValid(int xx , int yy){
				boolean answer = isMoveLegal(xx, yy);
				if (answer == false)
					return false;
				return super.isMoveValid(xx, yy);
			}
			public boolean isMoveLegal(int xx, int yy){
				int x = m_x;
				int y = m_y;
				if(getColor().equals("white")){
					if (x == 0) return false;
					if (x == 6 && xx == 4 && y == yy){
						if (board[xx][yy].getPiece() == null && board[x-1][yy].getPiece()==null)
							return true;
					}
					if (y == yy && x - 1 == xx && board[xx][yy].getPiece()==null){
						return true;
					}
					Piece ad;
					if (x - 1 == xx && (y - 1 == yy || y + 1 == yy) && 
							(ad = board[xx][yy].getPiece())!=null && ad.getColor()=="black"){
						return true;
					}
					return  false;
				}else {
					if (x == 7) return false;
					if (x == 1 && xx == 3 && y == yy){
						if (board[xx][yy].getPiece() == null  && board[x+1][yy].getPiece()==null)
							return true;
					}
					if (y == yy && x + 1 == xx && board[xx][yy].getPiece()==null){
						return true;
					}
					Piece ad;
					if (x + 1 == xx && (y - 1 == yy || y + 1 == yy) && 
							(ad = board[xx][yy].getPiece())!=null && ad.getColor()=="white"){
						return true;
					}
					return  false;
				}
			}
		}
		
		class Rook extends Piece{
			public Rook(String color){
				super(color);
				m_type = PieceType.rook;
				m_pieceName = "rook";
				Image img = Toolkit.getDefaultToolkit().getImage("icons/"+color+"_"+m_pieceName+".png");
				img = img.getScaledInstance( 35, 35,  java.awt.Image.SCALE_SMOOTH ) ;
				m_icon = new ImageIcon(img);
			}
			public boolean isMoveValid(int xx , int yy){
				boolean answer = isMoveLegal(xx, yy);
				if (answer == false)
					return false;
				return super.isMoveValid(xx, yy);
			}
			public boolean isMoveLegal(int xx, int yy){
				int x = m_x;
				int y = m_y;
				if (x == xx){
					for(int y1 = Math.min(y,yy)+1;y1<Math.max(y, yy);y1++){
						if(board[x][y1].getPiece()!=null) return super.isMoveValid(xx, yy);
					}
					if(board[xx][yy].getPiece()!=null && 
							board[xx][yy].getPiece().getColor().equals(getColor()) )
						return false;
					return super.isMoveValid(xx, yy);
				}
				if (y == yy){
					for(int x1 = Math.min(x,xx)+1;x1<Math.max(x, xx);x1++){
						if(board[x1][y].getPiece()!=null) return false;
					}
					if(board[xx][yy].getPiece()!=null && 
							board[xx][yy].getPiece().getColor().equals(getColor()) )
						return false;
					return super.isMoveValid(xx, yy);
				}
				return false;
			}
		}
		class Bishop extends Piece{
			public Bishop(String color){
				super(color);
				m_type = PieceType.bishop;
				m_pieceName = "bishop";
				Image img = Toolkit.getDefaultToolkit().getImage("icons/"+color+"_"+m_pieceName+".png");
				img = img.getScaledInstance( 35, 35,  java.awt.Image.SCALE_SMOOTH ) ;
				m_icon = new ImageIcon(img);
			}
			public boolean isMoveValid(int xx , int yy){
				boolean answer = isMoveLegal(xx, yy);
				if (answer == false)
					return false;
				return super.isMoveValid(xx, yy);
			}
			public boolean isMoveLegal(int xx, int yy){
				int x = m_x;
				int y = m_y;
				Piece p = board[xx][yy].getPiece();
				if (p!= null && p.getColor().equals(getColor()))
					return false;
				if (Math.abs(x-xx) != Math.abs(y-yy))
					return false;
				int dx = (x > xx ? -1 : 1);
				int dy = (y > yy ? -1 : 1);
				x += dx;
				y += dy;
				while(!(x==xx && y==yy)){
					if(board[x][y].getPiece()!=null) return false;
					x += dx;
					y += dy;
				}
				return super.isMoveValid(xx, yy);
			}
		}
		class King extends Piece{
			public boolean castlingDone;
			public King(String color){
				super(color);
				castlingDone = false;
				m_type = PieceType.king;
				m_pieceName = "king";
				Image img = Toolkit.getDefaultToolkit().getImage("icons/"+color+"_"+m_pieceName+".png");
				img = img.getScaledInstance( 35, 35,  java.awt.Image.SCALE_SMOOTH ) ;
				m_icon = new ImageIcon(img);
			}
			public boolean isMoveValid(int xx, int yy){
				boolean answer = isMoveLegal(xx, yy);
				if (answer == false)
					return false;
				return super.isMoveValid(xx, yy);
			}
			public boolean isMoveLegal(int xx, int yy){
				int x = m_x;
				int y = m_y;
				
				if (!pieceMoved && x == xx/*same line*/){
					// try casling
					Piece rock1 = board[x][0].getPiece();
					Piece rock2 = board[x][7].getPiece();
					if(!rock1.hasPieceMoved()){
						if (Math.abs(y-yy) == 2 && yy < y){
							boolean castlePossible1 = true;
							for(int y1=0;y1<=y;y1++)
								if (true/*board[x][y1].isUnderAttackBy(getRevColor())*/)
									castlePossible1 = false;
							if (castlePossible1) {
								//rock1.moveTo(board[x][yy+1]);
								return true;
							}
						}
					}
					if(!rock2.hasPieceMoved()){
						if (Math.abs(y-yy) == 2 && y < yy){
							boolean castlePossible1 = true;
							for(int y1=y;y1<=yy;y1++)
								if (true/*board[x][y1].isUnderAttackBy(getRevColor())*/)
									castlePossible1 = false;
							if (castlePossible1) {
								//rock2.moveTo(board[x][yy-1]);
								return true;
							}
						}
					}
				}
				if(Math.abs(x - xx) <= 1 && Math.abs(y - yy) <= 1){
					if (!(board[xx][yy].getPiece()!=null && board[xx][yy].getPiece().getColor().equals(getColor())))
								return true;
				}
				return false;
			}
		}
		
		class Knight extends Piece{
			public Knight(String color){
				super(color);
				m_type = PieceType.knight;
				m_pieceName = "knight";
				Image img = Toolkit.getDefaultToolkit().getImage("icons/"+color+"_"+m_pieceName+".png");
				img = img.getScaledInstance( 35, 35,  java.awt.Image.SCALE_SMOOTH ) ;
				m_icon = new ImageIcon(img);
			}
			public boolean isMoveValid(int xx, int yy){
				boolean answer = isMoveLegal(xx, yy);
				if (answer == false)
					return false;
				return super.isMoveValid(xx, yy);
			}
			public boolean isMoveLegal(int xx, int yy){
				int x = m_x;
				int y = m_y;
				Piece p = board[xx][yy].getPiece();
				if (p != null && p.getColor().equals(getColor())){
					return false;
				}
				int difx = Math.abs(x - xx);
				int dify = Math.abs(y - yy);
				if ((difx == 1 && dify == 2) || (difx == 2 && dify == 1))
					return super.isMoveValid(xx, yy);
				return false;
			}
		}
		class Queen extends Piece{
			public Queen(String color){
				super(color);
				m_type = PieceType.queen;
				m_pieceName = "queen";
				Image img = Toolkit.getDefaultToolkit().getImage("icons/"+color+"_"+m_pieceName+".png");
				img = img.getScaledInstance( 35, 35,  java.awt.Image.SCALE_SMOOTH ) ;
				m_icon = new ImageIcon(img);
			}
			public boolean isMoveValid(int xx , int yy){
				boolean answer = isMoveLegal(xx, yy);
				if (answer == false)
					return false;
				return super.isMoveValid(xx, yy);
			}
			public boolean isMoveLegal(int xx, int yy){
				Rook rook = new Rook(getColor());
				rook.setPosX(m_x);
				rook.setPosY(m_y);
				Bishop bishop = new Bishop(getColor());
				bishop.setPosX(m_x);
				bishop.setPosY(m_y);
				boolean answer =  rook.isMoveLegal(xx,yy) || bishop.isMoveLegal(xx, yy);
				board[m_x][m_y].setPiece(this);
				return answer;
			}
		}
		
	
}
