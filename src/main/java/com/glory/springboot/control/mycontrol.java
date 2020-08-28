package com.glory.springboot.control;

import com.glory.springboot.entities.StaticEntity;
import com.glory.springboot.entities.es_dynamic;
import com.glory.springboot.reposity.staticResp;
import org.elasticsearch.common.geo.GeoPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

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
    public List<SearchHit<es_dynamic>> QueryByTime(@RequestBody Map<String,Object> params) {
//        String qtime = request.getParameter("date");
        String timein = (String) params.get("timein");
        String timeout = (String) params.get("timeout");
//        Object centerPoint = params.get("CenterPoint");
//        Object lineLength = params.get("LineLength");
        List<SearchHit<es_dynamic>> list = mydao.queryLocationByTime(timein, timeout);
        return list;
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
//        List centerPoint = (List) params.get("center_point");
//        Double lineLength = Double.parseDouble(params.get("line_length").toString());
//        Double[] cpoint = new Double[2];
//        cpoint[0] = new Double(centerPoint.get(0).toString());
//        cpoint[1] = new Double(centerPoint.get(1).toString());
//        System.out.println(lineLength);
        String startPoint = (String) params.get("start_point");//前端限制了这两个字段为字符串类型
        String[] start = startPoint.split(",");
        String endPoint = (String) params.get("end_point");
        String[] end = endPoint.split(",");
        Double top = Math.max(Double.parseDouble(start[1]),Double.parseDouble(end[1]));
        Double bottom = Math.min(Double.parseDouble(start[1]),Double.parseDouble(end[1]));
        Double right = Math.max(Double.parseDouble(start[0]),Double.parseDouble(end[0]));
        Double left = Math.min(Double.parseDouble(start[0]),Double.parseDouble(end[0]));
        List<SearchHit<es_dynamic>> searchHits = mydao.QueryByTimeAndLocation2(timein, timeout, top, left, bottom, right);
        if(start.length<1||end.length<1){
            return null;
        }else {
            List<es_dynamic> check = mydao.Check(searchHits, new BigDecimal(start[0]), new BigDecimal(start[1]), new BigDecimal(end[0]), new BigDecimal(end[1]));
            System.out.println("流量统计" + check.size());
            return check;//landCourse,landSpeed,time
        }
    }
    @PostMapping("/QueryByPolygonRange")
    public List<SearchHit<es_dynamic>> QueryByPolygonRange(@RequestBody Map<String,Object> params) {
        String timein = (String) params.get("timein");
        String timeout = (String) params.get("timeout");
        Object in = params.get("in"); // in可能为空
        Object out = params.get("out");
        ArrayList<List<Double>> buffer_in = (ArrayList<List<Double>>) in;
        ArrayList<List<Double>> buffer_out = (ArrayList<List<Double>>) out;
        List<GeoPoint> geoPoints_in = new ArrayList<>();
        List<GeoPoint> geoPoints_out = new ArrayList<>();
//        for (int i=0;i<buffer_out.size();i++){
//            geoPoints.add(new GeoPoint(buffer_test.get(i).get(1),buffer_test.get(i).get(0)));
////            System.out.println(buffer_test.get(i).get(0));
//        }
        for(List<Double> inPo : buffer_in){
            geoPoints_in.add(new GeoPoint(inPo.get(1),inPo.get(0)));
        }
        for (List<Double> outPo : buffer_out){
            geoPoints_out.add(new GeoPoint(outPo.get(1),outPo.get(0)));
        }
        List<SearchHit<es_dynamic>> searchHits = mydao.QueryByPolygonRange(timein, timeout, geoPoints_in, geoPoints_out);
        return searchHits;
    }

}
