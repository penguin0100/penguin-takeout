package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Transactional
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        // 保存菜品数据
        Dish dish = new Dish();
        // 属性拷贝
        BeanUtils.copyProperties(dishDTO,dish);
        //向菜品表插入数据
        dishMapper.insert(dish);
        //获取inert语句生成的主键id
        Long dishId = dish.getId();
        //向口味表插入n数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
            dishFlavorMapper.insertBatch(flavors);
        }
    }
    // 分页查询
    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult( page.getTotal(), page.getResult());
    }
    // 根据id查询菜品和对应的口味
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        // 查询菜品数据
        Dish dish = dishMapper.getById(id);
        // 查询口味数据
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
        // 封装DishVO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);//封装菜品数据
        dishVO.setFlavors(dishFlavors);//封装口味数据
        return dishVO;//返回菜品VO对象
    }
    // 修改菜品
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        // 更新菜品基本数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        // 更新口味数据
        //删除原有的口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //重新插入口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            //向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    // 批量删除
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteBatch(List<Long> ids) {
        //判断当前菜品是否能够删除---是否存在起售中的菜品？？
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                //当前菜品处于起售中，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 判断当前菜品是否能够删除---是否被套餐关联了？？
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            // 当前菜品被套餐关联了，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品表中的菜品数据
        for (Long id : ids){
            dishMapper.deleteById(id);
            //批量删除口味数据
            dishFlavorMapper.deleteByDishId(id);
        }
    }
    // 起售、停售
    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);

        if (status == StatusConstant.DISABLE) {
            // 如果是停售操作，还需要将包含当前菜品的套餐也停售
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id); //将当前菜品的 ID 添加到列表中
            // select setmeal_id from setmeal_dish where dish_id in (?,?,?)
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(dishIds); //查询包含当前菜品的所有套餐 ID
            if (setmealIds != null && setmealIds.size() > 0) { //检查是否有套餐包含了当前菜品
                for (Long setmealId : setmealIds) {//遍历所有需要停售的套餐 ID
                    Setmeal setmeal = Setmeal.builder()//创建一个套餐对象，设置新的状态为"停售"
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    setmealMapper.update(setmeal);//更新数据库中套餐的状态
                }
            }
        }
    }

    // 根据分类id查询菜品
    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                    .categoryId(categoryId)
                    .status(StatusConstant.ENABLE)
                    .build();
        return dishMapper.list(dish);
    }


    // 根据分类id查询菜品和对应的口味
    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }



}
