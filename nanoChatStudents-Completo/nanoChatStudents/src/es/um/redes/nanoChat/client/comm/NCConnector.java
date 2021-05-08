package es.um.redes.nanoChat.client.comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import es.um.redes.nanoChat.messageML.NCInfoListMessage;
import es.um.redes.nanoChat.messageML.NCInfoMessage;
import es.um.redes.nanoChat.messageML.NCMessage;
import es.um.redes.nanoChat.messageML.NCNickMessage;
import es.um.redes.nanoChat.messageML.NCOperationMessage;
import es.um.redes.nanoChat.messageML.NCTextMessage;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor de NanoChat
public class NCConnector {
	private Socket socket;
	protected DataOutputStream dos;
	protected DataInputStream dis;
	
	public NCConnector(InetSocketAddress serverAddress) throws UnknownHostException, IOException {
		//TODO Se crea el socket a partir de la dirección proporcionada 
		socket = new Socket(serverAddress.getAddress(), serverAddress.getPort());
		//TODO Se extraen los streams de entrada y salida
		// "dos" = data  output stream
		// "dis" = data input stream
		dos = new DataOutputStream(socket.getOutputStream());
		dis = new DataInputStream(socket.getInputStream());
	}

	/**
	//Método para registrar el nick en el servidor. Nos informa sobre si la inscripción se hizo con éxito o no.
	public boolean registerNickname_UnformattedMessage(String nick) throws IOException {
		//Funcionamiento resumido: SEND(nick) and RCV(NICK_OK) or RCV(NICK_DUPLICATED)
		//TODO Enviamos una cadena con el nick por el flujo de salida
		dos.writeUTF(nick);
		//TODO Leemos la cadena recibida como respuesta por el flujo de entrada 
		String rcv = dis.readUTF();
		//TODO Si la cadena recibida es NICK_OK entonces no está duplicado (en función de ello modificar el return)
		boolean resp = false;
		if(rcv.equals("NICK_OK")) {
			resp = true;
		}
		return resp;
	}
	*/

	
	//Método para registrar el nick en el servidor. Nos informa sobre si la inscripción se hizo con éxito o no.
	public boolean registerNickname(String nick) throws IOException {
		//Funcionamiento resumido: SEND(nick) and RCV(NICK_OK) or RCV(NICK_DUPLICATED)
		//Creamos un mensaje de tipo RoomMessage con opcode OP_NICK en el que se inserte el nick
		NCTextMessage message = (NCTextMessage) NCMessage.makeTextMessage(NCMessage.OP_REGISTER_NICK, nick);
		//Obtenemos el mensaje de texto listo para enviar
		String rawMessage = message.toEncodedString();
		//Escribimos el mensaje en el flujo de salida, es decir, provocamos que se envíe por la conexión TCP
		dos.writeUTF(rawMessage);
		//TODO Leemos el mensaje recibido como respuesta por el flujo de entrada 
		NCMessage response = NCMessage.readMessageFromSocket(dis);
		//TODO Analizamos el mensaje para saber si está duplicado el nick (modificar el return en consecuencia)
		if (response.getOpcode() == NCMessage.OP_NICK_OK) {
			return true;
		} else  {
			return false;
		}		
		
	}
	
	//Método para obtener la lista de salas del servidor
	public List<NCRoomDescription> getRooms() throws IOException {
		//Funcionamiento resumido: SND(GET_ROOMS) and RCV(ROOM_LIST)
		//TODO completar el método
		NCMessage request = NCMessage.makeOperationMessage(NCMessage.OP_ROOM_LIST_REQUEST);
		dos.writeUTF(((NCOperationMessage)request).toEncodedString());
		
		NCMessage response = NCMessage.readMessageFromSocket(dis);
		
		return ((NCInfoListMessage)response).getList();
	}
	
