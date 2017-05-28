package chess;

import java.io.Serializable;

public class Message implements Serializable{
			public enum MsgType { send_invitation, send_message, send_username,
				propose_draw, resign, send_move
			};
			MsgType type;
			String s;
			String invitDestUser;
			public int startX, startY, destX, destY;
			
		
			public Message(String s){
				this.s = s;
				this.type = MsgType.send_message;
			}
			public Message(String s, MsgType t){
				this.s = s;
				this.type = t;
			}

			public MsgType getType(){
				return type;
			}
			public void setType(MsgType t){
				type = t;
			}
			public String getStr(){
				return s;
			}
			public void setStr(String s){
				this.s = s; 
			}
			public String getInvitDestUser() {
				return invitDestUser;
			}
			public void setInvitDestUser(String invitDestUser) {
				this.invitDestUser = invitDestUser;
			}
			
			
}
