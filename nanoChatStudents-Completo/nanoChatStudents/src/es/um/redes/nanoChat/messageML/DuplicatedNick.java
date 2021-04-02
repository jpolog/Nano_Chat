package es.um.redes.nanoChat.messageML;

public class DuplicatedNick extends NCMessage {

	private static final byte OPCODE = 3;
	
	public DuplicatedNick() {
		this.opcode = OPCODE;
	}
	
	
	@Override
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<"+MESSAGE_MARK+">"+END_LINE);
		sb.append("<"+OPERATION_MARK+">"+this.opcode+"</"+OPERATION_MARK+">"+END_LINE);
		sb.append("</"+MESSAGE_MARK+">"+END_LINE);
		
		String message = sb.toString();
		return message;
	}
	
	public static DuplicatedNick readFromString(String message) {
		return new DuplicatedNick();
	}

}
