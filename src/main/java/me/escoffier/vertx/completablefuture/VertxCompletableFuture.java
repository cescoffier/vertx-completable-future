package me.escoffier.vertx.completablefuture;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import me.escoffier.vertx.completablefuture.stages.*;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.*;

public class VertxCompletableFuture<T> extends CompletableFuture<T> implements CompletionStage<T> {

  // TODO Test from Vertx Future
  // TODO Copy javadoc and edit
  // TODO write a bit of doc
  // TODO implement executor based on context + exec blocking

  protected enum ExecutionMode {
    ON_CALLER_THREAD,
    ON_CONTEXT,
    ON_EXECUTOR
  }

  protected Context context;
  protected final Future<T> current;
  protected final ExecutionMode mode;

  private final List<VertxCompletableFuture> dependents;
  protected final Executor executor;

  public VertxCompletableFuture(Vertx vertx) {
    this(vertx.getOrCreateContext(), ExecutionMode.ON_CALLER_THREAD, null);
  }

  public VertxCompletableFuture(Context context) {
    this(context, ExecutionMode.ON_CALLER_THREAD, null);
  }

  public static <T> VertxCompletableFuture<T> from(Vertx vertx, CompletableFuture<T> future) {
    return from(vertx.getOrCreateContext(), future);
  }

  public static <T> VertxCompletableFuture<T> from(Vertx vertx, Future<T> future) {
    return from(vertx.getOrCreateContext(), future);
  }

  /**
   * Returns a new CompletableFuture that is completed when all of the given CompletableFutures complete.  If any of
   * the given CompletableFutures complete exceptionally, then the returned CompletableFuture also does so, with a
   * CompletionException holding this exception as its cause.  Otherwise, the results, if any, of the given
   * CompletableFutures are not reflected in the returned CompletableFuture, but may be obtained by inspecting them
   * individually. If no CompletableFutures are provided, returns a CompletableFuture completed with the value
   * {@code null}.
   * <p>
   * <p>Among the applications of this method is to await completion
   * of a set of independent CompletableFutures before continuing a
   * program, as in: {@code CompletableFuture.allOf(c1, c2, c3).join();}.
   * <p>
   * Unlike the original {@link CompletableFuture#allOf(CompletableFuture[])} this method invokes the dependent
   * stages into the Vert.x context.
   *
   * @param vertx   the Vert.x instance to retrieve the context
   * @param futures the CompletableFutures
   * @return a new CompletableFuture that is completed when all of the given CompletableFutures complete
   * @throws NullPointerException if the array or any of its elements are {@code null}
   */
  public static VertxCompletableFuture<Void> allOf(Vertx vertx, CompletableFuture<?>... futures) {
    CompletableFuture<Void> all = CompletableFuture.allOf(futures);
    return VertxCompletableFuture.from(vertx, all);
  }

  /**
   * Returns a new CompletableFuture that is completed when all of the given CompletableFutures complete.  If any of
   * the given CompletableFutures complete exceptionally, then the returned CompletableFuture also does so, with a
   * CompletionException holding this exception as its cause.  Otherwise, the results, if any, of the given
   * CompletableFutures are not reflected in the returned CompletableFuture, but may be obtained by inspecting them
   * individually. If no CompletableFutures are provided, returns a CompletableFuture completed with the value
   * {@code null}.
   * <p>
   * <p>Among the applications of this method is to await completion
   * of a set of independent CompletableFutures before continuing a
   * program, as in: {@code CompletableFuture.allOf(c1, c2, c3).join();}.
   * <p>
   * Unlike the original {@link CompletableFuture#allOf(CompletableFuture[])} this method invokes the dependent
   * stages into the Vert.x context.
   *
   * @param context the context
   * @param futures the CompletableFutures
   * @return a new CompletableFuture that is completed when all of the given CompletableFutures complete
   * @throws NullPointerException if the array or any of its elements are {@code null}
   */
  public static VertxCompletableFuture<Void> allOf(Context context, CompletableFuture<?>... futures) {
    CompletableFuture<Void> all = CompletableFuture.allOf(futures);
    return VertxCompletableFuture.from(context, all);
  }

