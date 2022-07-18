package com.citizenweb.tooling.core;

import com.citizenweb.tooling.taskpipeline.core.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;

@Slf4j
public class ReactiveContext {

    private static final DataStreamer dataStreamer = DataStreamer.getInstance();

    public ReactiveContext() {}

    static Pipeline createPipeline(String pipelineName, Set<Task> allTasks) {
        return new Pipeline(pipelineName, allTasks);
    }

    static Pipeline createPipeline(String pipelineName, Set<Task> allTasks, WorkGroupOptimizer optimizer) {
        return new Pipeline(pipelineName, allTasks, optimizer);
    }

    static Task createTask(String taskName, Operation operation, List<Task> predecessors) {
        return new Task(taskName, operation, predecessors);
    }

    static Flux<ServerSentEvent<String>> getAllPipelinesStatesFlux() {
        return dataStreamer.exportData();
    }

    static Flux<ServerSentEvent<String>> getSinglePipelineStatesFlux(Pipeline pipeline) {
        return dataStreamer.exportData(pipeline);
    }

}
