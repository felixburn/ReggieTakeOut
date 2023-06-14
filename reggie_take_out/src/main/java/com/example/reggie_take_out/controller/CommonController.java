package com.example.reggie_take_out.controller;

import com.example.reggie_take_out.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件的上传和下载
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     * @param file
     * @return
     */
    // http://localhost:8080/common/upload
    @PostMapping("/upload")
    // 注意这里的参数名要和前端的一致，或者用@RequestPart指定
    public R<String> upload(MultipartFile file){
        //file是一个临时文件，需要转存到指定位置，否则本次请求完成之后临时文件会被删除
        log.info(file.toString());
        //原始文件名
        String originalFilename = file.getOriginalFilename();
        //截取文件名的后缀(eg:happy.jpg 截取.jpg)
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //使用UUID随机生成文件名，防止因为文件名相同造成文件覆盖
        String fileName = UUID.randomUUID().toString()+suffix;

        //创建一个目录对象
        File dir = new File(basePath);
        //判断当前目录是否存在
        if(!dir.exists()){
            //目录不存在,需要创建
            dir.mkdirs();
        }

        try {
            //将临时文件转存到指定位置
            //file.transferTo(new File(E:\瑞吉外卖\images\hello.jpg));
            file.transferTo(new File(basePath+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName);
    }

    /**
     * 文件下载
     * @param name  name=${response.data}  upload()返回的
     * @param response  输出流需要response来获得
     */
    // this.imageUrl = `/common/download?name=${response.data}`
    // http://localhost:8080/common/download?name=2362354d-40df-4852-be3d-27cfd0eaa8ec.png
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
            //输入流，通过输入流读取文件内容
            //new File(basePath + name) 是为了从存储图片的地方获取用户需要的图片对象
            FileInputStream fileInputStream=new FileInputStream(new File(basePath+name));
            //输出流，通过输出流将文件写回浏览器，在浏览器中展示图片
            ServletOutputStream outputStream = response.getOutputStream();
            //设置写回去的文件类型(表示响应给页面的是一个图片内容,JPEG类型)
            response.setContentType("image/jpeg");
            //定义缓存区，准备读写文件
            int len= 0 ;
            byte[] bytes = new byte[1024];
            while ((len=fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }
            //关流
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
