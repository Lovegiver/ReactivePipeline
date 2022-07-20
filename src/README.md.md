# Reactive pipeline

The "*ReactivePipeline*" project is a simple interface, a single class project.

The `ReactiveContext` class just contains the static methods we need to instantiate the necessary objects for creating a persistent Flux pipeline for your whole application.
We will see each of them in the first part of this document.

Nevertheless, these methods / objects are not the only ones that are required to build a reactive app.
So the second part of this README will present you all the objects needed to make this API complete and working.

## How things may happen, how it will work

Most of the time, we use to design and build apps containing methods which are triggered sequencialy in a very procedural way. This can be represented by a straight line of processing operations : A --> B --> C --> ...

But we can also imagine operations as a tree in which methods A and B are independant, so parallelized,

## Objects from ReactiveContext

Many objects we'll talk about are wrappers. It is important to understand how they interact with each others.


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
eyJoaXN0b3J5IjpbMTc0MjA5Mjg4LDU1NDczMDU4NywxMDQ5MD
Y2MzM0LC01NDg2MjIzNzVdfQ==
-->