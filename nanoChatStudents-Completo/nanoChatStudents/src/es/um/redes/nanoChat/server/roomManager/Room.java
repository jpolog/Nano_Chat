package es.um.redes.nanoChat.server.roomManager;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;

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
	public void broadcastMessage(String u, String message) throws IOException {
		// TODO Auto-generated method stub
		
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
