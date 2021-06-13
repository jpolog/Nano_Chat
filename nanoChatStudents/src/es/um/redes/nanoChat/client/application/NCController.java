package es.um.redes.nanoChat.client.application;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import es.um.redes.nanoChat.client.comm.NCConnector;
import es.um.redes.nanoChat.client.shell.NCCommands;
import es.um.redes.nanoChat.client.shell.NCShell;
import es.um.redes.nanoChat.directory.connector.DirectoryConnector;
import es.um.redes.nanoChat.messageML.NCMessage;
import es.um.redes.nanoChat.messageML.NCNickMessage;
import es.um.redes.nanoChat.messageML.NCTextMessage;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

public class NCController {
	//Diferentes estados del cliente de acuerdo con el autómata
	private static final byte PRE_CONNECTION = 1;	//Ponerle el mismo nombre que en el autómata??
	private static final byte PRE_REGISTRATION = 2;
	private static final byte OFF_ROOM = 3;
	private static final byte IN_ROOM = 4;
	
	
	//Código de protocolo implementado por este cliente
	//TODO Cambiar para cada grupo
	private static final byte PROTOCOL = 30;
	//Conector para enviar y recibir mensajes del directorio
	private DirectoryConnector directoryConnector;
	//Conector para enviar y recibir mensajes con el servidor de NanoChat
	private NCConnector ncConnector;
	//Shell para leer comandos de usuario de la entrada estándar
	private NCShell shell;
	//Último comando proporcionado por el usuario
	private byte currentCommand;
	//Nick del usuario
	private String nickname;
	//Sala de chat en la que se quiere entrar
	private String room;
	//Mensaje enviado o por enviar al chat
	private String chatMessage;
	//Destinatario del mensaje privado
	private String receiver;
	//Nuevo nombre de la sala
	private String newName;
	//Dirección de internet del servidor de NanoChat
	private InetSocketAddress serverAddress;
	//Estado actual del cliente, de acuerdo con el autómata
	private byte clientStatus = PRE_CONNECTION;

	//Constructor
	public NCController() {
		shell = new NCShell();
	}

	//Devuelve el comando actual introducido por el usuario
	public byte getCurrentCommand() {		
		return this.currentCommand;
	}

	//Establece el comando actual
	public void setCurrentCommand(byte command) {
		currentCommand = command;
	}

	//Registra en atributos internos los posibles parámetros del comando tecleado por el usuario
	public void setCurrentCommandArguments(String[] args) {
		//Comprobaremos también si el comando es válido para el estado actual del autómata
		switch (currentCommand) {
		case NCCommands.COM_NICK:
			if (clientStatus == PRE_REGISTRATION)
				nickname = args[0];
			break;
		case NCCommands.COM_ENTER:
			if (clientStatus == OFF_ROOM)
			room = args[0];
			break;
		case NCCommands.COM_SEND:
			if (clientStatus == IN_ROOM)
			chatMessage = args[0];
			break;
		case NCCommands.COM_ROOM_RENAME:
			if (clientStatus == IN_ROOM)
			newName = args[0];
			break;
		case NCCommands.COM_SEND_PRIVATE:
			if (clientStatus == IN_ROOM)
			receiver = args[0];
			chatMessage = args[1];
			break;
		default:
		}
	}

	//Procesa los comandos introducidos por un usuario que aún no está dentro de una sala
	public void processCommand() {
		switch (currentCommand) {
		case NCCommands.COM_NICK:
			if (clientStatus == PRE_REGISTRATION)
				registerNickName();
			else
				System.out.println("* You have already registered a nickname ("+nickname+")");
			break;
		case NCCommands.COM_ROOMLIST:
			//TODO LLamar a getAndShowRooms() si el estado actual del autómata lo permite
			//TODO Si no está permitido informar al usuario
			if (clientStatus == OFF_ROOM)
				getAndShowRooms();
			else
				System.out.println("* You must be registered and out of a room to request the room list");
			break;
		case NCCommands.COM_ENTER:
			//TODO LLamar a enterChat() si el estado actual del autómata lo permite
			//TODO Si no está permitido informar al usuario
			if (clientStatus == OFF_ROOM)
				enterChat();
			else
				System.out.println("* You must be registered and out of a room to enter a chat room");
			break;
		case NCCommands.COM_QUIT:
			//Cuando salimos tenemos que cerrar todas las conexiones y sockets abiertos
			ncConnector.disconnect();			
			directoryConnector.close();
			break;
		default:
		}
	}
	
