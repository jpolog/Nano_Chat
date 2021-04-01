package es.um.redes.nanoChat.messageML;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;


public abstract class NCMessage {
	protected byte opcode;

	// TODO IMPLEMENTAR TODAS LAS CONSTANTES RELACIONADAS CON LOS CODIGOS DE OPERACION
	public static final byte OP_INVALID_CODE = 0;
	public static final byte OP_REGISTER_NICK = 1;
	public static final byte OP_NICK_OK = 2;
	public static final byte OP_DUPLICATED_NICK = 3;
	public static final byte OP_ROOM_REQUEST = 4;
	public static final byte OP_SEND_ROOM_OK = 5;
	public static final byte OP_SEND_ROOM_FAIL = 6;
	public static final byte OP_ENTER_ROOM = 7;
	public static final byte OP_EXIT_ROOM = 8;
	public static final byte OP_ROOM_LIST_REQUEST = 9;
	public static final byte OP_SEND_ROOM_LIST = 10;

	public static final char DELIMITER = ':';    //Define el delimitador
	public static final char END_LINE = '\n';    //Define el carácter de fin de línea
	
	public static final String OPERATION_MARK = "operation";
	public static final String MESSAGE_MARK = "message";

	/**
	 * Códigos de los opcodes válidos  El orden
	 * es importante para relacionarlos con la cadena
	 * que aparece en los mensajes
	 */
	private static final Byte[] _valid_opcodes = { 
		OP_REGISTER_NICK,
		OP_NICK_OK,
		OP_DUPLICATED_NICK,
		OP_ROOM_REQUEST,
		OP_SEND_ROOM_OK,
		OP_SEND_ROOM_FAIL,
		OP_ENTER_ROOM,
		OP_EXIT_ROOM,
		OP_ROOM_LIST_REQUEST,
		OP_SEND_ROOM_LIST
		
		};

	/**
	 * cadena exacta de cada orden
	 */
	private static final String[] _valid_operations_str = {
		"Register Nick",
		"Nick OK",
		"Duplicated Nick",
		"Request Room",
		"Send Room Ok",
		"Send Room Fail",
		"Enter Room",
		"Exit Room",
		"Room List Request",
		"Send Room List"
	};

	private static Map<String, Byte> _operation_to_opcode;
	private static Map<Byte, String> _opcode_to_operation;
	
	static {
		_operation_to_opcode = new TreeMap<>();
		_opcode_to_operation = new TreeMap<>();
		for (int i = 0 ; i < _valid_operations_str.length; ++i)
		{
			_operation_to_opcode.put(_valid_operations_str[i].toLowerCase(), _valid_opcodes[i]);
			_opcode_to_operation.put(_valid_opcodes[i], _valid_operations_str[i]);
		}
	}
	
	/**
	 * Transforma una cadena en el opcode correspondiente
	 */
	protected static byte stringToOpcode(String opStr) {
		return _operation_to_opcode.getOrDefault(opStr.toLowerCase(), OP_INVALID_CODE);
	}

	/**
	 * Transforma un opcode en la cadena correspondiente
	 */
	protected static String opcodeToString(byte opcode) {
		return _opcode_to_operation.getOrDefault(opcode, null);
	}
	
	//Devuelve el opcode del mensaje
	public byte getOpcode() {
		return opcode;
	}

	//Método que debe ser implementado por cada subclase de NCMessage
	protected abstract String toEncodedString();

	//Analiza la operación de cada mensaje y usa el método readFromString() de cada subclase para parsear
	public static NCMessage readMessageFromSocket(DataInputStream dis) throws IOException {
		String message = dis.readUTF();
		String regexpr = "<"+MESSAGE_MARK+">(.*?)</"+MESSAGE_MARK+">";
		Pattern pat = Pattern.compile(regexpr,Pattern.DOTALL);
		Matcher mat = pat.matcher(message);
		if (!mat.find()) {
			System.out.println("Mensaje mal formado:\n"+message);
			return null;
			// Message not found
		} 
		String inner_msg = mat.group(1);  // extraemos el mensaje

		String regexpr1 = "<"+OPERATION_MARK+">(.*?)</"+OPERATION_MARK+">";
		Pattern pat1 = Pattern.compile(regexpr1);
		Matcher mat1 = pat1.matcher(inner_msg);
		if (!mat1.find()) {
			System.out.println("Mensaje mal formado:\n" +message);
			return null;
			// Operation not found
		} 
		String operation = mat1.group(1);  // extraemos la operación
		
		byte code = stringToOpcode(operation);
		if (code == OP_INVALID_CODE) return null;
		
		switch (code) {
		//TODO Parsear el resto de mensajes 
		case OP_REGISTER_NICK:
		{
			return RegisterNick.readFromString(message);
		}
		case OP_NICK_OK: {
			return NickOk.readFromString(message);
		}
		case OP_DUPLICATED_NICK: {
			return DuplicatedNick.readFromString(message);
		}
		case OP_ROOM_REQUEST: {
			return RoomRequest.readFromString(message);
		}
		case OP_SEND_ROOM_OK: {
			return SendRoomOk.readFromString(message);
		}
		case OP_SEND_ROOM_FAIL: {
			return SendRoomFail.readFromString(message);
		}
		case OP_ENTER_ROOM: {
			return EnterRoom.readFromString(message);
		}
		case OP_EXIT_ROOM: {
			return ExitRoom.readFromString(message);
		}
		case OP_ROOM_LIST_REQUEST: {
			return RoomListRequest.readFromString(message);
		}
		case OP_SEND_ROOM_LIST: {
			return SendRoomList.readFromString(message);
		}
		default:
			System.err.println("Unknown message type received:" + code);
			return null;
		}

	}

	//TODO Programar el resto de métodos para crear otros tipos de mensajes
	
	public static NCMessage makeRoomMessage(byte code, String room) {
		return new NCRoomMessage(code, room);
	}
	
	public static NCMessage makeRegisterNickMessage(String nick) {
		return new RegisterNick(nick);
	}
	public static NCMessage makeNickOkMessage() {
		return new NickOk();
	}
	public static NCMessage makeDuplicatedNick() {
		return new DuplicatedNick();
	}
	public static NCMessage makeRoomRequest(String room_name) {
		return new RoomRequest(room_name);
	}
	public static NCMessage makeSendRoomOk() {
		return new SendRoomOk();
	}
	public static NCMessage makeSenRoomFail() {
		return new SendRoomFail();
	}
	public static NCMessage makeEnterRoomMessage(String room_name) {
		return new EnterRoom(room_name);
	}
	public static NCMessage makeExitRoomMessage() {
		return new ExitRoom();
	}
	public static NCMessage makeRoomListRequestMessage() {
		return new RoomListRequest();
	}
	public static NCMessage makeSendRoomListMessage(List<NCRoomDescription> descList) {
		return new SendRoomList(descList);
	}
}
