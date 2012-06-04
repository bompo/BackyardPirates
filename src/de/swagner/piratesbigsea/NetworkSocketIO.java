package de.swagner.piratesbigsea;

import java.util.HashMap;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import de.swagner.piratesbigsea.Player.STATE;


public class NetworkSocketIO {

	public MultiPlayerScreen gameSession;

	public Array<String> messageList = new Array<String>();

	public boolean connected = false;
	public float timeToConnect = 5;

	// network vars
	public Integer place = 0;
	public String id = "";
	private STATE currentState = Player.STATE.IDLE;
	public HashMap<String, Integer> connectedIDs = new HashMap<String, Integer>();
	public Array<NetworkShip> enemies = new Array<NetworkShip>();

	static NetworkSocketIO instance;

	public native void connect(NetworkSocketIO instance) /*-{var socket = $wnd.io.connect('http://backyardpirates.nodester.com:80');
										$wnd.socket = socket;
		
										socket.on('playerconnect', function (data) {
										    $wnd.console.log('connect',data);
										    instance.@de.swagner.piratesbigsea.NetworkSocketIO::onConnect(Ljava/lang/String;Ljava/lang/String;)(data.count, data.player);
									  	});
									  	
									  	socket.on('playerdisconnect', function (data) {
										    $wnd.console.log('connect',data);
										    instance.@de.swagner.piratesbigsea.NetworkSocketIO::onDisconnect(Ljava/lang/String;Ljava/lang/String;)(data.count, data.player);
									  	});
	
										socket.on('init', function (data) {
										    $wnd.console.log('init',data);
										    instance.@de.swagner.piratesbigsea.NetworkSocketIO::onInit(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(data.count, data.player, data.room);
									  	});
									  	
									  	socket.on('ready', function (data) {
										    $wnd.console.log('ready',data);
										    instance.@de.swagner.piratesbigsea.NetworkSocketIO::onReady(Ljava/lang/String;)(data.player);
									  	});
									  	
									  	socket.on('death', function (data) {
										    $wnd.console.log('death',data);
										    instance.@de.swagner.piratesbigsea.NetworkSocketIO::onDeath(Ljava/lang/String;)(data.player);
									  	});
									  	
									  	socket.on('startround', function (data) {
										    $wnd.console.log('startround',data);
										    instance.@de.swagner.piratesbigsea.NetworkSocketIO::onStartRound()();
									  	});		
									  	
									  	//public void onUpdate(String id, String state, String positionx, String positiony, String angle, String fire) {
										socket.on('update', function (data) {
										    $wnd.console.log('update',data);
										    var payload = JSON.parse(data.message);
										    instance.@de.swagner.piratesbigsea.NetworkSocketIO::onUpdate(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(data.player, payload.state, payload.positionx, payload.positiony, payload.angle, payload.fire);
									  	});

									  	//public void onSyncUpdate(String id, String state, String positionx, String positiony, String angle, String fire) {
										socket.on('synchronize', function (data) {
										    $wnd.console.log('synchronize',data);
										    var payload = JSON.parse(data.message);
										    instance.@de.swagner.piratesbigsea.NetworkSocketIO::onSyncUpdate(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(data.player, payload.state, payload.positionx, payload.positiony, payload.angle, payload.fire);
									  	});
									  	
									    //public void onHit(String id, String state, String positionx, String positiony, String angle, String fire) {
										socket.on('hit', function (data) {
										    $wnd.console.log('hit',data);
										    var payload = JSON.parse(data.message);
										    instance.@de.swagner.piratesbigsea.NetworkSocketIO::onHit(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(data.player, payload.state, payload.positionx, payload.positiony, payload.angle, payload.fire);
									  	});
									  	
									  	socket.on('message', function (data) {									  		
										    $wnd.console.log('message',data);
									  	});
									  }-*/;
	
	public native void sendMessage(String type, String data) /*-{
		$wnd.socket.emit(type, data);
	  }-*/;

	private NetworkSocketIO() {
	}
	
	public void connect() {
		connect(instance);
	}
	
	public void onConnect(String count, String id) {
		
    	connectedIDs.put(id, Integer.parseInt(count));
    	System.out.println("Player " + id + ", " + count + " connected");

    	addMessage("player " + id + ", " + count + " connected");
    	if(connectedIDs.keySet().size() == 1) {
    		System.out.println("reinit");
    		if(gameSession!=null) {
        		gameSession.waitingForOtherPlayers = false;
        		gameSession.winLoseCounter = 0;
        		gameSession.initLevel();
    		}
    	}
	}
	
