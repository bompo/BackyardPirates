package de.swagner.piratesbigsea;

import java.util.HashMap;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

import de.swagner.piratesbigsea.Player.STATE;

public class NetworkSocketIO {

	private Json json = new Json();

	public MultiPlayerScreen gameSession;

	public Array<String> messageList = new Array<String>();

	public boolean connected = false;
	public float timeToConnect = 5;

	// network vars
	public Integer place;
	public String id;
	private STATE currentState = Player.STATE.IDLE;
	public HashMap<String, Integer> connectedIDs = new HashMap<String, Integer>();

	public Array<NetworkShip> enemies = new Array<NetworkShip>();

	static NetworkSocketIO instance;

	public native void connect(NetworkSocketIO instance) /*-{var socket = $wnd.io.connect('http://backyardpirates.nodester.com:80');
										socket.on('init', function (data) {
										    $wnd.console.log('init',data);
										    instance.@de.swagner.piratesbigsea.NetworkSocketIO::getInit(Ljava/lang/String;)('test');
										    socket.emit('my other event', { my: 'data' });
									  	});
									  	
									  	socket.on('message', function (data) {
										    $wnd.console.log('message',data);
										    socket.emit('my other event', { my: 'data' });
									  	});
									  }-*/;

	
	
	private NetworkSocketIO() {
		connect(this);
	}
	
	public void getInit(String data) {
		connected = true;
    	addMessage("connected");
    	Json jsonData = new Json();
    	System.out.println("jsssssoonnn " + data);
    	
	}
	

	public void sendCurrentState(Player player, int fire) {
		if (currentState == player.state && fire == 0)
			return;
		System.out.println("send update");

		Json json = new Json();
		json.writeValue("fire", fire);
		json.writeValue("state", player.state);
		json.writeValue("positionx", player.body.getPosition().x);
		json.writeValue("positiony", player.body.getPosition().y);
		json.writeValue("angle", player.body.getAngle());
		json.writeValue("angledir", player.body.getAngularVelocity());
		

//		socket.send("update," + json.toString());
		currentState = player.state;
	}

	public void sendSyncState(Player player) {
		System.out.println("send sync");

		Json json = new Json();
		json.writeValue("state", player.state);
		json.writeValue("positionx", player.body.getPosition().x);
		json.writeValue("positiony", player.body.getPosition().y);
		json.writeValue("angle", player.body.getAngle());
		json.writeValue("angledir", player.body.getAngularVelocity());
		

//		socket.send("synchronize, " + json.toString());
		currentState = player.state;
	}

	public void sendReady(Player player) {
		System.out.println("send ready");

		Json json = new Json();
		json.writeValue("player", id);
		
//		socket.send("ready," + json.toString());
	}

	public void sendNotReady(Player player) {
		System.out.println("send not ready");

		Json json = new Json();
		json.writeValue("player", id);

//		socket.send("notready, " + json.toString());
	}

	public void sendHit(Player player) {
		System.out.println("send hit");

		Json json = new Json();
		json.writeValue("state", player.state);
		json.writeValue("positionx", player.body.getPosition().x);
		json.writeValue("positiony", player.body.getPosition().y);
		json.writeValue("angle", player.body.getAngle());
		json.writeValue("angledir", player.body.getAngularVelocity());
		

//		socket.send("hit, " + json.toString());
	}

	public static NetworkSocketIO getInstance() {
		if (instance != null)
			return instance;
		instance = new NetworkSocketIO();
		return instance;
	}

	public void addMessage(String m) {
		if (messageList.size > 5) {
			messageList.removeIndex(0);
		}
		messageList.add(m);
	}

	public void setGameSession(MultiPlayerScreen multiPlayerScreen) {
		this.gameSession = multiPlayerScreen;
	}
}
