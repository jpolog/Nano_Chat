package es.um.redes.nanoChat.server.roomManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;

import es.um.redes.nanoChat.messageML.NCMessage;
import es.um.redes.nanoChat.messageML.NCNickMessage;

public class Room extends NCRoomManager {
	
	private HashMap<String,Socket> usersMap;
	private LinkedList<String> msgHistory;
	private long timeLastMessage;
	
	

	public Room() {
		super();
		this.usersMap = new HashMap<String,Socket>();
		this.msgHistory = new LinkedList<String>();
		this.timeLastMessage = 0;
	}

	@Override
	public boolean registerUser(String u, Socket s) {
		// TODO Auto-generated method stub
		if(this.usersMap.put(u,s)!=null) {
			return true;
		} return false;
	}

	@Override
	public void broadcastMessage(byte opcode, String u, String message) throws IOException {
		for(String user : this.usersMap.keySet()) {
			DataOutputStream dos = new DataOutputStream(this.usersMap.get(user).getOutputStream());
			
			
			switch (opcode) {
			case NCMessage.OP_TEXT_MESSAGE_IN: {
				NCMessage reply = NCMessage.makeNickMessage(NCMessage.OP_TEXT_MESSAGE_IN, u, message);
				dos.writeUTF(((NCNickMessage) reply).toEncodedString());
				break;
			}
			case NCMessage.OP_ENTER_ROOM_NOTIFICATION: {
				NCMessage reply = NCMessage.makeNickMessage(NCMessage.OP_ENTER_ROOM_NOTIFICATION, u, message);
				dos.writeUTF(((NCNickMessage) reply).toEncodedString());
				break;
			}
			case NCMessage.OP_EXIT_ROOM_NOTIFICATION: {
				NCMessage reply = NCMessage.makeNickMessage(NCMessage.OP_EXIT_ROOM_NOTIFICATION, u, message);
				dos.writeUTF(((NCNickMessage) reply).toEncodedString());
				break;
			}
//AÑADIR UN DEFAULT??????????
			}
				
		}
	}
	
	
	public void sendPrivateMessage(String u, String receiver, String message) throws IOException {
		DataOutputStream dos = new DataOutputStream(this.usersMap.get(receiver).getOutputStream());
		NCMessage reply = NCMessage.makeNickMessage(NCMessage.OP_PRIVATE_TEXT_MESSAGE_IN, u, message);
		dos.writeUTF(((NCNickMessage)reply).toEncodedString());
	}
	

	@Override
	public void removeUser(String u) {
		this.usersMap.remove(u);
		
	}

	@Override
	public void setRoomName(String roomName) {
		this.roomName = roomName;
		
	}

	@Override
	public NCRoomDescription getDescription() {
		return new NCRoomDescription(this.roomName, this.usersMap.keySet(), timeLastMessage);
	}

	@Override
	public int usersInRoom() {
		return this.usersMap.size();
	}

}
