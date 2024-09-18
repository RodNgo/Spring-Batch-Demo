package com.rodngo.Spring_Batch_Demo.student;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentUpdateRepository extends JpaRepository<StudentUpdate, Integer> {
}
