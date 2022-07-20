# Reactive pipeline

The "*ReactivePipeline*" project is a simple interface, a single class project.

The `ReactiveContext` class just contains the static methods we need to instantiate the necessary objects for creating a persistent Flux pipeline for your whole application.
We will see each of them in the first part of this document.

Nevertheless, these methods / objects are not the only ones that are required to build a reactive app.
So the second part of this README will present you all the objects needed to make this API complete and working.

## Objects from ReactiveContext


    static Pipeline createPipeline(String pipelineName, Set<Task> allTasks)


    static Pipeline createPipeline(String pipelineName, Set<Task> allTasks, WorkGroupOptimizer optimizer)


    static Task createTask(String taskName, Operation operation, List<Task> predecessors)


    static Flux<ServerSentEvent<String>> getAllPipelinesStatesFlux()


    static Flux<ServerSentEvent<String>> getSinglePipelineStatesFlux(Pipeline pipeline)



> Written with [StackEdit](https://stackedit.io/).
<!--stackedit_data:
eyJoaXN0b3J5IjpbLTk3NDg1NjY1MSwxMDQ5MDY2MzM0LC01ND
g2MjIzNzVdfQ==
-->