package com.algoExpert.demo.Mapper;


import com.algoExpert.demo.Dto.TaskDto;
import com.algoExpert.demo.Entity.Task;



public class TaskMapper {
    public static TaskDto mapToTaskDto(Task task) {
        return new TaskDto(
                task.getTask_id(),
                task.getTitle(),
                task.getDescription(),
                task.getUsername(),
                task.getStart_date(),
                task.getEnd_date(),
                task.getStatus(),
                task.getPriority(),
                task.getComments(),
                task.getAssignees()
        );
    }

    public static Task mapToTask(TaskDto taskDto) {
        return new Task(
                taskDto.getTask_id(),
                taskDto.getTitle(),
                taskDto.getDescription(),
                taskDto.getUsername(),
                taskDto.getStart_date(),
                taskDto.getEnd_date(),
                taskDto.getStatus(),
                taskDto.getPriority(),
                taskDto.getComments(),
                taskDto.getAssignees()
        );
    }
}
