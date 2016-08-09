package me.escoffier.vertx.completablefuture.stages;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

import java.util.concurrent.Executor;
import java.util.function.Function;

public class VertxExceptionallyCompletionStage<T> extends VertxCompletableFuture<T> {

  private Function<Throwable, ? extends T> function;

  public VertxExceptionallyCompletionStage(Context context, ExecutionMode mode, Executor executor,
                                           Function<Throwable, ? extends T> fn) {
    super(context, mode, executor);
    this.function = fn;
  }

  @Override
  protected void call(AsyncResult ar) {
    if (ar.failed()) {
      Throwable input = ar.cause();
      switch (mode) {
      case ON_CALLER_THREAD:
        apply(input);
        break;
      case ON_CONTEXT:
        applyAsync(input);
        break;
      case ON_EXECUTOR:
        applyAsync(input, executor);
        break;
      }
    } else {
      current.complete((T) ar.result());
    }
  }

  private void apply(Throwable input) {
    execute(input);
  }

  private void applyAsync(Throwable input) {
    context.runOnContext(x -> {
      execute(input);
    });
  }

  private void applyAsync(Throwable input, Executor executor) {
    executor.execute(() -> {
      execute(input);
    });
  }

  private void execute(Throwable input) {
    try {
      current.complete(function.apply(input));
    } catch (Throwable t) {
      current.fail(t);
    }
  }

}
