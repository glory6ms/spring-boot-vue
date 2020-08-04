package com.glory.springboot.control;

import com.glory.springboot.dao.mydao;
import com.glory.springboot.entities.DynamicEntity;
import com.glory.springboot.entities.StaticEntity;
import com.glory.springboot.entities.es_dynamic;
import com.glory.springboot.reposity.staticResp;
import com.google.gson.Gson;
import org.apache.lucene.document.DoublePoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

//操作类 RestController=Controller+ResponseBody,COntrol能返回视图解析器比如html，不能返回json,xml，需要加上ResponseBody
@RestController
public class mycontrol {

    //静态数据表操作工具类
    @Autowired
    private staticResp staticResp;
    @Autowired
    private com.glory.springboot.dao.mydao mydao;

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
    public Page<DynamicEntity> QueryByTime(@RequestBody Map<String,Object> params) {
//        String qtime = request.getParameter("date");
        String timein = (String) params.get("timein");
        String timeout = (String) params.get("timeout");
//        Object centerPoint = params.get("CenterPoint");
//        Object lineLength = params.get("LineLength");
        System.out.println(timein);
        Page<DynamicEntity> dynamicEntities = mydao.queryLntByTime(timein, timeout);

//        System.out.println(dynamicEntities.size());
        return dynamicEntities;
    }
//    @GetMapping("/queryByTime1")
//    public String QueryByTime(@RequestParam("timein") String timein,@RequestParam("timeout") String timeout,@RequestParam("start_point") Double start_point,@RequestParam("end_point") Double end_point){
//        System.out.println(timein);
//        System.out.println(start_point);
//        return "end";
//    }
    @PostMapping("/queryByTimeAndLocation")
    public Collection<es_dynamic> QueryByTimeAndLocation(@RequestBody Map<String,Object> params) {
        String timein = (String) params.get("timein");
        String timeout = (String) params.get("timeout");
        List centerPoint = (List) params.get("center_point");
        Double lineLength = Double.parseDouble(params.get("line_length").toString());
//        Double[] center_point = {120.853,31.95};
        Double[] cpoint = new Double[2];
        cpoint[0] = new Double(centerPoint.get(0).toString());
        cpoint[1] = new Double(centerPoint.get(1).toString());
//        System.out.println(lineLength);
        List<SearchHit<es_dynamic>> searchHits = mydao.QueryByTimeAndLocation(timein, timeout, cpoint[0], cpoint[1], lineLength);
        String startPoint = (String) params.get("start_point");//前端限制了这两个字段为字符串类型
        String[] start = startPoint.split(",");

        String endPoint = (String) params.get("end_point");
        String[] end = endPoint.split(",");

        if(start.length<1||end.length<1){
            return null;
        }else {
            List<es_dynamic> check = mydao.Check(searchHits, new BigDecimal(start[0]), new BigDecimal(start[1]), new BigDecimal(end[0]), new BigDecimal(end[1]));
            System.out.println("流量统计" + check.size());
            return check;//landCourse,landSpeed,time
        }
    }
}
