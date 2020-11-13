package com.glory.springboot.dao;

import com.glory.springboot.entities.es_dynamic;
import com.glory.springboot.entities.mmsi;
import com.glory.springboot.entities.place;
import com.glory.springboot.entities.redis_dynamic;
import com.glory.springboot.reposity.dynamicResp;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Repository
public class mydao {
    @Autowired
    dynamicResp dynamicResp;
    @Autowired
    ElasticsearchOperations operations;
    @Autowired
    ElasticsearchRestTemplate template;

    /**
     * 2-1 es
     *
     * @param timein
     * @param timeout
     * @return List<SearchHit < es_dynamic>> 未使用评分机制，只取location字段，SearchHit<es_dynamic>.content.location
     */
    public List<SearchHit<redis_dynamic>> queryLocationByTime(Long timein, Long timeout, Long tmp_start, Long tmp_end) {
        PageRequest pageRequest = PageRequest.of(0, 50);
        String[] include = {"trajectory.location"}; // 输出参数过滤
        FetchSourceFilter fetchSourceFilter = new FetchSourceFilter(include, null);
        BoolQueryBuilder boolQueryBuilder;
        if (tmp_start != tmp_end) {
            boolQueryBuilder = QueryBuilders.boolQuery()
                    .filter(boolQuery().must(rangeQuery("did").gte(timein).lte(timeout)).must(rangeQuery("did").gte(tmp_start).lte(tmp_end)));
        } else {
            boolQueryBuilder = QueryBuilders.boolQuery()
                    .filter(rangeQuery("did").gte(timein).lte(timeout));
        }
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                .withPageable(pageRequest)
                .withSourceFilter(fetchSourceFilter);
        IndexCoordinates index = IndexCoordinates.of("trajectory");
        SearchHits<redis_dynamic> search = operations.search(nativeSearchQueryBuilder.build(), redis_dynamic.class, index);
        List<SearchHit<redis_dynamic>> searchHits = search.getSearchHits();
        return searchHits;
//        SearchScrollHits<redis_dynamic> scroll = template.searchScrollStart(100, nativeSearchQueryBuilder.build(), redis_dynamic.class, index);
//        String scrollId = scroll.getScrollId();
//        List<String> id = new ArrayList<>();
//        id.add(scrollId);
//        List<SearchHit<redis_dynamic>> list = new ArrayList<>();
//        int count = 0;
//        while (scroll.hasSearchHits()&&count<10) {
//            list.addAll(scroll.getSearchHits());
//            scrollId = scroll.getScrollId();
//            scroll = template.searchScrollContinue(scrollId, 100, redis_dynamic.class, index);
//            id.add(scrollId);
//            count++;
//        }
//        template.searchScrollClear(id);
//        return list;
    }

    /**
     *
     */
    public List<SearchHit<es_dynamic>> queryTrajectoryByMMSI(int[] MMSI) {
        PageRequest pageRequest = PageRequest.of(0, 10000);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(termsQuery("mmsi", MMSI));
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                .withPageable(pageRequest)
                .withSort(SortBuilders.fieldSort("time").order(SortOrder.ASC));
        IndexCoordinates index = IndexCoordinates.of("trajectory");
        SearchHits<es_dynamic> search = operations.search(nativeSearchQueryBuilder.build(), es_dynamic.class, index);
        List<SearchHit<es_dynamic>> searchHits = search.getSearchHits();
        System.out.println(nativeSearchQueryBuilder.build().getQuery().toString());
        System.out.println("点个数:" + search.getTotalHits());
        return searchHits;
    }

    //根据id查询区域范围
    public List<SearchHit<place>> queryPlace(String placeId) {
//        String[] include = {"lon3","lat3"}; // 输出参数过滤
//        FetchSourceFilter fetchSourceFilter = new FetchSourceFilter(include, null);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(termQuery("placeId", Integer.parseInt(placeId)));
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder);
//                .withSourceFilter(fetchSourceFilter)
//                .withPageable(pageRequest);
        IndexCoordinates index = IndexCoordinates.of("place");
        SearchHits<place> search = operations.search(nativeSearchQueryBuilder.build(), place.class, index);
        List<SearchHit<place>> searchHits = search.getSearchHits();
        System.out.println(nativeSearchQueryBuilder.build().getQuery().toString());
        return searchHits;
    }

    //根据时间和区域查询经过该区域的mmsi
    public List<SearchHit<mmsi>> queryMmsi(List<String> mmsiKey) {
        String[] include = {"MMSI"}; // 输出参数过滤
        FetchSourceFilter fetchSourceFilter = new FetchSourceFilter(include, null);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(termsQuery("did", mmsiKey));
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                .withSourceFilter(fetchSourceFilter);
//                .withPageable(pageRequest);
        IndexCoordinates index = IndexCoordinates.of("mmsi");
        SearchHits<mmsi> search = operations.search(nativeSearchQueryBuilder.build(), mmsi.class, index);
        List<SearchHit<mmsi>> searchHits = search.getSearchHits();
        System.out.println(nativeSearchQueryBuilder.build().getQuery().toString());
        return searchHits;
    }
