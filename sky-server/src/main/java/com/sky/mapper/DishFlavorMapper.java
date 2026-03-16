package com.sky.mapper;

import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper

public interface DishFlavorMapper {
    public void insertBatch(List<DishFlavor> flavors);

    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> getByDishId(Long dishId);

    // 根据菜品ID删除口味
    @Select("delete from dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(Long dishId); //菜品 ID
}
