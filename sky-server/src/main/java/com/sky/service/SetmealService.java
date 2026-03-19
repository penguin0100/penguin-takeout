package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    //分页查询
    PageResult page(SetmealPageQueryDTO setmealPageQueryDTO);

    //新增套餐
    void save(SetmealDTO setmealDTO);

    //根据ID查询
    SetmealVO getIdWithDish(Long id);

    void deleteBatch(List<Long> ids);

    void update(SetmealDTO setmealDTO);

}