	//Método para registrar el nick del usuario en el servidor de NanoChat
	private void registerNickName() {
		try {
			//Pedimos que se registre el nick (se comprobará si está duplicado)
			boolean registered = ncConnector.registerNickname(nickname);
			//TODO: Cambiar la llamada anterior a registerNickname() al usar mensajes formateados 
			if (registered) {
				//TODO Si el registro fue exitoso pasamos al siguiente estado del autómata
				clientStatus = OFF_ROOM;
				System.out.println("* Your nickname is now "+nickname);
			}
			else
				//En este caso el nick ya existía
				System.out.println("* The nickname is already registered. Try a different one.");			
		} catch (IOException e) {
			System.out.println("* There was an error registering the nickname");
		}
	}

	//Método que solicita al servidor de NanoChat la lista de salas e imprime el resultado obtenido
	private void getAndShowRooms() {
		//TODO Lista que contendrá las descripciones de las salas existentes
		//TODO Le pedimos al conector que obtenga la lista de salas ncConnector.getRooms()
		//TODO Una vez recibidas iteramos sobre la lista para imprimir información de cada sala
		try {
			List<NCRoomDescription> roomList = ncConnector.getRooms();
			for(NCRoomDescription roomInfo : roomList) {
				System.out.println(roomInfo.toPrintableString());
			}
		} catch (IOException e) {
			System.out.println("* There was an error processing the room list");
		}
	}

	//Método para tramitar la solicitud de acceso del usuario a una sala concreta
	private void enterChat() {
		//TODO Se solicita al servidor la entrada en la sala correspondiente 
		try {
			boolean entered = ncConnector.enterRoom(room);
			if (!entered) { //TODO Si la respuesta es un rechazo entonces informamos al usuario y salimos
				System.out.println("* There was an error entering the room");
				return;
			}
			else { //TODO En caso contrario informamos que estamos dentro y seguimos		
				//TODO Cambiamos el estado del autómata para aceptar nuevos comandos
				clientStatus = IN_ROOM;
				System.out.println("* You have entered the room "+room);
			}
				
		} catch (IOException e) {
			System.out.println("* There was an error entering the room");
		}
		
		do {
			//Pasamos a aceptar sólo los comandos que son válidos dentro de una sala
			readRoomCommandFromShell();
			processRoomCommand();
			
		} while (currentCommand != NCCommands.COM_EXIT);
		System.out.println("* Your are out of the room");
		//TODO Llegados a este punto el usuario ha querido salir de la sala, cambiamos el estado del autómata
		clientStatus = OFF_ROOM;
	}

	//Método para procesar los comandos específicos de una sala
	private void processRoomCommand() {
		switch (currentCommand) {
		case NCCommands.COM_ROOMINFO:
			//El usuario ha solicitado información sobre la sala y llamamos al método que la obtendrá
			getAndShowInfo();
			break;
		case NCCommands.COM_SEND:
			//El usuario quiere enviar un mensaje al chat de la sala
			sendChatMessage();
			break;
		case NCCommands.COM_SEND_PRIVATE:
			//El usuario quiere enviar un mensaje privado a un usario de la sala
			sendPrivateChatMessage();
			break;
		case NCCommands.COM_SOCKET_IN:
			//En este caso lo que ha sucedido es que hemos recibido un mensaje desde la sala y hay que procesarlo
			processIncommingMessage();
			break;
		case NCCommands.COM_ROOM_RENAME:
			//El usuario quiere renombrar la sala actual
			RoomRename();
			break;
		case NCCommands.COM_EXIT:
			//El usuario quiere salir de la sala
			exitTheRoom();
		}		
	}

	//Método para solicitar al servidor la información sobre una sala y para mostrarla por pantalla
	private void getAndShowInfo() {
		//TODO Pedimos al servidor información sobre la sala en concreto
		//TODO Mostramos por pantalla la información
		try {
			NCRoomDescription roomInfo = ncConnector.getRoomInfo();
			System.out.println(roomInfo.toPrintableString());
		} catch (IOException e) {
			System.out.println("* There was an error processing the room info");
		}
		
	}
	
	private void RoomRename() {
		try {
			//Pedimos que se cambie el nombre de la sala (se comprobará si está duplicado)
			boolean renamed = ncConnector.roomRename(newName);
			//TODO: Cambiar la llamada anterior a registerNickname() al usar mensajes formateados 
			if (renamed) {
				//Se ha renombrado la slaa  correctamente
				System.out.println("* The room was successfully renamed");
			}
			else
				//En este caso el nick ya existía
				System.out.println("* That name is already in use. Try a different one.");			
		} catch (IOException e) {
			System.out.println("* There was an error renaming the room");
		}
	}

	//Método para notificar al servidor que salimos de la sala
	private void exitTheRoom() {
		//TODO Mandamos al servidor el mensaje de salida
		//TODO Cambiamos el estado del autómata para indicar que estamos fuera de la sala
		try {
			ncConnector.leaveRoom();
			clientStatus = OFF_ROOM;
		} catch (IOException e) {
			System.out.println("* There was an error leaving the room");
		}
	}

