package es.um.redes.nanoChat.directory.connector;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Cliente con métodos de consulta y actualización específicos del directorio
 */
public class DirectoryConnector {
	//Tamaño máximo del paquete UDP (los mensajes intercambiados son muy cortos)
	private static final int PACKET_MAX_SIZE = 128;
	//Puerto en el que atienden los servidores de directorio
	private static final int DEFAULT_PORT = 6868;
	//Valor del TIMEOUT
	private static final int TIMEOUT = 1000;

	private DatagramSocket socket; // socket UDP
	private InetSocketAddress directoryAddress; // dirección del servidor de directorio

	public DirectoryConnector(String agentAddress) throws IOException {
		//TODO A partir de la dirección y del puerto generar la dirección de conexión para el Socket
		directoryAddress = new InetSocketAddress(InetAddress.getByName(agentAddress), DEFAULT_PORT);
		//TODO Crear el socket UDP
		socket = new DatagramSocket();
	}

	public String convertToUpper(String strToConvert) throws IOException{
		byte[] bufSend = strToConvert.getBytes();
		DatagramPacket dpSend = new DatagramPacket(bufSend, bufSend.length, directoryAddress);
		byte[] bufRec = new byte[PACKET_MAX_SIZE];
		DatagramPacket dpRec = new DatagramPacket(bufRec, bufRec.length);
		socket.send(dpSend);
		int i=1;	//reintentos
		while(i<6) {
			try {
				socket.setSoTimeout(TIMEOUT);
				socket.receive(dpRec);
				i=6;
			} catch (SocketTimeoutException e) {
				if(i<5) {
					socket.send(dpSend);
					System.out.printf("Reintento número %d\n", i);
					
				}else {
					System.err.println("No se ha recibido el paquete");
					
				}
				i+=1;
			}
		}
		
		
		String strConverted = new String(dpRec.getData());
		return strConverted;
	}
	
	/**
	 * Envía una solicitud para obtener el servidor de chat asociado a un determinado protocolo
	 * 
	 */
	public InetSocketAddress getServerForProtocol(byte protocol) throws IOException {

		//TODO Generar el mensaje de consulta llamando a buildQuery()
		byte[] men = buildQuery(protocol);
		//TODO Construir el datagrama con la consulta
		DatagramPacket dpSend = new DatagramPacket(men, men.length, directoryAddress);
		//TODO Enviar datagrama por el socket
		socket.send(dpSend);
		//TODO preparar el buffer para la respuesta
		byte[] res = new byte[PACKET_MAX_SIZE];
		DatagramPacket dpRec = new DatagramPacket(res, res.length);
		//TODO Establecer el temporizador para el caso en que no haya respuesta
		//TODO Recibir la respuesta
		int i=1;	
		while(i<4) {	//reintentos
			try {
				socket.setSoTimeout(TIMEOUT);
				socket.receive(dpRec);
				i=4;
			} catch (SocketTimeoutException e) {
				if(i<3) {
					socket.send(dpSend);
					System.out.printf("Reintento número %d\n", i);
					
				}else {
					System.err.println("No se ha recibido el paquete");
					
				}
				i+=1;
			}
		}
		//TODO Procesamos la respuesta para devolver la dirección que hay en ella
		return getAddressFromResponse(dpRec);
		}

	//Método para generar el mensaje de consulta (para obtener el servidor asociado a un protocolo)
	private byte[] buildQuery(byte protocol) {
		//TODO Devolvemos el mensaje codificado en binario según el formato acordado
		ByteBuffer bb = ByteBuffer.allocate(2);
		byte opCode = 3;
		byte protocolId = protocol;
		bb.put(opCode);
		bb.put(protocolId);
		byte[] men = bb.array();
		return men;
	}

	//Método para obtener la dirección de internet a partir del mensaje UDP de respuesta
	private InetSocketAddress getAddressFromResponse(DatagramPacket packet) throws UnknownHostException {
		ByteBuffer ret = ByteBuffer.wrap(packet.getData());
		byte opCode = ret.get();
		//TODO Analizar si la respuesta no contiene dirección (devolver null)
		if (opCode == 5) {
			return null;
		//TODO Si la respuesta no está vacía, devolver la dirección (extraerla del mensaje)
		} else {
			byte[] iA = new byte[4];
			for(int i=0; i<4; ++i)
				iA[i] = ret.get();
			InetSocketAddress iSA = new InetSocketAddress(InetAddress.getByAddress(iA), ret.getInt());
			return iSA;
		}
		
		
		
	}
	
	/**
	 * Envía una solicitud para registrar el servidor de chat asociado a un determinado protocolo
	 * 
	 */
	public boolean registerServerForProtocol(byte protocol, int port) throws IOException {
		boolean resp = false;
		//TODO Construir solicitud de registro (buildRegistration)
		byte[] men = buildRegistration(protocol, port);
		//TODO Enviar solicitud
		DatagramPacket dpSend = new DatagramPacket(men, men.length, directoryAddress);
		socket.send(dpSend);
		//TODO Recibe respuesta
		byte[] res = new byte[PACKET_MAX_SIZE];
		DatagramPacket dpRec = new DatagramPacket(res, res.length);
		int i=1;	
		while(i<4) {	//reintentos
			try {
				socket.setSoTimeout(TIMEOUT);
				socket.receive(dpRec);
				i=4;
			} catch (SocketTimeoutException e) {
				if(i<3) {
					socket.send(dpSend);
					System.out.printf("Reintento número %d\n", i);
					
				}else {
					System.err.println("No se ha recibido el paquete");
					
				}
				i+=1;
			}
		}
		//TODO Procesamos la respuesta para ver si se ha podido registrar correctamente
		ByteBuffer ret = ByteBuffer.wrap(dpRec.getData());
		byte opCode = ret.get();
		if (opCode == 2) {
			resp = true;
		}
		return resp;
	}


	//Método para construir una solicitud de registro de servidor
	//OJO: No hace falta proporcionar la dirección porque se toma la misma desde la que se envió el mensaje
	private byte[] buildRegistration(byte protocol, int port) {
		ByteBuffer bb = ByteBuffer.allocate(6);
		byte opCode = 1;
		byte protocolId = protocol;
		bb.put(opCode);
		bb.put(protocolId);
		bb.putInt(port);
		byte[] men = bb.array();
		return men;
	}

	public void close() {
		socket.close();
	}
}

