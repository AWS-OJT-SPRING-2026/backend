package ojt.aws.educare.repository;

import ojt.aws.educare.entity.ClassMember;
import ojt.aws.educare.entity.ClassMemberID;
import ojt.aws.educare.entity.Classroom;
import ojt.aws.educare.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassMemberRepository extends JpaRepository<ClassMember, ClassMemberID> {
    boolean existsByStudentAndClassroom(Student student, Classroom classroom);

    List<ClassMember> findByClassroomClassID(Integer classID);

    List<ClassMember> findByClassroomClassIDOrderByStudentFullNameAsc(Integer classID);

    List<ClassMember> findByStudent_StudentID(Integer studentID);

    @Modifying
    @Query("DELETE FROM ClassMember cm WHERE cm.classMemberID.classID = :classID AND cm.classMemberID.studentID IN :studentIDs")
    void deleteByClassIDAndStudentIDIn(@Param("classID") Integer classID, @Param("studentIDs") List<Integer> studentIDs);
}