  /**
   * Returns a new CompletableFuture that is completed when any of the given CompletableFutures complete, with the
   * same result. Otherwise, if it completed exceptionally, the returned CompletableFuture also does so, with a
   * CompletionException holding this exception as its cause.  If no CompletableFutures are provided, returns an
   * incomplete CompletableFuture.
   * <p>
   * Unlike the original {@link CompletableFuture#allOf(CompletableFuture[])} this method invokes the dependent
   * stages into the Vert.x context.
   *
   * @param vertx   the Vert.x instance to retrieve the context
   * @param futures the CompletableFutures
   * @return a new CompletableFuture that is completed with the result or exception of any of the given
   * CompletableFutures when one completes
   * @throws NullPointerException if the array or any of its elements are {@code null}
   */
  public static VertxCompletableFuture<Object> anyOf(Vertx vertx, CompletableFuture<?>... futures) {
    CompletableFuture<Object> all = CompletableFuture.anyOf(futures);
    return VertxCompletableFuture.from(vertx, all);
  }

  /**
   * Returns a new CompletableFuture that is completed when any of the given CompletableFutures complete, with the
   * same result. Otherwise, if it completed exceptionally, the returned CompletableFuture also does so, with a
   * CompletionException holding this exception as its cause.  If no CompletableFutures are provided, returns an
   * incomplete CompletableFuture.
   * <p>
   * Unlike the original {@link CompletableFuture#allOf(CompletableFuture[])} this method invokes the dependent
   * stages into the Vert.x context.
   *
   * @param context the context
   * @param futures the CompletableFutures
   * @return a new CompletableFuture that is completed with the result or exception of any of the given
   * CompletableFutures when one completes
   * @throws NullPointerException if the array or any of its elements are {@code null}
   */
  public static VertxCompletableFuture<Object> anyOf(Context context, CompletableFuture<?>... futures) {
    CompletableFuture<Object> all = CompletableFuture.anyOf(futures);
    return VertxCompletableFuture.from(context, all);
  }

  public static <T> VertxCompletableFuture<T> from(Context context, CompletableFuture<T> future) {
    VertxCompletableFuture<T> res = new VertxCompletableFuture<T>(context);
    future.whenComplete((result, error) -> {
      if (context == Vertx.currentContext()) {
        res.complete(result, error);
      } else {
        res.context.runOnContext(v -> {
          res.complete(result, error);
        });
      }
    });
    return res;
  }

  public static <T> VertxCompletableFuture<T> from(Context context, Future<T> future) {
    VertxCompletableFuture<T> res = new VertxCompletableFuture<T>(context);
    future.setHandler(ar -> {
      if (context == Vertx.currentContext()) {
        res.completeFromAsyncResult(ar);
      } else {
        res.context.runOnContext(v -> {
          res.completeFromAsyncResult(ar);
        });
      }
    });
    return res;
  }

  private void complete(T result, Throwable error) {
    if (error == null) {
      complete(result);
    } else {
      completeExceptionally(error);
    }
  }

  private void completeFromAsyncResult(AsyncResult<T> ar) {
    if (ar.succeeded()) {
      complete(ar.result());
    } else {
      completeExceptionally(ar.cause());
    }
  }

  /**
   * Returns a new CompletableFuture that is asynchronously completed by a task running in the current Vert.x
   * {@link Context} with the value obtained by calling the given Supplier.
   * <p>
   * This method is different from {@link CompletableFuture#supplyAsync(Supplier)} as it does not use a fork join
   * executor, but use the Vert.x context.
   *
   * @param vertx    the Vert.x instance
   * @param supplier a function returning the value to be used to complete the returned CompletableFuture
   * @param <T>      the function's return type
   * @return the new CompletableFuture
   */
  public static <T> VertxCompletableFuture<T> supplyAsync(Vertx vertx, Supplier<T> supplier) {
    return supplyAsync(vertx.getOrCreateContext(), supplier);
  }

