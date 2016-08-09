package me.escoffier.vertx.completablefuture.stages;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

public class VertxCombineCompletionStage<T, U, V> extends VertxCompletableFuture<V> {

  private CompletionStage<? extends U> second;
  private BiFunction<? super T,? super U,? extends V> function;

  public VertxCombineCompletionStage(Context context, ExecutionMode mode,
      Executor executor,
      CompletionStage<? extends U> second,
      BiFunction<? super T,? super U,? extends V> fn) {
    super(context, mode, executor);
    this.function = fn;
    this.second = second;
  }

  @Override
  protected void call(AsyncResult ar) {
    // First has completed
    if (ar.succeeded()) {
      this.second.handle((r2, err) -> {
        if (err != null) {
          forwardFailure(Future.failedFuture(err));
          return null;
        }
        T r1 = (T) ar.result();
        switch (mode) {
        case ON_CALLER_THREAD:
          apply(r1, r2);
          break;
        case ON_CONTEXT:
          applyAsync(r1, r2);
          break;
        case ON_EXECUTOR:
          applyAsync(r1, r2, executor);
          break;
        }
        return null;
      });
    } else {
      forwardFailure(ar);
    }
  }

  private void apply(T f, U s) {
    execute(f, s);
  }

  private void applyAsync(T f, U s) {
    context.runOnContext(x -> {
      execute(f, s);
    });
  }

  private void applyAsync(T f, U s, Executor executor) {
    executor.execute(() -> {
      execute(f, s);
    });
  }

  private void execute(T f, U s) {
    try {
      current.complete(function.apply(f, s));
    } catch (Throwable t) {
      current.fail(t);
    }
  }

}
