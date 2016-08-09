package me.escoffier.vertx.completablefuture.stages;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

public class VertxAcceptCompletionStage<I> extends AbstractAcceptOneStage<I> {

  public VertxAcceptCompletionStage(Context context, ExecutionMode mode,
      Executor executor,
      Consumer<? super I> action) {
    super(context, mode, executor, action);
  }

}
