package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotion.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 新增菜品数据
     * @param dish
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);


    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据主键删除菜品
     * @param id
     */
//    @Select("select * from dish where id = #{id}")
//    void deleteByIds(Long id);
    //根据id查询菜品数据
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    // 根据 id 批量查询菜品
    List<Dish> getByIds(List<Long> ids);

    //根据主键删除菜品数据
    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);
    //根据id动态修改菜品数据
    @AutoFill(value = OperationType.UPDATE)// 更新
    void update(Dish dish);

}
