package es.um.redes.nanoChat.messageML;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RoomRequest extends NCMessage{

	private static final byte OPCODE = 4;
	private String room;
	
	private static final String RE_ROOM = "<room>(.*?)</room>";
	private static final String ROOM_MARK = "room";
	
	public RoomRequest(String room) {
		this.opcode = OPCODE;
		this.room = room;
	}
	
	
	@Override
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<"+MESSAGE_MARK+">"+END_LINE);
		sb.append("<"+OPERATION_MARK+">"+this.opcode+"</"+OPERATION_MARK+">"+END_LINE);
		sb.append("<"+ROOM_MARK+">"+this.room+"</"+ROOM_MARK+">"+END_LINE);
		sb.append("</"+MESSAGE_MARK+">"+END_LINE);
		
		String message = sb.toString();
		return message;
	}
	
	public static RoomRequest readFromString(String message) {
		String found_room = null;

		// Tienen que estar los campos porque el mensaje es de tipo RoomMessage
		Pattern pat_room = Pattern.compile(RE_ROOM);
		Matcher mat_room = pat_room.matcher(message);
		if (mat_room.find()) {
			// Name found
			found_room = mat_room.group(1);
		} else {
			System.out.println("Error en RegisterNick: no se ha encontrado parametro.");
			return null;
		}
		return new RoomRequest(found_room);
	}
	
	public String getRoom() {
		return this.room;
	}
	
}