  /**
   * Returns a new CompletableFuture that is asynchronously completed by a task running in the
   * current Vert.x {@link Context} after it runs the given action.
   * <p>
   * This method is different from {@link CompletableFuture#supplyAsync(Supplier)} as it does not use a fork join
   * executor, but use the Vert.x context.
   *
   * @param vertx    the Vert.x instance
   * @param runnable the action to run before completing the returned CompletableFuture
   * @return the new CompletableFuture
   */
  public static VertxCompletableFuture<Void> runAsync(Vertx vertx, Runnable runnable) {
    return runAsync(vertx.getOrCreateContext(), runnable);
  }

  /**
   * Returns a new CompletableFuture that is asynchronously completed by a task running in the current Vert.x
   * {@link Context} with the value obtained by calling the given Supplier.
   * <p>
   * This method is different from {@link CompletableFuture#supplyAsync(Supplier)} as it does not use a fork join
   * executor, but use the Vert.x context.
   *
   * @param context  the context in which the supplier is executed.
   * @param supplier a function returning the value to be used to complete the returned CompletableFuture
   * @param <T>      the function's return type
   * @return the new CompletableFuture
   */
  public static <T> VertxCompletableFuture<T> supplyAsync(Context context, Supplier<T> supplier) {
    VertxCompletableFuture<T> future = new VertxCompletableFuture<>(context, ExecutionMode.ON_CONTEXT, null);
    context.runOnContext(v -> {
      try {
        future.complete(supplier.get());
      } catch (Throwable e) {
        future.completeExceptionally(e);
      }
    });
    return future;
  }

  /**
   * Returns a new CompletableFuture that is asynchronously completed by a task running in the
   * current Vert.x {@link Context} after it runs the given action.
   * <p>
   * This method is different from {@link CompletableFuture#runAsync(Runnable)} as it does not use a fork join
   * executor, but use the Vert.x context.
   *
   * @param context  the context
   * @param runnable the action to run before completing the returned CompletableFuture
   * @return the new CompletableFuture
   */
  public static VertxCompletableFuture<Void> runAsync(Context context, Runnable runnable) {
    VertxCompletableFuture<Void> future = new VertxCompletableFuture<>(context, ExecutionMode.ON_CONTEXT, null);
    context.runOnContext(v -> {
      try {
        runnable.run();
        future.complete(null);
      } catch (Throwable e) {
        future.completeExceptionally(e);
      }
    });
    return future;
  }

  /**
   * Returns a new CompletableFuture that is asynchronously completed by a task running in the worker thread pool of
   * Vert.x
   * <p>
   * This method is different from {@link CompletableFuture#supplyAsync(Supplier)} as it does not use a fork join
   * executor, but the worker thread pool.
   *
   * @param vertx    the Vert.x instance
   * @param supplier a function returning the value to be used to complete the returned CompletableFuture
   * @param <T>      the function's return type
   * @return the new CompletableFuture
   */
  public static <T> VertxCompletableFuture<T> supplyBlockingAsync(Vertx vertx, Supplier<T> supplier) {
    return supplyBlockingAsync(vertx.getOrCreateContext(), supplier);
  }

  public static VertxCompletableFuture<Void> runBlockingAsync(Vertx vertx, Runnable runnable) {
    return runBlockingAsync(vertx.getOrCreateContext(), runnable);
  }

  public static VertxCompletableFuture<Void> runBlockingAsync(Context context, Runnable runnable) {
    VertxCompletableFuture<Void> future = new VertxCompletableFuture<>(context, ExecutionMode.ON_CONTEXT, null);
    context.executeBlocking(
        fut -> {
          try {
            runnable.run();
            future.complete(null);
          } catch (Throwable e) {
            future.completeExceptionally(e);
          }
        },
        null
    );
    return future;
  }

