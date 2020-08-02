package com.glory.springboot.control;

import com.glory.springboot.entities.DynamicEntity;
import com.glory.springboot.entities.StaticEntity;
import com.glory.springboot.reposity.staticResp;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;

//操作类 RestController=Controller+ResponseBody,COntrol能返回视图解析器比如html，不能返回json,xml，需要加上ResponseBody
@RestController
public class mycontrol {

    //静态数据表操作工具类
    @Autowired
    private staticResp staticResp;

    /*
    axios调用get和post方法，其中get可以不带参数，或者在路径中带，使用@RequstParm获取
    post可传入String或者json，会话域内传参，使用@RequestBody获取
     */
    //get请求到页面上返回json数据
    @GetMapping("/table")
    public List<StaticEntity> findAll(){
        System.out.println(staticResp.findAll());
        return staticResp.findAll();
    }

    //根据时间段查询动态数据 post方法
    @PostMapping("/queryByTime")
    public String QueryByTime(@RequestBody Map<String,Object> params) {
//        String qtime = request.getParameter("date");
        Object timein = params.get("timein");
        Object timeout = params.get("timeout");
        Object centerPoint = params.get("CenterPoint");
        Object lineLength = params.get("LineLength");

        System.out.println(timein);
        System.out.println(params);
        return "success";
    }
//    @GetMapping("/queryByTime1")
//    public String QueryByTime(@RequestParam("timein") String timein,@RequestParam("timeout") String timeout,@RequestParam("start_point") Double start_point,@RequestParam("end_point") Double end_point){
//        System.out.println(timein);
//        System.out.println(start_point);
//        return "end";
//    }
}
