package com.rodngo.Spring_Batch_Demo.config;

import com.rodngo.Spring_Batch_Demo.student.Student;
import com.rodngo.Spring_Batch_Demo.student.StudentRepository;
import com.rodngo.Spring_Batch_Demo.student.StudentUpdate;
import com.rodngo.Spring_Batch_Demo.student.StudentUpdateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Locale;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final StudentRepository studentRepository;
    private final StudentUpdateRepository studentUpdateRepository;

    @Bean
    public FlatFileItemReader<Student> reader() {
        FlatFileItemReader<Student> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new ClassPathResource("students.csv"));
        itemReader.setName("csvReader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }
    //Jobrepository sử dụng Jobbuilder và StepBuilder
    // JobRepository đã được sử dụng trong các StepBuilder và JobBuilder để quản lý quá trình chạy của Job và Step,
    // cho phép theo dõi trạng thái và phục hồi lại từ các lần chạy trước nếu xảy ra lỗi.

    @Bean
    public StudentProcessor processor() {
        return new StudentProcessor(studentRepository);
    }

    @Bean
    public RepositoryItemWriter<StudentUpdate> writer() {
        RepositoryItemWriter<StudentUpdate> itemWriter = new RepositoryItemWriter<>();
        itemWriter.setRepository(studentUpdateRepository);
        itemWriter.setMethodName("save");
        return itemWriter;
    }

    @Bean
    public Step step1() {
        return new StepBuilder("csvImport", jobRepository)
                .<Student, StudentUpdate>chunk(10, platformTransactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .taskExecutor(taskExecutor())
                .build();
    }
    @Bean
    public Job runJob() {
        return new JobBuilder("importStudents", jobRepository)
                .start(step1())
                .listener(new JobExecutionListener() {
                    private LocalDateTime startTime;
                    @Override
                    public void beforeJob(JobExecution jobExecution) {
                        startTime = LocalDateTime.now();
                        System.out.println("Before Job");
                    }

                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        LocalDateTime  endTime = LocalDateTime.now();
                        Long duration = java.time.Duration.between(startTime, endTime).toMillis();
                        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                            System.out.println("Job completed");
                            System.out.println(duration);
                        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
                            System.out.println("Job failed");
                        }
                    }
                })
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(10);
        return asyncTaskExecutor;
    }

    private LineMapper<Student> lineMapper() {
        DefaultLineMapper<Student> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id", "firstName", "lastName", "age");

        BeanWrapperFieldSetMapper<Student> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Student.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }
}