  /**
   * Returns a new CompletableFuture that is asynchronously completed by a task running in the worker thread pool of
   * Vert.x
   * <p>
   * This method is different from {@link CompletableFuture#supplyAsync(Supplier)} as it does not use a fork join
   * executor, but the worker thread pool.
   *
   * @param context  the context in which the supplier is executed.
   * @param supplier a function returning the value to be used to complete the returned CompletableFuture
   * @param <T>      the function's return type
   * @return the new CompletableFuture
   */
  public static <T> VertxCompletableFuture<T> supplyBlockingAsync(Context context, Supplier<T> supplier) {
    VertxCompletableFuture<T> future = new VertxCompletableFuture<>(context, ExecutionMode.ON_CONTEXT, null);
    context.<T>executeBlocking(
        fut -> {
          try {
            fut.complete(supplier.get());
          } catch (Throwable e) {
            fut.fail(e);
          }
        },
        ar -> {
          if (ar.failed()) {
            future.completeExceptionally(ar.cause());
          } else {
            future.complete(ar.result());
          }
        }
    );
    return future;
  }

  public VertxCompletableFuture(Context context, ExecutionMode mode, Executor executor) {
    Objects.requireNonNull(context);
    Objects.requireNonNull(mode);
    this.context = context;
    this.mode = mode;
    this.current = Future.future();
    if (mode == ExecutionMode.ON_EXECUTOR) {
      Objects.requireNonNull(executor);
    }
    this.executor = executor;
    this.dependents = new CopyOnWriteArrayList<>();

    this.current.setHandler(ar -> {
      // Will be called only once.
      if (ar.succeeded()) {
        super.complete(ar.result());
      } else {
        super.completeExceptionally(ar.cause());
      }

      // COW list, so create a copy of the iterator.
      dependents.forEach(stage -> {
        if (isCancelled()) {
          stage.completeExceptionally(new CancellationException());
        } else {
          stage.call(ar);
        }
      });
    });
  }

  public synchronized VertxCompletableFuture<T> withContext() {
    Context context = Vertx.currentContext();
    Objects.requireNonNull(context);
    return withContext(context);
  }

  public synchronized VertxCompletableFuture<T> withContext(Vertx vertx) {
    return withContext(vertx.getOrCreateContext());
  }

  public synchronized VertxCompletableFuture<T> withContext(Context context) {
    this.context = context;
    return this;
  }

  public synchronized Context getContext() {
    return context;
  }

  protected void call(AsyncResult ar) {
    // Do nothing by default.
  }

  protected void forwardFailure(AsyncResult ar) {
    if (!ar.failed()) {
      throw new IllegalStateException("Cannot forward a failure if the input result is not a failure");
    }
    if (current.isComplete()) {
      throw new IllegalStateException("Cannot forward a failure, the current future has already been completed");
    }
    Throwable cause = ar.cause();
    CompletionException ce = new CompletionException(cause);
    switch (mode) {
      case ON_CALLER_THREAD:
        current.fail(ce);
        break;
      case ON_CONTEXT:
        Context ctx;
        synchronized (this) {
          ctx = context;
        }
        ctx.runOnContext(v -> {
          current.fail(ce);
        });
        break;
      case ON_EXECUTOR:
        executor.execute(() -> {
          current.fail(ce);
        });
        break;
    }
  }

  protected synchronized void addDependent(VertxCompletableFuture dep) {
    dependents.add(dep);
    if (current.isComplete()) {
      if (isCancelled()) {
        dep.completeExceptionally(new CancellationException());
      } else {
        AsyncResult<T> ar = current.succeeded() ? Future.succeededFuture(current.result())
            : Future.failedFuture(current.cause());
        dep.call(ar);
      }
    }
  }

  @Override
  public synchronized <U> VertxCompletableFuture<U> thenApply(Function<? super T, ? extends U> fn) {
    VertxApplyCompletionStage<T, U> apply = new VertxApplyCompletionStage<>(context,
        ExecutionMode.ON_CALLER_THREAD, null, fn);
    addDependent(apply);
    return apply;
  }

