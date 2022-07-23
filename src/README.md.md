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
This is why the Task takes a `List<Task>` as argument. 


    static Task createTask(String taskName, Operation operation, List<Task> predecessors)

When defining a Task, what you concretely do is :

 1. Referencing the Operation to execute
 2. Referencing the Task(s) whom resulting Flux(es) will be used as argument for the task's Operation



    static Flux<ServerSentEvent<String>> getAllPipelinesStatesFlux()


    static Flux<ServerSentEvent<String>> getSinglePipelineStatesFlux(Pipeline pipeline)

## Other usefull objects

> Written with [StackEdit](https://stackedit.io/).
<!--stackedit_data:
eyJoaXN0b3J5IjpbMjA5Nzc5ODk2Niw0NDQ2NjM1NjQsLTE0Mj
c5MTc4ODQsMTgwMDE5MzgyMSwxNTAzNjM3MzM0LDI3MzE4Nzgw
OSwtMjczNzU3MDQ2LC05OTc1MDU1LDkwNjU4MzU4MywtMjA4MD
Q0MzIxNiw3MTQyNDUxMDEsMjAxNTAxMzY5NCw1NTQ3MzA1ODcs
MTA0OTA2NjMzNCwtNTQ4NjIyMzc1XX0=
-->