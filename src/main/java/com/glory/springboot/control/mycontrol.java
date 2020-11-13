package com.glory.springboot.control;

import com.glory.springboot.entities.*;
import com.glory.springboot.reposity.staticResp;
import org.elasticsearch.common.geo.GeoPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    public Long getTime(String time,String placeId){
        String[] split = time.split("-");
        StringBuilder key = new StringBuilder(10);
        key.append(split[0], 2, 4).append(placeId).append(split[1]).append(split[2]).append("0");
        return Long.valueOf(key.toString());
    }

    /**
     *
     * @param params (开始时间，结束时间)
     * @return 时间区域内的部分动态信息
     */
    @PostMapping("/queryByTime")
    public List<SearchHit<redis_dynamic>> QueryByTime(@RequestBody HashMap<String,String> params) {
        String timein =  params.get("timein");
        String timeout =  params.get("timeout");
        String placeId =  params.get("placeId");
//        String[] split = timein.split("-");
//        String[] split1 = timeout.split("-");
        Long sTime = getTime(timein,placeId);
        Long eTime = getTime(timeout,placeId);
        Long tmp_end = 1L;
        Long tmp_start = 1L;
//      跨年的需要考虑
//        if(!split[0].equals(split1[0])){
//            // 需要改为StringBuilder
//            tmp_end = Long.valueOf(split[0].substring(2,4)+placeId+"12"+"31"+"0");
//            tmp_start = Long.valueOf(split1[0].substring(2,4)+placeId+"01"+"01"+"0");
//        }
        List<SearchHit<redis_dynamic>> list = mydao.queryLocationByTime(sTime, eTime,tmp_start,tmp_end);
        return list;
    }

    /**
     * 截面流量统计
     * @param params (开始时间，结束时间，起点，终点，中心点，截面半径长)
     * @method 1.查询时间范围内以中心点为半径的圆域内的轨迹点，结果按mmsi和时间两个字段升序排 2.一次循环，判断连续的轨迹线是否通过截面
     * @return 哈希表 ，速度分布，航行状态分布，ais类型分布，原始动态数据，总数
     */
    @PostMapping("/queryByTimeAndLocation")
    public Map<String, Object> QueryByTimeAndLocation(@RequestBody HashMap params) throws ParseException {
        String timein = (String) params.get("timein"); // 开始时间
        String timeout = (String) params.get("timeout"); // 结束时间
        String placeId = (String) params.get("placeId");
        Long sTime = getTime(timein,placeId);
        Long eTime = getTime(timeout,placeId);
        String startPoint = (String) params.get("start_point");//前端限制了这两个字段为字符串类型
        String[] start = startPoint.split(",");
        String endPoint = (String) params.get("end_point");
        String[] end = endPoint.split(",");
        Double lineLength = (Double) params.get("line_length");
//        String[] split = timein.split("-");
//        String[] split1 = timeout.split("-");
        Long tmp_end = 1L;
        Long tmp_start = 1L;
//        if(!split[0].equals(split1[0])){
//            tmp_end = Long.valueOf(split[0].substring(2,4)+placeId+"12"+"31"+"0");
//            tmp_start = Long.valueOf(split1[0].substring(2,4)+placeId+"01"+"01"+"0");
//        }
        ArrayList<Double> centerPoint = (ArrayList<Double>) params.get("center_point");
        List<SearchHit<redis_dynamic>> searchHits = mydao.QueryByTimeAndLocation(sTime, eTime,tmp_start,tmp_end, centerPoint.get(0), centerPoint.get(1), lineLength);
        System.out.println("轨迹数据"+searchHits.size());
        if(start.length<1||end.length<1){
            return null;
        }else {
            Map<String, Object> passLine = mydao.isPassLine(searchHits, Double.parseDouble(start[0]), Double.parseDouble(start[1]), Double.parseDouble(end[0]), Double.parseDouble(end[1]));
            return passLine;
        }
    }

    /**
     * @param params {timein:开始时间,timeout:结束时间,in:缓冲区内环,out:缓冲区外环，line:绘制的区域边界}
     *               method:1.es时间段过滤and geoPolygonQuery落在缓冲区外环内的点 not 落在缓冲区内环的点+sort(mmsi,time)，得到所有落在缓冲区的点
     *               2.对所有点循环,判断是否在给定区域内，如果同一mmsi下状态不同，即为进出了一次区域
     * @return 船舶进出区域的数量，时间
     */
    @PostMapping("/QueryByPolygonRange")
    public Map<String, Object> QueryByPolygonRange(@RequestBody HashMap params) {
        String timein = (String) params.get("timein");
        String timeout = (String) params.get("timeout");
        Object in = params.get("in"); // in可能为空
        Object out = params.get("out");
        Object lines = params.get("line");
        String placeId = (String) params.get("placeId");
        String[] split = timein.split("-");
        String[] split1 = timeout.split("-");
        Long sTime = Long.valueOf(split[0].substring(2,4)+placeId+split[1]+split[2]+"0");
        Long eTime = Long.valueOf(split1[0].substring(2,4)+placeId+split1[1]+split1[2]+"0");
        Long tmp_end = 1L;
        Long tmp_start = 1L;
        if(!split[0].equals(split1[0])){
            tmp_end = Long.valueOf(split[0].substring(2,4)+placeId+"12"+"31");
            tmp_start = Long.valueOf(split1[0].substring(2,4)+placeId+"01"+"01");
        }

        ArrayList<List<Double>> buffer_in = (ArrayList<List<Double>>) in;
        ArrayList<List<Double>> buffer_out = (ArrayList<List<Double>>) out;
        ArrayList<List<Double>> edge = (ArrayList<List<Double>>) lines;
//        System.out.println(edge.get(1).get(0));
        List<GeoPoint> geoPoints_in = new ArrayList<>();
        List<GeoPoint> geoPoints_out = new ArrayList<>();
        if (buffer_in!=null) {
            for (List<Double> inPo : buffer_in) {
                geoPoints_in.add(new GeoPoint(inPo.get(1), inPo.get(0)));
            }
        }else {
            geoPoints_in=null;
        }

        for (List<Double> outPo : buffer_out) {
            geoPoints_out.add(new GeoPoint(outPo.get(1), outPo.get(0)));
        }
//查询落在缓冲区内的点
        List<SearchHit<redis_dynamic>> searchHits = mydao.QueryByPolygonRange(sTime, eTime,tmp_start,tmp_end,geoPoints_in, geoPoints_out);
        Map<String, Object> map1 = mydao.CheckInPolygon(searchHits, edge);
//        System.out.println(es_dynamics.size());
        Map<String, Object> map = new HashMap<>();
        map.put("pois", searchHits);
        map.put("result", map1);
        return map;
    }

    /**
     * 根据mmsi查询轨迹信息
     */
    @PostMapping("/queryByMMSI")
    public List<SearchHit<es_dynamic>> QueryByMMSI(@RequestBody Map<String,Object> params) {
        String mmsi = (String) params.get("MMSI");
        String[] strings = mmsi.split(",");
        int[] col = new int[strings.length];
        for (int i = 0 ;i<strings.length;i++){
            int t = Integer.parseInt(strings[i]);
            col[i] = t;
        }
        List<SearchHit<es_dynamic>> list = mydao.queryTrajectoryByMMSI(col);
        return list;
    }

    /**
     * 根据mmsi查询轨迹信息
     */
    @PostMapping("/QueryPlaceById")
    public List<SearchHit<place>> QueryPlaceById(@RequestBody Map<String,Object> params) {
        String placeId = (String) params.get("placeId");
        List<SearchHit<place>> searchHits = mydao.queryPlace(placeId);
        return searchHits;
    }
}
