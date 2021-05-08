package es.um.redes.nanoChat.messageML;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

public class NCInfoListMessage extends NCMessage {
	
	private List<NCRoomDescription> roomList;
	
	//Constantes asociadas a las marcas específicas de este tipo de mensaje
	private static final String RE_LIST = "<list>(.*?)</list>";
	private static final String LIST_MARK = "list";
	private static final String RE_ROOM = "<room>(.*?)</room>";
	private static final String ROOM_MARK = "room";
	private static final String RE_NAME = "<name>(.*?)</name>";
	private static final String NAME_MARK = "name";
	private static final String RE_USERS = "<users>(.*?)</users>";
	private static final String USERS_MARK = "users";
	private static final String RE_NICK = "<nick>(.*?)</nick>";
	private static final String NICK_MARK = "nick";
	private static final String RE_TIME = "<time>(.*?)</time>";
	private static final String TIME_MARK = "time";


	/**
	 * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
	 */
	public NCInfoListMessage(byte opcode, List<NCRoomDescription> roomList) {
		this.opcode = opcode;
		this.roomList = roomList;
	}

	@Override
	//Pasamos los campos del mensaje a la codificación correcta en lenguaje de marcas
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<"+MESSAGE_MARK+">"+END_LINE);
		sb.append("<"+OPERATION_MARK+">"+opcodeToString(opcode)+"</"+OPERATION_MARK+">"+END_LINE); //Construimos el campo
		sb.append("<"+LIST_MARK+">" + END_LINE);
	
		for (NCRoomDescription roomDescription : this.roomList) {
			sb.append("<"+ROOM_MARK+">"+END_LINE);
			sb.append("<"+NAME_MARK+">"+roomDescription.roomName+"</"+NAME_MARK+">"+END_LINE);
			sb.append("<"+USERS_MARK+">"+END_LINE);
			for (String nick : roomDescription.members) {
				sb.append("<"+NICK_MARK+">"+nick+"</"+NICK_MARK+">"+END_LINE);
			}
			sb.append("</"+USERS_MARK+">"+END_LINE);
			sb.append("<"+TIME_MARK+">"+roomDescription.timeLastMessage+"</"+TIME_MARK+">"+END_LINE);
			sb.append("</"+ROOM_MARK+">"+END_LINE);
			
		}	
	
		
		
		sb.append("</"+LIST_MARK+">"+END_LINE);
		sb.append("</"+MESSAGE_MARK+">"+END_LINE);

		return sb.toString(); //Se obtiene el mensaje

	}


	//Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
	public static NCInfoListMessage readFromString(byte code, String message) {
		List<NCRoomDescription> roomList = new LinkedList<>();
		Set<String> membersSet = new HashSet<>();
		NCRoomDescription roomDescription;
		
		// Tienen que estar los campos porque el mensaje es de tipo RoomMessage
		Pattern pat_list = Pattern.compile(RE_LIST, Pattern.DOTALL);
		Matcher mat_list = pat_list.matcher(message);
		

		
		String room_list = null;
		if (mat_list.find()) {
			room_list = mat_list.group(1);
		} else {
			System.out.println("Error en NCInfoListMessage.");
			return null;
		}
		
		Pattern pat_room = Pattern.compile(RE_ROOM, Pattern.DOTALL);
		Matcher mat_room = pat_room.matcher(room_list);
		

		
		Pattern pat_name = Pattern.compile(RE_NAME);
		Pattern pat_users = Pattern.compile(RE_USERS, Pattern.DOTALL);
		Pattern pat_time = Pattern.compile(RE_TIME);
		Pattern pat_nick = Pattern.compile(RE_NICK);
		
		//Se comprueba si hay al menos una etiqueta <room> (siempre debe haber al menos una sala)
		if (mat_room.find()) {
			membersSet = new HashSet<>();
			
			String room_description = mat_room.group(1);
			Matcher mat_name = pat_name.matcher(room_description);
			Matcher mat_users = pat_users.matcher(room_description);
			Matcher mat_time = pat_time.matcher(room_description);
			
			String name = null;
			if (mat_name.find()) {
				// Name found
				name = mat_name.group(1);
			} else {
				System.out.println("Error en NCInfoListMessage: no se ha encontrado parametro.");
				return null;
			}
			
			String users = null;
			if (mat_users.find()) {
				users = mat_users.group(1);
			} else {
				System.out.println("Error en NCInfoMessage: no se ha encontrado parametro.");
				return null;
			}

			Matcher mat_nick = pat_nick.matcher(users);
			while (mat_nick.find()) {
				membersSet.add(mat_nick.group(1));	
			} 
		
			long time = 0;
			if (mat_time.find()) {
				time = Long.parseLong(mat_time.group(1));
			} else {
				System.out.println("Error en NCInfoMessage: no se ha encontrado parametro.");
				return null;
			}
		
			roomDescription = new NCRoomDescription(name, membersSet, time);
			roomList.add(roomDescription);
			
		} else {
			System.out.println("Error en NCInfoListMessage: no se ha encontrado parametro.");
			return null;
		}
		
		//Se procesa el resto de room
		while(mat_room.find()) {
			
			membersSet = new HashSet<>();
			
			String room_description = mat_room.group(1);
			Matcher mat_name = pat_name.matcher(room_description);
			Matcher mat_users = pat_users.matcher(room_description);
			Matcher mat_time = pat_time.matcher(room_description);
			
			String name = null;
			if (mat_name.find()) {
				// Name found
				name = mat_name.group(1);
			} else {
				System.out.println("Error en NCInfoListMessage: no se ha encontrado parametro.");
				return null;
			}
			
			String users = null;
			if (mat_users.find()) {
				users = mat_users.group(1);
			} else {
				System.out.println("Error en NCInfoMessage: no se ha encontrado parametro.");
				return null;
			}

			Matcher mat_nick = pat_nick.matcher(users);
			while (mat_nick.find()) {
				membersSet.add(mat_nick.group(1));	
			} 
		
			long time = 0;
			if (mat_time.find()) {
				time = Long.parseLong(mat_time.group(1));
			} else {
				System.out.println("Error en NCInfoMessage: no se ha encontrado parametro.");
				return null;
			}
		
			roomDescription = new NCRoomDescription(name, membersSet, time);
			roomList.add(roomDescription);
		}
		
		return new NCInfoListMessage(code, roomList);
	}

	
	public List<NCRoomDescription> getList() {
		return Collections.unmodifiableList(roomList);
	}

	
}
