package me.escoffier.vertx.completablefuture.stages;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public abstract class AbstractAcceptOneStage<I> extends VertxCompletableFuture<Void> {


  private final Consumer<? super I> action;

  public AbstractAcceptOneStage(Context context, ExecutionMode mode, Executor executor,
                                Consumer<? super I> action) {
    super(context, mode, executor);
    this.action = action;
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
        accept(input);
        break;
      case ON_CONTEXT:
        acceptAsync(input);
        break;
      case ON_EXECUTOR:
        acceptAsync(input, executor);
        break;
    }
  }

  private void accept(I input) {
    execute(input);
  }

  private void acceptAsync(I input) {
    context.runOnContext(x -> {
      execute(input);
    });
  }

  private void acceptAsync(I input, Executor executor) {
    executor.execute(() -> {
      execute(input);
    });
  }

  private void execute(I input) {
    try {
      action.accept(input);
      current.complete();
    } catch (Throwable t) {
      current.fail(t);
    }
  }

}
