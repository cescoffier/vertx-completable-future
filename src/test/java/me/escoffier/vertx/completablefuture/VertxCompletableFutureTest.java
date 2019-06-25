package me.escoffier.vertx.completablefuture;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Repeat;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(VertxUnitRunner.class)
public class VertxCompletableFutureTest {

  private Vertx vertx;
  private Executor executor;

  @Rule
  public RepeatRule rule = new RepeatRule();

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    ThreadFactory namedThreadFactory =
      new ThreadFactoryBuilder().setNameFormat("my-sad-thread-%d").build();
    executor = Executors.newSingleThreadExecutor(namedThreadFactory);
    vertx.exceptionHandler(tc.exceptionHandler());
  }

  @After
  public void tearDown() {
    vertx.close();
  }

  @Test
  public void testApply(TestContext tc) {
    Async async = tc.async();

    VertxCompletableFuture<Integer> future = new VertxCompletableFuture<>(vertx);
    future.complete(42);

    vertx.runOnContext(x -> {
      String thread = Thread.currentThread().getName();

      future.thenApply(i -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        tc.assertEquals(42, i);
        async.complete();
        return null;
      });
    });

  }

  @Test
  public void testApplyAsync(TestContext tc) {
    Async async = tc.async();

    vertx.runOnContext(x -> {
      VertxCompletableFuture<Integer> future = new VertxCompletableFuture<>(vertx);
      future.complete(42);

      String thread = Thread.currentThread().getName();

      future.thenApplyAsync(i -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        tc.assertEquals(42, i);
        async.complete();
        return null;
      });
    });

  }

  @Test
  public void testApplyAsyncWithExecutor(TestContext tc) {
    Async async = tc.async();

    vertx.runOnContext(x -> {
      VertxCompletableFuture<Integer> future = new VertxCompletableFuture<>(vertx);
      future.complete(42);

      String thread = Thread.currentThread().getName();

      future.thenApplyAsync(i -> {
        tc.assertNotEquals(thread, Thread.currentThread().getName());
        tc.assertEquals(42, i);
        async.complete();
        return null;
      }, executor);
    });

  }

  @Test
  public void testAccept(TestContext tc) {
    Async async = tc.async();

    VertxCompletableFuture<Integer> future = new VertxCompletableFuture<>(vertx);
    future.complete(42);

    vertx.runOnContext(x -> {
      String thread = Thread.currentThread().getName();

      future.thenAccept(i -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        tc.assertEquals(42, i);
        async.complete();
      });
    });

  }

  @Test
  public void testAcceptAsync(TestContext tc) {
    Async async = tc.async();

    vertx.runOnContext(x -> {
      VertxCompletableFuture<Integer> future = new VertxCompletableFuture<>(vertx);
      future.complete(42);

      String thread = Thread.currentThread().getName();

      future.thenAcceptAsync(i -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        tc.assertEquals(42, i);
        async.complete();
      });
    });

  }

  @Test
  public void testAcceptAsyncWithExecutor(TestContext tc) {
    Async async = tc.async();

    vertx.runOnContext(x -> {
      VertxCompletableFuture<Integer> future = new VertxCompletableFuture<>(vertx);
      future.complete(42);

      String thread = Thread.currentThread().getName();

      future.thenAcceptAsync(i -> {
        tc.assertNotEquals(thread, Thread.currentThread().getName());
        tc.assertEquals(42, i);
        async.complete();
      }, executor);
    });

  }

  @Test
  public void testRun(TestContext tc) {
    Async async = tc.async();

    VertxCompletableFuture<Integer> future = new VertxCompletableFuture<>(vertx);
    future.complete(42);

    vertx.runOnContext(x -> {
      String thread = Thread.currentThread().getName();

      future.thenRun(() -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        async.complete();
      });
    });

  }

  @Test
  public void testRunAsync(TestContext tc) {
    Async async = tc.async();

    vertx.runOnContext(x -> {
      VertxCompletableFuture<Integer> future = new VertxCompletableFuture<>(vertx);
      future.complete(42);

      String thread = Thread.currentThread().getName();

      future.thenRunAsync(() -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        async.complete();
      });
    });

  }

  @Test
  public void testRunAsyncWithExecutor(TestContext tc) {
    Async async = tc.async();

    vertx.runOnContext(x -> {
      VertxCompletableFuture<Integer> future = new VertxCompletableFuture<>(vertx);
      future.complete(42);

      String thread = Thread.currentThread().getName();

      future.thenRunAsync(() -> {
        tc.assertNotEquals(thread, Thread.currentThread().getName());
        async.complete();
      }, executor);
    });

  }

  @Test
  public void testRunAfterEither(TestContext tc) {
    // Success and success
    Async async1 = tc.async();
    // Failure and success
    Async async2 = tc.async();
    // Success and Failure
    Async async3 = tc.async();
    // Failure and failure
    Async async4 = tc.async();

    VertxCompletableFuture<Integer> success1 = new VertxCompletableFuture<>(vertx);
    VertxCompletableFuture<Integer> success2 = new VertxCompletableFuture<>(vertx);
    VertxCompletableFuture<Integer> failure1 = new VertxCompletableFuture<>(vertx);
    VertxCompletableFuture<Integer> failure2 = new VertxCompletableFuture<>(vertx);

    vertx.runOnContext(x -> {
      String thread = Thread.currentThread().getName();

      success1.runAfterEither(success2, () -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        async1.complete();
      });

      failure1.runAfterEither(success2, () -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        async2.complete();
      });

      success1.runAfterEither(failure2, () -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        async3.complete();
      });

      failure1.runAfterEither(failure2, () -> {
        tc.fail("Should not be called");
      }).whenComplete((res, err) -> {
        tc.assertNotNull(err);
        tc.assertNull(res);
        tc.assertEquals(thread, Thread.currentThread().getName());
        async4.complete();
      });

      success1.complete(42);
      failure2.completeExceptionally(new Exception("2"));
      vertx.setTimer(100, v -> {
        success2.complete(1);
        failure1.completeExceptionally(new Exception("1"));
      });
    });
  }

  @Test
  public void testRunAfterEitherAsync(TestContext tc) {
    // Success and success
    Async async1 = tc.async();
    // Failure and success
    Async async2 = tc.async();
    // Success and Failure
    Async async3 = tc.async();
    // Failure and failure
    Async async4 = tc.async();

    vertx.runOnContext(x -> {
      VertxCompletableFuture<Integer> success1 = new VertxCompletableFuture<>(vertx);
      VertxCompletableFuture<Integer> success2 = new VertxCompletableFuture<>(vertx);
      VertxCompletableFuture<Integer> failure1 = new VertxCompletableFuture<>(vertx);
      VertxCompletableFuture<Integer> failure2 = new VertxCompletableFuture<>(vertx);

      String thread = Thread.currentThread().getName();

      success1.runAfterEitherAsync(success2, () -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        async1.complete();
      });

      failure1
        .runAfterEitherAsync(success2, () -> tc.fail("Should not be called"))
        .whenComplete((res, err) -> {
          tc.assertNotNull(err);
          tc.assertNull(res);
          tc.assertEquals(thread, Thread.currentThread().getName());
          async2.complete();
        });

      success1.runAfterEitherAsync(failure2, () -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        async3.complete();
      });

      failure1.runAfterEitherAsync(failure2, () -> {
        tc.fail("Should not be called");
      }).whenComplete((res, err) -> {
        tc.assertNotNull(err);
        tc.assertNull(res);
        tc.assertEquals(thread, Thread.currentThread().getName());
        async4.complete();
      });

      success1.complete(42);
      failure2.completeExceptionally(new Exception("2"));
      vertx.setTimer(100, v -> {
        success2.complete(1);
        failure1.completeExceptionally(new Exception("1"));
      });
    });
  }

  @Test
  public void testRunAfterEitherAsyncWithExecutor(TestContext tc) {
    // Success and success
    Async async1 = tc.async();
    // Failure and success
    Async async2 = tc.async();
    // Success and Failure
    Async async3 = tc.async();
    // Failure and failure
    Async async4 = tc.async();

    VertxCompletableFuture<Integer> success1 = new VertxCompletableFuture<>(vertx);
    VertxCompletableFuture<Integer> success2 = new VertxCompletableFuture<>(vertx);
    VertxCompletableFuture<Integer> failure1 = new VertxCompletableFuture<>(vertx);
    VertxCompletableFuture<Integer> failure2 = new VertxCompletableFuture<>(vertx);

    vertx.runOnContext(x -> {
      String thread = Thread.currentThread().getName();

      success1.runAfterEitherAsync(success2, () -> {
        tc.assertNotEquals(thread, Thread.currentThread().getName());
        async1.complete();
      }, executor);

      failure2
        .runAfterEitherAsync(success2, () -> tc.fail("Should not be called"), executor)
        .whenComplete((res, err) -> {
          tc.assertNotNull(err);
          tc.assertNull(res);
          tc.assertNotEquals(thread, Thread.currentThread().getName());
          async2.complete();
        });

      success1.runAfterEitherAsync(failure2, () -> {
        tc.assertNotEquals(thread, Thread.currentThread().getName());
        async3.complete();
      }, executor);

      failure1.runAfterEitherAsync(failure2, () -> {
        tc.fail("Should not be called");
      }, executor).whenComplete((res, err) -> {
        tc.assertNotNull(err);
        tc.assertNull(res);
        tc.assertNotEquals(thread, Thread.currentThread().getName());
        async4.complete();
      });

      success1.complete(42);
      failure2.completeExceptionally(new Exception("2"));
      vertx.setTimer(100, v -> {
        success2.complete(1);
        failure1.completeExceptionally(new Exception("1"));
      });
    });
  }

  @Test
  public void testCombine(TestContext tc) {
    Async async = tc.async();

    VertxCompletableFuture<Integer> future1 = new VertxCompletableFuture<>(vertx);
    future1.complete(42);

    VertxCompletableFuture<Integer> future2 = new VertxCompletableFuture<>(vertx);
    future2.complete(1);

    vertx.runOnContext(v -> {
      String thread = Thread.currentThread().getName();

      future1.thenCombine(future2, (x, y) -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        tc.assertEquals(42, x);
        tc.assertEquals(1, y);
        return x + y;
      }).thenAccept(i -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        tc.assertEquals(i, 43);
        async.complete();
      });
    });

  }

  @Test
  public void testCombineWithFailures(TestContext tc) {
    // Success and success
    Async async1 = tc.async();
    // Failure and success
    Async async2 = tc.async();
    // Success and Failure
    Async async3 = tc.async();
    // Failure and failure
    Async async4 = tc.async();

    VertxCompletableFuture<Integer> success1 = new VertxCompletableFuture<>(vertx);
    VertxCompletableFuture<Integer> success2 = new VertxCompletableFuture<>(vertx);
    VertxCompletableFuture<Integer> failure1 = new VertxCompletableFuture<>(vertx);
    VertxCompletableFuture<Integer> failure2 = new VertxCompletableFuture<>(vertx);

    vertx.runOnContext(v -> {
      String thread = Thread.currentThread().getName();

      success1.thenCombine(success2, (x, y) -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        tc.assertEquals(42, x);
        tc.assertEquals(1, y);
        return x + y;
      }).thenAccept(i -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        tc.assertEquals(i, 43);
        async1.complete();
      });

      failure1.thenCombine(success1, (x, y) -> {
        tc.fail("Should not be called");
        return null;
      }).whenComplete((res, err) -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        tc.assertNotNull(err);
        tc.assertNull(res);
        async2.complete();
      });

      success1.thenCombine(failure2, (x, y) -> {
        tc.fail("Should not be called");
        return null;
      }).whenComplete((res, err) -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        tc.assertNotNull(err);
        tc.assertNull(res);
        async3.complete();
      });

      failure1.thenCombine(failure2, (x, y) -> {
        tc.fail("Should not be called");
        return null;
      }).whenComplete((res, err) -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        tc.assertNotNull(err);
        tc.assertNull(res);
        async4.complete();
      });

      success1.complete(42);
      success2.complete(1);
      failure1.completeExceptionally(new Exception("1"));
      failure2.completeExceptionally(new Exception("2"));
    });

  }

  @Test
  public void testCombineAsync(TestContext tc) {
    Async async = tc.async();

    VertxCompletableFuture<Integer> future2 = new VertxCompletableFuture<>(vertx);
    future2.complete(1);

    vertx.runOnContext(v -> {
      String thread = Thread.currentThread().getName();

      VertxCompletableFuture<Integer> future1 = new VertxCompletableFuture<>(vertx);
      future1.complete(42);

      future1.thenCombineAsync(future2, (x, y) -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        tc.assertEquals(42, x);
        tc.assertEquals(1, y);
        return x + y;
      }).thenAccept(i -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        tc.assertEquals(i, 43);
        async.complete();
      });
    });

  }

  @Test
  public void testCombineAsyncWithExecutor(TestContext tc) {
    Async async = tc.async();

    VertxCompletableFuture<Integer> future2 = new VertxCompletableFuture<>(vertx);
    future2.complete(1);

    vertx.runOnContext(v -> {
      String thread = Thread.currentThread().getName();

      VertxCompletableFuture<Integer> future1 = new VertxCompletableFuture<>(vertx);
      future1.complete(42);

      CompletableFuture<Integer> test = future1.thenCombineAsync(future2, (x, y) -> {
        tc.assertNotEquals(thread, Thread.currentThread().getName());
        tc.assertEquals(42, x);
        tc.assertEquals(1, y);
        return x + y;
      }, executor);
      System.out.println(test);
      test.thenAcceptAsync(i -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        tc.assertEquals(i, 43);
        async.complete();
      });
    });

  }

  @Test
  public void testExceptionally(TestContext tc) {
    Async async1 = tc.async();
    Async async2 = tc.async();

    VertxCompletableFuture<Integer> failure = new VertxCompletableFuture<>(vertx);
    VertxCompletableFuture<Integer> success = new VertxCompletableFuture<>(vertx);

    failure.exceptionally(t -> 43).thenAccept(i -> {
      tc.assertEquals(i, 43);
      async1.complete();
    });

    success.exceptionally(t -> 43).thenAccept(i -> {
      tc.assertEquals(i, 42);
      async2.complete();
    });

    success.complete(42);
    failure.completeExceptionally(new RuntimeException("my bad"));

  }

  @Test
  public void testHandle(TestContext tc) {
    Async async1 = tc.async();
    Async async2 = tc.async();

    VertxCompletableFuture<Integer> failure = new VertxCompletableFuture<>(vertx);
    VertxCompletableFuture<Integer> success = new VertxCompletableFuture<>(vertx);

    failure.handle((r, t) -> 43).thenAccept(i -> {
      tc.assertEquals(i, 43);
      async1.complete();
    });

    success.handle((r, t) -> r).thenAccept(i -> {
      tc.assertEquals(i, 42);
      async2.complete();
    });

    success.complete(42);
    failure.completeExceptionally(new RuntimeException("my bad"));

  }

  @Test
  public void testHandleAsync(TestContext tc) {
    Async async1 = tc.async();
    Async async2 = tc.async();

    VertxCompletableFuture<Integer> failure = new VertxCompletableFuture<>(vertx);
    VertxCompletableFuture<Integer> success = new VertxCompletableFuture<>(vertx);

    vertx.runOnContext(v -> {
      String thread = Thread.currentThread().getName();
      Context context = Vertx.currentContext();

      failure.withContext(context).handleAsync((r, t) -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        if (t != null) {
          return 43;
        }
        return r;
      }).thenAccept(i -> {
        tc.assertEquals(i, 43);
        async1.complete();
      });

      success.withContext(context).handleAsync((r, t) -> {
        tc.assertEquals(Thread.currentThread().getName(), thread);
        if (t != null) {
          return 43;
        }
        return r;
      }).thenAccept(i -> {
        tc.assertEquals(i, 42);
        async2.complete();
      });
    });
    success.complete(42);
    failure.completeExceptionally(new RuntimeException("my bad"));

  }

  @Test
  public void testHandleAsyncWithExecutor(TestContext tc) {
    Async async1 = tc.async();
    Async async2 = tc.async();

    VertxCompletableFuture<Integer> failure = new VertxCompletableFuture<>(vertx);
    VertxCompletableFuture<Integer> success = new VertxCompletableFuture<>(vertx);

    vertx.runOnContext(v -> {
      String thread = Thread.currentThread().getName();
      Context context = Vertx.currentContext();

      failure.withContext(context).handleAsync((r, t) -> {
        tc.assertNotEquals(Thread.currentThread().getName(), thread);
        if (t != null) {
          return 43;
        }
        return r;
      }, executor).thenAccept(i -> {
        tc.assertEquals(i, 43);
        async1.complete();
      });

      success.withContext(context).handleAsync((r, t) -> {
        tc.assertNotEquals(Thread.currentThread().getName(), thread);
        if (t != null) {
          return 43;
        }
        return r;
      }, executor).thenAccept(i -> {
        tc.assertEquals(i, 42);
        async2.complete();
      });
    });
    success.complete(42);
    failure.completeExceptionally(new RuntimeException("my bad"));
  }

  @Test
  public void testAcceptBoth(TestContext tc) {
    Async async = tc.async();

    VertxCompletableFuture<Integer> future1 = new VertxCompletableFuture<>(vertx);
    future1.complete(42);

    VertxCompletableFuture<Integer> future2 = new VertxCompletableFuture<>(vertx);
    future2.complete(1);

    vertx.runOnContext(v -> {
      String thread = Thread.currentThread().getName();

      future1.thenAcceptBoth(future2, (x, y) -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        tc.assertEquals(42, x);
        tc.assertEquals(1, y);
        async.complete();
      });
    });

  }

  @Test
  public void testAcceptBothAsync(TestContext tc) {
    Async async = tc.async();

    VertxCompletableFuture<Integer> future2 = new VertxCompletableFuture<>(vertx);
    future2.complete(1);

    vertx.runOnContext(v -> {
      String thread = Thread.currentThread().getName();

      VertxCompletableFuture<Integer> future1 = new VertxCompletableFuture<>(vertx);
      future1.complete(42);

      future1.thenAcceptBothAsync(future2, (x, y) -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        tc.assertEquals(42, x);
        tc.assertEquals(1, y);
        async.complete();
      });
    });

  }

  @Test
  public void testAcceptBothAsyncWithExecutor(TestContext tc) {
    Async async = tc.async();
    Async async2 = tc.async();


    VertxCompletableFuture<Integer> future2 = new VertxCompletableFuture<>(vertx);
    future2.complete(1);

    vertx.runOnContext(v -> {
      String thread = Thread.currentThread().getName();

      VertxCompletableFuture<Integer> future1 = new VertxCompletableFuture<>(vertx);
      vertx.setTimer(10, l -> future1.complete(42));

      future1.thenAcceptBothAsync(future2, (x, y) -> {
        tc.assertNotEquals(thread, Thread.currentThread().getName());
        tc.assertEquals(42, x);
        tc.assertEquals(1, y);
        async.complete();
      }, executor);

      future2.thenAcceptBothAsync(future1, (x, y) -> {
        tc.assertNotEquals(thread, Thread.currentThread().getName());
        tc.assertEquals(42, y);
        tc.assertEquals(1, x);
        async2.complete();
      }, executor);
    });

  }

  @Test
  public void testRunAfterBoth(TestContext tc) {
    Async async = tc.async();

    VertxCompletableFuture<Integer> future1 = new VertxCompletableFuture<>(vertx);
    future1.complete(42);

    VertxCompletableFuture<Integer> future2 = new VertxCompletableFuture<>(vertx);
    future2.complete(1);

    vertx.runOnContext(v -> {
      String thread = Thread.currentThread().getName();

      future1.runAfterBoth(future2, () -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        async.complete();
      });
    });

  }

  @Test
  public void testRunAfterBothAsync(TestContext tc) {
    Async async = tc.async();

    VertxCompletableFuture<Integer> future2 = new VertxCompletableFuture<>(vertx);
    future2.complete(1);

    vertx.runOnContext(v -> {
      String thread = Thread.currentThread().getName();

      VertxCompletableFuture<Integer> future1 = new VertxCompletableFuture<>(vertx);
      future1.complete(42);

      future1.runAfterBoth(future2, () -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        async.complete();
      });
    });

  }

  @Test
  public void testRunAfterBothAsyncWithExecutor(TestContext tc) {
    Async async = tc.async();
    Async async2 = tc.async();


    VertxCompletableFuture<Integer> future2 = new VertxCompletableFuture<>(vertx);
    future2.complete(1);

    vertx.runOnContext(v -> {
      String thread = Thread.currentThread().getName();

      VertxCompletableFuture<Integer> future1 = new VertxCompletableFuture<>(vertx);
      vertx.setTimer(10, l -> future1.complete(42));

      future1.runAfterBothAsync(future2, () -> {
        tc.assertNotEquals(thread, Thread.currentThread().getName());
        async.complete();
      }, executor);

      future2.runAfterBothAsync(future1, () -> {
        tc.assertNotEquals(thread, Thread.currentThread().getName());
        async2.complete();
      }, executor);
    });

  }

  private CompletableFuture<Integer> inc(int input) {
    VertxCompletableFuture<Integer> future = new VertxCompletableFuture<>(vertx);
    vertx.setTimer(10, l -> future.complete(input + 1));
    return future;
  }

  @Test
  public void testThenCompose(TestContext tc) {
    Async async = tc.async();

    VertxCompletableFuture<Integer> future1 = new VertxCompletableFuture<>(vertx);

    vertx.runOnContext(v -> {
      String thread = Thread.currentThread().getName();

      future1.thenCompose(this::inc).thenCompose(this::inc).thenAccept(i -> {
        tc.assertEquals(thread, Thread.currentThread().getName());
        tc.assertEquals(i, 44);
        async.complete();
      });

      future1.complete(42);
    });
  }

  @Test
  public void testThenComposeAsync(TestContext tc) {
    Async async = tc.async();

    VertxCompletableFuture<Integer> future1 = new VertxCompletableFuture<>(vertx);

    vertx.runOnContext(v -> {
      String thread = Thread.currentThread().getName();

      future1.withContext()
        .thenComposeAsync(this::inc)
        .thenComposeAsync(this::inc)
        .thenAccept(i -> {
          tc.assertEquals(thread, Thread.currentThread().getName());
          tc.assertEquals(i, 44);
          async.complete();
        });

      future1.complete(42);
    });
  }

  @Test
  public void testThenComposeAsyncWithExecutor(TestContext tc) {
    Async async = tc.async();

    VertxCompletableFuture<Integer> future1 = new VertxCompletableFuture<>(vertx);

    vertx.runOnContext(v -> {
      String thread = Thread.currentThread().getName();

      future1.thenComposeAsync(this::inc, executor).thenCompose(this::inc).thenAccept(i -> {
        tc.assertNotEquals(thread, Thread.currentThread().getName());
        tc.assertEquals(i, 44);
        async.complete();
      });

      future1.complete(42);
    });
  }

  @Test
  public void testAcceptEither(TestContext tc) {
    // Success and success
    Async async1 = tc.async();
    Async async11 = tc.async();
    // Failure and Success
    Async async2 = tc.async();
    // Success and Failure
    Async async3 = tc.async();

    VertxCompletableFuture<Integer> success = new VertxCompletableFuture<>(vertx);
    success.complete(1);

    VertxCompletableFuture<Integer> success2 = new VertxCompletableFuture<>(vertx);
    vertx.setTimer(100, l -> success2.complete(42));


    VertxCompletableFuture<Integer> failure = new VertxCompletableFuture<>(vertx);
    failure.completeExceptionally(new RuntimeException("My bad"));

    vertx.runOnContext(v -> {
      String thread = Thread.currentThread().getName();

      success.acceptEither(
        success2,
        i -> {
          tc.assertEquals(thread, Thread.currentThread().getName());
          tc.assertEquals(i, 1);
          async1.complete();
        }
      );

      success2.acceptEither(
        success,
        i -> {
          tc.assertEquals(thread, Thread.currentThread().getName());
          tc.assertEquals(i, 1);
          async11.complete();
        }
      );

      failure.acceptEither(
        success,
        i -> {
          tc.fail("Should not be called");
        }
      ).whenComplete((res, err) -> {
        tc.assertNotNull(err);
        tc.assertNull(res);
        tc.assertEquals(thread, Thread.currentThread().getName());
        async2.complete();
      });

      success.acceptEither(
        failure,
        i -> {
          tc.assertEquals(thread, Thread.currentThread().getName());
          tc.assertEquals(i, 1);
          async3.complete();
        }
      );
    });

  }

  @Test
  public void testAcceptEitherAsync(TestContext tc) {
    // Success and success
    Async async1 = tc.async();
    Async async11 = tc.async();
    // Failure and Success
    Async async2 = tc.async();
    // Success and Failure
    Async async3 = tc.async();

    VertxCompletableFuture<Integer> success = new VertxCompletableFuture<>(vertx);
    success.complete(1);

    VertxCompletableFuture<Integer> success2 = new VertxCompletableFuture<>(vertx);
    vertx.setTimer(100, l -> success2.complete(42));


    VertxCompletableFuture<Integer> failure = new VertxCompletableFuture<>(vertx);
    failure.completeExceptionally(new RuntimeException("My bad"));

    vertx.runOnContext(v -> {
      String thread = Thread.currentThread().getName();

      success.withContext().acceptEitherAsync(
        success2,
        i -> {
          tc.assertEquals(thread, Thread.currentThread().getName());
          tc.assertEquals(i, 1);
          async1.complete();
        }
      );

      success2.withContext().acceptEitherAsync(
        success,
        i -> {
          tc.assertEquals(thread, Thread.currentThread().getName());
          tc.assertEquals(i, 1);
          async11.complete();
        }
      );

      failure.acceptEitherAsync(
        success,
        i -> tc.fail("Should not be called")
      ).whenComplete((res, err) -> {
        tc.assertNotNull(err);
        tc.assertNull(res);
        async2.complete();
      });

      success.withContext().acceptEitherAsync(
        failure,
        i -> {
          tc.assertEquals(thread, Thread.currentThread().getName());
          tc.assertEquals(i, 1);
          async3.complete();
        }
      );
    });

  }

  @Test
  @Repeat(50)
  public void testAcceptEitherAsyncWithExecutor(TestContext tc) {
    // Success and success
    Async async1 = tc.async();
    Async async11 = tc.async();
    // Failure and Success
    Async async2 = tc.async();
    // Success and Failure
    Async async3 = tc.async();

    VertxCompletableFuture<Integer> success = new VertxCompletableFuture<>(vertx);
    success.complete(1);

    VertxCompletableFuture<Integer> success2 = new VertxCompletableFuture<>(vertx);
    vertx.setTimer(100, l -> success2.complete(42));


    VertxCompletableFuture<Integer> failure = new VertxCompletableFuture<>(vertx);
    failure.completeExceptionally(new RuntimeException("My bad"));

    vertx.runOnContext(v -> {
      String thread = Thread.currentThread().getName();

      success.acceptEitherAsync(
        success2,
        i -> {
          tc.assertNotEquals(thread, Thread.currentThread().getName());
          tc.assertEquals(i, 1);
          async1.complete();
        }, executor
      );

      success2.acceptEitherAsync(
        success,
        i -> {
          tc.assertNotEquals(thread, Thread.currentThread().getName());
          tc.assertEquals(i, 1);
          async11.complete();
        },
        executor
      );

      failure.acceptEitherAsync(
        success,
        i -> tc.fail("Should not be called"),
        executor
      ).whenComplete((res, err) -> {
        tc.assertNotNull(err);
        tc.assertNull(res);
        // We can't enforce the thread used here - it's non-deterministic.
        async2.complete();
      });

      success.acceptEitherAsync(
        failure,
        i -> {
          tc.assertNotEquals(thread, Thread.currentThread().getName());
          tc.assertEquals(i, 1);
          async3.complete();
        },
        executor
      );
    });

  }

  @Test
  public void testApplyToEither(TestContext tc) {
    // Success and success
    Async async1 = tc.async();
    Async async11 = tc.async();
    // Failure and Success
    Async async2 = tc.async();
    // Success and Failure
    Async async3 = tc.async();

    VertxCompletableFuture<Integer> success = new VertxCompletableFuture<>(vertx);
    success.complete(1);

    VertxCompletableFuture<Integer> success2 = new VertxCompletableFuture<>(vertx);
    vertx.setTimer(100, l -> success2.complete(42));


    VertxCompletableFuture<Integer> failure = new VertxCompletableFuture<>(vertx);
    failure.completeExceptionally(new RuntimeException("My bad"));

    vertx.runOnContext(v -> {
      String thread = Thread.currentThread().getName();

      success.applyToEither(
        success2,
        i -> {
          tc.assertEquals(thread, Thread.currentThread().getName());
          tc.assertEquals(i, 1);
          return i + 1;
        }
      ).whenComplete((res, err) -> {
        tc.assertNotNull(res);
        tc.assertNull(err);
        tc.assertEquals(res, 2);
        async1.complete();
      });

      success2.applyToEither(
        success,
        i -> {
          tc.assertEquals(thread, Thread.currentThread().getName());
          tc.assertEquals(i, 1);
          return i + 5;
        }
      ).whenComplete((res, err) -> {
        tc.assertNotNull(res);
        tc.assertNull(err);
        tc.assertEquals(res, 6);
        async11.complete();
      });

      success.applyToEither(
        failure,
        i -> {
          tc.assertEquals(thread, Thread.currentThread().getName());
          tc.assertEquals(i, 1);
          return i * 2;
        }
      ).whenComplete((res, err) -> {
        tc.assertNotNull(res);
        tc.assertNull(err);
        tc.assertEquals(res, 2);
        async3.complete();
      });

      // Fails, because the first one fails.
      failure.applyToEither(
        success,
        i -> {
          tc.fail("Should not be called");
          return null;
        }
      ).whenComplete((res, err) -> {
        tc.assertNotNull(err);
        tc.assertNull(res);
        tc.assertEquals(thread, Thread.currentThread().getName());
        async2.complete();
      });
    });
  }

  @Test
  public void testApplyEitherAsync(TestContext tc) {
    // Success and success
    Async async1 = tc.async();
    Async async11 = tc.async();
    // Failure and Success
    Async async2 = tc.async();
    // Success and Failure
    Async async3 = tc.async();


    vertx.runOnContext(v -> {
      VertxCompletableFuture<Integer> success = new VertxCompletableFuture<>(vertx);
      success.complete(1);

      VertxCompletableFuture<Integer> success2 = new VertxCompletableFuture<>(vertx);
      vertx.setTimer(100, l -> success2.complete(42));

      VertxCompletableFuture<Integer> failure = new VertxCompletableFuture<>(vertx);
      failure.completeExceptionally(new RuntimeException("My bad"));
      String thread = Thread.currentThread().getName();

      success.applyToEitherAsync(
        success2,
        i -> {
          tc.assertEquals(thread, Thread.currentThread().getName());
          tc.assertEquals(i, 1);
          return i + 1;
        }
      ).whenComplete((res, err) -> {
        tc.assertNotNull(res);
        tc.assertNull(err);
        tc.assertEquals(res, 2);
        async1.complete();
      });

      success2.applyToEitherAsync(
        success,
        i -> {
          tc.assertEquals(thread, Thread.currentThread().getName());
          tc.assertEquals(i, 1);
          return i + 5;
        }
      ).whenComplete((res, err) -> {
        tc.assertNotNull(res);
        tc.assertNull(err);
        tc.assertEquals(res, 6);
        async11.complete();
      });

      failure.applyToEitherAsync(
        success,
        i -> {
          tc.fail("Should not be called");
          return null;
        }
      ).whenComplete((res, err) -> {
        tc.assertNotNull(err);
        tc.assertNull(res);
        tc.assertEquals(thread, Thread.currentThread().getName());
        async2.complete();
      });

      success.applyToEitherAsync(
        failure,
        i -> {
          tc.assertEquals(thread, Thread.currentThread().getName());
          tc.assertEquals(i, 1);
          return i * 2;
        }
      ).whenComplete((res, err) -> {
        tc.assertNotNull(res);
        tc.assertNull(err);
        tc.assertEquals(res, 2);
        async3.complete();
      });
    });

  }

  @Test
  @Repeat(50)
  public void testApplyEitherAsyncWithExecutor(TestContext tc) {
    // Success and success
    Async async1 = tc.async();
    Async async11 = tc.async();
    // Failure and Success
    Async async2 = tc.async();
    // Success and Failure
    Async async3 = tc.async();

    VertxCompletableFuture<Integer> success = new VertxCompletableFuture<>(vertx);
    vertx.setTimer(500, l -> success.complete(1));

    VertxCompletableFuture<Integer> success2 = new VertxCompletableFuture<>(vertx);
    vertx.setTimer(1500, l -> success2.complete(42));

    VertxCompletableFuture<Integer> failure = new VertxCompletableFuture<>(vertx);
    failure.completeExceptionally(new RuntimeException("My bad"));

    VertxCompletableFuture<Integer> failure2 = new VertxCompletableFuture<>(vertx);
    vertx.setTimer(1500, l -> failure.completeExceptionally(new RuntimeException("My bad")));

    vertx.runOnContext(v -> {
      String thread = Thread.currentThread().getName();

      success.applyToEitherAsync(
        success2,
        i -> {
          tc.assertNotEquals(thread, Thread.currentThread().getName());
          tc.assertEquals(i, 1);
          return i + 1;
        },
        executor
      ).whenComplete((res, err) -> {
        tc.assertNotNull(res);
        tc.assertNull(err);
        tc.assertEquals(res, 2);
        async1.complete();
      });

      success2.applyToEitherAsync(
        success,
        i -> {
          tc.assertNotEquals(thread, Thread.currentThread().getName());
          tc.assertEquals(i, 1);
          return i + 5;
        },
        executor
      ).whenComplete((res, err) -> {
        tc.assertNotNull(res);
        tc.assertNull(err);
        tc.assertEquals(res, 6);
        async11.complete();
      });

      failure
        .applyToEitherAsync(
        success2,
        i -> {
          tc.fail("should not be called");
          return "not the right result";
        },
        executor
      )
        .whenComplete((res, err) -> {
          tc.assertNotNull(err);
          tc.assertNull(res);
          // We can't enforce the thread used here.
          async2.complete();
        });

      success.applyToEitherAsync(
        failure2,
        i -> {
          tc.assertNotEquals(thread, Thread.currentThread().getName());
          tc.assertEquals(i, 1);
          return i * 2;
        },
        executor
      ).whenComplete((res, err) -> {
        tc.assertNotNull(res);
        tc.assertNull(err);
        tc.assertNotEquals(thread, Thread.currentThread().getName());
        tc.assertEquals(res, 2);
        async3.complete();
      });
    });

  }


  @Test
  public void testGetGetNowAndJoin(TestContext tc) throws ExecutionException, InterruptedException,
    TimeoutException {
    CompletableFuture<Integer> success = new VertxCompletableFuture<>(vertx);
    CompletableFuture<Integer> failure = new VertxCompletableFuture<>(vertx);

    vertx.setTimer(1000, l -> {
      success.complete(42);
      failure.completeExceptionally(new Exception("My Bad !"));
    });

    try {
      success.get(1, TimeUnit.MILLISECONDS);
      tc.fail("timeout expected");
    } catch (TimeoutException e) {
      // OK
    }

    tc.assertEquals(22, success.getNow(22));

    tc.assertEquals(success.get(), 42);
    tc.assertEquals(success.join(), 42);

    try {
      failure.get();
      tc.fail("Exception expected");
    } catch (ExecutionException e) {
      // OK.
    }

    try {
      failure.join();
      tc.fail("Exception expected");
    } catch (CompletionException e) {
      // OK
    }

    tc.assertEquals(42, success.get(10, TimeUnit.SECONDS));

    CompletableFuture<Integer> immediate = new VertxCompletableFuture<>(vertx);
    immediate.complete(55);
    // Immediate get.
    tc.assertEquals(immediate.get(), 55);
    tc.assertEquals(immediate.join(), 55);
    tc.assertEquals(immediate.getNow(23), 55);
    tc.assertEquals(immediate.get(1, TimeUnit.SECONDS), 55);

    immediate.obtrudeValue(23);
    tc.assertEquals(immediate.get(), 23);
    tc.assertEquals(immediate.join(), 23);
    tc.assertEquals(immediate.get(1, TimeUnit.SECONDS), 23);

    CompletableFuture<Integer> cancelled = new VertxCompletableFuture<>(vertx);
    cancelled.cancel(false);

    try {
      cancelled.get();
      tc.fail("Exception expected");
    } catch (CancellationException e) {
      // OK
    }

    try {
      cancelled.join();
      tc.fail("Exception expected");
    } catch (CancellationException e) {
      // OK
    }

    try {
      cancelled.getNow(22);
      tc.fail("Exception expected");
    } catch (CancellationException e) {
      // OK
    }
  }


  @Test
  public void testTwoDependents(TestContext tc) {
    Async async1 = tc.async();
    Async async2 = tc.async();

    vertx.runOnContext(v -> {
      VertxCompletableFuture<Integer> future = new VertxCompletableFuture<>(vertx);
      future.complete(42);

      future.thenApplyAsync(i -> {
        tc.assertEquals(42, i);
        return i + 1;
      }).thenAcceptAsync(i -> {
        tc.assertEquals(43, i);
        async1.complete();
      });

      future.thenAccept(i -> {
        tc.assertEquals(42, i);
        async2.complete();
      });
    });
  }

  private CompletableFuture<String> display(String s) {
    CompletableFuture<String> future = new VertxCompletableFuture<>(vertx);
    future.complete("Result: " + s);
    return future;
  }

  @Test
  public void testMoreComplexComposition() throws ExecutionException, InterruptedException {
    CompletableFuture<Integer> first = new VertxCompletableFuture<>(vertx);
    CompletableFuture<Integer> second = first.thenApply(x -> x + 1);
    CompletableFuture<String> toString = second.thenApplyAsync(i -> Integer.toString(i));
    CompletableFuture<String> displayed = toString.thenCompose(this::display);

    vertx.setTimer(100, l -> first.complete(42));

    Assert.assertEquals("Result: " + 43, displayed.join());
    Assert.assertTrue(first.get() == 42);
    Assert.assertTrue(second.get() == 43);
    Assert.assertEquals(toString.get(), "43");
    Assert.assertTrue(displayed.isDone());
  }

  @Test
  public void testCancellationOnDependents() {
    CompletableFuture<Integer> first = new VertxCompletableFuture<>(vertx);
    CompletableFuture<Integer> second = first.thenApply(x -> x + 1);
    CompletableFuture<String> toString = second.thenApplyAsync(i -> Integer.toString(i));
    CompletableFuture<String> displayed = toString.thenCompose(this::display);

    vertx.setTimer(1000, l -> first.complete(42));

    first.cancel(true);

    Assert.assertTrue(first.isCancelled());
    Assert.assertTrue(second.isCompletedExceptionally());
    Assert.assertTrue(toString.isCompletedExceptionally());
    Assert.assertTrue(displayed.isCompletedExceptionally());
  }


  @Test
  public void testWhenComplete(TestContext tc) {
    Async async1 = tc.async();
    Async async2 = tc.async();

    CompletableFuture<Integer> first = new VertxCompletableFuture<>(vertx);
    first
      .thenApply(x -> x + 1)
      .thenApplyAsync(i -> Integer.toString(i))
      .thenCompose(this::display)
      .whenComplete((res, err) -> {
        tc.assertNull(err);
        tc.assertEquals(res, "Result: " + 43);
        async1.complete();
      });

    first.complete(42);

    VertxCompletableFuture<Integer> second = new VertxCompletableFuture<>(vertx);
    second
      .thenApply(x -> x + 1)
      .thenApplyAsync(i -> Integer.toString(i))
      .thenCompose(this::display)
      .whenComplete((res, err) -> {
        tc.assertNull(res);
        tc.assertNotNull(err);
        async2.complete();
      });

    second.completeExceptionally(new Exception("My bad"));

  }

  @Test
  public void testWhenCompleteAsync(TestContext tc) {
    Async async1 = tc.async();
    Async async2 = tc.async();

    VertxCompletableFuture<Integer> first = new VertxCompletableFuture<>(vertx);
    first
      .thenApply(x -> x + 1)
      .thenApplyAsync(i -> Integer.toString(i))
      .thenCompose(this::display)
      .whenCompleteAsync((res, err) -> {
        tc.assertTrue(Context.isOnEventLoopThread());
        tc.assertNull(err);
        tc.assertEquals(res, "Result: " + 43);
        async1.complete();
      });

    first.complete(42);

    VertxCompletableFuture<Integer> second = new VertxCompletableFuture<>(vertx);
    second
      .thenApply(x -> x + 1)
      .thenApplyAsync(i -> Integer.toString(i))
      .thenCompose(this::display)
      .whenCompleteAsync((res, err) -> {
        tc.assertTrue(Context.isOnEventLoopThread());
        tc.assertNull(res);
        tc.assertNotNull(err);
        async2.complete();
      });

    second.completeExceptionally(new Exception("My bad"));
  }

  @Test
  public void testWhenCompleteAsyncWithExecutor(TestContext tc) {
    Async async1 = tc.async();
    Async async2 = tc.async();

    VertxCompletableFuture<Integer> first = new VertxCompletableFuture<>(vertx);
    first
      .thenApply(x -> x + 1)
      .thenApplyAsync(i -> Integer.toString(i))
      .thenCompose(this::display)
      .whenCompleteAsync((res, err) -> {
        tc.assertFalse(Context.isOnEventLoopThread());
        tc.assertNull(err);
        tc.assertEquals(res, "Result: " + 43);
        async1.complete();
      }, executor);

    first.complete(42);

    VertxCompletableFuture<Integer> second = new VertxCompletableFuture<>(vertx);
    second
      .thenApply(x -> x + 1)
      .thenApplyAsync(i -> Integer.toString(i))
      .thenCompose(this::display)
      .whenCompleteAsync((res, err) -> {
        tc.assertFalse(Context.isOnEventLoopThread());
        tc.assertNull(res);
        tc.assertNotNull(err);
        async2.complete();
      }, executor);

    second.completeExceptionally(new Exception("My bad"));
  }

  @Test
  public void testAcceptFailurePropagation(TestContext tc) {
    Async async0 = tc.async();
    Async async1 = tc.async();
    Async async2 = tc.async();
    CompletableFuture<Integer> failure1 = new VertxCompletableFuture<>(vertx);
    CompletableFuture<Integer> failure2 = new VertxCompletableFuture<>(vertx);

    failure1.thenAccept(i -> tc.fail("Should not be called"))
      .whenComplete((res, err) -> {
        tc.assertEquals(err.getClass(), CompletionException.class);
        tc.assertTrue(err.getMessage().contains("A"));
        async0.complete();
      });

    failure1.thenAcceptBoth(failure2, (a, b) -> tc.fail("Should not be called"))
      .whenComplete((res, err) -> {
        tc.assertEquals(err.getClass(), CompletionException.class);
        tc.assertTrue(err.getMessage().contains("A"));
        async2.complete();
      });

    failure1.acceptEither(failure2, i -> tc.fail("Should not be called"))
      .whenComplete((res, err) -> {
        tc.assertEquals(err.getClass(), CompletionException.class);
        tc.assertTrue(err.getMessage().contains("B"));
        async1.complete();
      });

    failure2.completeExceptionally(new RuntimeException("B"));
    failure1.completeExceptionally(new RuntimeException("A"));
  }

  @Test
  public void testApplyFailurePropagation(TestContext tc) {
    Async async0 = tc.async();
    Async async1 = tc.async();
    CompletableFuture<Integer> failure1 = new VertxCompletableFuture<>(vertx);
    CompletableFuture<Integer> failure2 = new VertxCompletableFuture<>(vertx);

    failure1.thenApply(i -> {
      tc.fail("Should not be called");
      return 0;
    }).whenComplete((res, err) -> {
      tc.assertEquals(err.getClass(), CompletionException.class);
      tc.assertTrue(err.getMessage().contains("A"));
      async0.complete();
    });

    failure1.applyToEither(failure2, i -> {
      tc.fail("Should not be called");
      return 0;
    }).whenComplete((res, err) -> {
      tc.assertEquals(err.getClass(), CompletionException.class);
      tc.assertTrue(err.getMessage().contains("B"));
      async1.complete();
    });

    failure2.completeExceptionally(new RuntimeException("B"));
    failure1.completeExceptionally(new RuntimeException("A"));

  }

  @Test
  public void testCombineFailurePropagation(TestContext tc) {
    Async async0 = tc.async();
    Async async1 = tc.async();
    CompletableFuture<Integer> failure1 = new VertxCompletableFuture<>(vertx);
    CompletableFuture<Integer> failure2 = new VertxCompletableFuture<>(vertx);

    VertxCompletableFuture<Integer> future1 = new VertxCompletableFuture<>(vertx);
    future1.complete(42);

    VertxCompletableFuture<Integer> future2 = new VertxCompletableFuture<>(vertx);
    future2.complete(1);


    failure1.thenCombine(failure2, (a, b) -> {
      tc.fail("Should not be called");
      return 0;
    }).whenComplete((res, err) -> {
      tc.assertEquals(err.getClass(), CompletionException.class);
      tc.assertTrue(err.getMessage().contains("A"));
      async0.complete();
    });

    failure1.thenCombine(failure2, (a, b) -> {
      tc.fail("Should not be called");
      return 0;
    }).whenComplete((res, err) -> {
      tc.assertEquals(err.getClass(), CompletionException.class);
      // Here we are not mimicking CompletableFuture behavior. We return the failure1 cause instead of the first
      // one that has arrived. For pure completable future is would be:
      //tc.assertTrue(err.getMessage().contains("B"));
      // For us it's
      tc.assertTrue(err.getMessage().contains("A"));
      async1.complete();
    });

    failure2.completeExceptionally(new RuntimeException("B"));
    failure1.completeExceptionally(new RuntimeException("A"));
  }

  private Integer square(int x) {
    return x * x;
  }

  @Test
  public void testExceptionallyPropagation(TestContext tc) {
    Async async1 = tc.async();
    Async async2 = tc.async();
    CompletableFuture<Integer> future = new VertxCompletableFuture<>(vertx);
    future.thenApply(this::square)
      .exceptionally(t -> 22)
      .whenComplete((res, err) -> {
        tc.assertEquals(res, 16);
        async1.complete();
      })
      .thenAccept(System.out::print)
      .thenRun(System.out::println);
    future.complete(4);

    future = new VertxCompletableFuture<>(vertx);
    future.thenApply(this::square).thenApply(i -> {
      throw new RuntimeException("My Bad");
    })
      .exceptionally(t -> 22)
      .whenComplete((res, err) -> {
        tc.assertEquals(res, 22);
        async2.complete();
      })
      .thenAccept(System.out::print)
      .thenRun(System.out::println);
    future.complete(4);
  }

  @Test
  public void testRunFailurePropagation(TestContext tc) {
    Async async0 = tc.async();
    Async async1 = tc.async();
    Async async2 = tc.async();
    CompletableFuture<Integer> failure1 = new VertxCompletableFuture<>(vertx);
    CompletableFuture<Integer> failure2 = new VertxCompletableFuture<>(vertx);

    failure1.thenRun(() -> tc.fail("Should not be called"))
      .whenComplete((res, err) -> {
        tc.assertEquals(err.getClass(), CompletionException.class);
        tc.assertTrue(err.getMessage().contains("A"));
        async0.complete();
      });

    failure1.runAfterBoth(failure2, () -> tc.fail("Should not be called"))
      .whenComplete((res, err) -> {
        tc.assertEquals(err.getClass(), CompletionException.class);
        tc.assertTrue(err.getMessage().contains("A"));
        async2.complete();
      });

    failure1.runAfterEither(failure2, () -> tc.fail("Should not be called"))
      .whenComplete((res, err) -> {
        tc.assertEquals(err.getClass(), CompletionException.class);
        tc.assertTrue(err.getMessage().contains("B"));
        async1.complete();
      });

    failure2.completeExceptionally(new RuntimeException("B"));
    failure1.completeExceptionally(new RuntimeException("A"));
  }


  @Test
  public void testFromCompletableStageJavadoc() {
    VertxCompletableFuture<Integer> future = new VertxCompletableFuture<>(vertx);
    future
      .thenApply(this::square)
      .thenAccept(System.out::print)
      .thenRun(System.out::println);

    future.complete(42);

  }

  @Test
  public void testWithContext(TestContext tc) {
    Async async1 = tc.async();
    Async async2 = tc.async();
    VertxCompletableFuture<Void> future = new VertxCompletableFuture<>(vertx);
    assertThat(future.context(), is(not(vertx.getOrCreateContext())));
    vertx.runOnContext(v -> {
      assertThat(future.withContext().context(), is(vertx.getOrCreateContext()));
      async1.complete();
    });

    vertx.<Void>executeBlocking(
      fut -> {
        assertThat(future.withContext().context(), is(Vertx.currentContext()));
        async2.complete();
      },
      null
    );
  }

  @Test
  public void testToVertxFuture(TestContext tc) {
    Async async1 = tc.async();
    Async async2 = tc.async();
    Async async3 = tc.async();
    Async async4 = tc.async();
    Async async5 = tc.async();

    VertxCompletableFuture<Integer> future = new VertxCompletableFuture<>(vertx);
    Future<Integer> success = future.thenApply(i -> i + 1).thenApplyAsync(i -> i + 1).toFuture();
    Future<Void> success2 = future.thenApply(i -> i + 1).thenApplyAsync(i -> i + 1).thenRunAsync(() -> {
      // Do nothing
    }).toFuture();
    success.setHandler(ar -> {
      assertThat(ar.failed(), is(false));
      assertThat(ar.succeeded(), is(true));
      assertThat(ar.result(), is(44));
      async1.complete();
    });
    success2.setHandler(ar -> {
      assertThat(ar.failed(), is(false));
      assertThat(ar.succeeded(), is(true));
      assertThat(ar.result(), is(nullValue()));
      async2.complete();
    });

    VertxCompletableFuture<Integer> failingFuture = new VertxCompletableFuture<>(vertx);
    Future<Integer> failure = failingFuture.thenApply(i -> i + 1).thenApplyAsync(i -> i + i + 1).toFuture();
    failure.setHandler(ar -> {
      assertThat(ar.failed(), is(true));
      assertThat(ar.succeeded(), is(false));
      assertThat(ar.result(), is(nullValue()));
      assertThat(ar.cause(), is(notNullValue()));
      assertThat(ar.cause().getMessage(), containsString("My bad"));
      async3.complete();
    });

    // test that `VertxCompletableFuture` receives callbacks from the Vert.x Future
    VertxCompletableFuture<Integer> awaitedFuture = new VertxCompletableFuture<>(vertx);

    awaitedFuture.handle((val, except) -> {
      assertThat(val, is(42));
      assertThat(except, is(nullValue()));
      async4.complete();

      return (Void) null;
    });

    VertxCompletableFuture<Integer> awaitedFailingFuture = new VertxCompletableFuture<>(vertx);

    awaitedFailingFuture.handle((val, except) -> {
      assertThat(val, is(nullValue()));
      assertThat(except, is(notNullValue()));
      assertThat(except.getMessage(), containsString("My bad again"));
      async5.complete();

      return (Void) null;
    });

    future.complete(42);
    failingFuture.completeExceptionally(new Exception("My bad"));
    awaitedFuture.toFuture().complete(42);
    awaitedFailingFuture.completeExceptionally(new Exception("My bad again"));
  }

  @Test
  public void testFromVertxFuture(TestContext tc) {
    Async async1 = tc.async();
    Async async2 = tc.async();

    Future<Integer> vertxFuture1 = Future.future();
    Future<Integer> vertxFuture2 = Future.future();
    VertxCompletableFuture.from(vertx, vertxFuture1).thenApply(i -> i + 1).whenComplete((res, err) -> {
      tc.assertNotNull(res);
      tc.assertNull(err);
      tc.assertEquals(43, res);
      async1.complete();
    });

    VertxCompletableFuture.from(vertx, vertxFuture2).thenApply(i -> i + 1).whenComplete((res, err) -> {
      tc.assertNotNull(err);
      tc.assertNull(res);
      tc.assertTrue(err.getMessage().contains("My bad"));
      async2.complete();
    });

    vertxFuture1.complete(42);
    vertxFuture2.fail(new Exception("My bad"));
  }

}
