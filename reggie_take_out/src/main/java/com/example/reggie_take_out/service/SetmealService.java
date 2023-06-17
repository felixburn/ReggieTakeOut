package com.example.reggie_take_out.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.reggie_take_out.dto.SetmealDto;
import com.example.reggie_take_out.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    //新增套餐，同时需要保存套餐和菜品的关联关系
    void saveWithDish(SetmealDto setmealDto);

    //删除套餐，同时删除套餐与菜品的关联数据
    void removeWithDish(List<Long> ids);

    //根据id更改套餐的售卖status(起售/停售)
    void updateSetmealStatusById(Integer status, List<Long> ids);
}
