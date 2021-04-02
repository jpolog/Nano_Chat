package es.um.redes.nanoChat.messageML;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SendRoomList extends NCMessage {
	private static final byte OPCODE = 10;
	private byte opcode;
	private List<NCRoomDescription> descRoomList;
	
	
	private static final String RE_ROOM = "<room>(.*?)</room>";
	private static final String ROOM_MARK = "room";
	private static final String RE_USERS = "<users>(.*?)</users>";
	private static final String USERS_MARK = "users";
	private static final String RE_NICK = "<nick>(.*?)</nick>";
	private static final String NICK_MARK = "nick";
	
	

	public SendRoomList(List<NCRoomDescription> list) {
		this.opcode = OPCODE;
		this.descRoomList = Collections.unmodifiableList(list); //Colección no modificable
	}
	

	@Override
	public String toEncodedString() {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("<"+MESSAGE_MARK+">"+END_LINE);
		sb.append("<"+OPERATION_MARK+">"+this.opcode+"</"+OPERATION_MARK+">"+END_LINE);
		for(NCRoomDescription room : this.descRoomList) {
			sb.append("<"+ROOM_MARK+">"+room.roomName+"</"+ROOM_MARK+">"+END_LINE);
			sb.append("<"+USERS_MARK+">"+END_LINE);
			for(String nick : room.members) {
				sb.append("<"+NICK_MARK+">"+nick+"</"+NICK_MARK+">"+END_LINE);
			}
			sb.append("</"+USERS_MARK+">"+END_LINE);
		}
		
		sb.append("</"+MESSAGE_MARK+">"+END_LINE);
		
		String message = sb.toString();
		return message;
	}
	
	public static SendRoomList readFromString(String message) {
		List<NCRoomDescription> list = new ArrayList<>();

		// Tienen que estar los campos porque el mensaje es de tipo RoomMessage
		Pattern pat_room = Pattern.compile(RE_ROOM);
		Matcher mat_room = pat_room.matcher(message);
		Pattern pat_users = Pattern.compile(RE_USERS);
		Matcher mat_users = pat_users.matcher(message);
		Pattern pat_nick = Pattern.compile(RE_NICK);
		Matcher mat_nick;
		String found_room = null;
		
		while (mat_room.find()) {
			NCRoomDescription roomDesc;
			found_room = mat_room.group(1);
			if(mat_users.find()) {
				mat_nick = pat_nick.matcher(mat_users.group(1));
				Set<String> nkSet = new HashSet<>();
				while(mat_nick.find()) {
					nkSet.add(mat_nick.group(1));
				}
				roomDesc = new NCRoomDescription(found_room, nkSet, 0);
				list.add(roomDesc);
			}
			
		} 
		return new SendRoomList(list);
	}
	
	public List<NCRoomDescription> getDescRoomList(){
		return Collections.unmodifiableList(this.descRoomList);
	}
	
}