	public void onDisconnect(String count, String id) {
		
    	connectedIDs.remove(id);
    	NetworkShip remove = enemies.get(0);
    	for(NetworkShip enemie:enemies) {
    		if(enemie.id == id) {
    			remove = enemie;
    		}
    	}
    	remove.life = -1;
    	System.out.println("Player " + id + " disconnected");
    	addMessage("player " + id + " disconnected");
    	
    	if(connectedIDs.keySet().size() == 0) {
    		gameSession.waitingForOtherPlayers = true;
    		gameSession.winLoseCounter = 0;
    		gameSession.initLevel();
    	}
	}
	
	public void onInit(String count, String id, String room) {
		connected = true;
    	addMessage("connected");     	

    	this.id = id;
    	this.place = Integer.parseInt(count); 
		
    	addMessage("joined room " + room + " as player " +  count);
	}
	
	public void onReady(String id) {
		System.out.println("Player " + id + " ready");
    	addMessage("player " + id + " ready");
	}
	
	public void onNotReady(String id) {
		System.out.println("Player " + id + " not ready");
    	addMessage("player " + id + " not ready");
	}
	
	public void onDeath(String id) {
		System.out.println("Player " + id + " death");
    	addMessage("player " + id + " death");
	}
	
	public void onStartRound() {
		System.out.println("startround");
		addMessage("start round");
    	gameSession.startNewRound();
	}
	
	public void onUpdate(String id, String state, String positionx, String positiony, String angle, String fire) {
    	System.out.println("update");
    	for(NetworkShip ship:enemies) {
    		if(ship.id.equalsIgnoreCase(id)) {
    			System.out.println("update " + ship.id);	
    			gameSession.networkUpdates.add(new UpdatePackage(ship, new Vector2(Float.parseFloat(positionx),Float.parseFloat(positiony)), Float.parseFloat(angle)));
    			
    			if(state.equalsIgnoreCase("IDLE")) {
    				ship.state = NetworkShip.STATE.IDLE;
    			} else if(state.equalsIgnoreCase("UP")) {
    				ship.state = NetworkShip.STATE.UP;
    			} else if(state.equalsIgnoreCase("LEFT")) {
    				ship.state = NetworkShip.STATE.LEFT;
    			} else if(state.equalsIgnoreCase("RIGHT")) {
    				ship.state = NetworkShip.STATE.RIGHT;
    			} else if(state.equalsIgnoreCase("UPLEFT")) {
    				ship.state = NetworkShip.STATE.UPLEFT;
    			} else if(state.equalsIgnoreCase("UPRIGHT")) {
    				ship.state = NetworkShip.STATE.UPRIGHT;
    			}
    			
    			if(Integer.parseInt(fire) == 1) {		                				
					gameSession.shootEnemy(ship.body.getWorldCenter().add(ship.body.getWorldVector(new Vector2(3f,1f))), ship.body.getWorldVector(new Vector2(0,1f)).rotate(-90).cpy());
					ship.hitAnimation = -4;		                				
    			} else if(Integer.parseInt(fire) == -1) {
    				gameSession.shootEnemy(ship.body.getWorldCenter().add(ship.body.getWorldVector(new Vector2(-3f,1f))), ship.body.getWorldVector(new Vector2(0,1f)).rotate(90).cpy());
    				ship.hitAnimation = 4;
    			}
    		}
    	}
	}
	
