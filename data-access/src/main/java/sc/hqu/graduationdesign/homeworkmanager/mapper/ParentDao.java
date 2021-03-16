package sc.hqu.graduationdesign.homeworkmanager.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import sc.hqu.graduationdesign.homeworkmanager.entity.ParentEntity;
import sc.hqu.graduationdesign.homeworkmanager.model.ClassStudentParentView;

import java.util.List;

/**
 * 家长信息数据访问接口
 * @author tzx
 * @date 2021-03-14 23:43
 */
@Repository
public interface ParentDao {

    /**
     * 通过视图数据查询班级家长详细信息
     * @param cid       班级id
     * @return          {@link ClassStudentParentView}
     */
    @Select("select student_no,parent_id,name,contact from v_class_student_parent where class_id=#{cid}")
    List<ClassStudentParentView> queryStudentParentByClassIdInView(Long cid);

    /**
     * 批量插入家长信息记录
     * @param pes       {@link ParentEntity}
     */
    void batchInsertParent(@Param("pes") List<ParentEntity> pes);

}