package me.escoffier.vertx.completablefuture.stages;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class VertxApplyEitherCompletionStage<T, U> extends AbstractApplyStage<T, U> {


  private boolean called;
  private Throwable firstFailure;

  public VertxApplyEitherCompletionStage(Context context, ExecutionMode mode,
                                         Executor executor,
                                         CompletionStage<? extends T> other,
                                         Function<? super T, U> fn) {
    super(context, mode, executor, fn);
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
