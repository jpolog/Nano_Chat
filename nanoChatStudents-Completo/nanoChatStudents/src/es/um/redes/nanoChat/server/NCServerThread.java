package es.um.redes.nanoChat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

import es.um.redes.nanoChat.messageML.NCInfoListMessage;
import es.um.redes.nanoChat.messageML.NCInfoMessage;
import es.um.redes.nanoChat.messageML.NCMessage;
import es.um.redes.nanoChat.messageML.NCNickMessage;
import es.um.redes.nanoChat.messageML.NCOperationMessage;
import es.um.redes.nanoChat.messageML.NCTextMessage;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;
import es.um.redes.nanoChat.server.roomManager.NCRoomManager;

/**
 * A new thread runs for each connected client
 */
public class NCServerThread extends Thread {
	
	private Socket socket = null;
	//Manager global compartido entre los Threads
	private NCServerManager serverManager = null;
	//Input and Output Streams
	private DataInputStream dis;
	private DataOutputStream dos;
	//Usuario actual al que atiende este Thread
	String user;
	//RoomManager actual (dependerá de la sala a la que entre el usuario)
	NCRoomManager roomManager;
	//Sala actual
	String currentRoom;

	//Inicialización de la sala
	public NCServerThread(NCServerManager manager, Socket socket) throws IOException {
		super("NCServerThread");
		this.socket = socket;
		this.serverManager = manager;
	}

	//Main loop
	public void run() {
		try {
			//Se obtienen los streams a partir del Socket
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			//En primer lugar hay que recibir y verificar el nick
			receiveAndVerifyNickname();
			//Mientras que la conexión esté activa entonces...
			while (true) {
				//TODO Obtenemos el mensaje que llega y analizamos su código de operación
				NCMessage message = NCMessage.readMessageFromSocket(dis);
				switch (message.getOpcode()) {
				//TODO 1) si se nos pide la lista de salas se envía llamando a sendRoomList();
				case NCMessage.OP_ROOM_LIST_REQUEST: {
					sendRoomList();
				}
				//TODO 2) Si se nos pide entrar en la sala entonces obtenemos el RoomManager de la sala,
				case NCMessage.OP_ENTER_ROOM: {
					String roomName = ((NCTextMessage)message).getText();
					NCRoomManager roomManager = serverManager.enterRoom(user, roomName, socket);
					//TODO 2) Si el usuario no es aceptado en la sala entonces se le notifica al cliente
					//TODO 2) notificamos al usuario que ha sido aceptado y procesamos mensajes con processRoomMessages()
					if (roomManager == null) {
						NCMessage reply = NCMessage.makeOperationMessage(NCMessage.OP_ENTER_ROOM_FAIL);
						dos.writeUTF(((NCOperationMessage)reply).toEncodedString());
					}else {
						this.currentRoom = roomName;
						this.roomManager = roomManager;
						NCMessage reply = NCMessage.makeOperationMessage(NCMessage.OP_ENTER_ROOM_OK);
						dos.writeUTF(((NCOperationMessage)reply).toEncodedString());
						
						//Se notifica la entrada a la sala
/**						String nick = ((NCNickMessage)message).getNick();
						String text = ((NCNickMessage)message).getText();
*/
						//sendMessage(NCMessage.OP_ENTER_ROOM_NOTIFICATION, nick, text);
						
						processRoomMessages();
					}
					
				}
				//AÑADIR UN DEFAULT??????????
				
				}
			}
		} catch (Exception e) {
			//If an error occurs with the communications the user is removed from all the managers and the connection is closed
			System.out.println("* User "+ user + " disconnected.");
			serverManager.leaveRoom(user, currentRoom);
			serverManager.removeUser(user);
		}
		finally {
			if (!socket.isClosed())
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		}
	}

