package me.escoffier.vertx.completablefuture.stages;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

public class VertxRunAfterCompletionStage extends AbstractRunStage {

  private CompletionStage<?> other;

  public VertxRunAfterCompletionStage(Context context, ExecutionMode mode,
                                      Executor executor,
                                      CompletionStage<?> other,
                                      Runnable runnable) {
    super(context, mode, executor, runnable);
    this.other = other;
  }

  @Override
  protected void call(AsyncResult ar) {
    // First has completed
    if (ar.succeeded()) {
      this.other.whenComplete((r2, err2) -> {
        if (err2 != null) {
          forwardFailure(Future.failedFuture(err2));
        } else {
          invoke();
        }
      });
    } else {
      forwardFailure(ar);
    }
  }


}
