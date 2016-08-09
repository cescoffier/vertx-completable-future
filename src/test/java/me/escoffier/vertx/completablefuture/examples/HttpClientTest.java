package me.escoffier.vertx.completablefuture.examples;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class HttpClientTest {

  private Vertx vertx;

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();

    vertx.createHttpServer().requestHandler(request -> {
          switch (request.path()) {
            case "/A":
              request.response().end("42");
              break;
            case "/B":
              request.response().end("23");
              break;
            default:
              request.response().end("Hello");
          }
        }
    ).listen(8080, tc.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext tc) {
    vertx.close(tc.asyncAssertSuccess());
  }

  @Test
  public void test(TestContext tc) {
    Async async = tc.async();

    HttpClientOptions options = new HttpClientOptions().setDefaultPort(8080).setDefaultHost("localhost");
    HttpClient client1 = vertx.createHttpClient(options);
    HttpClient client2 = vertx.createHttpClient(options);

    VertxCompletableFuture<Integer> requestA = new VertxCompletableFuture<>(vertx);
    client1.get("/A").handler(resp -> {
      resp.exceptionHandler(requestA::completeExceptionally)
          .bodyHandler(buffer -> {
            requestA.complete(Integer.parseInt(buffer.toString()));
          });
    }).exceptionHandler(requestA::completeExceptionally).end();

    VertxCompletableFuture<Integer> requestB = new VertxCompletableFuture<>(vertx);
    client2.get("/B").handler(resp -> {
      resp.exceptionHandler(requestB::completeExceptionally)
          .bodyHandler(buffer -> {
            requestB.complete(Integer.parseInt(buffer.toString()));
          });
    }).exceptionHandler(requestB::completeExceptionally).end();


    VertxCompletableFuture.allOf(requestA, requestB).thenApply(v -> requestA.join() + requestB.join())
        .thenAccept(i -> {
          tc.assertEquals(65, i);
          async.complete();
        });
  }

}
