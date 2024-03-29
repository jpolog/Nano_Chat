package es.um.redes.nanoChat.messageML;


import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class NCTextMessage extends NCMessage {

	private String text;
	
	//Constantes asociadas a las marcas específicas de este tipo de mensaje
	private static final String RE_TEXT = "<text>(.*?)</text>";
	private static final String TEXT_MARK = "text";


	/**
	 * Creamos un mensaje de tipo Text a partir del código de operación y del texto
	 */
	public NCTextMessage(byte opcode, String text) {
		this.opcode = opcode;
		this.text = text;
	}

	@Override
	//Pasamos los campos del mensaje a la codificación correcta en lenguaje de marcas
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<"+MESSAGE_MARK+">"+END_LINE);
		sb.append("<"+OPERATION_MARK+">"+opcodeToString(opcode)+"</"+OPERATION_MARK+">"+END_LINE); 
		sb.append("<"+TEXT_MARK+">"+text+"</"+TEXT_MARK+">"+END_LINE);
		sb.append("</"+MESSAGE_MARK+">"+END_LINE);

		return sb.toString(); //Se obtiene el mensaje

	}


	//Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
	public static NCTextMessage readFromString(byte code, String message) {
		String found_text = null;

		
		Pattern pat_text = Pattern.compile(RE_TEXT);
		Matcher mat_text = pat_text.matcher(message);
		if (mat_text.find()) {
			//texto encontrado
			found_text = mat_text.group(1);
		} else {
			System.out.println("Error en NCTextMessage: no se ha encontrado parametro.");
			return null;
		}
		
		return new NCTextMessage(code, found_text);
	}


	//Devolvemos el texto contenido en el mensaje
	public String getText() {
		return text;
	}

}