//    public List<SearchHit<es_dynamic>> queryMMSIBytimeAndLocation(String timein,String timeout,String placeId){
//        List<String> did = new ArrayList<>();
//        String[] split = timein.split("-");
//        String[] split1 = timeout.split("-");
//        Calendar c1 = Calendar.getInstance();
//        Calendar c2 = Calendar.getInstance();
//        c1.set(Integer.parseInt(split[0]),Integer.parseInt(split[1]),Integer.parseInt(split[2]));
//        c2.set(Integer.parseInt(split1[0]),Integer.parseInt(split1[1]),Integer.parseInt(split1[2]));
//        for(int day = 0;!c1.equals(c2);day++){
//            c1.add(Calendar.DATE,day);
//            int hour = c1.get(Calendar.HOUR_OF_DAY);
//            hour = hour/3+1; //小时以三小时为一段，分为1，2，3，4。。8段
//            String tmpId = String.valueOf(c1.get(Calendar.YEAR)).concat(String.valueOf(c1.get(Calendar.MONTH)+1)).concat(String.valueOf(c1.get(Calendar.DATE))).concat(String.valueOf(hour)).concat(placeId);
//            did.add(tmpId);
//        }
//        String[] include = {"MMSI"}; // 输出参数过滤
//        FetchSourceFilter fetchSourceFilter = new FetchSourceFilter(include,null);
//        PageRequest pageRequest = PageRequest.of(0,10000);
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
//                .filter(termsQuery("did",did));
//        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
//                .withSourceFilter(fetchSourceFilter)
//                .withPageable(pageRequest);
//        IndexCoordinates index = IndexCoordinates.of("mmsi");
//        SearchHits<mmsi> search = operations.search(nativeSearchQueryBuilder.build(), mmsi.class, index);
//        List<SearchHit<mmsi>> searchHits_mmsi = search.getSearchHits();
//
//        BoolQueryBuilder boolQueryBuilder1 = QueryBuilders.boolQuery()
//                .filter(termsQuery("did",searchHits_mmsi.));
//        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
//                .withPageable(pageRequest)
//                .withSort(SortBuilders.fieldSort("time").order(SortOrder.ASC));
//        IndexCoordinates index = IndexCoordinates.of("trajectory");
//        SearchHits<es_dynamic> search = operations.search(nativeSearchQueryBuilder.build(), es_dynamic.class, index);
//        List<SearchHit<es_dynamic>> searchHits = search.getSearchHits();
//
//
//        return searchHits;
//    }

    public List<SearchHit<redis_dynamic>> scroll( NativeSearchQueryBuilder nativeSearchQueryBuilder){
        IndexCoordinates index = IndexCoordinates.of("trajectory");
        SearchScrollHits<redis_dynamic> scroll = template.searchScrollStart(500, nativeSearchQueryBuilder.build(), redis_dynamic.class, index);
        String scrollId = scroll.getScrollId();
        List<String> id = new ArrayList<>();
        id.add(scrollId);
        List<SearchHit<redis_dynamic>> list = new ArrayList<>();
        while (scroll.hasSearchHits()) {
            list.addAll(scroll.getSearchHits());
            scrollId = scroll.getScrollId();
            scroll = template.searchScrollContinue(scrollId, 1050, redis_dynamic.class, index);
            id.add(scrollId);
        }
        template.searchScrollClear(id);
        return list;
    }

    /**
     * @param timein：开始时间
     * @param timeout：结束时间
     * @param lng：中心点经度
     * @param lat：中心点纬度
     * @param lineLength：截面长度
     * @return 不包含得到和其他信息的文档集合Long timein, Long timeout,Long tmp_start,Long tmp_end, Double lng,Double lat, Double lineLength
     * @method 游标滚动，查询全部结果集
     */
    public List<SearchHit<redis_dynamic>> QueryByTimeAndLocation(Long timein, Long timeout, Long tmp_start, Long tmp_end, Double lng, Double lat, Double lineLength) {
        String[] include = {"mmsi", "trajectory"}; // 输出参数过滤
        FetchSourceFilter fetchSourceFilter = new FetchSourceFilter(include, null);
//        PageRequest pageRequest = PageRequest.of(0, 10000);
        //1-1
        BoolQueryBuilder boolQueryBuilder;
        Collection<QueryBuilder> queryBuilders = null;
        if (tmp_start != tmp_end) {
            queryBuilders.add(rangeQuery("did").gte(timein).lte(tmp_end));
            queryBuilders.add(rangeQuery("did").gte(tmp_start).lte(timeout));
            queryBuilders.add(nestedQuery("trajectory", geoDistanceQuery("trajectory.location").distance(lineLength, DistanceUnit.KILOMETERS).point(lat, lng).geoDistance(GeoDistance.PLANE), ScoreMode.None));
            boolQueryBuilder = QueryBuilders.boolQuery()
                    .filter(boolQuery().must(rangeQuery("did").gte(timein).lte(tmp_end)).should(rangeQuery("did").gte(tmp_start).lte(timeout)).must(nestedQuery("trajectory", geoDistanceQuery("trajectory.location").distance(lineLength, DistanceUnit.KILOMETERS).point(lat, lng).geoDistance(GeoDistance.PLANE), ScoreMode.None)));
        } else {
            boolQueryBuilder = QueryBuilders.boolQuery()
                    .filter(boolQuery().must(rangeQuery("did").gte(timein).lte(timeout)).filter(nestedQuery("trajectory", geoDistanceQuery("trajectory.location").distance(lineLength, DistanceUnit.KILOMETERS).point(lat, lng).geoDistance(GeoDistance.PLANE), ScoreMode.None)));
        }
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                .withSourceFilter(fetchSourceFilter);
//                .withPageable(pageRequest);

        System.out.println(nativeSearchQueryBuilder.build().getQuery().toString());

        return scroll(nativeSearchQueryBuilder);
        //1-2
//        SearchScrollHits<redis_dynamic> scroll = template.searchScrollStart(500, nativeSearchQueryBuilder.build(), redis_dynamic.class, index);
//        String scrollId = scroll.getScrollId();
//        List<String> id = new ArrayList<>();
//        id.add(scrollId);
//        List<SearchHit<redis_dynamic>> list = new ArrayList<>();
//        while (scroll.hasSearchHits()) {
//            list.addAll(scroll.getSearchHits());
//            scrollId = scroll.getScrollId();
//            scroll = template.searchScrollContinue(scrollId, 1050, redis_dynamic.class, index);
//            id.add(scrollId);
//        }
//        template.searchScrollClear(id);
//        return list;
    }

    /**
     * @param timein  开始时间
     * @param timeout 结束时间
     * @param in      缓冲区内圈
     * @param out     缓冲区外圈
     * @return 落在缓冲区内的轨迹点
     * @method 如果内圈不存在，query落在外圈内的轨迹点 and rangeQuery("time") 如果内圈存在 query 落在外圈内的轨迹点 and rangeQuery("time") not in 内圈
     */
    public List<SearchHit<redis_dynamic>> QueryByPolygonRange(Long timein, Long timeout, Long tmp_start, Long tmp_end, List<GeoPoint> in, List<GeoPoint> out) {
        String[] include = {"mmsi", "trajectory"}; // 输出参数过滤
        FetchSourceFilter fetchSourceFilter = new FetchSourceFilter(include, null);
//        PageRequest pageRequest = PageRequest.of(0, 10000);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (tmp_start != tmp_end) {
            boolQueryBuilder.filter(boolQuery().must(rangeQuery("did").gte(timein).lte(timeout)).should(rangeQuery("did").gte(tmp_start).lte(tmp_end)));
        } else {
            boolQueryBuilder.filter(rangeQuery("did").gte(timein).lte(timeout));
        }
        if (in == null) {
            boolQueryBuilder.filter(nestedQuery("trajectory", geoPolygonQuery("trajectory.location", out), ScoreMode.None));
        } else {
            boolQueryBuilder.must(nestedQuery("trajectory", geoPolygonQuery("trajectory.location", out), ScoreMode.None))
                    .mustNot(nestedQuery("trajectory", geoPolygonQuery("trajectory.location", in), ScoreMode.None));
        }
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withSourceFilter(fetchSourceFilter);
//                .withPageable(pageRequest);
        System.out.println(nativeSearchQueryBuilder.build().getQuery().toString());