	public void onSyncUpdate(String id, String state, String positionx, String positiony, String angle, String fire) {
    	System.out.println("synchronize");
    	for(NetworkShip ship:enemies) {
    		if(ship.id.equalsIgnoreCase(id)) {
    			System.out.println("synchronize " + ship.id);	
    			Vector2 networkPos = new Vector2(Float.parseFloat(positionx),Float.parseFloat(positiony));
    			if(networkPos.dst(ship.body.getPosition())>1) {		                				
    				gameSession.networkUpdates.add(new UpdatePackage(ship, networkPos, Float.parseFloat(angle)));
    			} else {
        			networkPos.sub(ship.body.getPosition());
        			Vector2 newPos = ship.body.getPosition().tmp().add(networkPos.mul(0.1f));
        			gameSession.networkUpdates.add(new UpdatePackage(ship, newPos, Float.parseFloat(angle)));
    			}	
    			
    			if(state.equalsIgnoreCase("IDLE")) {
    				ship.state = NetworkShip.STATE.IDLE;
    			} else if(state.equalsIgnoreCase("UP")) {
    				ship.state = NetworkShip.STATE.UP;
    			} else if(state.equalsIgnoreCase("LEFT")) {
    				ship.state = NetworkShip.STATE.LEFT;
    			} else if(state.equalsIgnoreCase("RIGHT")) {
    				ship.state = NetworkShip.STATE.RIGHT;
    			} else if(state.equalsIgnoreCase("UPLEFT")) {
    				ship.state = NetworkShip.STATE.UPLEFT;
    			} else if(state.equalsIgnoreCase("UPRIGHT")) {
    				ship.state = NetworkShip.STATE.UPRIGHT;
    			}
    		}
    	}
	}
	
	public void onHit(String id, String state, String positionx, String positiony, String angle, String fire) {
    	System.out.println("hit");    	
    	for(NetworkShip ship:enemies) {
    		if(ship.id.equalsIgnoreCase(id)) {
    			addMessage("player " + id + " hit" + " health: " + ship.life);
    			
    			System.out.println("hit " + ship.id);	
    			ship.life = ship.life - 1;    			
    			
    			//sync ship
    			Vector2 networkPos = new Vector2(Float.parseFloat(positionx),Float.parseFloat(positiony));
    			networkPos.sub(ship.body.getPosition());
    			Vector2 newPos = ship.body.getPosition().tmp().add(networkPos.mul(0.1f));
    			
    			gameSession.networkUpdates.add(new UpdatePackage(ship, newPos, Float.parseFloat(angle)));
    			
    			if(state.equalsIgnoreCase("IDLE")) {
    				ship.state = NetworkShip.STATE.IDLE;
    			} else if(state.equalsIgnoreCase("UP")) {
    				ship.state = NetworkShip.STATE.UP;
    			} else if(state.equalsIgnoreCase("LEFT")) {
    				ship.state = NetworkShip.STATE.LEFT;
    			} else if(state.equalsIgnoreCase("RIGHT")) {
    				ship.state = NetworkShip.STATE.RIGHT;
    			} else if(state.equalsIgnoreCase("UPLEFT")) {
    				ship.state = NetworkShip.STATE.UPLEFT;
    			} else if(state.equalsIgnoreCase("UPRIGHT")) {
    				ship.state = NetworkShip.STATE.UPRIGHT;
    			}
    		}
    	}
	}

	public void sendCurrentState(Player player, int fire) {
		if(currentState == player.state && fire == 0) return;
		System.out.println("send update");
		
        sendMessage("update", "{ \"fire\": \"" + fire + "\", \"state\":\"" + player.state + "\", \"positionx\":\"" + player.body.getPosition().x + "\", \"positiony\":\"" + player.body.getPosition().y + "\", \"angle\":\"" + player.body.getAngle() + "\", \"angledir\":\"" + player.body.getAngularVelocity() + "\" }");
		currentState = player.state;
	}

	public void sendSyncState(Player player) {
		System.out.println("send sync");
		
        sendMessage("synchronize", "{ \"state\":\"" + player.state + "\", \"positionx\":\"" + player.body.getPosition().x + "\", \"positiony\":\"" + player.body.getPosition().y + "\", \"angle\":\"" + player.body.getAngle() + "\", \"angledir\":\"" + player.body.getAngularVelocity() + "\" }");
		currentState = player.state;
	}

	public void sendReady(Player player) {
		System.out.println("send ready");
	
		sendMessage("ready", "{ player:" + id + "}");
	}

	public void sendNotReady() {
		System.out.println("send not ready");
		
		sendMessage("notready", "{ player:" + id + "}");
	}

	public void sendHit(Player player) {
		System.out.println("send hit");
			
        sendMessage("hit", "{ \"state\":\"" + player.state + "\", \"positionx\":\"" + player.body.getPosition().x + "\", \"positiony\":\"" + player.body.getPosition().y + "\", \"angle\":\"" + player.body.getAngle() + "\", \"angledir\":\"" + player.body.getAngularVelocity() + "\" }");	
	}

	public static NetworkSocketIO getInstance() {
		if(instance!=null) return instance;
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
