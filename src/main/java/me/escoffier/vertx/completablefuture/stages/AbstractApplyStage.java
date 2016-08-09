package me.escoffier.vertx.completablefuture.stages;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public abstract class AbstractApplyStage<I, O> extends VertxCompletableFuture<O> {

  private final Function<? super I, ? extends O> function;

  public AbstractApplyStage(Context context, ExecutionMode mode, Executor executor,
                            Function<? super I, ? extends O> function) {
    super(context, mode, executor);
    this.function = function;
  }

  @Override
  protected void call(AsyncResult ar) {
    if (ar.succeeded()) {
      I input = (I) ar.result();
      invokeAction(input);
    } else {
      manageFailure(ar.cause());
    }
  }

  protected void manageFailure(Throwable err) {
    forwardFailure(Future.failedFuture(err));
  }


  protected void invokeAction(I input) {
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
  }

  private void apply(I input) {
    execute(input);
  }

  private void applyAsync(I input) {
    context.runOnContext(x -> execute(input));
  }

  private void applyAsync(I input, Executor executor) {
    executor.execute(() -> execute(input));
  }

  private synchronized void execute(I input) {
    try {
      current.complete(function.apply(input));
    } catch (Throwable t) {
      current.fail(t);
    }
  }

}
