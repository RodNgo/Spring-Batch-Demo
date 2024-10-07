package com.rodngo.Spring_Batch_Demo.config;

import com.rodngo.Spring_Batch_Demo.student.Student;
import com.rodngo.Spring_Batch_Demo.student.StudentRepository;
import com.rodngo.Spring_Batch_Demo.student.StudentUpdate;
import org.springframework.batch.item.ItemProcessor;

public class StudentProcessor implements ItemProcessor<Student, StudentUpdate> {

    private final StudentRepository studentRepository;

    public StudentProcessor(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public StudentUpdate process(Student student) throws Exception {
        // Kiểm tra nếu ID của Student là null thì ném ra RuntimeException
        if (student.getId() == null) {
            throw new RuntimeException("Student ID is null, processing failed");
        }
        // Lưu student vào bảng student trước
        studentRepository.save(student);

        // Tăng age lên 1
        Integer updatedAge = student.getAge() + 1;

        // Tạo đối tượng StudentUpdate
        StudentUpdate studentUpdate = new StudentUpdate();
        studentUpdate.setId(student.getId());
        studentUpdate.setFirstName(student.getFirstname());
        studentUpdate.setLastName(student.getLastname());
        studentUpdate.setAge(updatedAge);
        studentUpdate.setEditedContent("Age updated from " + student.getAge() + " to " + updatedAge);

        return studentUpdate;
    }
}
