package com.rodngo.Spring_Batch_Demo.student;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "student_update")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentUpdate {
    @Id
    private Integer id;
    private String firstName;
    private String lastName;
    private Integer age;
    private String editedContent;
}
