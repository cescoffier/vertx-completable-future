package me.escoffier.vertx.completablefuture.stages;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class VertxAcceptEitherCompletionStage<T> extends AbstractAcceptOneStage<T> {

  /**
   * Flag indicating whether the action has already been called.
   */
  private boolean called;

  /**
   * Stores the first failure as CompletableFuture does.
   */
  private Throwable firstFailure;



  public VertxAcceptEitherCompletionStage(Context context, ExecutionMode mode,
                                          Executor executor,
                                          CompletionStage<? extends T> other,
                                          Consumer<? super T> action) {
    super(context, mode, executor, action);

    other.whenComplete((res, err) -> {
      if (err != null) {
        manageFailure(err);
      } else {
        invokeAction(res);
      }
    });
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

  protected void invokeAction(T input) {
    synchronized (this) {
      if (called) {
        return;
      }
      called = true;
    }
    super.invokeAction(input);
  }

}
