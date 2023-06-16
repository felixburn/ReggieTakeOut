package com.example.reggie_take_out.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie_take_out.common.R;
import com.example.reggie_take_out.dto.DishDto;
import com.example.reggie_take_out.entity.Category;
import com.example.reggie_take_out.entity.Dish;
import com.example.reggie_take_out.service.CategoryService;
import com.example.reggie_take_out.service.DishFlavorService;
import com.example.reggie_take_out.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    // http://localhost:8080/dish
    @PostMapping()
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    // http://localhost:8080/dish/page?page=1&pageSize=10
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //构造分页构造器
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(!StringUtils.isEmpty(name), Dish::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //进行分页查询
        dishService.page(pageInfo, queryWrapper);
        //对象拷贝
        //Page<DishDto>没有对应的Mapper层，先用Page<Dish>查出数据
        //忽略records，即查询出来的记录lists(因为这里的records并不带有name而是id)
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        //拿出pageInfo中的records进行特定的处理(简单来说就是让其带上name)
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list=records.stream().map((item)->{
            DishDto dishDto=new DishDto();
            //先拿到records(不做这一步的话我们的dishDto中就只有name,我们是要添加不是覆盖)
            BeanUtils.copyProperties(item,dishDto);
            //拿到菜品分类id
            Long categoryId = item.getCategoryId();
            //根据id查分类对象
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                //根据分类对象拿到其name，并set进dishDto中
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        //将list作为records set进我们的dishDtoPage中
        dishDtoPage.setRecords(list);
        //注意这里不要返回pageInfo
        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    // 	http://localhost:8080/dish/1669297871807627265
    @GetMapping("/{id}")
    // 注意这里是DishDto不是Dish,因为页面的回显包含了菜品口味信息
    public R<DishDto> getById(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    // http://localhost:8080/dish
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }

    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
    //http://localhost:8080/dish/list?categoryId=1397844263642378242
    @GetMapping("/list")
    //这里不直接传id传dish是希望该list()更通用，能根据条件查询而非局限于根据id查询
    //public R<List<Dish>> list(Long categoryId)
    public R<List<Dish>> list(Dish dish){
        //构造查询条件
        LambdaQueryWrapper<Dish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        //添加条件，查询状态为1的（起售状态）
        lambdaQueryWrapper.eq(Dish::getStatus,1);
        lambdaQueryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        //条件排序条件
        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list=dishService.list(lambdaQueryWrapper);
        return R.success(list);
    }
}