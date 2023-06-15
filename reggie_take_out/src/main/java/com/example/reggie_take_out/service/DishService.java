package com.example.reggie_take_out.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.reggie_take_out.dto.DishDto;
import com.example.reggie_take_out.entity.Dish;

public interface DishService extends IService<Dish> {

    //新增菜品,同时插入菜品对应的口味数据,需要同时操作两张表:dish  dish_flavor
    void saveWithFlavor(DishDto dishDto);
}