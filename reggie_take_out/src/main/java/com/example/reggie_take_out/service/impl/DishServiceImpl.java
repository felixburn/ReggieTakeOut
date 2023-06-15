package com.example.reggie_take_out.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.reggie_take_out.dto.DishDto;
import com.example.reggie_take_out.entity.Dish;
import com.example.reggie_take_out.entity.DishFlavor;
import com.example.reggie_take_out.mapper.DishMapper;
import com.example.reggie_take_out.service.DishFlavorService;
import com.example.reggie_take_out.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDto
     */
    @Override
    //涉及到对多张表的数据进行操作,需要加事务，需要事务生效,需要在启动类加上事务注解生效
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        // DishDto extends Dish
        this.save(dishDto);
        //获取菜品Id
        Long dishId = dishDto.getId();
        //为了把dishId  set进flavors表中
        //拿到菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        //这里对集合进行赋值 可以使用循环或者是stream流
        //.stream() ——> 将集合转换为流
        //.map() ——> 将流中的元素计算或者转换
        //.collect() ——> 将流转换成其他形式；接收一个 Collector 接口的实现
        flavors = flavors.stream().map((item) ->{
            //拿到的这个item就是这个DishFlavor集合
            item.setDishId(dishId);
            return item; //记得把数据返回去
        }).collect(Collectors.toList()); //把返回的集合搜集起来,用来被接收

        //保存菜品口味的数据到口味表 dish_flavor
        //注意dish_flavor只是封装了name、value，并没有封装dishId
        // (从前端传过来的数据发现的,然而数据库又需要这个数据)
        //因此才有了我们上面获取菜品id设置进入flavors表中的操作
        dishFlavorService.saveBatch(dishDto.getFlavors()); //这个方法是批量保存
    }
}
