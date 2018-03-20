package main;

import java.util.Calendar;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;



public class appJsons extends AbstractVerticle{
	
	
	HttpServer server;
	
	
	@Override
	public void start() throws Exception {
		
		super.start();
		
		
		//Json
		JsonObject door = new JsonObject();
		door.put("doorId", 1);
		door.put("doorState", 1); //Estado de la puerta (0 = cerrado, 1 = abierto, 2 = bloqueado)
		
		JsonObject terminal = new JsonObject();
		terminal.put("tryId", "0"); //Identificador del intento
		terminal.put("tryState", true); //Acierto o fallo del intento
		terminal.put("tryDate", Calendar.getInstance().getTimeInMillis()); //Fecha del intento
		terminal.put("tryNumber", 1); //Número de intentos consecutivos actuales
		terminal.put("tryContent", "1234"); //Contenido del intento
		terminal.put("tryBlock", false); //Si este intento bloquea o no
		
		JsonObject pass = new JsonObject();
		pass.put("adminPass", "0000"); //Contraseña de administrador
		pass.put("pass", "1234"); //Contraseña de apertura
		
		
		System.out.println(door.encodePrettily());
		String jsonDoorString = door.encode();
		JsonObject jsonDoorResult = new JsonObject(jsonDoorString);
		System.out.println(jsonDoorResult.getInteger("doorId"));
		System.out.println(jsonDoorResult.getInteger("doorState"));
		
		
		System.out.println(terminal.encodePrettily());
		String jsonTerminalString = terminal.encode();
		JsonObject jsonTerminalResult = new JsonObject(jsonTerminalString);
		System.out.println(jsonTerminalResult.getInteger("tryId"));
		System.out.println(jsonTerminalResult.getBoolean("tryState"));
		System.out.println(jsonTerminalResult.getLong("tryDate"));
		System.out.println(jsonTerminalResult.getInteger("tryNumber"));
		System.out.println(jsonTerminalResult.getString("tryContent"));
		System.out.println(jsonTerminalResult.getBoolean("tryBlock"));
		
		
		System.out.println(pass.encodePrettily());
		String jsonPassString = pass.encode();
		JsonObject jsonPassResult = new JsonObject(jsonPassString);
		System.out.println(jsonPassResult.getString("adminPass"));
		System.out.println(jsonPassResult.getString("pass"));
		
		
		
		
		
		
		/*Lectura lectura = new Lectura();
		lectura.setHumedad(89);
		lectura.setTemperatura(20);
		String lecturaStr = Json.encode(lectura);
		
		System.out.println(lecturaStr);
		Lectura l = Json.decodeValue(lecturaStr, Lectura.class);

		System.out.println(l.toString());	*/	
		
		
		//Bloqueo
		vertx.executeBlocking(param -> {
			try {
				Thread.sleep(10000);
				param.complete("Bloqueo finalizado.");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}, param2 ->{
			System.out.println(param2.result().toString());
		});
		
		
		
		vertx.deployVerticle(DadSecond.class.getName(),
				result -> {
					if (result.succeeded()) {
						System.out.println("Verticle myapp desplegado");
					}else {
						System.out.println("Error al desplegar el Verticle myapp");
						result.cause().getStackTrace();
					}
				});
		
		
		super.start();
		server = vertx.createHttpServer()
		.requestHandler(new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				req.response().end("Hola mundo, cabesa.");
				//server.close();
			}
		})
		.listen(8081);
		
		
		EventBus bus = vertx.eventBus();
		vertx.setPeriodic(4000, handler -> {
			bus.send("mensaje-punto-a-punto", "Hola, soy myapp, ¿estás ahí?", res -> {
				System.out.println(res.result().body());
				res.result().reply("Recibido.");
			});
		});
	
		vertx.setPeriodic(1000, hand -> {
			bus.publish("mensaje-broadcast", "Esto es un mensaje broadcast");
		});
		
	}
	
	public void stop() {
		if(server != null) {
			server.close();
		}
	}
/*
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		vertx.createHttpServer()
		     .requestHandler(r -> {
		    	 r.response().end("<h1>Bienvenido a mi primera aplicacion Vert.x 3</h1>"
		          + "Esto es un ejemplo de una Verticle sencillo para probar el despliegue");
		        })
		     .listen(8089, result -> {
		    	 if (result.succeeded()) {
		    		 startFuture.complete();
		         } else {
		        	 startFuture.fail(result.cause());
		         }
		     });
		     }
		    */ 
	

	
}
