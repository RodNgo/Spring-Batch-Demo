package com.rodngo.Spring_Batch_Demo.student;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final JobLauncher jobLauncher;
    private final Job job;
    private final JobExplorer jobExplorer;
    private final JobOperator jobOperator;
    //JobLauncher chịu trách nhiệm khởi chạy Job. Nó cần Job và các thông số (parameters) để bắt đầu quá trình xử lý.
    @PostMapping
    public ResponseEntity<String> importCsvToDBJob() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .toJobParameters();
        try {
            jobLauncher.run(job, jobParameters);
            return ResponseEntity.ok("Job started successfully!");
        } catch (JobExecutionAlreadyRunningException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Job is already running.");
        } catch (JobRestartException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Job restart failed.");
        } catch (JobInstanceAlreadyCompleteException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Job instance already completed.");
        } catch (JobParametersInvalidException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid job parameters.");
        }
    }

    @GetMapping("/status")
    public ResponseEntity<List<String>> getJobStatus() {
        List<JobInstance> jobInstances = jobExplorer.getJobInstances("importStudents", 0, 5);
        List<String> statuses = new ArrayList<>();
        for (JobInstance instance : jobInstances) {
            List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(instance);
            for (JobExecution execution : jobExecutions) {
                statuses.add("Job Instance ID: " + instance.getInstanceId() +
                        " Execution ID: " + execution.getId() +
                        " Status: " + execution.getStatus());
            }
        }
        return ResponseEntity.ok(statuses);
    }
    //Công dụng: JobOperator cung cấp các phương thức để quản lý Job từ xa, như khởi động, dừng, hoặc khởi động lại Job.
    // Nó đóng vai trò như giao diện quản lý Job.
    // New endpoint to stop the job using JobOperator
    //Start a Job: Bạn có thể bắt đầu một job với jobOperator.start().
    // Nhưng trong trường hợp này, JobLauncher đã đủ để bắt đầu job.
    @PostMapping("/stop/{executionId}")
    public ResponseEntity<String> stopJob(@PathVariable Long executionId) {
        try {
            jobOperator.stop(executionId);
            return ResponseEntity.ok("Job stopped successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to stop the job: " + e.getMessage());
        }
    }

    // New endpoint to restart a failed job using JobOperator
    @PostMapping("/restart/{executionId}")
    public ResponseEntity<String> restartJob(@PathVariable Long executionId) {
        try {
            jobOperator.restart(executionId);
            return ResponseEntity.ok("Job restarted successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to restart the job: " + e.getMessage());
        }
    }
//    @PostMapping("/update")
//    public ResponseEntity<String> updateStudentsInDBJob() {
//        JobParameters jobParameters = new JobParametersBuilder()
//                .addLong("startAt", System.currentTimeMillis())  // Thêm timestamp để đảm bảo job luôn chạy
//                .toJobParameters();
//        try {
//            jobLauncher.run(job, jobParameters);
//            return ResponseEntity.ok("Batch job to update students is started.");
//
//        } catch (JobExecutionAlreadyRunningException
//                 | JobRestartException
//                 | JobInstanceAlreadyCompleteException
//                 | JobParametersInvalidException e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while starting the batch job.");
//        }
//    }
}
