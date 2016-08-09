package me.escoffier.vertx.completablefuture.stages;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;

public class VertxHandleCompletionStage<T, U> extends VertxCompletableFuture<U> {

  //TODO Would be better to split this class in two as it's often used, and we do additional checks

  private final BiFunction<? super T, Throwable, ? extends U> function;
  private final Function<AsyncResult<T>, ? extends U> handler;

  public VertxHandleCompletionStage(Context context, ExecutionMode mode,
                                    Executor executor,
                                    BiFunction<? super T, Throwable, ? extends U> function) {
    super(context, mode, executor);
    this.function = function;
    this.handler = null;
  }

  public VertxHandleCompletionStage(Context context, ExecutionMode mode,
                                    Executor executor,
                                    Function<AsyncResult<T>, ? extends U> function) {
    super(context, mode, executor);
    this.function = null;
    this.handler = function;
  }

  @Override
  protected void call(AsyncResult ar) {
    T input = (T) ar.result();
    switch (mode) {
      case ON_CALLER_THREAD:
        apply(input, ar.cause());
        break;
      case ON_CONTEXT:
        applyAsync(input, ar.cause());
        break;
      case ON_EXECUTOR:
        applyAsync(input, ar.cause(), executor);
        break;
    }
  }

  private void apply(T input, Throwable failure) {
    execute(input, failure);
  }

  private void applyAsync(T input, Throwable failure) {
    context.runOnContext(x -> execute(input, failure));
  }

  private void applyAsync(T input, Throwable failure, Executor executor) {
    executor.execute(() -> execute(input, failure));
  }

  private void execute(T input, Throwable failure) {
    try {
      if (function != null) {
        current.complete(function.apply(input, failure));
      } else {
        Future<T> future;
        if (failure != null) {
          future = Future.failedFuture(failure);
        } else {
          future = Future.succeededFuture(input);
        }
        current.complete(handler.apply(future));
      }
    } catch (Throwable t) {
      current.fail(t);
    }
  }

}
