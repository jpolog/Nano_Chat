package es.um.redes.nanoChat.messageML;

public class NCOperationMessage extends NCMessage {


	/**
	 * Creamos un mensaje de tipo Operation a partir del código de operación
	 */
	public NCOperationMessage(byte opcode) {
		this.opcode = opcode;
	}

	@Override
	//Pasamos los campos del mensaje a la codificación correcta en lenguaje de marcas
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<"+MESSAGE_MARK+">"+END_LINE);
		sb.append("<"+OPERATION_MARK+">"+opcodeToString(opcode)+"</"+OPERATION_MARK+">"+END_LINE);
		sb.append("</"+MESSAGE_MARK+">"+END_LINE);

		return sb.toString(); //Se obtiene el mensaje

	}

}
