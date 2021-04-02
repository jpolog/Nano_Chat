package es.um.redes.nanoChat.messageML;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterNick extends NCMessage{
	
	private static final byte OPCODE = 1;
	private String nick;
	
	private static final String RE_NICK = "<nick>(.*?)</nick>";
	private static final String NICK_MARK = "nick";
	
	

	public RegisterNick(String nick) {
		this.opcode = OPCODE;
		this.nick = nick;
		
	}
	

	@Override
	public String toEncodedString() {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("<"+MESSAGE_MARK+">"+END_LINE);
		sb.append("<"+OPERATION_MARK+">"+this.opcode+"</"+OPERATION_MARK+">"+END_LINE);
		sb.append("<"+NICK_MARK+">"+this.nick+"</"+NICK_MARK+">"+END_LINE);
		sb.append("</"+MESSAGE_MARK+">"+END_LINE);
		
		String message = sb.toString();
		return message;
	}
	
	public static RegisterNick readFromString(String message) {
		String found_nick = null;

		// Tienen que estar los campos porque el mensaje es de tipo RoomMessage
		Pattern pat_nick = Pattern.compile(RE_NICK);
		Matcher mat_nick = pat_nick.matcher(message);
		if (mat_nick.find()) {
			// Name found
			found_nick = mat_nick.group(1);
		} else {
			System.out.println("Error en RegisterNick: no se ha encontrado parametro.");
			return null;
		}
		return new RegisterNick(found_nick);
	}
	
	public String getNick() {
		return this.nick;
	}
	

}
