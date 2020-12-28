package cn.mybatisle.dao;

import cn.mybatisle.bean.Employee;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: CodeEmp
 * @time: 2020/12/15 9:06
 */
public interface EmployeeMapper {

    //多条记录封装一个map：Map<Integer, Employee> 键是这个条记录的主键，记录封装后的javaBean
    //告诉mybatis封装这个map的时候使用哪个属性作为map的主键
    @MapKey("id")
    Map<Integer, Employee> getEmpByLastNameLikeReturnMap(String lastName);


    //返回一条记录的map，key就是列名，值就是对应的值
    Map<String, Object> getEmpByIdReturnMap(Integer id);

    List<Employee> getEmpsByLastName(String lastName);

    Employee getEmpByMap(Map<String, Object> map);

    Employee getEmpByIdAndLastName(@Param("id")Integer id, @Param("lastName")String lastName);

    Employee getEmpById(Integer id);

    Integer addEmp(Employee employee);

    Integer updateEmp(Employee employee);

    Integer deleteEmpById(Integer id);
}