	//Método para enviar un mensaje al chat de la sala
	private void sendChatMessage() {
		//TODO Mandamos al servidor un mensaje de chat
		try {
			ncConnector.sendMessage(chatMessage);
		} catch (IOException e) {
			System.out.println("* There was an error sending the message");
		}
	}
	
	//Método para enviar un mensaje privado a un miembro del chat de la sala
		private void sendPrivateChatMessage() {
			//Mandamos al servidor un mensaje de chat con el nick del destinatario
			try {
				ncConnector.sendPrivateMessage(receiver, chatMessage);
			} catch (IOException e) {
				System.out.println("* There was an error sending the message");
			}
		}

	//Método para procesar los mensajes recibidos del servidor mientras que el shell estaba esperando un comando de usuario
	private void processIncommingMessage() {		
		//TODO Recibir el mensaje
		try {
			NCMessage message = ncConnector.receiveMessage();
			//TODO En función del tipo de mensaje, actuar en consecuencia
			//TODO (Ejemplo) En el caso de que fuera un mensaje de chat de broadcast mostramos la información de quién envía el mensaje y el mensaje en sí
			switch (message.getOpcode()){
			case NCMessage.OP_TEXT_MESSAGE_IN: {
				String nick = ((NCNickMessage)message).getNick();
				String text = ((NCNickMessage)message).getText();
				System.out.printf("%s: %s\n", nick, text);
				break;
			}case NCMessage.OP_PRIVATE_TEXT_MESSAGE_IN: {
				String nick = ((NCNickMessage)message).getNick();
				String text = ((NCNickMessage)message).getText();
				System.out.printf("[PRIVATE] %s: %s\n", nick, text);
				break;
			}case NCMessage.OP_ENTER_ROOM_NOTIFICATION: {
				String nick = ((NCTextMessage)message).getText();
				System.out.printf("%s joined the room\n", nick);
				break;
			}case NCMessage.OP_EXIT_ROOM_NOTIFICATION: {
				String nick = ((NCTextMessage)message).getText();
				System.out.printf("%s left the room\n", nick);
				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + message.getOpcode());
			}
		} catch (IOException e) {
			System.out.println("* There was an error receiving the message");
		}
		
		
	}

	//Método para leer un comando de la sala 
	public void readRoomCommandFromShell() {
		//Pedimos un nuevo comando de sala al shell (pasando el conector por si nos llega un mensaje entrante)
		shell.readChatCommand(ncConnector);
		//Establecemos el comando tecleado (o el mensaje recibido) como comando actual
		setCurrentCommand(shell.getCommand());
		//Procesamos los posibles parámetros (si los hubiera)
		setCurrentCommandArguments(shell.getCommandArguments());
	}

	//Método para leer un comando general (fuera de una sala)
	public void readGeneralCommandFromShell() {
		//Pedimos el comando al shell
		shell.readGeneralCommand();
		//Establecemos que el comando actual es el que ha obtenido el shell
		setCurrentCommand(shell.getCommand());
		//Analizamos los posibles parámetros asociados al comando
		setCurrentCommandArguments(shell.getCommandArguments());
	}

	//Método para obtener el servidor de NanoChat que nos proporcione el directorio
	public boolean getServerFromDirectory(String directoryHostname) {
		//Inicializamos el conector con el directorio y el shell
		System.out.println("* Connecting to the directory...");
		//Intentamos obtener la dirección del servidor de NanoChat que trabaja con nuestro protocolo
		try {
			directoryConnector = new DirectoryConnector(directoryHostname);
			serverAddress = directoryConnector.getServerForProtocol(PROTOCOL);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			serverAddress = null;
		}
		//Si no hemos recibido la dirección entonces nos quedan menos intentos
		if (serverAddress == null) {
			System.out.println("* Check your connection, the directory is not available.");		
			return false;
		}
		else return true;
	}
	
	//Método para establecer la conexión con el servidor de Chat (a través del NCConnector)
	public boolean connectToChatServer() {
			try {
				//Inicializamos el conector para intercambiar mensajes con el servidor de NanoChat (lo hace la clase NCConnector)
				ncConnector = new NCConnector(serverAddress);
			} catch (IOException e) {
				System.out.println("* Check your connection, the game server is not available.");
				serverAddress = null;
			}
			//Si la conexión se ha establecido con éxito informamos al usuario y cambiamos el estado del autómata
			if (serverAddress != null) {
				System.out.println("* Connected to "+serverAddress);
				clientStatus = PRE_REGISTRATION;
				return true;
			}
			else return false;
	}

	//Método que comprueba si el usuario ha introducido el comando para salir de la aplicación
	public boolean shouldQuit() {
		return currentCommand == NCCommands.COM_QUIT;
	}

}
