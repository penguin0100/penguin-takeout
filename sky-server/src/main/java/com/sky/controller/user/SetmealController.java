package com.sky.controller.user;

import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端套餐管理
 */
@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Api(tags = "C 端 - 套餐接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据分类 id 查询套餐列表
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类 id 查询套餐列表")
    public Result<List<Setmeal>> list(Long categoryId) {
        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(categoryId);
        List<Setmeal> list = setmealService.list(setmeal);
        return Result.success(list);
    }
}
