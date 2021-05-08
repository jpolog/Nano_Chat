package es.um.redes.nanoChat.messageML;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	public static final byte OP_ROOM_LIST_REQUEST = 4;
	public static final byte OP_ROOM_LIST = 5;
	public static final byte OP_ENTER_ROOM = 6;
	public static final byte OP_ENTER_ROOM_OK = 7;
	public static final byte OP_ENTER_ROOM_FAIL = 8;
	public static final byte OP_ENTER_ROOM_NOTIFICATION = 9;
	public static final byte OP_EXIT_ROOM = 10;
	public static final byte OP_EXIT_ROOM_NOTIFICATION = 11;
	public static final byte OP_ROOM_INFO_REQUEST = 12;
	public static final byte OP_ROOM_INFO = 13;
	public static final byte OP_TEXT_MESSAGE_OUT = 14;
	public static final byte OP_TEXT_MESSAGE_IN = 15;
	public static final byte OP_PRIVATE_TEXT_MESSAGE_OUT = 16;
	public static final byte OP_PRIVATE_TEXT_MESSAGE_IN = 17;
	public static final byte OP_ROOM_RENAME_REQUEST= 18;
	public static final byte OP_ROOM_RENAME_OK = 19;
	public static final byte OP_ROOM_RENAME_DUPLICATED = 20;
	
	
	
	


	//public static final char DELIMITER = ':';    //Define el delimitador
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
		OP_ROOM_LIST_REQUEST,
		OP_ROOM_LIST,
		OP_ENTER_ROOM,
		OP_ENTER_ROOM_OK,
		OP_ENTER_ROOM_FAIL,
		OP_ENTER_ROOM_NOTIFICATION,
		OP_EXIT_ROOM,
		OP_EXIT_ROOM_NOTIFICATION,
		OP_ROOM_INFO_REQUEST,
		OP_ROOM_INFO,
		OP_TEXT_MESSAGE_OUT,
		OP_TEXT_MESSAGE_IN,
		OP_PRIVATE_TEXT_MESSAGE_OUT,
		OP_PRIVATE_TEXT_MESSAGE_IN,
		OP_ROOM_RENAME_REQUEST,
		OP_ROOM_RENAME_OK,
		OP_ROOM_RENAME_DUPLICATED,
	};

	/**
	 * cadena exacta de cada orden
	 */
	private static final String[] _valid_operations_str = {
		"Register Nick",
		"Nick Ok",
		"Duplicated Nick",
		"Room List Request",
		"Room List",
		"Enter Room",
		"Enter Room Ok",
		"Enter Room Fail",
		"Enter Room Notification",
		"Exit Room",
		"Exit Room Notification",
		"Room Info Request",
		"Room Info",
		"Text Message Out",		
		"Text Message In",
		"Private Text Message Out",
		"Private Text Message In",
		"Room Rename Request",
		"Romm Rename Ok",
		"Room Rename Duplicated",
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
		case OP_REGISTER_NICK: {
			return NCTextMessage.readFromString(OP_REGISTER_NICK, message);
		}
		case OP_NICK_OK: {
			return new NCOperationMessage(OP_NICK_OK);
		}
		case OP_DUPLICATED_NICK: {
			return new NCOperationMessage(OP_DUPLICATED_NICK);
		}
		case OP_ROOM_LIST_REQUEST: {
			return new NCOperationMessage(OP_ROOM_LIST_REQUEST);
		}
		case OP_ROOM_LIST: {	
			return NCInfoListMessage.readFromString(OP_ROOM_LIST, message);
		}
		case OP_ENTER_ROOM: {
			return NCTextMessage.readFromString(OP_ENTER_ROOM, message);
		}
		case OP_ENTER_ROOM_OK: {
			return new NCOperationMessage(OP_ENTER_ROOM_OK);
		}
		case OP_ENTER_ROOM_FAIL: {
			return new NCOperationMessage(OP_ENTER_ROOM_FAIL);
		}
		case OP_ENTER_ROOM_NOTIFICATION: {
			return NCTextMessage.readFromString(OP_ENTER_ROOM_NOTIFICATION, message);
		}
		case OP_EXIT_ROOM: {
			return new NCOperationMessage(OP_EXIT_ROOM);
		}
		case OP_EXIT_ROOM_NOTIFICATION: {
			return NCTextMessage.readFromString(OP_EXIT_ROOM_NOTIFICATION, message);
		}
		case OP_ROOM_INFO_REQUEST: {
			return new NCOperationMessage(OP_ROOM_INFO_REQUEST);
		}
		case OP_ROOM_INFO: {
			return NCInfoMessage.readFromString(OP_ROOM_INFO, message);
		}
		case OP_TEXT_MESSAGE_OUT: {
			return NCTextMessage.readFromString(OP_TEXT_MESSAGE_OUT, message);
		}
		case OP_TEXT_MESSAGE_IN: {
			return NCNickMessage.readFromString(OP_TEXT_MESSAGE_IN, message);
		}
		case OP_PRIVATE_TEXT_MESSAGE_OUT: {
			return NCNickMessage.readFromString(OP_PRIVATE_TEXT_MESSAGE_OUT, message);
		}
		case OP_PRIVATE_TEXT_MESSAGE_IN: {
			return NCNickMessage.readFromString(OP_PRIVATE_TEXT_MESSAGE_IN, message);
		}
		case OP_ROOM_RENAME_REQUEST: {
			return NCTextMessage.readFromString(OP_ROOM_RENAME_REQUEST, message);
		}
		case OP_ROOM_RENAME_OK: {
			return new NCOperationMessage(OP_ROOM_RENAME_OK);
		}
		case OP_ROOM_RENAME_DUPLICATED: {
			return new NCOperationMessage(OP_ROOM_RENAME_DUPLICATED);
		}
		default:
			System.err.println("Unknown message type received:" + code);
			return null;
		}

	}

	//TODO Programar el resto de métodos para crear otros tipos de mensajes
	public static NCMessage makeOperationMessage(byte opcode) {
		return new NCOperationMessage(opcode);
	}
	public static NCMessage makeTextMessage(byte opcode, String text) {
		return new NCTextMessage(opcode, text);
	}
	public static NCMessage makeInfoListMessage(byte opcode, List<NCRoomDescription> roomList) {
		return new NCInfoListMessage(opcode, roomList);
	}
	public static NCMessage makeInfoMessage(byte opcode, String name, Set<String> members, long timeLastMessage) {
		return new NCInfoMessage(opcode, name, members, timeLastMessage);
	}
	public static NCMessage makeNickMessage(byte opcode, String nick, String text) {
		return new NCNickMessage(opcode, nick, text);
	}
	
	
	
/**
	
	public static NCMessage makeRoomMessage(byte code, String room) {
		return new NCRoomMessage(code, room);
	}
	public static NCMessage makeRegisterNickMessage(String nick) {
		return new NCTextMessage(code, nick);
	}
	public static NCMessage makeNickOkMessage() {
		return new NickOk();
	}
	public static NCMessage makeDuplicatedNickMessage() {
		return new DuplicatedNick();
	}
	public static NCMessage makeEnterRoomMessage(String room_name) {
		return new EnterRoom(room_name);
	}
	public static NCMessage makeEnterRoomOkMessage() {
		return new EnterRoomOk();
	}
	public static NCMessage makeEnterRoomFailMessage() {
		return new EnterRoomFail();
	}
	public static NCMessage makeExitRoomMessage() {
		return new ExitRoom();
	}
	public static NCMessage makeRoomListRequestMessage() {
		return new RoomListRequest();
	}
	public static NCMessage makeRoomListMessage(List<NCRoomDescription> descList) {
		return new RoomList(descList);
	}
*/
	
}