//        IndexCoordinates index = IndexCoordinates.of("trajectory");
//        SearchScrollHits<redis_dynamic> scroll = template.searchScrollStart(500, nativeSearchQueryBuilder.build(), redis_dynamic.class, index);
//        String scrollId = scroll.getScrollId();
//        List<String> id = new ArrayList<>();
//        id.add(scrollId);
//        List<SearchHit<redis_dynamic>> list = new ArrayList<>();
//        while (scroll.hasSearchHits()) {
//            list.addAll(scroll.getSearchHits());
//            scrollId = scroll.getScrollId();
//            scroll = template.searchScrollContinue(scrollId, 500, redis_dynamic.class, index);
//            id.add(scrollId);
//        }
//        template.searchScrollClear(id);
//        System.out.println(list.size());
        return scroll(nativeSearchQueryBuilder);
    }

    /**
     * @param hits    落在缓冲区内的点集合
     * @param polygon 活跃度统计绘制的区域
     * @return
     * @method 一次循环，判断同一个mmsi下的船舶轨迹是否在区域内的状态，如果上个轨迹点在区域内，下一个不在即认定出了区域
     */
    public Map<String, Object> CheckInPolygon(List<SearchHit<redis_dynamic>> hits, ArrayList<List<Double>> polygon) {
        List<es_dynamic> result = new ArrayList<>();

        int[] sp_count = {0, 0, 0, 0, 0};
        for (int i = 0; i < hits.size(); i++) {
            List<es_dynamic> trajectory = hits.get(i).getContent().trajectory;
            List<Boolean> states = new ArrayList<>();
            for (int j = 0; j < trajectory.size(); j++) {
                es_dynamic dynamic = trajectory.get(j);
                states.add(isPoiWithinPoly(dynamic, polygon));
                if (j != 0) {

                    if (states.get(j) != states.get(j - 1)) {
                        result.add(dynamic);
                        if (dynamic.landSpeed < 4) {
                            sp_count[0]++;
                        } else if (dynamic.landSpeed < 7) {
                            sp_count[1]++;
                        } else if (dynamic.landSpeed < 10) {
                            sp_count[2]++;
                        } else if (dynamic.landSpeed < 13) {
                            sp_count[3]++;
                        } else {
                            sp_count[4]++;
                        }
                    }

                }
            }
        }
//        for (int i = 0; i < hits.size(); i++) {
//            es_dynamic dynamic = hits.get(i).getContent();
//            states.add(isPoiWithinPoly(dynamic, polygon));
//            if (i != 0) {
//                if (dynamic.mmsi == hits.get(i - 1).getContent().mmsi) {
//                    if (states.get(i) != states.get(i - 1)) {
//                        result.add(dynamic);
//                        if (dynamic.landSpeed.doubleValue()<4){
//                            sp_count[0]++;
//                        }else if (dynamic.landSpeed.doubleValue()<7){
//                            sp_count[1]++;
//                        }else if (dynamic.landSpeed.doubleValue()<10){
//                            sp_count[2]++;
//                        }else if (dynamic.landSpeed.doubleValue()<13){
//                            sp_count[3]++;
//                        }else {
//                            sp_count[4]++;
//                        }
//                    }
//                }
//            }
//        }
        Map<String, Object> map = new HashMap<>();
        map.put("result", result);
        map.put("sp_count", sp_count);
        return map;
    }

    /**
     * @param dynamic 轨迹点
     * @param poly    多边形区域
     * @return 对轨迹点做水平线，如果与多边形边界有奇数个交点，判断它落在多边形内，否则不在
     */
    public Boolean isPoiWithinPoly(es_dynamic dynamic, ArrayList<List<Double>> poly) {
        int sissc = 0;
        for (int i = 0; i < poly.size() - 1; i++) {
            Double[] poi = {dynamic.location[0], dynamic.location[1]};
            if (isRayIntersectsSegment(poi, poly.get(i), poly.get(i + 1))) {
                sissc++;
            }
        }
        return sissc % 2 != 0;
    }

    //快速排除几种情况再计算
    public Boolean isRayIntersectsSegment(Double[] poi, List<Double> s_poi, List<Double> e_poi) {
        if (s_poi.get(1) == e_poi.get(1)) {
            return false;
        }
        if (s_poi.get(1) > poi[1] && e_poi.get(1) > poi[1]) {
            return false;
        }
        if (s_poi.get(1) < poi[1] && e_poi.get(1) < poi[1]) {
            return false;
        }
        if (s_poi.get(1) == poi[1] && e_poi.get(1) > poi[1]) {
            return false;
        }
        if (e_poi.get(1) == poi[1] && s_poi.get(1) > poi[1]) {
            return false;
        }
        if (s_poi.get(0) < poi[0] && e_poi.get(0) < poi[1]) {
            return false;
        }
        Double xseg = e_poi.get(0) - (e_poi.get(0) - s_poi.get(0)) * (e_poi.get(1) - poi[1]) / (e_poi.get(1) - s_poi.get(1));
        if (xseg < poi[0]) {
            return false;
        }
        return true;
    }

    /**
     * @param searchHits 轨迹点结果集
     * @param lng1       截面信息
     * @param lat1
     * @param lng2
     * @param lat2
     * @return 统计结果及轨迹点
     * @method
     */
    public HashMap<String, Object> isPassLine(List<SearchHit<redis_dynamic>> searchHits, double lng1, double lat1, double lng2, double lat2) {
        int j;
        int step = 2;
        Collection<es_dynamic> collection = new ArrayList<>();

        int[] sp_count = new int[5];
        Arrays.fill(sp_count,0);
        int[] state_count = new int[3];
        Arrays.fill(state_count,0);
        int[] aType_count = new int[2];
        Arrays.fill(aType_count,0);
        HashMap<String, Object> map = new HashMap<>();
        for (SearchHit<redis_dynamic> hit:searchHits){
            List<es_dynamic> trajectory = hit.getContent().trajectory;
            if (trajectory.size() == 1){
                es_dynamic dynamicStart = trajectory.get(0);
                //判断该船将来是否会通过截面
                Double lc1 = Math.toRadians(dynamicStart.landCourse); // 化为弧度
                double x_sp = dynamicStart.landSpeed * Math.sin(lc1);
                double y_sp = dynamicStart.landSpeed * Math.cos(lc1);
                double lng_new  = x_sp * 0.08 / 60 + dynamicStart.location[0];
                double lat_new = y_sp * 0.08 / 60 + dynamicStart.location[1];
                if (intersection(lng1, lat1, lng2, lat2, dynamicStart.location[0], dynamicStart.location[1], lng_new, lat_new)){
                    collection.add(dynamicStart);
                    if (dynamicStart.landSpeed < 4) {
                        sp_count[0]++;
                    } else if (dynamicStart.landSpeed < 7) {
                        sp_count[1]++;
                    } else if (dynamicStart.landSpeed < 10) {
                        sp_count[2]++;
                    } else if (dynamicStart.landSpeed < 13) {
                        sp_count[3]++;
                    } else {
                        sp_count[4]++;
                    }
                    if (dynamicStart.shipState.equals("机动航行")) {
                        state_count[0]++;
                    } else if (dynamicStart.shipState.contains("泊")) {
                        state_count[1]++;
                    } else if (dynamicStart.shipState.contains("限")) {
                        state_count[2]++;
                    }
                    if (dynamicStart.aisType.equals("Class A")) {
                        aType_count[0]++;
                    } else aType_count[1]++;
                }

            }else {
                for (j = 0; j < trajectory.size(); ) {
                    es_dynamic dynamicStart = trajectory.get(j);
                    Double lengthStart = getLength(lng1, lat1, lng2, lat2, dynamicStart.location[0], dynamicStart.location[1]);
                    j = j + step;
                    if (j >= trajectory.size()) {
                        break;
                    }
                    es_dynamic dynamicEnd = trajectory.get(j);
                    Double lengthEnd = getLength(lng1, lat1, lng2, lat2, dynamicEnd.location[0], dynamicEnd.location[1]);
                    Double p1 = (lat1 - lat2) * dynamicStart.location[0] + (lng2 - lng1) * dynamicStart.location[1] + lng1 * lat2 - lng2 * lat1;
                    Double p2 = (lat1 - lat2) * dynamicEnd.location[0] + (lng2 - lng1) * dynamicEnd.location[1] + lng1 * lat2 - lng2 * lat1;
                    if (p1 * p2 > 0) {
                        //在同侧.
                        if (lengthEnd < lengthStart) {
                            step = 2;
                        } else {
                            step = step * 2;
                        }
                    } else {
                        //不在同侧，连线判断是否过截面
                        if (!intersection(lng1, lat1, lng2, lat2, dynamicStart.location[0], dynamicStart.location[1], dynamicEnd.location[0], dynamicEnd.location[1])) {
//                            System.out.println("不相交");
                            step = step * 2;
                        } else {
//                            System.out.println("相交");
                            collection.add(dynamicStart);
                            if (dynamicStart.landSpeed < 4) {
                                sp_count[0]++;
                            } else if (dynamicStart.landSpeed < 7) {
                                sp_count[1]++;
                            } else if (dynamicStart.landSpeed < 10) {
                                sp_count[2]++;
                            } else if (dynamicStart.landSpeed < 13) {
                                sp_count[3]++;
                            } else {
                                sp_count[4]++;
                            }
                            if (dynamicStart.shipState.equals("机动航行")) {
                                state_count[0]++;
                            } else if (dynamicStart.shipState.contains("泊")) {
                                state_count[1]++;
                            } else if (dynamicStart.shipState.contains("限")) {
                                state_count[2]++;
                            }
                            if (dynamicStart.aisType.equals("Class A")) {
                                aType_count[0]++;
                            } else aType_count[1]++;
                            map.put("sp", sp_count);
                            map.put("state", state_count);
                            map.put("aisType", aType_count);
                        }
                    }
                }
            }
        }
//        for (int i = 0; i < searchHits.size(); ++i) {
//            List<es_dynamic> trajectory = searchHits.get(i).getContent().trajectory;
//            if (trajectory.size() == 1){
//                es_dynamic dynamicStart = trajectory.get(0);
//                //判断该船将来是否会通过截面
//                Double lc1 = Math.toRadians(dynamicStart.landCourse.doubleValue()); // 化为弧度
//                if (lc1 > 0){
//                    BigDecimal x_sp = dynamicStart.landSpeed.multiply(BigDecimal.valueOf(Math.sin(lc1))).setScale(5, RoundingMode.HALF_UP);
//                    BigDecimal y_sp = dynamicStart.landSpeed.multiply(BigDecimal.valueOf(Math.cos(lc1))).setScale(5, RoundingMode.HALF_UP);
//                    BigDecimal lng_new = x_sp.multiply(new BigDecimal(0.08)).divide(new BigDecimal(60),6, RoundingMode.HALF_UP).add(dynamicStart.location[0]);
//                    BigDecimal lat_new = y_sp.multiply(new BigDecimal(0.08)).divide(new BigDecimal(60),6, RoundingMode.HALF_UP).add(dynamicStart.location[1]);
//                    if (!intersection(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), dynamicStart.location[0].doubleValue(), dynamicStart.location[1].doubleValue(), lng_new.doubleValue(), lat_new.doubleValue())) {
////                            System.out.println("不相交");
//                    } else {
////                            System.out.println("相交");
//                        collection.add(dynamicStart);
//                        if (dynamicStart.landSpeed.doubleValue() < 4) {
//                            sp_count[0]++;
//                        } else if (dynamicStart.landSpeed.doubleValue() < 7) {
//                            sp_count[1]++;
//                        } else if (dynamicStart.landSpeed.doubleValue() < 10) {
//                            sp_count[2]++;
//                        } else if (dynamicStart.landSpeed.doubleValue() < 13) {
//                            sp_count[3]++;
//                        } else {
//                            sp_count[4]++;
//                        }
//                        if (dynamicStart.shipState.equals("机动航行")) {
//                            state_count[0]++;
//                        } else if (dynamicStart.shipState.contains("泊")) {
//                            state_count[1]++;
//                        } else if (dynamicStart.shipState.contains("限")) {
//                            state_count[2]++;
//                        }
//                        if (dynamicStart.aisType.equals("Class A")) {
//                            aType_count[0]++;
//                        } else aType_count[1]++;
//                    }
//                }
//            }
//            for (j = 0; j < trajectory.size(); ) {
//                es_dynamic dynamicStart = trajectory.get(j);
//                Double lengthStart = getLength(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), dynamicStart.location[0].doubleValue(), dynamicStart.location[1].doubleValue());
//                j = j + step;
//                if (j >= trajectory.size()) {
//                    break;
//                }
//                es_dynamic dynamicEnd = trajectory.get(j);
//                Double lengthEnd = getLength(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), dynamicEnd.location[0].doubleValue(), dynamicEnd.location[1].doubleValue());
//                Double p1 = (lat1.doubleValue() - lat2.doubleValue()) * dynamicStart.location[0].doubleValue() + (lng2.doubleValue() - lng1.doubleValue()) * dynamicStart.location[1].doubleValue() + lng1.doubleValue() * lat2.doubleValue() - lng2.doubleValue() * lat1.doubleValue();
//                Double p2 = (lat1.doubleValue() - lat2.doubleValue()) * dynamicEnd.location[0].doubleValue() + (lng2.doubleValue() - lng1.doubleValue()) * dynamicEnd.location[1].doubleValue() + lng1.doubleValue() * lat2.doubleValue() - lng2.doubleValue() * lat1.doubleValue();
//                if (p1 * p2 > 0) {
//                    //在同侧.
//                    if (lengthEnd < lengthStart) {
//                        step = 2;
//                    } else {
//                        step = step * 2;
//                    }
//                } else {
//                    //不在同侧，连线判断是否过截面
//                    if (!intersection(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), dynamicStart.location[0].doubleValue(), dynamicStart.location[1].doubleValue(), dynamicEnd.location[0].doubleValue(), dynamicEnd.location[1].doubleValue())) {
////                            System.out.println("不相交");
//                        step = step * 2;
//                    } else {
////                            System.out.println("相交");
//                        collection.add(dynamicStart);
//                        if (dynamicStart.landSpeed.doubleValue() < 4) {
//                            sp_count[0]++;
//                        } else if (dynamicStart.landSpeed.doubleValue() < 7) {
//                            sp_count[1]++;
//                        } else if (dynamicStart.landSpeed.doubleValue() < 10) {
//                            sp_count[2]++;
//                        } else if (dynamicStart.landSpeed.doubleValue() < 13) {
//                            sp_count[3]++;
//                        } else {
//                            sp_count[4]++;
//                        }
//                        if (dynamicStart.shipState.equals("机动航行")) {
//                            state_count[0]++;
//                        } else if (dynamicStart.shipState.contains("泊")) {
//                            state_count[1]++;
//                        } else if (dynamicStart.shipState.contains("限")) {
//                            state_count[2]++;
//                        }
//                        if (dynamicStart.aisType.equals("Class A")) {
//                            aType_count[0]++;
//                        } else aType_count[1]++;
//                        map.put("sp", sp_count);
//                        map.put("state", state_count);
//                        map.put("aisType", aType_count);
//                    }
//                }
//            }
//        }

        //


//        for(int i=0;i<searchHits.size();){
//            es_dynamic dynamicStart = searchHits.get(i).getContent();
//            Double lengthStart = getLength(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), dynamicStart.location[0].doubleValue(), dynamicStart.location[1].doubleValue());
//            j=i+step;
//            if (j>=searchHits.size()){
//                break;
//            }
//            es_dynamic dynamicEnd = searchHits.get(j).getContent();
//            Double lengthEnd = getLength(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), dynamicEnd.location[0].doubleValue(), dynamicEnd.location[1].doubleValue());
//            if (dynamicStart.mmsi == dynamicEnd.mmsi){
//                // 判断两个点是否中截面同侧
//                Double p1 = (lat1.doubleValue() - lat2.doubleValue()) * dynamicStart.location[0].doubleValue() + (lng2.doubleValue() - lng1.doubleValue()) * dynamicStart.location[1].doubleValue() + lng1.doubleValue() * lat2.doubleValue() - lng2.doubleValue() * lat1.doubleValue();
//                Double p2 = (lat1.doubleValue() - lat2.doubleValue()) * dynamicEnd.location[0].doubleValue() + (lng2.doubleValue() - lng1.doubleValue()) * dynamicEnd.location[1].doubleValue() + lng1.doubleValue() * lat2.doubleValue() - lng2.doubleValue() * lat1.doubleValue();
//                if (p1 * p2 > 0){
//                    //在同侧.
//                    if (lengthEnd<lengthStart){
//                        step=2;
//                    }else {
//                        step=step*2;
//                    }
//                }else {
//                    //不在同侧，连线判断是否过截面
//                    if (!intersection(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), dynamicStart.location[0].doubleValue(), dynamicStart.location[1].doubleValue(), dynamicEnd.location[0].doubleValue(), dynamicEnd.location[1].doubleValue())) {
////                            System.out.println("不相交");
//                        step=step*2;
//                    } else {
////                            System.out.println("相交");
//                        collection.add(dynamicStart);
//                        if (dynamicStart.landSpeed.doubleValue()<4){
//                            sp_count[0]++;
//                        }else if (dynamicStart.landSpeed.doubleValue()<7){
//                            sp_count[1]++;
//                        }else if (dynamicStart.landSpeed.doubleValue()<10){
//                            sp_count[2]++;
//                        }else if (dynamicStart.landSpeed.doubleValue()<13){
//                            sp_count[3]++;
//                        }else {
//                            sp_count[4]++;
//                        }
//                        if (dynamicStart.shipState.equals("机动航行")){
//                            state_count[0]++;
//                        }else if (dynamicStart.shipState.contains("泊")){
//                            state_count[1]++;
//                        }else if (dynamicStart.shipState.contains("限")){
//                            state_count[2]++;
//                        }
//                        if (dynamicStart.aisType.equals("Class A")){
//                            aType_count[0]++;
//                        }else aType_count[1]++;
//                        map.put("sp",sp_count);
//                        map.put("state",state_count);
//                        map.put("aisType",aType_count);
//                    }
//                }
//            }else {
//                //判断该船将来是否会通过截面
//                Double lc1 = Math.toRadians(dynamicStart.landCourse.doubleValue()); // 化为弧度
//                if (lc1 > 0){
//                    BigDecimal x_sp = dynamicStart.landSpeed.multiply(BigDecimal.valueOf(Math.sin(lc1))).setScale(5, RoundingMode.HALF_UP);
//                    BigDecimal y_sp = dynamicStart.landSpeed.multiply(BigDecimal.valueOf(Math.cos(lc1))).setScale(5, RoundingMode.HALF_UP);
//                    BigDecimal lng_new = x_sp.multiply(new BigDecimal(0.08)).divide(new BigDecimal(60),6, RoundingMode.HALF_UP).add(dynamicStart.location[0]);
//                    BigDecimal lat_new = y_sp.multiply(new BigDecimal(0.08)).divide(new BigDecimal(60),6, RoundingMode.HALF_UP).add(dynamicStart.location[1]);
//                    if (!intersection(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), dynamicStart.location[0].doubleValue(), dynamicStart.location[1].doubleValue(), lng_new.doubleValue(), lat_new.doubleValue())) {
////                            System.out.println("不相交");
//                    } else {
////                            System.out.println("相交");
//                        collection.add(dynamicStart);
//                    }
//                }
//                j=i+1;
//                while (searchHits.get(j).getContent().mmsi==dynamicStart.mmsi){
//                    j=j+2;
//                }
//            }
//            i=j;
//        }
        map.put("collection", collection); // 传入动态数据
        System.out.println("ispassLine:" + collection.size());
        map.put("flow", collection.size()); // 传入通过截面的船舶数量
        return map;
    }

    private static Double getLenWithPoints(double p1x, double p1y, double p2x, double p2y) {
        Double length;
        length = Math.sqrt(Math.pow(p2x - p1x, 2) + Math.pow(p2y - p1y, 2));
        return length;
    }

    public static Double getLength(double lx1, double ly1, double lx2,
                                   double ly2, double px, double py) {
        Double length;
        double b = getLenWithPoints(lx1, ly1, px, py);
        double c = getLenWithPoints(lx2, ly2, px, py);
        double a = getLenWithPoints(lx1, ly1, lx2, ly2);

        if (c + b == a) {// 点在线段上
            length = (double) 0;
        } else {
            // 组成锐角三角形，则求三角形的高
            double p = (a + b + c) / 2;// 半周长
            double s = Math.sqrt(p * (p - a) * (p - b) * (p - c));// 海伦公式求面积
            length = 2 * s / c;// 返回点到线的距离（利用三角形面积公式求高）
        }
        return length;
    }

    public static boolean intersection(double l1x1, double l1y1, double l1x2, double l1y2,
                                       double l2x1, double l2y1, double l2x2, double l2y2) {
        // 快速排斥实验 首先判断两条线段在 x 以及 y 坐标的投影是否有重合。 有一个为真，则代表两线段必不可交。
        if (Math.max(l1x1, l1x2) < Math.min(l2x1, l2x2)
                || Math.max(l1y1, l1y2) < Math.min(l2y1, l2y2)
                || Math.max(l2x1, l2x2) < Math.min(l1x1, l1x2)
                || Math.max(l2y1, l2y2) < Math.min(l1y1, l1y2)) {
            return false;
        }
        // 跨立实验  如果相交则矢量叉积异号或为零，大于零则不相交
        if ((((l1x1 - l2x1) * (l2y2 - l2y1) - (l1y1 - l2y1) * (l2x2 - l2x1))
                * ((l1x2 - l2x1) * (l2y2 - l2y1) - (l1y2 - l2y1) * (l2x2 - l2x1))) > 0
                || (((l2x1 - l1x1) * (l1y2 - l1y1) - (l2y1 - l1y1) * (l1x2 - l1x1))
                * ((l2x2 - l1x1) * (l1y2 - l1y1) - (l2y2 - l1y1) * (l1x2 - l1x1))) > 0) {
            return false;
        }
        return true;
    }
}
