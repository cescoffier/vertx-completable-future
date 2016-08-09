package me.escoffier.vertx.completablefuture.stages;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

public class VertxAcceptBothCompletionStage<T, U> extends VertxCompletableFuture<Void> {

  private CompletionStage<? extends U> second;
  private BiConsumer<? super T,? super U> consumer;

  public VertxAcceptBothCompletionStage(Context context, ExecutionMode mode,
                                        Executor executor,
                                        CompletionStage<? extends U> second,
                                        BiConsumer<? super T,? super U> consumer) {
    super(context, mode, executor);
    this.consumer = consumer;
    this.second = second;
  }

  @Override
  protected void call(AsyncResult ar) {
    // First has completed
    if (ar.succeeded()) {
      this.second.whenComplete((r2, err2) -> {
        if (err2 != null) {
          forwardFailure(Future.failedFuture(err2));
        } else {
          T r1 = (T) ar.result();
          switch (mode) {
            case ON_CALLER_THREAD:
              accept(r1, r2);
              break;
            case ON_CONTEXT:
              acceptAsync(r1, r2);
              break;
            case ON_EXECUTOR:
              acceptAsync(r1, r2, executor);
              break;
          }
        }
      });
    } else {
      forwardFailure(ar);
    }
  }

  private void accept(T f, U s) {
    execute(f, s);
  }

  private void acceptAsync(T f, U s) {
    context.runOnContext(x -> {
      execute(f, s);
    });
  }

  private void acceptAsync(T f, U s, Executor executor) {
    executor.execute(() -> {
      execute(f, s);
    });
  }

  private void execute(T f, U s) {
    try {
      consumer.accept(f, s);
      current.complete();
    } catch (Throwable t) {
      current.fail(t);
    }
  }

}
