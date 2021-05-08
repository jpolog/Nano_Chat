package es.um.redes.nanoChat.messageML;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NCNickMessage extends NCMessage{
	private String nick;
	private String text;
	
	
	//Constantes asociadas a las marcas específicas de este tipo de mensaje
	private static final String RE_NICK = "<nick>(.*?)</nick>";
	private static final String NICK_MARK = "nick";
	
	private static final String RE_TEXT = "<text>(.*?)</text>";
	private static final String TEXT_MARK = "text";


	/**
	 * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
	 */
	public NCNickMessage(byte opcode, String nick, String text) {
		this.opcode = opcode;
		this.nick = nick;
		this.text = text;
	}

	@Override
	//Pasamos los campos del mensaje a la codificación correcta en lenguaje de marcas
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<"+MESSAGE_MARK+">"+END_LINE);
		sb.append("<"+OPERATION_MARK+">"+opcodeToString(opcode)+"</"+OPERATION_MARK+">"+END_LINE); //Construimos el campo
		sb.append("<"+NICK_MARK+">"+nick+"</"+NICK_MARK+">"+END_LINE);
		sb.append("<"+TEXT_MARK+">"+text+"</"+TEXT_MARK+">"+END_LINE);
		sb.append("</"+MESSAGE_MARK+">"+END_LINE);

		return sb.toString(); //Se obtiene el mensaje

	}


	//Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
	public static NCNickMessage readFromString(byte code, String message) {
		String found_nick = null;
		String found_text = null;
		
		Pattern pat_nick = Pattern.compile(RE_NICK);
		Matcher mat_nick = pat_nick.matcher(message);
		if (mat_nick.find()) {
			// Nick found
			found_nick = mat_nick.group(1);
		} else {
			System.out.println("Error en NCNickMessage: no se ha encontrado parametro.");
			return null;
		}
		
		// Tienen que estar los campos porque el mensaje es de tipo RoomMessage
		Pattern pat_text = Pattern.compile(RE_TEXT);
		Matcher mat_text = pat_text.matcher(message);
		if (mat_text.find()) {
			// Name found
			found_text = mat_text.group(1);
		} else {
			System.out.println("Error en NCNickMessage: no se ha encontrado parametro.");
			return null;
		}
		
		return new NCNickMessage(code, found_nick, found_text);
	}


	//Devolvemos el nick contenido en el mensaje
	public String getNick() {
		return nick;
	}
	
	//Devolvemos el nombre contenido en el mensaje
	public String getText() {
		return text;
	}


}
