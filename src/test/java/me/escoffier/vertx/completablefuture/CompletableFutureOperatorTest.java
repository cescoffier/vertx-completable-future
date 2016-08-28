package me.escoffier.vertx.completablefuture;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class CompletableFutureOperatorTest {

  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown() {
    vertx.close();
  }

  @Test
  public void testAllOf(TestContext tc) {
    Async async1 = tc.async();
    Async async2 = tc.async();


    CompletableFuture<Integer> one = new CompletableFuture<>();
    CompletableFuture<Integer> two = new VertxCompletableFuture<>(vertx);
    CompletableFuture<Integer> three = new VertxCompletableFuture<>(vertx);
    CompletableFuture<Integer> four = new CompletableFuture<>();
    CompletableFuture<Integer> failure = new CompletableFuture<>();
    CompletableFuture<Integer> failure2 = new CompletableFuture<>();


    List<CompletableFuture<Integer>> list = Arrays.asList(one, two, three, four);

    vertx.runOnContext(v -> {
      String thread = Thread.currentThread().getName();
      VertxCompletableFuture.allOf(vertx, one, two, three, four).thenApply(x -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        return list.stream()
            .map(CompletableFuture::join)
            .mapToInt(i -> i)
            .sum();
      }).whenCompleteAsync((res, err) -> {
        tc.assertEquals(10, res);
        tc.assertNull(err);
        tc.assertEquals(thread, Thread.currentThread().getName());
        async1.complete();
      });

      VertxCompletableFuture.allOf(vertx, one, two, failure, four, failure2).whenComplete((res, err) -> {
        tc.assertNotNull(err);
        tc.assertTrue(err.getMessage().contains("A"));
        tc.assertEquals(thread, Thread.currentThread().getName());
        async2.complete();
      });
    });

    one.complete(1);
    vertx.setTimer(100, l -> {
      two.complete(2);
      four.complete(4);
      failure.completeExceptionally(new Exception("A"));
    });
    failure2.completeExceptionally(new Exception("B"));
    three.complete(3);

  }

  @Test
  public void testAnyOf(TestContext tc) {
    Async async1 = tc.async();

    CompletableFuture<Integer> one = new CompletableFuture<>();
    CompletableFuture<Integer> two = new VertxCompletableFuture<>(vertx);
    CompletableFuture<Integer> three = new VertxCompletableFuture<>(vertx);
    CompletableFuture<Integer> four = new CompletableFuture<>();
    CompletableFuture<Integer> failure = new CompletableFuture<>();
    CompletableFuture<Integer> failure2 = new CompletableFuture<>();


    vertx.runOnContext(v -> {
      String thread = Thread.currentThread().getName();
      VertxCompletableFuture.anyOf(vertx, one, failure, two, failure2, three, four).thenApply(x -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        return (int) x * (int) x;
      }).whenCompleteAsync((res, err) -> {
        tc.assertNotNull(res);
        tc.assertNull(err);
        tc.assertEquals(thread, Thread.currentThread().getName());
        async1.complete();
      });

    });

    one.complete(1);
    vertx.setTimer(100, l -> {
      two.complete(2);
      four.complete(4);
      failure.completeExceptionally(new Exception("A"));
    });
    failure2.completeExceptionally(new Exception("B"));
    three.complete(3);

  }



}