  @Override
  public synchronized <U> VertxCompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
    VertxApplyCompletionStage<T, U> apply = new VertxApplyCompletionStage<>(context,
        ExecutionMode.ON_CONTEXT, null, fn);
    addDependent(apply);
    return apply;
  }

  @Override
  public synchronized <U> VertxCompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
    VertxApplyCompletionStage<T, U> apply = new VertxApplyCompletionStage<>(context,
        ExecutionMode.ON_EXECUTOR, executor, fn);
    addDependent(apply);
    return apply;
  }

  @Override
  public synchronized VertxCompletableFuture<Void> thenAccept(Consumer<? super T> action) {
    VertxAcceptCompletionStage<T> accept = new VertxAcceptCompletionStage<>(context,
        ExecutionMode.ON_CALLER_THREAD, null, action);
    addDependent(accept);
    return accept;
  }

  @Override
  public synchronized VertxCompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action) {
    VertxAcceptCompletionStage<T> accept = new VertxAcceptCompletionStage<>(context,
        ExecutionMode.ON_CONTEXT, null, action);
    addDependent(accept);
    return accept;
  }

  @Override
  public synchronized VertxCompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor) {
    VertxAcceptCompletionStage<T> accept = new VertxAcceptCompletionStage<>(context,
        ExecutionMode.ON_EXECUTOR, executor, action);
    addDependent(accept);
    return accept;
  }

  @Override
  public synchronized VertxCompletableFuture<Void> thenRun(Runnable action) {
    VertxRunCompletionStage run = new VertxRunCompletionStage(context,
        ExecutionMode.ON_CALLER_THREAD, null, action);
    addDependent(run);
    return run;
  }

  @Override
  public synchronized VertxCompletableFuture<Void> thenRunAsync(Runnable action) {
    VertxRunCompletionStage run = new VertxRunCompletionStage(context,
        ExecutionMode.ON_CONTEXT, null, action);
    addDependent(run);
    return run;
  }

  @Override
  public synchronized VertxCompletableFuture<Void> thenRunAsync(Runnable action, Executor executor) {
    VertxRunCompletionStage run = new VertxRunCompletionStage(context,
        ExecutionMode.ON_EXECUTOR, executor, action);
    addDependent(run);
    return run;
  }

  /**
   * Returns a new CompletionStage that, when this and the other
   * given stage both complete normally, is executed with the two
   * results as arguments to the supplied function.
   *
   * @param other the other CompletionStage
   * @param fn    the function to use to compute the value of
   *              the returned CompletionStage
   * @param <U>   the type of the other CompletionStage's result
   * @param <V>   the function's return type
   * @return the new CompletionStage
   */
  @Override
  public synchronized <U, V> VertxCompletableFuture<V> thenCombine(
      CompletionStage<? extends U> other,
      BiFunction<? super T, ? super U, ? extends V> fn) {
    VertxCombineCompletionStage<T, U, V> combine = new VertxCombineCompletionStage<>(context,
        ExecutionMode.ON_CALLER_THREAD, null, other, fn);
    addDependent(combine);
    return combine;
  }

  /**
   * Returns a new CompletionStage that, when this and the other
   * given stage complete normally, is executed using this stage's
   * default asynchronous execution facility, with the two results
   * as arguments to the supplied function.
   *
   * @param other the other CompletionStage
   * @param fn    the function to use to compute the value of
   *              the returned CompletionStage
   * @param <U>   the type of the other CompletionStage's result
   * @param <V>   the function's return type
   * @return the new CompletionStage
   */
  @Override
  public synchronized <U, V> VertxCompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other,
                                                                        BiFunction<? super T, ? super U, ? extends V> fn) {
    VertxCombineCompletionStage<T, U, V> combine = new VertxCombineCompletionStage<>(context,
        ExecutionMode.ON_CONTEXT, null, other, fn);
    addDependent(combine);
    return combine;
  }

  /**
   * Returns a new CompletionStage that, when this and the other
   * given stage complete normally, is executed using the supplied
   * executor, with the two results as arguments to the supplied
   * function.
   * <p>
   * See the {@link CompletionStage} documentation for rules
   * covering exceptional completion.
   *
   * @param other    the other CompletionStage
   * @param fn       the function to use to compute the value of
   *                 the returned CompletionStage
   * @param executor the executor to use for asynchronous execution
   * @param <U>      the type of the other CompletionStage's result
   * @param <V>      the function's return type
   * @return the new CompletionStage
   */
  @Override
  public synchronized <U, V> VertxCompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other,
                                                                        BiFunction<? super T, ? super U, ? extends V> fn, Executor executor) {
    VertxCombineCompletionStage<T, U, V> combine = new VertxCombineCompletionStage<>(context,
        ExecutionMode.ON_EXECUTOR, executor, other, fn);
    addDependent(combine);
    return combine;
  }

  @Override
  public synchronized <U> VertxCompletableFuture<Void> thenAcceptBoth(CompletionStage<? extends U> other,
                                                                      BiConsumer<? super T, ? super U> action) {
    VertxAcceptBothCompletionStage<T, U> operation = new VertxAcceptBothCompletionStage<>(context,
        ExecutionMode.ON_CALLER_THREAD, null, other, action);
    addDependent(operation);
    return operation;
  }

  @Override
  public synchronized <U> VertxCompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,
                                                                           BiConsumer<? super T, ? super U> action) {
    VertxAcceptBothCompletionStage<T, U> operation = new VertxAcceptBothCompletionStage<>(context,
        ExecutionMode.ON_CONTEXT, null, other, action);
    addDependent(operation);
    return operation;
  }

  @Override
  public synchronized <U> VertxCompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,
                                                                           BiConsumer<? super T, ? super U> action, Executor executor) {
    VertxAcceptBothCompletionStage<T, U> operation = new VertxAcceptBothCompletionStage<>(context,
        ExecutionMode.ON_EXECUTOR, executor, other, action);
    addDependent(operation);
    return operation;
  }

  @Override
  public synchronized VertxCompletableFuture<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
    VertxRunAfterCompletionStage operation = new VertxRunAfterCompletionStage(context,
        ExecutionMode.ON_CALLER_THREAD, null, other, action);
    addDependent(operation);
    return operation;
  }

  @Override
  public synchronized VertxCompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
    VertxRunAfterCompletionStage operation = new VertxRunAfterCompletionStage(context,
        ExecutionMode.ON_CONTEXT, null, other, action);
    addDependent(operation);
    return operation;
  }

  @Override
  public synchronized VertxCompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor) {
    VertxRunAfterCompletionStage operation = new VertxRunAfterCompletionStage(context,
        ExecutionMode.ON_EXECUTOR, executor, other, action);
    addDependent(operation);
    return operation;
  }

  @Override
  public synchronized <U> VertxCompletableFuture<U> applyToEither(CompletionStage<? extends T> other, Function<? super T, U> fn) {
    VertxApplyEitherCompletionStage<T, U> operation = new VertxApplyEitherCompletionStage<>(context,
        ExecutionMode.ON_CALLER_THREAD, null, other, fn);
    addDependent(operation);
    return operation;
  }

  @Override
  public synchronized <U> VertxCompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn) {
    VertxApplyEitherCompletionStage<T, U> operation = new VertxApplyEitherCompletionStage<>(context,
        ExecutionMode.ON_CONTEXT, null, other, fn);
    addDependent(operation);
    return operation;
  }

  @Override
  public synchronized <U> VertxCompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn,
                                                                       Executor executor) {
    VertxApplyEitherCompletionStage<T, U> operation = new VertxApplyEitherCompletionStage<>(context,
        ExecutionMode.ON_EXECUTOR, executor, other, fn);
    addDependent(operation);
    return operation;
  }

  @Override
  public synchronized VertxCompletableFuture<Void> acceptEither(CompletionStage<? extends T> other, Consumer<? super T> action) {
    VertxAcceptEitherCompletionStage<T> accept = new VertxAcceptEitherCompletionStage<>(context,
        ExecutionMode.ON_CALLER_THREAD, null, other, action);
    addDependent(accept);
    return accept;
  }

  @Override
  public synchronized VertxCompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action) {
    VertxAcceptEitherCompletionStage<T> accept = new VertxAcceptEitherCompletionStage<>(context,
        ExecutionMode.ON_CONTEXT, null, other, action);
    addDependent(accept);
    return accept;
  }

  @Override
  public synchronized VertxCompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action,
                                                                     Executor executor) {
    VertxAcceptEitherCompletionStage<T> accept = new VertxAcceptEitherCompletionStage<>(context,
        ExecutionMode.ON_EXECUTOR, executor, other, action);
    addDependent(accept);
    return accept;
  }

  @Override
  public synchronized VertxCompletableFuture<Void> runAfterEither(CompletionStage<?> other, Runnable action) {
    VertxRunAfterEitherCompletionStage run = new VertxRunAfterEitherCompletionStage(context,
        ExecutionMode.ON_CALLER_THREAD, null, other, action);
    addDependent(run);
    return run;
  }

  @Override
  public synchronized VertxCompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) {
    VertxRunAfterEitherCompletionStage run = new VertxRunAfterEitherCompletionStage(context,
        ExecutionMode.ON_CONTEXT, null, other, action);
    addDependent(run);
    return run;
  }

  @Override
  public synchronized VertxCompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor) {
    VertxRunAfterEitherCompletionStage run = new VertxRunAfterEitherCompletionStage(context,
        ExecutionMode.ON_EXECUTOR, executor, other, action);
    addDependent(run);
    return run;
  }

  @Override
  public <U> VertxCompletableFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
    VertxCompletableFuture<U> ret = new VertxCompletableFuture<>(context);
    handle((res, err) -> {
      if (err == null) {
        CompletionStage<U> apply;
        try {
          apply = fn.apply(res);
        } catch (Throwable e) {
          ret.completeExceptionally(e);
          return null;
        }

        apply.handle((res2, err2) -> {
          if (err2 != null) {
            ret.completeExceptionally(err2);
          } else {
            ret.complete(res2);
          }
          return null;
        });
      } else {
        ret.completeExceptionally(err);
      }
      return null;
    });
    addDependent(ret);
    return ret;
  }

  @Override
  public <U> VertxCompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
    VertxCompletableFuture<U> ret = new VertxCompletableFuture<>(context);
    handle((res, err) -> {
      if (err == null) {
        CompletionStage<U> apply;
        try {
          apply = fn.apply(res);
        } catch (Throwable e) {
          ret.completeExceptionally(e);
          return null;
        }

        apply.handleAsync((res2, err2) -> {
          if (err2 != null) {
            ret.completeExceptionally(err2);
          } else {
            ret.complete(res2);
          }
          return null;
        });
      } else {
        ret.completeExceptionally(err);
      }
      return null;
    });
    addDependent(ret);
    return ret;
  }

  @Override
  public <U> VertxCompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn,
                                                        Executor executor) {
    VertxCompletableFuture<U> ret = new VertxCompletableFuture<>(context);
    handle((res, err) -> {
      if (err == null) {
        CompletionStage<U> apply;
        try {
          apply = fn.apply(res);
        } catch (Throwable e) {
          ret.completeExceptionally(e);
          return null;
        }

        apply.handleAsync((res2, err2) -> {
          if (err2 != null) {
            ret.completeExceptionally(err2);
          } else {
            ret.complete(res2);
          }
          return null;
        }, executor);
      } else {
        ret.completeExceptionally(err);
      }
      return null;
    });
    addDependent(ret);
    return ret;
  }

  @Override
  public synchronized VertxCompletableFuture<T> exceptionally(Function<Throwable, ? extends T> fn) {
    VertxExceptionallyCompletionStage<T> accept = new VertxExceptionallyCompletionStage<>(context,
        ExecutionMode.ON_CALLER_THREAD, null, fn);
    addDependent(accept);
    return accept;
  }

  /**
   * Returns a new CompletionStage with the same result or exception as
   * this stage, that executes the given action when this stage completes.
   * <p>
   * <p>When this stage is complete, the given action is invoked with the
   * result (or {@code null} if none) and the exception (or {@code null}
   * if none) of this stage as arguments.  The returned stage is completed
   * when the action returns.  If the supplied action itself encounters an
   * exception, then the returned stage exceptionally completes with this
   * exception unless this stage also completed exceptionally.
   *
   * @param action the action to perform
   * @return the new CompletionStage
   */
  @Override
  public VertxCompletableFuture<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
    VertxCompletableFuture<T> ret = new VertxCompletableFuture<>(context);
    handle((res, err) -> {
      try {
        action.accept(res, err);
      } catch (Throwable e) {
        if (err == null) {
          ret.completeExceptionally(e);
        }
      }
      if (err == null) {
        ret.complete(res);
      } else {
        ret.completeExceptionally(err);
      }
      return null;
    });
    addDependent(ret);
    return ret;
  }

  @Override
  public VertxCompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
    VertxCompletableFuture<T> ret = new VertxCompletableFuture<>(context);
    handleAsync((res, err) -> {
      try {
        action.accept(res, err);
      } catch (Throwable e) {
        if (err == null) {
          ret.completeExceptionally(e);
        }
      }
      if (err == null) {
        ret.complete(res);
      } else {
        ret.completeExceptionally(err);
      }
      return null;
    });
    return ret;
  }


  @Override
  public VertxCompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor) {
    VertxCompletableFuture<T> ret = new VertxCompletableFuture<>(context);
    handleAsync((res, err) -> {
      try {
        action.accept(res, err);
      } catch (Throwable e) {
        if (err == null) {
          ret.completeExceptionally(e);
        }
      }
      return null;
    }, executor);
    return ret;
  }

  @Override
  public synchronized <U> VertxCompletableFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
    VertxHandleCompletionStage<T, U> handle = new VertxHandleCompletionStage<>(context,
        ExecutionMode.ON_CALLER_THREAD, null, fn);
    addDependent(handle);
    return handle;
  }

  public synchronized <U> VertxCompletableFuture<U> handle(Function<AsyncResult<T>, ? extends U> fn) {
    VertxHandleCompletionStage<T, U> handle = new VertxHandleCompletionStage<>(context,
        ExecutionMode.ON_CALLER_THREAD, null, fn);
    addDependent(handle);
    return handle;
  }

  @Override
  public synchronized <U> VertxCompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
    VertxHandleCompletionStage<T, U> handle = new VertxHandleCompletionStage<>(context,
        ExecutionMode.ON_CONTEXT, null, fn);
    addDependent(handle);
    return handle;
  }

  public synchronized <U> VertxCompletableFuture<U> handleAsync(Function<AsyncResult<T>, ? extends U> fn) {
    VertxHandleCompletionStage<T, U> handle = new VertxHandleCompletionStage<>(context,
        ExecutionMode.ON_CONTEXT, null, fn);
    addDependent(handle);
    return handle;
  }

  @Override
  public synchronized <U> VertxCompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
    VertxHandleCompletionStage<T, U> handle = new VertxHandleCompletionStage<>(context,
        ExecutionMode.ON_EXECUTOR, executor, fn);
    addDependent(handle);
    return handle;
  }

  public synchronized <U> VertxCompletableFuture<U> handleAsync(Function<AsyncResult<T>, ? extends U> fn, Executor executor) {
    VertxHandleCompletionStage<T, U> handle = new VertxHandleCompletionStage<>(context,
        ExecutionMode.ON_EXECUTOR, executor, fn);
    addDependent(handle);
    return handle;
  }

  @Override
  public VertxCompletableFuture<T> toCompletableFuture() {
    return this;
  }

  @Override
  public synchronized boolean complete(T value) {
    if (super.complete(value)) {
      current.complete(value);
      return true;
    }
    return false;
  }

  @Override
  public synchronized boolean completeExceptionally(Throwable ex) {
    if (super.completeExceptionally(ex)) {
      current.fail(ex);
      return true;
    }
    return false;
  }

  @Override
  public synchronized boolean cancel(boolean mayInterruptIfRunning) {
    if (super.cancel(mayInterruptIfRunning)) {
      dependents.forEach(stage -> {
        stage.cancel(mayInterruptIfRunning);
        stage.completeExceptionally(new CancellationException());
      });
      return true;
    }
    return false;
  }

  @Override
  public int getNumberOfDependents() {
    return dependents.size();
  }


}
