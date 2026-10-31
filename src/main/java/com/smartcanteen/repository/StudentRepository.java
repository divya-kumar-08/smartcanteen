package com.smartcanteen.repository;

import com.smartcanteen.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentIdNumber(String studentIdNumber);
    Optional<Student> findByPhone(String phone);
}
