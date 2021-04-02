package es.um.redes.nanoChat.messageML;

public class SendRoomFail extends NCMessage{
	
	private static final byte OPCODE = 6;


	public SendRoomFail() {
		this.opcode = OPCODE;
		
	}
	

	@Override
	public String toEncodedString() {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("<"+MESSAGE_MARK+">");
		sb.append("<"+OPERATION_MARK+">"+this.opcode+"</"+OPERATION_MARK+">");
		sb.append("</"+MESSAGE_MARK+">");
		
		String message = sb.toString();
		return message;
	}
	
	public static SendRoomFail readFromString(String message) {
		return new SendRoomFail();
	}

}
