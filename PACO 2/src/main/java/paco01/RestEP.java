package paco01;

import java.util.ArrayList;
import java.util.List;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttTopicSubscription;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;


public class RestEP extends AbstractVerticle{
	
	
	private SQLClient mySQLClient;

	public void start(Future<Void> startFuture) {
		
		
		JsonObject mySQLClientConfig = new JsonObject()
				.put("host", "127.0.0.1")
				.put("port", 3306)
				.put("database", "paco")
				.put("username", "root")
				.put("password", "4953")
				;
		
		mySQLClient = MySQLClient.createShared(vertx, mySQLClientConfig);
		
		Router router = Router.router(vertx);
		
		vertx.createHttpServer().requestHandler(router::accept).
		listen(8083, res -> {
			if (res.succeeded()) {
				System.out.println("Servidor REST desplegado");
			}else {
				System.out.println("Error: " + res.cause());
			}
			
		});
		
		router.route("/*").handler(BodyHandler.create());
		router.get("/api/door/:id").handler(this::getOnePuerta);
		router.get("/api/terminal/:id").handler(this::getOneTerminal);
		router.get("/api/door/dir/:doorAddress").handler(this::getPuertaDir);
		router.get("/api/terminal/logFecha/:accFecha").handler(this::getAccFecha);
		
		router.delete("/api/door/:id").handler(this::deleteOnePuerta);
		router.delete("/api/terminal/:id").handler(this::deleteOneTerminal);
		
		router.put("/api/door").handler(this::putElementPuerta);
		router.put("/api/terminal").handler(this::putElementTerminal);
		
		MqttServer mqttserver = MqttServer.create(vertx);
		initialize(mqttserver);
		
		MqttClient mqttCliente = MqttClient.create(vertx, 
				new MqttClientOptions().setAutoKeepAlive(true));
		mqttCliente.connect(8123, "localhost", handler -> {
			mqttCliente.publish("topic", Buffer.buffer("Mi primer mensaje"), );
		});

		
	}
	
