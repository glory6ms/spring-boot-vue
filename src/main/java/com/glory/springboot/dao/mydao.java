package com.glory.springboot.dao;

import com.glory.springboot.entities.DynamicEntity;
import com.glory.springboot.entities.es_dynamic;
import com.glory.springboot.reposity.dynamicResp;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Repository
public class mydao {
    @Autowired
    dynamicResp dynamicResp;
    @Autowired
    private ElasticsearchOperations operations;
    @Autowired
    ElasticsearchRestTemplate template;

    /**
     *1-1 jpa
     * @param Timein
     * @param Timeout
     * @return jpa查询前10000条动态数据
     */
    public Page<DynamicEntity> queryLntByTime(String Timein, String Timeout){
        Specification specification = new Specification() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Path time =root.get("time");
                Predicate predicate = criteriaBuilder.between(time, Timein, Timeout);
                return predicate;
            }
        };
        Pageable pageable = PageRequest.of(1, 1000);
        Page<DynamicEntity> page1 = dynamicResp.findAll(specification, pageable);
        return page1;
    }

    /**
     * 2-1 es
     * @param timein
     * @param timeout
     * @return List<SearchHit<es_dynamic>> 未使用评分机制，只取location字段，SearchHit<es_dynamic>.content.location
     */
    public List<SearchHit<es_dynamic>> queryLocationByTime(String timein, String timeout){
        String[] include = {"location"}; // 输出参数过滤
        FetchSourceFilter fetchSourceFilter = new FetchSourceFilter(include, null);
        PageRequest pageRequest = PageRequest.of(0,10000);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(rangeQuery("time").gte(timein).lte(timeout));
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                .withSourceFilter(fetchSourceFilter)
                .withPageable(pageRequest);
        IndexCoordinates index = IndexCoordinates.of("trajectory");
        SearchHits<es_dynamic> search = operations.search(nativeSearchQueryBuilder.build(), es_dynamic.class, index);
        List<SearchHit<es_dynamic>> searchHits = search.getSearchHits();
        System.out.println(nativeSearchQueryBuilder.build().getQuery().toString());
        System.out.println("时间范围内的点个数:"+search.getTotalHits());
        return searchHits;
    }

    /**
     *
     * @param timein：开始时间
     * @param timeout：结束时间
     * @param lng：中心点经度
     * @param lat：中心点纬度
     * @param lineLength：截面长度
     * @return 不包含得到和其他信息的文档集合
     */
    public List<SearchHit<es_dynamic>> QueryByTimeAndLocation(String timein, String timeout, Double lng,Double lat, Double lineLength){
        String[] include = {"mmsi","location","landCourse","landSpeed","time"}; // 输出参数过滤
        FetchSourceFilter fetchSourceFilter = new FetchSourceFilter(include,null);
        PageRequest pageRequest = PageRequest.of(0,1000);
        //1-1
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(rangeQuery("time").gte(timein).lte(timeout))
                        .must(boolQuery().must(geoDistanceQuery("location").distance(lineLength, DistanceUnit.KILOMETERS).point(lat, lng).geoDistance(GeoDistance.PLANE)));
//        GeoDistanceSortBuilder sort = SortBuilders.geoDistanceSort("location", lat, lng).geoDistance(GeoDistance.PLANE).unit(DistanceUnit.KILOMETERS).order(SortOrder.ASC);
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                .withSourceFilter(fetchSourceFilter)
                .withPageable(pageRequest)
                .withSort(SortBuilders.fieldSort("mmsi").order(SortOrder.ASC))
                .withSort(SortBuilders.fieldSort("time").order(SortOrder.ASC));
        IndexCoordinates index = IndexCoordinates.of("trajectory");
        //1-1
//        SearchHits<es_dynamic> search = operations.search(nativeSearchQueryBuilder.build(), es_dynamic.class, index);
//        List<SearchHit<es_dynamic>> searchHits = search.getSearchHits();
//        System.out.println(nativeSearchQueryBuilder.build().getQuery().toString());
//        System.out.println("领域内的点个数: "+search.getTotalHits());
//
//        Iterator<SearchHit<es_dynamic>> iterator = searchHits.iterator();
//        while (iterator.hasNext()){
//            System.out.println(iterator.next().getContent());
//        }
//        return searchHits;

        //1-2
        SearchScrollHits<es_dynamic> scroll = template.searchScrollStart(1000,nativeSearchQueryBuilder.build(),es_dynamic.class,index);
        String scrollId = scroll.getScrollId();
        List<String> id =new ArrayList<>();
        id.add(scrollId);
        List<SearchHit<es_dynamic>> list = new ArrayList<>();
        while(scroll.hasSearchHits()){
            list.addAll(scroll.getSearchHits());
            scrollId = scroll.getScrollId();
            scroll=template.searchScrollContinue(scrollId,1000,es_dynamic.class,index);
            id.add(scrollId);
        }
        template.searchScrollClear(id);
        System.out.println(list.size());
        return list;
    }
    public List<SearchHit<es_dynamic>> QueryByPolygonRange(String timein, String timeout, List<GeoPoint> in, List<GeoPoint> out) {
        String[] include = {"mmsi", "location", "time"}; // 输出参数过滤
        FetchSourceFilter fetchSourceFilter = new FetchSourceFilter(include, null);
        PageRequest pageRequest = PageRequest.of(0, 10000);
        BoolQueryBuilder boolQueryBuilder;
        if (in == null){
            boolQueryBuilder = QueryBuilders.boolQuery()
                    .filter(boolQuery().must(rangeQuery("time").gte(timein).lte(timeout)))
                    .must(geoPolygonQuery("location", out));
        }else {
            boolQueryBuilder = QueryBuilders.boolQuery()
                    .filter(boolQuery().must(rangeQuery("time").gte(timein).lte(timeout)))
                    .must(geoPolygonQuery("location", out))
                    .mustNot(geoPolygonQuery("location", in));
        }
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                .withSourceFilter(fetchSourceFilter)
                .withPageable(pageRequest)
                .withSort(SortBuilders.fieldSort("mmsi").order(SortOrder.ASC))
                .withSort(SortBuilders.fieldSort("time").order(SortOrder.ASC));
        IndexCoordinates index = IndexCoordinates.of("trajectory");
        SearchHits<es_dynamic> search = operations.search(nativeSearchQueryBuilder.build(), es_dynamic.class, index);
        System.out.println("领域内的点个数: " + search.getTotalHits());
//        System.out.println(nativeSearchQueryBuilder.build().getQuery().toString());
        return search.getSearchHits();
    }

    /**
     * @param hits    落在缓冲区内的点集合
     * @param polygon 活跃度统计绘制的区域
     * @return
     * @method 一次循环，判断同一个mmsi下的船舶轨迹是否在区域内的状态，如果上个轨迹点在区域内，下一个不在即认定出了区域
     */
    public Collection<es_dynamic> CheckInPolygon(List<SearchHit<es_dynamic>> hits, ArrayList<List<Double>> polygon) {
        List<es_dynamic> result = new ArrayList<>();
        List<Boolean> states = new ArrayList<>();
        for (int i = 0; i < hits.size(); i++) {
            es_dynamic dynamic = hits.get(i).getContent();
            states.add(isPoiWithinPoly(dynamic, polygon));
            if (i != 0) {
                if (dynamic.mmsi == hits.get(i - 1).getContent().mmsi) {
                    if (states.get(i) != states.get(i - 1)) {
                        result.add(dynamic);
                    }
                }
            }
        }
        return result;
    }

    public Boolean isPoiWithinPoly(es_dynamic dynamic, ArrayList<List<Double>> poly) {
        int sissc = 0;
        for (int i = 0; i < poly.size() - 1; i++) {
            Double[] poi = {dynamic.location[0].doubleValue(), dynamic.location[1].doubleValue()};
            if (isRayIntersectsSegment(poi, poly.get(i), poly.get(i + 1))) {
                sissc++;
            }
        }
        return sissc % 2 != 0;
    }

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

    public List<SearchHit<es_dynamic>> QueryByTimeAndLocation2(String timein, String timeout,Double top,Double left,Double bottom,Double right){
        //2-1
        String[] include = {"mmsi","location","landCourse","landSpeed","time"}; // 输出参数过滤
        FetchSourceFilter fetchSourceFilter = new FetchSourceFilter(include,null);
        PageRequest pageRequest = PageRequest.of(0,10000);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(boolQuery().must(rangeQuery("time").gte(timein).lte(timeout)))
                .must(geoBoundingBoxQuery("location").setCorners(top,left,bottom,right));
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                .withSourceFilter(fetchSourceFilter)
                .withPageable(pageRequest)
                .withSort(SortBuilders.fieldSort("mmsi").order(SortOrder.ASC))
                .withSort(SortBuilders.fieldSort("time").order(SortOrder.ASC));
        IndexCoordinates index = IndexCoordinates.of("trajectory");
        //2-1
//        SearchHits<es_dynamic> search = operations.search(nativeSearchQueryBuilder.build(), es_dynamic.class, index);
//        System.out.println("领域内的点个数: "+search.getTotalHits());
//        return search.getSearchHits();
        //2-1
        SearchScrollHits<es_dynamic> scroll = template.searchScrollStart(1000,nativeSearchQueryBuilder.build(),es_dynamic.class,index);
        String scrollId = scroll.getScrollId();
        List<String> id =new ArrayList<>();
        id.add(scrollId);
        List<SearchHit<es_dynamic>> list = new ArrayList<>();
        while(scroll.hasSearchHits()){
            list.addAll(scroll.getSearchHits());
            scrollId = scroll.getScrollId();
            scroll=template.searchScrollContinue(scrollId,1000,es_dynamic.class,index);
            id.add(scrollId);
        }
        template.searchScrollClear(id);
        System.out.println("领域内的点个数: "+list.size());
        return list;
    }
    public Collection<es_dynamic> isPassLine(List<SearchHit<es_dynamic>> searchHits,BigDecimal lng1, BigDecimal lat1, BigDecimal lng2, BigDecimal lat2){
        int j;
        int step = 2;
        List<es_dynamic> collection = new ArrayList<>();
        for(int i=0;i<searchHits.size();){
            es_dynamic dynamicStart = searchHits.get(i).getContent();
            Double lengthStart = getLength(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), dynamicStart.location[0].doubleValue(), dynamicStart.location[1].doubleValue());
            j=i+step;
            if (j>=searchHits.size()){
                break;
            }
            es_dynamic dynamicEnd = searchHits.get(j).getContent();
            Double lengthEnd = getLength(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), dynamicEnd.location[0].doubleValue(), dynamicEnd.location[1].doubleValue());
            if (dynamicStart.mmsi == dynamicEnd.mmsi){
                // 判断两个点是否中截面同侧
                Double p1 = (lat1.doubleValue() - lat2.doubleValue()) * dynamicStart.location[0].doubleValue() + (lng2.doubleValue() - lng1.doubleValue()) * dynamicStart.location[1].doubleValue() + lng1.doubleValue() * lat2.doubleValue() - lng2.doubleValue() * lat1.doubleValue();
                Double p2 = (lat1.doubleValue() - lat2.doubleValue()) * dynamicEnd.location[0].doubleValue() + (lng2.doubleValue() - lng1.doubleValue()) * dynamicEnd.location[1].doubleValue() + lng1.doubleValue() * lat2.doubleValue() - lng2.doubleValue() * lat1.doubleValue();
                if (p1 * p2 > 0){
                    //在同侧.
                    if (lengthEnd<lengthStart){
                        step=2;
                    }else {
                        step=step*2;
                    }
                }else {
                    //不在同侧，连线判断是否过截面
                    if (!intersection(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), dynamicStart.location[0].doubleValue(), dynamicStart.location[1].doubleValue(), dynamicEnd.location[0].doubleValue(), dynamicEnd.location[1].doubleValue())) {
//                            System.out.println("不相交");
                        step=step*2;
                    } else {
//                            System.out.println("相交");
                        collection.add(dynamicStart);
                    }
                }
            }else {
                //判断该船将来是否会通过截面
                Double lc1 = Math.toRadians(dynamicStart.landCourse.doubleValue()); // 化为弧度
                if (lc1 > 0){
                    BigDecimal x_sp = dynamicStart.landSpeed.multiply(BigDecimal.valueOf(Math.sin(lc1))).setScale(5, RoundingMode.HALF_UP);
                    BigDecimal y_sp = dynamicStart.landSpeed.multiply(BigDecimal.valueOf(Math.cos(lc1))).setScale(5, RoundingMode.HALF_UP);
                    BigDecimal lng_new = x_sp.multiply(new BigDecimal(0.08)).divide(new BigDecimal(60),6, RoundingMode.HALF_UP).add(dynamicStart.location[0]);
                    BigDecimal lat_new = y_sp.multiply(new BigDecimal(0.08)).divide(new BigDecimal(60),6, RoundingMode.HALF_UP).add(dynamicStart.location[1]);
                    if (!intersection(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), dynamicStart.location[0].doubleValue(), dynamicStart.location[1].doubleValue(), lng_new.doubleValue(), lat_new.doubleValue())) {
//                            System.out.println("不相交");
                    } else {
//                            System.out.println("相交");
                        collection.add(dynamicStart);
                    }
                }
                j=i+1;
                while (searchHits.get(j).getContent().mmsi==dynamicStart.mmsi){
                    j=j+2;
                }
            }
            i=j;
        }
        return collection;
    }
    public List<es_dynamic> Check(List<SearchHit<es_dynamic>> searchHits,BigDecimal lng1, BigDecimal lat1, BigDecimal lng2, BigDecimal lat2){
        List<es_dynamic> collection1= new ArrayList<>();
        List<es_dynamic> collection3 = new ArrayList<>();
        for (int i = 0; i < searchHits.size(); i++) {
            int flag = 0;
            if (!collection3.isEmpty()) {
                for (int o = 0; o < collection3.size(); o++) {
                    if (collection3.get(o).mmsi==searchHits.get(i).getContent().mmsi) {
                        flag = 1;
                        break;
                    }
                }
            }
            if (flag == 0) {
                List<es_dynamic> collection2 = new ArrayList<>();
                collection2.add(searchHits.get(i).getContent());
                collection3.add(searchHits.get(i).getContent());
                for (int j = i + 1; j < searchHits.size(); j++) {
                    if (searchHits.get(j).getContent().mmsi==searchHits.get(i).getContent().mmsi) {
                        collection2.add(searchHits.get(j).getContent());
                    }
                }

                if (collection2.size() < 2) {
                    //按现有速度判断五分钟后，该船的位置，把两个点相连，判断是否过断面
                    BigDecimal landCourse = collection2.get(0).landCourse;
                    BigDecimal landSpeed = collection2.get(0).landSpeed;
                    double lc1 = Math.toRadians(landCourse.doubleValue());
                    if (lc1 > 0) {
                        //画单位为30.8666m/min
//                        BigDecimal sp = landSpeed.multiply(new BigDecimal(30.8666 * 5));
                        BigDecimal x_sp = landSpeed.multiply(BigDecimal.valueOf(Math.sin(lc1))).setScale(5, RoundingMode.HALF_UP);
                        BigDecimal y_sp = landSpeed.multiply(BigDecimal.valueOf(Math.cos(lc1))).setScale(5, RoundingMode.HALF_UP);

                        //五分钟后的移动的距离
                        //距离是否需要转换为经纬度？
                        BigDecimal lng_new = x_sp.multiply(new BigDecimal(0.08)).divide(new BigDecimal(60),6, RoundingMode.HALF_UP);
                        BigDecimal lat_new = y_sp.multiply(new BigDecimal(0.08)).divide(new BigDecimal(60),6, RoundingMode.HALF_UP);
                        lng_new = lng_new.add(collection2.get(0).location[0]);
                        lat_new = lat_new.add(collection2.get(0).location[1]);
                        if (!intersection(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), collection2.get(0).location[0].doubleValue(), collection2.get(0).location[1].doubleValue(), lng_new.doubleValue(), lat_new.doubleValue())) {

//                            System.out.println("不相交");

                        } else {
//                            System.out.println("相交");
                            collection1.add(collection2.get(0));
                        }
                    }
                } else {
                    //此处左右点筛选无误
                    List<es_dynamic> left = new ArrayList<>();
                    List<es_dynamic> right = new ArrayList<>();
                    for (int k = 0; k < collection2.size(); k++) {
                        Double tmp = (lat1.doubleValue() - lat2.doubleValue()) * collection2.get(k).location[0].doubleValue() + (lng2.doubleValue() - lng1.doubleValue()) * collection2.get(k).location[1].doubleValue() + lng1.doubleValue() * lat2.doubleValue() - lng2.doubleValue() * lat1.doubleValue();
                        if (tmp > 0) {
                            left.add(collection2.get(k));

                        } else {
                            right.add(collection2.get(k));
                        }
                    }
                    if (left.size() != 0 && right.size() != 0) {
                        //此处选择排序选最近点无误
                        double leftlength[] = new double[left.size()];
                        double rightlength[] = new double[right.size()];
                        int leftnum[] = new int[left.size()];
                        int rightnum[] = new int[right.size()];
                        for (int lf = 0; lf < left.size(); lf++) {
                            leftlength[lf] = getLength(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), left.get(lf).location[0].doubleValue(), left.get(lf).location[1].doubleValue());
                            leftnum[lf] = lf;
                        }
                        for (int rf = 0; rf < right.size(); rf++) {
                            rightlength[rf] = getLength(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), right.get(rf).location[0].doubleValue(), right.get(rf).location[1].doubleValue());
                            rightnum[rf] = rf;
                        }
                        int lflag = 0;
                        int rflag = 0;
                        for (int q = 0; q < left.size(); q++) {
                            if (leftlength[q] < leftlength[lflag]) {
                                lflag = q;
                            }
                        }
                        for (int e = 0; e < right.size(); e++) {
                            if (rightlength[e] < rightlength[rflag]) {
                                rflag = e;
                            }
                        }
                        //System.out.println("判断是否相交。。。" + left.get(lflag).getLng().doubleValue() + "," + left.get(lflag).getLat().doubleValue() + "=" + right.get(rflag).getLng().doubleValue() + "," + left.get(lflag).getLat().doubleValue());

                        //相交判断无误
                        if (!intersection(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), left.get(lflag).location[0].doubleValue(), left.get(lflag).location[1].doubleValue(), right.get(rflag).location[0].doubleValue(), right.get(rflag).location[1].doubleValue())) {

//                            System.out.println("不相交");

                        } else {
//                            System.out.println("相交");
                            collection1.add(left.get(lflag));
                        }
                    }else {
                        //判断轨迹点都在一侧的情况下，船舶行驶五分钟能不能通过断面
                        es_dynamic tmp = collection2.get(0);
                        for (es_dynamic dy : collection2){
                            if(tmp.time.compareTo(dy.time)>0){
                                tmp = dy;
                            }
                        }
                        BigDecimal old_lng = tmp.location[0];
                        BigDecimal old_lat = tmp.location[1];
                        BigDecimal land_course = tmp.landCourse;
                        BigDecimal land_speed = tmp.landSpeed;
                        //行驶的航速
                        BigDecimal x_sp = land_speed.multiply(BigDecimal.valueOf(Math.sin(Math.toRadians(land_course.doubleValue())))).setScale(5, RoundingMode.HALF_UP);
                        BigDecimal y_sp = land_speed.multiply(BigDecimal.valueOf(Math.cos(Math.toRadians(land_course.doubleValue())))).setScale(5, RoundingMode.HALF_UP);
//                        System.out.println("x_sp:"+x_sp+"y_sp"+y_sp);
                        //行驶了多少海里,0.03小时
                        BigDecimal x_distance = x_sp.multiply(new BigDecimal(0.08)).divide(new BigDecimal(60),6, RoundingMode.HALF_UP);
                        BigDecimal y_distance = y_sp.multiply(new BigDecimal(0.08)).divide(new BigDecimal(60),6, RoundingMode.HALF_UP);
                        BigDecimal add_lng = old_lng.add(x_distance);
                        BigDecimal add_lat = old_lat.add(y_distance);
                        //相交判断无误
                        if (!intersection(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), add_lng.doubleValue(), add_lat.doubleValue(), old_lng.doubleValue(), old_lat.doubleValue())) {
//                            System.out.println("不相交");
                        } else {
//                            System.out.println("相交");
                            collection1.add(tmp);
                        }
                    }
                }
            }
        }
        return collection1;
    }
    private static Double getLenWithPoints(double p1x, double p1y, double p2x, double p2y) {
        Double length ;
        length = Math.sqrt(Math.pow(p2x - p1x, 2) + Math.pow(p2y - p1y, 2));
        return length;
    }

    public static Double getLength(double lx1, double ly1, double lx2,
                                   double ly2, double px, double py) {
        Double length ;
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
