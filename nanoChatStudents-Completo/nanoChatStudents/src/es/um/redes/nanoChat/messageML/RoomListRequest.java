package es.um.redes.nanoChat.messageML;

public class RoomListRequest extends NCMessage {

	private static final byte OPCODE = 9;
	
	public RoomListRequest() {
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
	
	public static RoomListRequest readFromString(String message) {
		return new RoomListRequest();
	}

}
