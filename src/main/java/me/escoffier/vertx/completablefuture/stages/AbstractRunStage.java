package me.escoffier.vertx.completablefuture.stages;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

import java.util.concurrent.Executor;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public abstract class AbstractRunStage extends VertxCompletableFuture<Void> {

  private final Runnable runnable;

  public AbstractRunStage(Context context, ExecutionMode mode, Executor executor,
                          Runnable runnable) {
    super(context, mode, executor);
    this.runnable = runnable;
  }

  @Override
  protected void call(AsyncResult ar) {
    if (ar.succeeded()) {
      invoke();
    } else {
      manageFailure(ar.cause());
    }
  }

  protected void invoke() {
    switch (mode) {
      case ON_CALLER_THREAD:
        run();
        break;
      case ON_CONTEXT:
        runAsync();
        break;
      case ON_EXECUTOR:
        runAsync(executor);
        break;
    }
  }

  protected void manageFailure(Throwable err) {
    forwardFailure(Future.failedFuture(err));
  }

  protected void run() {
    execute();
  }

  protected void runAsync() {
    context.runOnContext(x -> {
      execute();
    });
  }

  protected void runAsync(Executor executor) {
    executor.execute(this::execute);
  }

  private void execute() {
    try {
      runnable.run();
      current.complete();
    } catch (Throwable t) {
      current.fail(t);
    }
  }


}
