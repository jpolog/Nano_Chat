package es.um.redes.nanoChat.messageML;

public class NickOk extends NCMessage {

	private static final byte OPCODE = 2;
	
	public NickOk() {
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
	
	public static NickOk readFromString(String message) {
		return new NickOk();
	}

}
