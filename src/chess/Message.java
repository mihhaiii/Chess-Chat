package chess;

import java.io.Serializable;
import java.util.HashMap;

public class Message implements Serializable{
			public enum MsgType{
				to_server_send_message,
				to_server_send_username,
				to_server_invite_player,
				to_server_resign,
				to_server_propose_draw,
				to_server_confirm_invitation,
				to_server_decline_invitation,
				to_server_confirm_draw,
				to_server_decline_draw,
				to_server_move,
				to_client_send_invitation,
				to_client_send_response_declined_to_invitation,
				to_client_send_response_accepted_to_invitation,
				to_client_send_message,
				to_client_send_username_taken,
				to_client_propose_draw,
				to_client_resign,
				to_client_user_already_playing,
				to_client_move,
				to_client_new_user,
				to_client_confirm_draw,
				to_client_decline_draw,
			}
			private static final HashMap<MsgType, String> map;
			static
			{
				map = new HashMap<>();
				map.put(MsgType.to_server_send_message,"to_server_send_message");
				map.put(MsgType.to_server_send_username,"to_server_send_username");
				map.put(MsgType.to_server_invite_player,"to_server_invite_player");
				map.put(MsgType.to_server_resign,"to_server_resign");
				map.put(MsgType.to_server_propose_draw,"to_server_propose_draw");
				map.put(MsgType.to_server_confirm_invitation,"to_server_confirm_invitation");
				map.put(MsgType.to_server_decline_invitation,"to_server_decline_invitation");
				map.put(MsgType.to_server_confirm_draw,"to_server_confirm_draw");
				map.put(MsgType.to_server_decline_draw,"to_server_decline_draw");
				map.put(MsgType.to_server_move,"to_server_move");
				map.put(MsgType.to_client_send_invitation,"to_client_send_invitation");
				map.put(MsgType.to_client_send_response_declined_to_invitation,"to_client_send_response_declined_to_invitation");
				map.put(MsgType.to_client_send_response_accepted_to_invitation,"to_client_send_response_accepted_to_invitation");
				map.put(MsgType.to_client_send_message,"to_client_send_message");
				map.put(MsgType.to_client_send_username_taken,"to_client_send_username_taken");
				map.put(MsgType.to_client_propose_draw,"to_client_propose_draw");
				map.put(MsgType.to_client_resign,"to_client_resign");
				map.put(MsgType.to_client_user_already_playing,"to_client_user_already_playing");
				map.put(MsgType.to_client_move,"to_client_move");
			}
			
			MsgType type;
			String messageText;
			String sourceUsername;
			String destUsername;
			public int startX, startY, destX, destY;
			
			public String toString(){
				if (getType() == null) return sourceUsername+messageText;
				return map.get(getType());
			}
		
			public Message(MsgType type){
				this.type = type;
			}
			public MsgType getType(){
				return type;
			}
			public void setType(MsgType t){
				type = t;
			}
			public String getMessageText() {
				return messageText;
			}
			public void setMessageText(String messageText) {
				this.messageText = messageText;
			}
			public String getSourceUsername() {
				return sourceUsername;
			}
			public void setSourceUsername(String sourceUsername) {
				this.sourceUsername = sourceUsername;
			}
			public String getDestUsername() {
				return destUsername;
			}
			public void setDestUsername(String destUsername) {
				this.destUsername = destUsername;
			}
			public int getStartX() {
				return startX;
			}
			public void setStartX(int startX) {
				this.startX = startX;
			}
			public int getStartY() {
				return startY;
			}
			public void setStartY(int startY) {
				this.startY = startY;
			}
			public int getDestX() {
				return destX;
			}
			public void setDestX(int destX) {
				this.destX = destX;
			}
			public int getDestY() {
				return destY;
			}
			public void setDestY(int destY) {
				this.destY = destY;
			}
			public void flipUsers(){
				String s = sourceUsername;
				sourceUsername = destUsername;
				destUsername = s;
			}
			
			
			
}
