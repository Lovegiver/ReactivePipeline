# Reactive pipeline : the App

The "*ReactivePipeline*" project is a simple interface, a single class project. You can find link to the Github repository in [Part. #1](https://dev.to/lovegiver/reactive-pipeline-a-starter-part-1-578n) of this post.

The `ReactiveContext` class just contains the *static methods* we need to instantiate the necessary objects for creating a persistent `Flux` *pipeline* for your whole application.
We will see each of them in the first part of this document.

Nevertheless, these methods / objects are not the only ones that are required to build a reactive app.
So the second part of this README will present you all the objects needed to make this API complete and working.

## How things may happen, how it will work

To start, just think about the fact that among all the operations of a particular process, we can distinguish :

 1. **Starting operations** which do not take any arguments, thus have no *predecessors*
 2. **Ending operations** which take arguments and have no *successors*
 3. **Intermediate operations** are the others : they need arguments from their predecessors and produce outputs that will be their successors' inputs

Most of the time, we use to design and build apps containing methods which are triggered sequentially in a very procedural way. This can be represented by a straight line of processing operations : A --> B --> C --> ...

But we can also imagine operations as a **tree** in which methods A and B are independent, so parallelized, and both producing a result which C deserves. In such a case, the C function will take result_A and result_B as arguments and we'll have to synchronize both operations in order to pass their respective results to C : C(result_A, result_B)
Or, at the opposite, a A function producing a result_A which will be consumed in a parallelized manner by B and C as soon as it will be available : B(result_A) // C(result_A)

To face all of these situations, we need a flexible data-structure where data - thus functions producing these data - will be organized smartly. Wrappers has been used for this to be possible.



## The toolbox

Many objects we'll talk about are wrappers. It is important to understand how they interact with each others.

 1. `Operation` is *the corner stone of our model*. It is a `Functional Interface`. Each action, each method, each function, has to be an `Operation`. An `Operation` takes a *varargs* of `Flux`(es) as arguments and produces a `Flux`.     
 2. `Task` wraps a single `Operation`. It is a class with some useful properties and methods. It triggers `Operation` execution and inject the produced `Flux` into the next `Operation` to maintain the reactive behavior.         
 3. `WorkGroup` is a wrapper for a set of `Task`s, but you won't use it directly. You will use it only if you decide to create your own `Optimizer`. If you rely on the default `Optimizer`, the `Pipeline` will create `WorkGroup`s for you. All you have to  understand about a `WorkGroup` is that it groups all `Task`s involved into the realization of a common final `Operation`.        
 4. Finally, the `Pipeline` is a wrapper for a set of `Task`s (and also for one or more `WorkGroup`s as it will dispatch all the tasks in different work-groups).
 5. A last object to talk about is the `DataStreamer`. It's aside from preceding objects as it is not a wrapper but the mechanism used to export the state of all `Monitorable` classes : `Task`, `WorkGroup` and `Pipeline` all have a `Monitor` property that describes their current state (new, running, done, in error). The `DataStreamer` produces a `Flux` containing all the states of all objects within the `Pipeline`. Each time one's state changes, a tick is triggered by a `Notifier` to the `DataStreamer`, which in turn triggers a new `Flux` that can, for example, be displayed on a web page for monitoring purpose. But it can be whatever you need. This is just the way I've chosen to talk about what is known under the ***hot stream*** name.

**The global philosophy is :**

 1. We create all necessary `Operation`s. Try to think this object as a pure function, doing just one thing.
 2. Each `Operation` is wrapped into a `Task` object. To be instantiated, a `Task` must have a single `Operation` and a Set of all the *previous* `Task`s whom produced `Flux`es are arguments for this `Task`.
 3. All the `Task`s will finally be used as arguments for a `Pipeline`. The `Pipeline`, thanks to its `Optimizer`, create one or more `WorkGroup`s. Once this is made, all `WorkGroup`s will be executed in *parallel threads* and in an *asynchronous* manner.

## Objects from the ReactiveContext

### The Pipeline

The Pipeline class is a wrapper for a set of Tasks. When calling its `.execute()` method, then all `Operation`s will be executed from the very first starting ones to the ending ones.

You can obtain a `Pipeline` using :

    static Pipeline createPipeline(String pipelineName, Set<Task> allTasks)

    static Pipeline createPipeline(String pipelineName, Set<Task> allTasks, WorkGroupOptimizer optimizer)

With the second method, you'll have to define your own `Optimizer`. This means that you define you own logic to group Tasks into `WorkGroup`s. In order to define your own `Optimizer`, you'll have to implement the following `Functional Interface` :

    Collection<WorkGroup> optimize(Set<Task> allTasks)


The existing default `Optimizer`'s logic is very basic and may be hugely improved and optimized (it is part of my To-Do list).

 1. First it looks for all ending (final, terminal) Tasks
 2. Then, it groups into a same WorkGroup all the Task involved into the realization of the same final Task

### The Task

The `Task` is the `Operation`'s wrapper. The `Operation` defines the domain's *logic* whereas the `Task` organize the interactions with other `Operation`s.
This is why the `Task` takes a `List<Task>` as argument. 


    static Task createTask(String taskName, Operation operation, List<Task> predecessors)

When defining a Task T, what you concretely do is :

 1. Referencing the Operation to execute
 2. Referencing the Task(s) whom resulting Flux(es) will be used as argument for the task T's Operation

### The DataStreamer

The `DataStreamer` produces a ***hot stream***, a potentially **never ending** `Flux`.
For the sake of demonstration, we used it as an exportable monitoring tool. This means that you can define a *REST controller* and a GET method returning a `Flux<ServerSentEvent>` that will be consumed by a web app.

    static Flux<ServerSentEvent<String>> getAllPipelinesStatesFlux()

    static Flux<ServerSentEvent<String>> getSinglePipelineStatesFlux(Pipeline pipeline)

In the case you define multiple Pipelines, the DataStreamer can produces a Flux for all of them or a single one. For a single Pipeline, you'll use the second method.

## Other useful objects

Other useful objects are not directly accessible through the `ReactiveContext` class.

### Operation

As already said, `Operation` is the corner-stone of this API. This interface is used to define you domain's logic. As it is a `Functional Interface`, you can use it as a Lambda expression.

    Flux<?> process(Flux<?>... inputs) throws TaskExecutionException;

We can take some frustrating examples to show how to use it :

    Operation o1 = inputs -> Flux.range(1,10);
    Operation o2 = inputs -> Flux.range(91,100);
    Operation o3 = inputs -> {  
      Flux<?> int1 = inputs[0];  
      Flux<?> int2 = inputs[1];  
      return Flux.zip(int1, int2, (x, y) -> (int) x + (int) y);  
    };

Operation o1 will produce a `Flux<Integer>` : 1, 2, 3... 10
Operation o2 will produce a `Flux<Integer>` : 91, 92, 93... 100
Operation o3 will use each single value from preceding `Flux`es by creating tuples that will be processed for the producing of a new result (it is the way to use the `.zip` operator) : 

 - (1, 91) will produce 92
 - (2, 92) will produce 94
 - (3, 93) will produce 96
 - etc.

Of course, this is possible only if you have created the necessary Tasks objects around your Operations :

    Task t1 = ReactiveContext.createTask("Integer Flux 1", o1, Collections.emptyList());
    Task t2 = ReactiveContext.createTask("Integer Flux 2", o2, Collections.emptyList());
    Task t3 = ReactiveContext.createTask("Sum t1 t2", o3, List.of(t1, t2));

There's many things to say here.

 - the `Operation`'s single abstract method, `process(Flux... inputs)`, may take 0, 1 or N `Flux`(es) as argument. This is why the Lambda expression starts this way : `inputs -> ... ;` In the case of a starting `Operation`, an `Operation` without any *predecessors*, there's no inputs to process but we have to respect the method's signature. In the example above, only the **o3** operation has inputs to process and this is done by getting them from the array of `Flux`es produced by the varargs argument.
 - the **t1** and **t2** `Task`s wrap starting `Operation`s, that's why there's no previous `Task`s to declare here. But we still have to pass an empty collection as argument.
 - the **t3** `Task` do have predecessors, respectively the **t1** and **t2** `Task`s which are respectively wrapping **o1** and **o2** `Operation`s. In that case, we pass a collection made of the **t1** and **t2** `Task`s. This collection is a `List`, because the order of the argument matters of course. 

### Notifier / StateNotifier

You theoretically will not have to handle the `Notifier` (interface) and the `StateNotifier` (implementation) in charge of notifying the `DataStreamer` of any change in any `Monitorable`'s inner state.
We here use a *Visitor*'s pattern to delegate the action of notifying to an independent object knowing the `DataStreamer` and the `Pipeline` it is reporting about.

### Monitorable / Monitor

The `Monitor` is a class holding the inner state of any `Monitorable` object. Like we have seen before, `Monitorable` objects are :

 - `Pipeline`
 - `WorkGroup`
 - `Task`

The `Monitorable` class is the abstract part from which the 3 above objects are derived. Its properties are :

    String name;
    Monitor monitor;
    Notifier notifier;
    Map<Task, Optional<Flux<?>>> inputFluxesMap = Collections.synchronizedMap(new LinkedHashMap<>());



> Written with [StackEdit](https://stackedit.io/).
<!--stackedit_data:
eyJoaXN0b3J5IjpbMzAzMDEwODEsLTcwNDk2MDgwMCwtMzYyMz
A1MTc0LDE1NjY5NjE3NTgsLTI0ODg4MDQsMTg5NzE4NjI1Myw0
NDQ2NjM1NjQsLTE0Mjc5MTc4ODQsMTgwMDE5MzgyMSwxNTAzNj
M3MzM0LDI3MzE4NzgwOSwtMjczNzU3MDQ2LC05OTc1MDU1LDkw
NjU4MzU4MywtMjA4MDQ0MzIxNiw3MTQyNDUxMDEsMjAxNTAxMz
Y5NCw1NTQ3MzA1ODcsMTA0OTA2NjMzNCwtNTQ4NjIyMzc1XX0=

-->