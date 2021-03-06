package sc.hqu.graduationdesign.homeworkmanager.consumer.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sc.hqu.graduationdesign.homeworkmanager.constant.ErrorCode;
import sc.hqu.graduationdesign.homeworkmanager.constant.FilePublishType;
import sc.hqu.graduationdesign.homeworkmanager.consumer.dto.*;
import sc.hqu.graduationdesign.homeworkmanager.consumer.service.ClassService;
import sc.hqu.graduationdesign.homeworkmanager.entity.*;
import sc.hqu.graduationdesign.homeworkmanager.exception.BusinessException;
import sc.hqu.graduationdesign.homeworkmanager.mapper.*;
import sc.hqu.graduationdesign.homeworkmanager.model.ClassStudentParentView;
import sc.hqu.graduationdesign.homeworkmanager.model.ClassStudentView;
import vinfer.learnjava.queryhelper.annotation.QueryHelper;
import vinfer.learnjava.queryhelper.constant.InterceptMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tzx
 * @date 2021-04-07 22:03
 */
@Service
public class ClassServiceImpl implements ClassService {

    @Autowired
    private ClassDao classDao;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private ParentDao parentDao;

    @Autowired
    private FileDao fileDao;

    @Override
    public List<ClassDataDto> getClassDataByTeacherNo(Long teacherNo) {
        // 查询所有班级数据
        List<ClassEntity> classEntities = classDao.queryAllByTeacherNo(teacherNo);
        List<ClassDataDto> classDataDtoList = new ArrayList<>(classEntities.size());
        // 遍历班级信息，查找详细的数据进行赋值
        classEntities.forEach(classEntity -> {
            Long classId = classEntity.getId();
            ClassDataDto classDataDto = new ClassDataDto();
            // 复制班级数据
            BeanUtils.copyProperties(classEntity,classDataDto);

            // 查找并复制班级的学生数据
            List<ClassStudentView> classStudentViews = studentDao.queryFullInfoByClassIdInView(classId);
            List<ClassStudentDto> classStudentDtoList = new ArrayList<>(classStudentViews.size());
            classStudentViews.forEach( classStudentView -> {
                ClassStudentDto classStudentDto = new ClassStudentDto();
                BeanUtils.copyProperties(classStudentView,classStudentDto);
                classStudentDtoList.add(classStudentDto);
            });

            // 查找并复制学生家长数据
            List<ClassStudentParentView> classStudentParentViews = parentDao.queryStudentParentByClassIdInView(classId);
            List<StudentParentDto> studentParentDtoList = new ArrayList<>(classStudentParentViews.size());
            classStudentParentViews.forEach(classStudentParentView -> {
                StudentParentDto studentParentDto = new StudentParentDto();
                String parentName = classStudentParentView.getName();
                String contact = classStudentParentView.getContact();
                if (classStudentParentView.getGender() == 1){
                    studentParentDto.setFatherName(parentName);
                    studentParentDto.setFatherContact(contact);
                }else {
                    studentParentDto.setMotherName(parentName);
                    studentParentDto.setMotherContact(contact);
                }
                studentParentDto.setStudentNo(classStudentParentView.getStudentNo());
                studentParentDtoList.add(studentParentDto);
            });

            // 查找并复制发布到该班级的文件
            List<FileEntity> fileEntities = fileDao.queryPublishFile(classId, FilePublishType.CLASS.getType());
            List<SimpleFileDataDto> fileDataDtoList = new ArrayList<>(fileEntities.size());
            fileEntities.forEach(fileEntity -> {
                SimpleFileDataDto simpleFileDataDto = new SimpleFileDataDto();
                BeanUtils.copyProperties(fileEntity,simpleFileDataDto);
                fileDataDtoList.add(simpleFileDataDto);
            });

            // 将数据保存到classDataDto中
            classDataDto.setStudentDtoList(classStudentDtoList);
            classDataDto.setStudentParentDtoList(studentParentDtoList);
            classDataDto.setFileDataDtoList(fileDataDtoList);
            classDataDtoList.add(classDataDto);
        });
        return classDataDtoList;
    }

    @Override
    public List<ClassDataDto> getSimpleClassByTeacherNo(Long teacherNo) {
        List<ClassEntity> classEntities = classDao.queryAllByTeacherNo(teacherNo);
        List<ClassDataDto> classDataDtoList = new ArrayList<>(classEntities.size());
        // 遍历班级信息，查找详细的数据进行赋值
        classEntities.forEach(classEntity -> {
            ClassDataDto classDataDto = new ClassDataDto();
            // 复制班级数据
            BeanUtils.copyProperties(classEntity,classDataDto);
            classDataDtoList.add(classDataDto);
        });
        return classDataDtoList;
    }

