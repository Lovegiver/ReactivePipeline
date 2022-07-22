# Reactive pipeline : the App

The "*ReactivePipeline*" project is a simple interface, a single class project. You can find link to the Github repository in Part. #1 of this post.

The `ReactiveContext` class just contains the *static methods* we need to instantiate the necessary objects for creating a persistent Flux pipeline for your whole application.
We will see each of them in the first part of this document.

Nevertheless, these methods / objects are not the only ones that are required to build a reactive app.
So the second part of this README will present you all the objects needed to make this API complete and working.

## How things may happen, how it will work

Most of the time, we use to design and build apps containing methods which are triggered sequentially in a very procedural way. This can be represented by a straight line of processing operations : A --> B --> C --> ...

But we can also imagine operations as a tree in which methods A and B are independent, so parallelized, and both producing a result which C deserves. In such a case, the C function will take result_A and result_B as arguments and we'll have to synchronize both operations in order to pass their respective results to C : C(result_A, result_B)
Or, at the opposite, a A function producing a result_A which will be consumed in a parallelized manner by B and C as soon as it will be available : B(result_A) // C(result_A)

To face all of these situations, we need a flexible data-structure where data - thus functions producing these data - will be organized smartly. Wrappers has been used for this to be possible.



## Objects from ReactiveContext

Many objects we'll talk about are wrappers. It is important to understand how they interact with each others.

 1. `Operation` is the corner stone of our model. It is a `Functional
    Interface`. Each action, each method, each function, has to be an
    Operation. An Operation takes a *varargs* of Flux as arguments and
    produces a Flux. 
    Task wraps a single Operation. It is a class with
    some usefull properties and methods. It triggers Operation execution
    and inject the produced Flux into the next Operation to maintain the
    reactiveness. WorkGroup is a wrapper for a set of Tasks, but you
    won't use it directly. You will use it only if you decide to create
    your own Optimizer. If you rely on the default Optimizer, the
    Pipeline will create WorkGroups for you. All you have to understand
    about a WorkGroup is that it groups all Tasks involved into the
    realization of a common final Operation. Finally, the Pipeline is a
    wrapper for a set of Tasks (and also for one or more WorkGroups as
    it will dispatch all the tasks in different workgroups).

The global philosophy is :

 1. We create all necessary Operations. Try to think this object as a pure function, doing just one thing.
 2. Each Operation is wrapped in a Task object. To be instantiated, a Task must have a single Operation and a Set of all the previous Tasks whom produced Fluxes are arguments for this Task.
 3. All the Tasks will finally be used as arguments for a Pipeline. The Pipeline, thanks to its Optimizer, create one or more WorkGroups. Once this is made, all WorkGroups will be executed in parallel threads and in an asynchronous manner.

### The Pipeline

The Pipeline class is a wrapper for 

    static Pipeline createPipeline(String pipelineName, Set<Task> allTasks)


    static Pipeline createPipeline(String pipelineName, Set<Task> allTasks, WorkGroupOptimizer optimizer)


    static Task createTask(String taskName, Operation operation, List<Task> predecessors)


    static Flux<ServerSentEvent<String>> getAllPipelinesStatesFlux()


    static Flux<ServerSentEvent<String>> getSinglePipelineStatesFlux(Pipeline pipeline)

## Other usefull objects

> Written with [StackEdit](https://stackedit.io/).
<!--stackedit_data:
eyJoaXN0b3J5IjpbNjQ1NzQzODM5LC05OTc1MDU1LDkwNjU4Mz
U4MywtMjA4MDQ0MzIxNiw3MTQyNDUxMDEsMjAxNTAxMzY5NCw1
NTQ3MzA1ODcsMTA0OTA2NjMzNCwtNTQ4NjIyMzc1XX0=
-->