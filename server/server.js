var socket = require('socket.io').listen(80);
var players = {}, count = 0;
 
socket.on('connection', function(client){
    // send own id to client
    client.emit('init',{ player: client.id, count: count });
    
    // send client id to other clients
    client.broadcast.emit('connect',{ player: client.id, count: count });
    
    // send active players to connected client
    var n = 0;
    for( var player in players ) { 
        client.emit('connect',{ player: players[player].id, count: n });
        n = n + 1;
    }
    
    // add own id to current players
    count = count + 1;
    players[count] = new Player(client.id);


    // update message
    client.on('update', function(message){
        client.broadcast.emit('update', {player: client.id, message: message});
    })
    
    // synchronize message
    client.on('synchronize', function(message){
        client.broadcast.volatile.emit('synchronize', {player: client.id, message: message});
    })
    
    // disconnect message
    client.on('disconnect', function(){
        console.log("disconnected");
    })
});
 
function Player(id) {
        this.id = id;
};
