package me.escoffier.vertx.completablefuture.stages;

import java.util.concurrent.Executor;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import me.escoffier.vertx.completablefuture.VertxCompletableFuture;

public class VertxRunCompletionStage extends AbstractRunStage {


  public VertxRunCompletionStage(Context context, ExecutionMode mode,
      Executor executor,
      Runnable runnable) {
    super(context, mode, executor, runnable);
  }

}
