package me.escoffier.vertx.completablefuture;

import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(VertxUnitRunner.class)
public class SupplyAndRunAsyncTest {

  private Vertx vertx;
  private Executor executor;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    executor = Executors.newSingleThreadExecutor();
  }

  @After
  public void tearDown() {
    vertx.close();
  }

  @Test
  public void testSupplyAsync() throws ExecutionException, InterruptedException {
    CompletableFuture<String> future = VertxCompletableFuture.supplyAsync(vertx, () -> Thread.currentThread().getName());
    String s = future.get();
    Assert.assertTrue(s.contains("eventloop"));
  }

  @Test
  public void testSupplyBlockingAsync() throws ExecutionException, InterruptedException {
    CompletableFuture<String> future = VertxCompletableFuture.supplyBlockingAsync(vertx, () ->
        Thread.currentThread().getName());
    String s = future.get();
    Assert.assertTrue(s.contains("worker"));
  }

  @Test
  public void testSupplyAsyncWithExecutor() throws ExecutionException, InterruptedException {
    CompletableFuture<String> future = VertxCompletableFuture.supplyAsync(
        () -> Thread.currentThread().getName(),
        executor);
    String s = future.get();
    Assert.assertTrue(s.startsWith("pool"));
  }

  @Test
  public void testRunAsync() throws ExecutionException, InterruptedException {
    AtomicReference<String> reference = new AtomicReference<>();
    CompletableFuture<Void> future = VertxCompletableFuture.runAsync(vertx, () ->
        reference.set(Thread.currentThread().getName()));
    future.get();
    Assert.assertTrue(reference.get().contains("eventloop"));
  }

  @Test
  public void testRunBlockingAsync() throws ExecutionException, InterruptedException {
    AtomicReference<String> reference = new AtomicReference<>();
    CompletableFuture<Void> future = VertxCompletableFuture.runBlockingAsync(vertx, () ->
        reference.set(Thread.currentThread().getName()));
    future.get();
    Assert.assertTrue(reference.get().contains("worker"));
  }

  @Test
  public void testSupplyBlockingAsyncOn() throws ExecutionException, InterruptedException {
    String threadPoolName = "testing-thread-pool";
    WorkerExecutor worker = vertx.createSharedWorkerExecutor(threadPoolName);
    CompletableFuture<String> future = VertxCompletableFuture.supplyBlockingAsyncOn(vertx, worker, () ->
        Thread.currentThread().getName());
    String s = future.get();
    Assert.assertTrue(s.contains(threadPoolName));
  }

  @Test
  public void testRunBlockingAsyncOn() throws ExecutionException, InterruptedException {
    String threadPoolName = "testing-thread-pool";
    WorkerExecutor worker = vertx.createSharedWorkerExecutor(threadPoolName);
    AtomicReference<String> reference = new AtomicReference<>();
    CompletableFuture<Void> future = VertxCompletableFuture.runBlockingAsyncOn(vertx, worker, () ->
        reference.set(Thread.currentThread().getName()));
    future.get();
    Assert.assertTrue(reference.get().contains(threadPoolName));
  }

}
