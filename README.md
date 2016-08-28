# Vert.x Completable Future

This project provides a way to use the Completable Future API with Vert.x.

## Why can't you use Completable Future with Vert.x

Using Completable Future with Vert.x may lead to some thread issues. 
  
When you execute an asynchronous operation with Vert.x, the `Handler<AsyncResult<T>>` is called on on the same thread as the method having enqueued the asynchronous operation. Thanks to this _single-threaded_ aspect, Vert.x removes the need of synchronization and also it improves performances.
   
Completable Future uses a fork-join thread pool. So callbacks (dependent stages) are called in a thread of this pool, so it's not the caller thread.
    
This project provides the Completable Future API but enforces the Vert.x threading model:

* When using `xAsync` methods (without `executor`), the callbacks are called on the Vert.x context
* When using non-async, it uses the caller thread. If it's a Vert.x thread the same thread is used. If not called from a Vert.x thread, 
it still uses the caller thread 
* When using `xAsync` methods with an `Executor` parameter, this executor is used to execute the callback (does not enforce the Vert.x thread system)
     
## Examples

All methods taking a `Vertx` instance as parameter exists with a `Context` parameter.
     
### Creating a VertxCompletableFuture
     
```
// From a Vert.x instance
CompletableFuture<T> future = new VertxCompletableFuture(vertx);

// From a specific context
Context context = ...
CompletableFuture<T> future = new VertxCompletableFuture(context);

// From a completable future
CompletableFuture cf = ...
CompletableFuture<T> future = VertxCompletableFuture.from(context, cf);

// From a Vert.x future
Future<T> fut = ...
CompletableFuture<T> future = VertxCompletableFuture.from(context, fut);
```     

You can also pass a `Supplier` or a `Runnable`:

```
// Run in context
CompletableFuture<String> future = VertxCompletableFuture.supplyAsync(vertx, () -> return "foo";);
// Run in Vert.x worker thread
CompletableFuture<String> future = VertxCompletableFuture.supplyBlockingAsync(vertx, () -> return "foo");
 
CompletableFuture<Void> future = VertxCompletableFuture.runAsync(vertx, () -> System.out.println(foo"));
CompletableFuture<Void> future = VertxCompletableFuture.runBlockingAsync(vertx, () -> System.out.println(foo"));
```

_*BlockingAsync_ method uses a Vert.x worker thread and do not block the Vert.x Event Loop.

### Stages

Once you have the `VertxCompletableFuture` instance, you can use the `CompletableFuture` API:

```
VertxCompletableFuture<Integer> future = new VertxCompletableFuture<>(vertx);
future
    .thenApply(this::square)
    .thenAccept(System.out::print)
    .thenRun(System.out::println);

// Somewhere in your code, later...
future.complete(42);
```

You can compose, combine, join completable futures with:

* combine
* runAfterEither
* acceptEither
* runAfterBoth
* ...        

### All and Any operations

The `VertxCompletableFuture` class offers two composition operators:
    
* `allOf` - execute all the passed `CompletableFuture` (not necessarily `VertxCompletableFuture`) and calls dependant stages when all of the futures have been completed or one has failed 
* `anyOf` - execute all the passed `CompletableFuture` (not necessarily `VertxCompletableFuture`) and calls dependant stages when one of them has been completed
     
Unlike `CompletableFuture`, the dependent stages are called in the Vert.x context.     

```
HttpClientOptions options = new HttpClientOptions().setDefaultPort(8080).setDefaultHost("localhost");
HttpClient client1 = vertx.createHttpClient(options);
HttpClient client2 = vertx.createHttpClient(options);

VertxCompletableFuture<Integer> requestA = new VertxCompletableFuture<>(vertx);
client1.get("/A").handler(resp -> {
  resp.exceptionHandler(requestA::completeExceptionally)
      .bodyHandler(buffer -> {
        requestA.complete(Integer.parseInt(buffer.toString()));
      });
}).exceptionHandler(requestA::completeExceptionally).end();

VertxCompletableFuture<Integer> requestB = new VertxCompletableFuture<>(vertx);
client2.get("/B").handler(resp -> {
  resp.exceptionHandler(requestB::completeExceptionally)
      .bodyHandler(buffer -> {
        requestB.complete(Integer.parseInt(buffer.toString()));
      });
}).exceptionHandler(requestB::completeExceptionally).end();


VertxCompletableFuture.allOf(requestA, requestB).thenApply(v -> requestA.join() + requestB.join())
    .thenAccept(i -> {
      tc.assertEquals(65, i);
      async.complete();
    });
}
```

### From / To Vert.x Futures

You can transform a `VertxCompletableFuture` to a Vert.x `Future` with the `toFuture` method.

You can also creates a new `VertxCompletableFuture` from a Vert.x `Future` using:

```
Future<Integer> vertxFuture = ...
VertxCompletableFuture<Integer> vcf = VertxCompletableFuture.from(vertx, vertxFuture);

vcf.thenAccept(i -> {...}).whenComplete((res, err) -> {...})
```



