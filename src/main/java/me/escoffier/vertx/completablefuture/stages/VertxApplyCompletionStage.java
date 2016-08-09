package me.escoffier.vertx.completablefuture.stages;

import java.util.concurrent.Executor;
import java.util.function.Function;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

public class VertxApplyCompletionStage<I, O> extends AbstractApplyStage<I, O> {

  public VertxApplyCompletionStage(Context context, ExecutionMode mode, Executor executor,
      Function<? super I, ? extends O> fn) {
    super(context, mode, executor, fn);
  }

}