    @Override
    public List<ClassStudentView> getAllStudentByTeacherNo(Long teacherNo) {
        return studentDao.queryAllStudentByTeacherNo(teacherNo);
    }

    @Override
    public List<ClassStudentParentView> getAllParentByTeacherNo(Long teacherNo) {
        return parentDao.queryAllStudentParentByTeacherNo(teacherNo);
    }


    @QueryHelper(mapperClass = StudentDao.class,interceptMode = InterceptMode.MODIFY_RESULT)
    @Override
    public Object getClassStudentPage(Long teacherNo, int pageSize, int pageNum) {
        return studentDao.queryAllStudentByTeacherNo(teacherNo);
    }

    @Override
    public ClassCreateDto create(ClassCreateDto dto) {
        ClassEntity classEntity = new ClassEntity();
        BeanUtils.copyProperties(dto,classEntity);
        long now = Calendar.getInstance().getTimeInMillis();
        classEntity.setCreateDate(now);
        classDao.insertClass(classEntity);
        dto.setId(classEntity.getId());
        return dto;
    }

    @Override
    public void update(ClassUpdateDto dto) {
        ClassEntity classEntity = new ClassEntity();
        BeanUtils.copyProperties(dto,classEntity);
        classDao.updateClass(classEntity);
    }

    @Override
    public void delete(Long classId) throws BusinessException {
        ClassEntity classEntity = classDao.queryById(classId);
        if (classEntity.getTotalStudentNum() == 0){
            classDao.deleteClassById(classId);
        }else {
            throw new BusinessException(ErrorCode.DELETE_FORBIDDEN);
        }
    }

    @Override
    @Transactional(rollbackFor = {Exception.class,RuntimeException.class})
    public void batchAddStudent(ClassStudentAddDto dto) throws BusinessException {
        // 先查询当前班级所有学生的学号
        Long classId = dto.getClassId();
        List<Long> existedStudentNo = studentDao.queryAllStudentNoExisted();
        // 学生与班级的关系是一对一，不允许一个学生加入多个班级
        List<Long> collect = dto.getStudentDtoList().stream().map(ClassStudentDto::getStudentNo).collect(Collectors.toList());
        for (Long studentNo:collect){
            if (existedStudentNo.contains(studentNo)){
                throw new BusinessException(ErrorCode.DUPLICATE_STUDENT);
            }
        }
        //对导入学生的基于学号进行去重
        List<ClassStudentDto> studentDtoList = dto.getStudentDtoList();
        // 学生家长信息也需要去重
        List<StudentParentDto> studentParentDtoList = dto.getStudentParentDtoList();
        if (studentDtoList.size() > 0){
            List<StudentEntity> studentEntities = new ArrayList<>(studentDtoList.size());
            List<ParentEntity> parentEntities = new ArrayList<>(studentParentDtoList.size() * 2);
            studentDtoList.forEach(classStudentDto -> {
                StudentEntity studentEntity = new StudentEntity();
                studentEntity.setClassId(classId);
                BeanUtils.copyProperties(classStudentDto,studentEntity);
                studentEntities.add(studentEntity);
            });
            studentParentDtoList.forEach(studentParentDto -> {
                ParentEntity father = new ParentEntity();
                ParentEntity mother = new ParentEntity();
                father.setContact(studentParentDto.getFatherContact());
                father.setName(studentParentDto.getFatherName());
                father.setStudentNo(studentParentDto.getStudentNo());
                father.setGender(1);
                mother.setStudentNo(studentParentDto.getStudentNo());
                mother.setContact(studentParentDto.getMotherContact());
                mother.setName(studentParentDto.getMotherName());
                mother.setGender(0);
                parentEntities.add(father);
                parentEntities.add(mother);
            });
            // 批量保存学生记录
            studentDao.batchInsertStudent(studentEntities);
            // 批量保存家长记录
            parentDao.batchInsertParent(parentEntities);
            // 更新班级总学生数
            classDao.updateClassStudentNumByClassId(classId,studentEntities.size());
        }
    }


    @Override
    @Transactional(rollbackFor = {Exception.class,RuntimeException.class})
    public void removeClassStudent(Long classId,Long studentNo) {
        // classId暂时不需要
        // 先根据学号同步该生所有班级的学生人数
        classDao.reduceClassStudentNumByStudentNo(studentNo);
        // 删除学生记录
        studentDao.deleteStudentFromClass(studentNo);

    }
}
