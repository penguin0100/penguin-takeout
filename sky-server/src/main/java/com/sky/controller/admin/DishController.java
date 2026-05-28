package com.sky.controller.admin;

import com.sky.dto.DataOverViewQueryDTO;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.utils.RedisLockUtil;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品管理接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品：{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);

        return Result.success();
    }
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("分页查询：{}",dishPageQueryDTO);
        PageResult pageResult = dishService.page(dishPageQueryDTO);
        return Result.success(pageResult);
    }
    @DeleteMapping
    @ApiOperation("批量删除")
    public Result delete(@RequestParam List<Long> ids){
        log.info("批量删除：{}",ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询菜品")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("查询菜品：{}",id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);//根据菜品ID查询菜品信息，包括菜品的口味信息
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品：{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);
        return Result.success();
    }
    @PostMapping("/status/{status}")
    @ApiOperation("起售、停售")
    public Result<String> startOrStop(@PathVariable Integer status,Long id){
        dishService.startOrStop(status,id);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(@RequestParam Long categoryId){
        log.info("根据分类Id查询菜品 {}",categoryId);
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

    /**
     * 设置菜品库存（Redis）
     */
    @PostMapping("/stock")
    @ApiOperation("设置菜品库存")
    public Result setStock(@RequestBody Map<String, Object> params) {
        Long dishId = Long.valueOf(params.get("dishId").toString());
        Integer stock = Integer.valueOf(params.get("stock").toString());
        String key = RedisLockUtil.getDishStockKey(dishId);
        redisTemplate.opsForValue().set(key, stock);
        log.info("设置菜品库存 dishId={}, stock={}", dishId, stock);
        return Result.success();
    }

    /**
     * 查询菜品库存（Redis）
     */
    @GetMapping("/stock/{id}")
    @ApiOperation("查询菜品库存")
    public Result<Integer> getStock(@PathVariable Long id) {
        String key = RedisLockUtil.getDishStockKey(id);
        Integer stock = (Integer) redisTemplate.opsForValue().get(key);
        return Result.success(stock);
    }
}
