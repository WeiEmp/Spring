package cn.mybatisle.dao;

import cn.mybatisle.bean.Department;

public interface DepartmentMapper {

    Department getDeptById(Integer id);

    Department getDepAndEmpsById(Integer id);

    Department getDepByIdPlus(Integer id);
}