	public void initialize(MqttServer mqttServer) {
		mqttServer.endpointHandler(new Handler<MqttEndpoint>() {
			public void handle(MqttEndpoint endpoint) {
				 endpoint.accept(false);
			        handleSubscription(endpoint);
			        handleUnsubscription(endpoint);
			        handlePublish(endpoint);
			        handleClientDisconnected(endpoint);
			}

			private void handlePublish(MqttEndpoint endpoint) {
				endpoint.publishHandler(message -> {
					System.out.println("Topic: "+ message.topicName() + 
							". Contenido: " + message.payload().toString());
					if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
						endpoint.publishRelease(message.messageId());
					}
				});
			}
			
			protected void handleClientDisconnected(MqttEndpoint endpoint) {
			    endpoint.disconnectHandler(disconnect -> {
			      System.out.println("El cliente " + endpoint.clientIdentifier() + "se ha desconectado.");
			    });
			  }

			protected void handleUnsubscription(MqttEndpoint endpoint) {
			    endpoint.unsubscribeHandler(unsubscribe -> {
			      for(String topic: unsubscribe.topics()) {
			        System.out.println("El cliente " + endpoint.clientIdentifier() + " ha elimidado su suscripción del canal " + topic);
			        
			      }
			      endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
			    });
			  }

			private void handleSubscription(MqttEndpoint endpoint) {
				endpoint.subscribeHandler(subscribe -> {
					List<MqttQoS> grantedQoS = new ArrayList<MqttQoS>();
					for (MqttTopicSubscription s: subscribe.topicSubscriptions()) {
						System.out.println("Suscripcion al topic: "+ s.toString());
						grantedQoS.add(s.qualityOfService());
					}
					endpoint.subscribeAcknowledge(subscribe.messageId(), grantedQoS);
				});
			}
		}).listen(8123, handler -> {
			if (handler.succeeded()) {
				System.out.println("Servidor MQTT desplegado");
			}else {
				System.out.println("Error"+ handler.cause());
			}
		});
	}
	
	
	// ------------------------------ GETONEs ------------------------------
	private void getAccFecha(RoutingContext routingContext) {
		String paramStr = routingContext.request().getParam("accFecha");
		if(paramStr != null) {
			try {
				int param = Integer.parseInt(paramStr);
				
				mySQLClient.getConnection(conn -> {
					if(conn.succeeded()) {

						SQLConnection connection = conn.result();
						String query = "SELECT acierto, fecha, bloqueo, numIntento, puertaRelacionada "
						+ "FROM terminal "
						+ "WHERE fecha = ?";
						
						JsonArray paramQuery = new JsonArray()
								.add(param);
						
						connection.queryWithParams(query, paramQuery, res -> {
							if (res.succeeded()) {
								routingContext.response().end(Json.encodePrettily(res.result().getRows()));
							}else {
								routingContext.response().setStatusCode(400).end(res.cause().toString());
							}
						});
						
					}else {
						routingContext.response().setStatusCode(400).end(conn.cause().toString());
					}
				});

			}catch (ClassCastException e) {
				routingContext.response().setStatusCode(400).end();
			}
		}else {
			routingContext.response().setStatusCode(400).end();
		}
	}
	
	
	private void getPuertaDir(RoutingContext routingContext) {
		String paramStr = routingContext.request().getParam("doorAddress");
		if(paramStr != null) {
			try {
				String param = paramStr;
				
				mySQLClient.getConnection(conn -> {
					if(conn.succeeded()) {

						SQLConnection connection = conn.result();
						String query = "SELECT idPuerta, estadoPuerta, direccionPuerta "
						+ "FROM puerta "
						+ "WHERE direccionPuerta = ?";
						
						JsonArray paramQuery = new JsonArray()
								.add(param);
						
						connection.queryWithParams(query, paramQuery, res -> {
							if (res.succeeded()) {
								routingContext.response().end(Json.encodePrettily(res.result().getRows()));
							}else {
								routingContext.response().setStatusCode(400).end(res.cause().toString());
							}
						});
						
					}else {
						routingContext.response().setStatusCode(400).end(conn.cause().toString());
					}
				});

			}catch (ClassCastException e) {
				routingContext.response().setStatusCode(400).end();
			}
		}else {
			routingContext.response().setStatusCode(400).end();
		}
	}
	
	private void getOnePuerta(RoutingContext routingContext) {
		String paramStr = routingContext.request().getParam("id");
		if(paramStr != null) {
			try {
				int param = Integer.parseInt(paramStr);
				
				mySQLClient.getConnection(conn -> {
					if(conn.succeeded()) {

						SQLConnection connection = conn.result();
						String query = "SELECT idPuerta, estadoPuerta, direccionPuerta, contraPuerta, adminPuerta "
						+ "FROM puerta "
						+ "WHERE idPuerta = ?";
						
						JsonArray paramQuery = new JsonArray()
								.add(param);
						
						connection.queryWithParams(query, paramQuery, res -> {
							if (res.succeeded()) {
								routingContext.response().end(Json.encodePrettily(res.result().getRows()));
							}else {
								routingContext.response().setStatusCode(400).end(res.cause().toString());
							}
						});
						
					}else {
						routingContext.response().setStatusCode(400).end(conn.cause().toString());
					}
				});

			}catch (ClassCastException e) {
				routingContext.response().setStatusCode(400).end();
			}
		}else {
			routingContext.response().setStatusCode(400).end();
		}
	}
	
	
	
	private void getOneTerminal(RoutingContext routingContext) {
		String paramStr = routingContext.request().getParam("id");
		if(paramStr != null) {
			try {
				int param = Integer.parseInt(paramStr);
				
				mySQLClient.getConnection(conn -> {
					if(conn.succeeded()) {

						SQLConnection connection = conn.result();
						String query = "SELECT idTerminal, acierto, fecha, bloqueo, numIntento, puertaRelacionada "
						+ "FROM terminal "
						+ "WHERE idTerminal = ?";
						
						JsonArray paramQuery = new JsonArray()
								.add(param);
						
						connection.queryWithParams(query, paramQuery, res -> {
							if (res.succeeded()) {
								routingContext.response().end(Json.encodePrettily(res.result().getRows()));
							}else {
								routingContext.response().setStatusCode(400).end(res.cause().toString());
							}
						});
						
					}else {
						routingContext.response().setStatusCode(400).end(conn.cause().toString());
					}
				});

			}catch (ClassCastException e) {
				routingContext.response().setStatusCode(400).end();
			}
		}else {
			routingContext.response().setStatusCode(400).end();
		}
	}
	
	
	
	
	
	
	
	
	
	// ------------------------------ DELETEs ------------------------------
	
	private void deleteOneTerminal(RoutingContext routingContext) {
		String paramStr = routingContext.request().getParam("id");
		if(paramStr != null) {
			try {
				int param = Integer.parseInt(paramStr);
				
				mySQLClient.getConnection(conn -> {
					if(conn.succeeded()) {

						SQLConnection connection = conn.result();
						String query = "DELETE FROM terminal "
						+ "WHERE idTerminal = ?";
						
						JsonArray paramQuery = new JsonArray()
								.add(param);
						
						connection.queryWithParams(query, paramQuery, res -> {
							if (res.succeeded()) {
								routingContext.response().end(Json.encodePrettily(res.result().getRows()));
							}else {
								routingContext.response().setStatusCode(400).end(res.cause().toString());
							}
						});
						
					}else {
						routingContext.response().setStatusCode(400).end(conn.cause().toString());
					}
				});

			}catch (ClassCastException e) {
				routingContext.response().setStatusCode(400).end();
			}
		}else {
			routingContext.response().setStatusCode(400).end();
		}
	}
	
	
	
	private void deleteOnePuerta(RoutingContext routingContext) {
		String paramStr = routingContext.request().getParam("id");
		if(paramStr != null) {
			try {
				int param = Integer.parseInt(paramStr);
				
				mySQLClient.getConnection(conn -> {
					if(conn.succeeded()) {

						SQLConnection connection = conn.result();
						String query = "DELETE FROM puerta "
						+ "WHERE idPuerta = ?";
						
						JsonArray paramQuery = new JsonArray()
								.add(param);
						
						connection.queryWithParams(query, paramQuery, res -> {
							if (res.succeeded()) {
								routingContext.response().end(Json.encodePrettily(res.result().getRows()));
							}else {
								routingContext.response().setStatusCode(400).end(res.cause().toString());
							}
						});
						
					}else {
						routingContext.response().setStatusCode(400).end(conn.cause().toString());
					}
				});

			}catch (ClassCastException e) {
				routingContext.response().setStatusCode(400).end();
			}
		}else {
			routingContext.response().setStatusCode(400).end();
		}
	}
	
	
	
	
	
	
	
	
	
	// ------------------------------ PUTs ------------------------------
	

	
	private void putElementPuerta(RoutingContext routingContext) {
		Puerta state = Json.decodeValue(routingContext.getBody().toString(), Puerta.class);		

			try {
				
				mySQLClient.getConnection(conn -> {
					if(conn.succeeded()) {
	
						SQLConnection connection = conn.result();
						String query = "INSERT INTO Puerta (idPuerta, estadoPuerta, direccionPuerta, contraPuerta, adminPuerta) "
						+ "VALUES (?, ?, ?, ?, ?) ";
						
						JsonArray paramQuery = new JsonArray()
								.add(state.getId())
								.add(state.getDoorState())
								.add(state.getDoorAddress())
								.add(state.getDoorPass())
								.add(state.getDoorAdmin());
						
						connection.queryWithParams(query, paramQuery, res -> {
							if (res.succeeded()) {
								routingContext.response().end(Json.encodePrettily(state));
							}else {
								routingContext.response().setStatusCode(400).end(res.cause().toString());
							}
						});
						
					}else {
						routingContext.response().setStatusCode(400).end(conn.cause().toString());
					}
				});

			}catch (ClassCastException e) {
				routingContext.response().setStatusCode(400).end();
			}

		
	}
	
	
	
	private void putElementTerminal(RoutingContext routingContext) {
		Terminal state = Json.decodeValue(routingContext.getBody().toString(), Terminal.class);		

			try {

				
				mySQLClient.getConnection(conn -> {
					if(conn.succeeded()) {

						SQLConnection connection = conn.result();
						String query = "INSERT INTO Terminal (idTerminal, acierto, fecha, bloqueo, numIntento, puertaRelacionada) "
						+ "VALUES (?, ?, ?, ?, ?, ?) ";
						
						JsonArray paramQuery = new JsonArray()
								.add(state.getId())
								.add(state.isTryState())
								.add(state.getTryDate())
								.add(state.isTryBlock())
								.add(state.getTryNumber())
								.add(state.getRelatedDoor());
						
						connection.queryWithParams(query, paramQuery, res -> {
							if (res.succeeded()) {
								routingContext.response().end(Json.encodePrettily(state));
							}else {
								routingContext.response().setStatusCode(400).end(res.cause().toString());
							}
						});
						
					}else {
						routingContext.response().setStatusCode(400).end(conn.cause().toString());
					}
				});

			}catch (ClassCastException e) {
				routingContext.response().setStatusCode(400).end();
			}

	}
	
	
}
