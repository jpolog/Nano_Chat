package es.um.redes.nanoChat.server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;
import es.um.redes.nanoChat.server.roomManager.NCRoomManager;
import es.um.redes.nanoChat.server.roomManager.Room;

/**
 * Esta clase contiene el estado general del servidor (sin la lógica relacionada con cada sala particular)
 */
class NCServerManager {

	//Primera habitación del servidor
	final static byte INITIAL_ROOM = 'A';
	final static String ROOM_PREFIX = "Room";
	//Siguiente habitación que se creará
	byte nextRoom;
	//Usuarios registrados en el servidor
	private Set<String> users = new HashSet<String>();
	//Habitaciones actuales asociadas a sus correspondientes RoomManagers
	private Map<String,NCRoomManager> rooms = new HashMap<String,NCRoomManager>();

	NCServerManager() {
		nextRoom = INITIAL_ROOM;
	}

	//Método para registrar un RoomManager
	public void registerRoomManager(NCRoomManager rm) {
		//TODO Dar soporte para que pueda haber más de una sala en el servidor
		String roomName = ROOM_PREFIX + (char) nextRoom;
		rm.setRoomName(roomName);
		rooms.put(roomName, rm);
		nextRoom+=1;	//Actualizar el valor
	}

	//Devuelve la descripción de las salas existentes
	public synchronized List<NCRoomDescription> getRoomList() {
		//TODO Pregunta a cada RoomManager cuál es la descripción actual de su sala
		List<NCRoomDescription> roomList = new ArrayList<NCRoomDescription>();
		for(String room : this.rooms.keySet()) {
			//TODO Añade la información al ArrayList
			roomList.add(this.rooms.get(room).getDescription());		
		}
		
		return roomList;
	}
	
	//Devuelve la descripción de la sala actual
	public synchronized NCRoomDescription getRoomInfo(String room) {
		//TODO Pregunta a cada RoomManager cuál es la descripción actual de su sala
		NCRoomDescription roomInfo = this.rooms.get(room).getDescription();
		return roomInfo;
	}


	//Intenta registrar al usuario en el servidor.
	public synchronized boolean addUser(String user) {
		boolean resp = false;
		//TODO Devuelve true si no hay otro usuario con su nombre
		if(!users.contains(user)) {
			users.add(user);
			resp = true;
			System.out.println("users updated, values: " + users);
		}
		//TODO Devuelve false si ya hay un usuario con su nombre
		return resp;
	}

	//Elimina al usuario del servidor
	public synchronized void removeUser(String user) {
		//TODO Elimina al usuario del servidor
		users.remove(user);
		System.out.println("users updated, values: " + users);
	}

	//Un usuario solicita acceso para entrar a una sala y registrar su conexión en ella
	public synchronized NCRoomManager enterRoom(String u, String room, Socket s) {
		//TODO Verificamos si la sala existe
		boolean exists = rooms.containsKey(room);
		//TODO Decidimos qué hacer si la sala no existe (devolver error O crear la sala)
		if(!exists) {
			System.out.println("* La sala solicitada no existe");
			return null;
		}
		//TODO Si la sala existe y si es aceptado en la sala entonces devolvemos el RoomManager de la sala
		rooms.get(room).registerUser(u, s);
		return rooms.get(room);
	}

	//Un usuario deja la sala en la que estaba
	public synchronized void leaveRoom(String u, String room) {
		//TODO Verificamos si la sala existe
		boolean exists = rooms.containsKey(room);
		//TODO Si la sala existe sacamos al usuario de la sala
		if(exists) {
			rooms.get(room).removeUser(u);
		}
		//TODO Decidir qué hacer si la sala se queda vacía
		//No se hace nada, simplemente se queda vacía
		
	}
	
	//Un usuario le cambia el nombre a la sala
	public synchronized boolean roomRename(String room, String newName) {
		if (rooms.containsKey(newName)) {
			return false;
		}else {
			NCRoomManager modified = rooms.get(room);
			modified.setRoomName(newName);
			rooms.remove(room);
			rooms.put(newName, modified);
			return true;
		}
		
		
	}

	public void sendMessage(byte opcode, String room, String user, String text) throws IOException {
		rooms.get(room).broadcastMessage(opcode, user, text);
	}
	
	public void sendPrivateMessage(String room, String user, String receiver, String text) throws IOException {
		((Room)rooms.get(room)).sendPrivateMessage(user, receiver, text);	
	}
	
	public void setTime(String room, long milliseconds) {
		((Room)rooms.get(room)).setTime(milliseconds);
	}
	
}
