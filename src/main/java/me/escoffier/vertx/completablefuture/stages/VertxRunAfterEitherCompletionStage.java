package me.escoffier.vertx.completablefuture.stages;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

public class VertxRunAfterEitherCompletionStage extends AbstractRunStage {


  /**
   * Flag indicating whether the action has already been called.
   */
  private boolean called;

  /**
   * Stores the first failure as CompletableFuture does.
   */
  private Throwable firstFailure;

  public <X> VertxRunAfterEitherCompletionStage(Context context, ExecutionMode mode,
                                            Executor executor,
                                            CompletionStage<X> other,
                                            Runnable runnable) {
    super(context, mode, executor, runnable);

    other.whenComplete((res, err) -> {
      if (err != null) {
        manageFailure(err);
      } else {
        invokeAction();
      }
    });
  }

  @Override
  protected void call(AsyncResult ar) {
    if (ar.succeeded()) {
      invokeAction();
    } else {
      manageFailure(ar.cause());
    }
  }

  private void invokeAction() {
    synchronized (this) {
      if (called) {
        return;
      }
      called = true;
    }

    super.invoke();
  }

  protected void manageFailure(Throwable err) {
    Future failed = null;
    synchronized (this) {
      if (firstFailure != null) {
        failed = Future.failedFuture(firstFailure);
      } else {
        firstFailure = err;
      }
    }

    if (failed != null) {
      forwardFailure(failed);
    }
  }

}
