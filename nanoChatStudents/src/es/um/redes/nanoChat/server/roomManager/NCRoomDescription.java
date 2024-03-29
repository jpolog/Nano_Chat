package es.um.redes.nanoChat.server.roomManager;

import java.util.Date;
import java.util.Set;

public class NCRoomDescription {
	//Campos de los que se compone una descripción de una sala
	public String roomName;
	public Set<String> members;	//Se sustituye el List por un Set porque es más eficiente
	public long timeLastMessage;

	//Constructor a partir de los valores para los campos
	public NCRoomDescription(String roomName, Set<String> members, long timeLastMessage) {
		this.roomName = roomName;
		this.members = members;
		this.timeLastMessage = timeLastMessage;
	}

	//Método que devuelve una representación de la Descripción lista para ser impresa por pantalla
	public String toPrintableString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Room Name: "+roomName+"\t Members ("+members.size()+ ") : ");
		for (String member: members) {
			sb.append(member+" ");
		}
		if (timeLastMessage != 0)
			sb.append("\tLast message: "+new Date(timeLastMessage).toString());
		else
			sb.append("\tLast message: not yet");
		return sb.toString();
	}
}