	//Obtenemos el nick y solicitamos al ServerManager que verifique si está duplicado
	private void receiveAndVerifyNickname() throws IOException {
		boolean retry = true;	//Reintentos
		while(retry) {
			//La lógica de nuestro programa nos obliga a que haya un nick registrado antes de proseguir
			//TODO Entramos en un bucle hasta comprobar que alguno de los nicks proporcionados no está duplicado
			//TODO Extraer el nick del mensaje
			String nick = NCTextMessage.readFromString(NCMessage.OP_REGISTER_NICK, dis.readUTF()).getText();
			//TODO Validar el nick utilizando el ServerManager - addUser()
			boolean userOK = serverManager.addUser(nick);
			//TODO Contestar al cliente con el resultado (éxito o duplicado)
			NCMessage resp;
			if(userOK) {
				this.user = nick;
				resp = NCMessage.makeOperationMessage(NCMessage.OP_NICK_OK);
				dos.writeUTF(((NCOperationMessage)resp).toEncodedString());
				
				retry=false;
			}else {
				resp = NCMessage.makeOperationMessage(NCMessage.OP_DUPLICATED_NICK);
				dos.writeUTF(((NCOperationMessage)resp).toEncodedString());
			}
			
		}
		
		
	}
	
	//Mandamos al cliente la lista de salas existentes
	private void sendRoomList() throws IOException  {
		
		//TODO La lista de salas debe obtenerse a partir del RoomManager y después enviarse mediante su mensaje correspondiente
		List<NCRoomDescription> roomList = this.serverManager.getRoomList();
		NCMessage message = NCMessage.makeInfoListMessage(NCMessage.OP_ROOM_LIST, roomList);
		dos.writeUTF(((NCInfoListMessage)message).toEncodedString());
	}
	
	//Mandamos al cliente la información de la sala
	private void sendRoomInfo() throws IOException  {
			
		//La sala se  obtiene a partir del RoomManager y después se envía mediante su mensaje correspondiente
		NCRoomDescription roomInfo = this.serverManager.getRoomInfo(currentRoom);
		NCMessage message = NCMessage.makeInfoMessage(NCMessage.OP_ROOM_INFO, roomInfo.roomName, roomInfo.members, roomInfo.timeLastMessage);
		dos.writeUTF(((NCInfoMessage)message).toEncodedString());
	}

	private void processRoomMessages() throws IOException  {
		//TODO Comprobamos los mensajes que llegan hasta que el usuario decida salir de la sala
		boolean exit = false;
		while (!exit) {
			//TODO Se recibe el mensaje enviado por el usuario
			NCMessage message = NCMessage.readMessageFromSocket(dis);
			//TODO Se analiza el código de operación del mensaje y se trata en consecuencia
			switch (message.getOpcode()) {
			//Se nos pide la información de la sala actual del cliente
			case NCMessage.OP_ROOM_INFO_REQUEST: {
				sendRoomInfo();
				break;
			}
			case NCMessage.OP_EXIT_ROOM: {
				exitRoom();
				exit = true;
				//Se notifica la salida de la sala
/**				String nick = ((NCNickMessage)message).getNick();
				String text = ((NCNickMessage)message).getText();
				sendMessage(NCMessage.OP_EXIT_ROOM_NOTIFICATION, nick, text);
*/
				break;
				
			}
			case NCMessage.OP_ROOM_RENAME_REQUEST: {
				// Hacerlo en un atributo en vez de pasarlo como parámetro??
				roomRename(((NCTextMessage)message).getText());
				break;
	
			}
			case NCMessage.OP_TEXT_MESSAGE_IN: {
				String nick = ((NCNickMessage)message).getNick();
				String text = ((NCNickMessage)message).getText();
				sendMessage(NCMessage.OP_TEXT_MESSAGE_IN, nick, text);
				break;
				
			}
			//AÑADIR UN DEFAULT??????????
			
			}
		}
	}
	
	private void exitRoom() throws IOException  {
		serverManager.leaveRoom(user, currentRoom);
	}
	
	private void roomRename(String newName) throws IOException  {
		boolean renamed = serverManager.roomRename(currentRoom, newName);
		if(renamed) {
			NCMessage reply = NCMessage.makeOperationMessage(NCMessage.OP_ROOM_RENAME_OK);
			dos.writeUTF(((NCOperationMessage)reply).toEncodedString());
			this.currentRoom = newName;
			this.roomManager.setRoomName(newName);
		}else {
			NCMessage reply = NCMessage.makeOperationMessage(NCMessage.OP_ROOM_RENAME_DUPLICATED);
			dos.writeUTF(((NCOperationMessage)reply).toEncodedString());
		}
	}
	
	private void sendMessage(byte opcode, String user,  String text) throws IOException  {
		serverManager.sendMessage(opcode, currentRoom, user, text);
	}
	
	
	
}
	
	
	
	
	
	
	
	
	
	
	
	
