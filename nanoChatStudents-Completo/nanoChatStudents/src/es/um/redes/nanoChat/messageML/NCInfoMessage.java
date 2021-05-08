package es.um.redes.nanoChat.messageML;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NCInfoMessage extends NCMessage {
	
	private String name;
	private Set<String> members;
	private long timeLastMessage;
	
	//Constantes asociadas a las marcas específicas de este tipo de mensaje
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
	public NCInfoMessage(byte opcode, String name, Set<String> members, long timeLastMessage) {
		this.opcode = opcode;
		this.name = name;
		this.members = members;
		this.timeLastMessage = timeLastMessage;
	}

	@Override
	//Pasamos los campos del mensaje a la codificación correcta en lenguaje de marcas
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<"+MESSAGE_MARK+">"+END_LINE);
		sb.append("<"+OPERATION_MARK+">"+opcodeToString(opcode)+"</"+OPERATION_MARK+">"+END_LINE); //Construimos el campo
		sb.append("<"+NAME_MARK+">"+name+"</"+NAME_MARK+">"+END_LINE);
		sb.append("<"+USERS_MARK+">"+END_LINE);
		for (String nick : this.members) {
			sb.append("<"+NICK_MARK+">"+nick+"</"+NICK_MARK+">"+END_LINE);
		}
		sb.append("</"+USERS_MARK+">"+END_LINE);
		sb.append("<"+TIME_MARK+">"+timeLastMessage+"</"+TIME_MARK+">"+END_LINE);
		sb.append("</"+MESSAGE_MARK+">"+END_LINE);

		return sb.toString(); //Se obtiene el mensaje

	}


	//Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
	public static NCInfoMessage readFromString(byte code, String message) {
		Set<String> membersSet = new HashSet<>();

		// Tienen que estar los campos porque el mensaje es de tipo RoomMessage
		Pattern pat_name = Pattern.compile(RE_NAME);
		Matcher mat_name = pat_name.matcher(message);
		Pattern pat_users = Pattern.compile(RE_USERS, Pattern.DOTALL);	//Pattern.DOTALL para que la expresión regular también incluya los \n
		Matcher mat_users = pat_users.matcher(message);
		Pattern pat_time = Pattern.compile(RE_TIME);
		Matcher mat_time = pat_time.matcher(message);
		
		String name = null;
		
		if (mat_name.find()) {
			// Name found
			name = mat_name.group(1);
		} else {
			System.out.println("Error en NCInfoMessage: no se ha encontrado parametro.");
			return null;
		}
		
		String users = null;
		if (mat_users.find()) {
			users = mat_users.group(1);
		} else {
			System.out.println("Error en NCInfoMessage: no se ha encontrado parametro.");
			return null;
		}
		Pattern pat_nick = Pattern.compile(RE_NICK);
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
		
		return new NCInfoMessage(code, name, membersSet, time);
	}


	//Devolvemos el nombre contenido en el mensaje
	public String getName() {
		return name;
	}

	//Devuelve un conjunto no modificable
	public Set<String> getMembers() {
		return Collections.unmodifiableSet(members);
	}

	public long getTimeLastMessage() {
		return timeLastMessage;
	}
	
	
	
}
