package kr.co.inhatc.lms.lecture;

import kr.co.inhatc.lms.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface LectureRepository extends JpaRepository<Lecture,Long> {
    boolean existsByPath(String path);

    Lecture findByPath(String path);

    List<Lecture> findFirst20ByLecturerContaining(Account account);

    List<Lecture> findFirst20ByStudentContaining(Account account);

    Lecture findStudyWithLecturerByPath(String path);

    Lecture findStudyWithStudentByPath(String path);

    Lecture findLectureOnlyByPath(String path);
}
