package main;

import javax.xml.ws.RespectBinding;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;

public class DadSecond extends AbstractVerticle{
	
	public void start(Future<Void> startFuture) throws Exception {
		System.out.println("Despliegue de DadSecond satisfactorio.");
		startFuture.succeeded();
		startFuture.complete();
		
		vertx.eventBus().consumer("mensaje-punto-a-punto", msg -> {
			System.out.println(msg.body().toString());
			msg.reply("Sí, estoy aquí", resp ->{
				System.out.println(resp.result().body());
			});
		});
		
		vertx.eventBus().consumer("mensaje-broadcast", msg -> {
			System.out.println(msg.body().toString());
		});
	}
	
}