	//Método para solicitar la entrada en una sala
	public boolean enterRoom(String room) throws IOException {
		//Funcionamiento resumido: SND(ENTER_ROOM<room>) and RCV(IN_ROOM) or RCV(REJECT)
		//TODO completar el método
		NCMessage request = NCMessage.makeTextMessage(NCMessage.OP_ENTER_ROOM, room);
		dos.writeUTF(((NCTextMessage)request).toEncodedString());
		
		NCMessage response = NCMessage.readMessageFromSocket(dis);
		
		//Analizamos el mensaje para saber si se puede acceder a la sala
		if (response.getOpcode() == NCMessage.OP_ENTER_ROOM_OK) {
			return true;
		} else  {
			return false;
		}	
	}
	
	//Método para renombrar la sala
	public boolean roomRename(String room) throws IOException {
		NCMessage request = NCMessage.makeTextMessage(NCMessage.OP_ROOM_RENAME_REQUEST, room);
		dos.writeUTF(((NCTextMessage)request).toEncodedString());
		
		NCMessage response = NCMessage.readMessageFromSocket(dis);
		
		//Analizamos el mensaje para saber si se puede acceder a la sala
		if (response.getOpcode() == NCMessage.OP_ROOM_RENAME_OK) {
			return true;
		} else  {
			return false;
		}	
	}
	
	//Método para salir de una sala
	public void leaveRoom(String room) throws IOException {
		//Funcionamiento resumido: SND(EXIT_ROOM)
		//TODO completar el método
		NCMessage request = NCMessage.makeOperationMessage(NCMessage.OP_EXIT_ROOM);
		dos.writeUTF(((NCOperationMessage)request).toEncodedString());
	}
	
	//Método que utiliza el Shell para ver si hay datos en el flujo de entrada
	public boolean isDataAvailable() throws IOException {
		return (dis.available() != 0);
	}
	
	
	//TODO Es necesario implementar métodos para recibir y enviar mensajes de chat a una sala
	public void sendMessage(String msg) throws IOException {
		//Crea el mensaje con el texto pasado como parámetro y lo envía
		NCMessage message = NCMessage.makeTextMessage(NCMessage.OP_TEXT_MESSAGE_OUT, msg);
		dos.writeUTF(((NCTextMessage)message).toEncodedString());
	}
	
	public void sendPrivateMessage(String nick, String msg) throws IOException {
		//Crea el mensaje con el texto pasado como parámetro y lo envía
		NCMessage message = NCMessage.makeNickMessage(NCMessage.OP_PRIVATE_TEXT_MESSAGE_OUT, nick, msg);
		dos.writeUTF(((NCNickMessage)message).toEncodedString());
	}
	
	public NCMessage receiveMessage() throws IOException {
		NCMessage message = NCMessage.readMessageFromSocket(dis);
		return message;
	}
	
	//Método para pedir la descripción de una sala
	public NCRoomDescription getRoomInfo(String room) throws IOException {
		//Funcionamiento resumido: SND(GET_ROOMINFO) and RCV(ROOMINFO)
		//TODO Construimos el mensaje de solicitud de información de la sala específica
		//TODO Recibimos el mensaje de respuesta
		//TODO Devolvemos la descripción contenida en el mensaje
		NCMessage request = NCMessage.makeOperationMessage(NCMessage.OP_ROOM_INFO_REQUEST);
		dos.writeUTF(((NCOperationMessage)request).toEncodedString());
		
		
		NCMessage response = NCMessage.readMessageFromSocket(dis);
		NCRoomDescription roomDescription = new NCRoomDescription(((NCInfoMessage)response).getName(), ((NCInfoMessage)response).getMembers(), ((NCInfoMessage)response).getTimeLastMessage());
		return roomDescription;
		
	}
	
	//Método para cerrar la comunicación con la sala
	//TODO (Opcional) Enviar un mensaje de salida del servidor de Chat
	public void disconnect() {
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
		} finally {
			socket = null;
		}
	}

}
