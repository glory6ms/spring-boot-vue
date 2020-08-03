package com.glory.springboot.dao;

import com.glory.springboot.entities.DynamicEntity;
import com.glory.springboot.entities.es_dynamic;
import com.glory.springboot.reposity.dynamicResp;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Repository
public class mydao {
    @Autowired
    dynamicResp dynamicResp;
    @Autowired
    private ElasticsearchOperations operations;
    public Page<DynamicEntity> queryLntByTime(String Timein, String Timeout){
//        Date timein = null;
//        Date timeout = null;
//        try {
//            timein = new SimpleDateFormat("yyyy-MM-dd").parse(Timein);
//            timeout = new SimpleDateFormat("yyyy-MM-dd").parse(Timeout);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
        Specification specification = new Specification() {
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Path time =root.get("time");
                Predicate predicate = criteriaBuilder.between(time, Timein, Timeout);
                return predicate;
            }
        };
        Sort sort = Sort.by(Sort.Order.asc("did"));
        Pageable pageable = PageRequest.of(0, 30000, sort);
        Page<DynamicEntity> page1 = dynamicResp.findAll(specification, pageable);
        System.out.println(page1.getContent().size());
        return page1;
//        Collection<DynamicEntity> objects = dynamicResp.findBetweenTime(Timein, Timeout);
//        return objects;
    }

    public List<SearchHit<es_dynamic>> QueryByTimeAndLocation(String timein, String timeout, Double lng,Double lat, Double lineLength){
        String[] include = {"mmsi","location","landCourse","landSpeed","time"};
        FetchSourceFilter fetchSourceFilter = new FetchSourceFilter(include,null);
        PageRequest pageRequest = PageRequest.of(0,100);
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
//                .filter(rangeQuery("time").gte(timein).lte(timeout)) //must。filter,none
//                .filter(geoDistanceQuery("location").distance(lineLength, DistanceUnit.KILOMETERS).point(lat, lng).geoDistance(GeoDistance.PLANE)); //must ,filter ,none
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(boolQuery().must(geoDistanceQuery("location").distance(lineLength, DistanceUnit.KILOMETERS).point(lat, lng).geoDistance(GeoDistance.PLANE))
                .must(rangeQuery("time").gte(timein).lte(timeout)));
        GeoDistanceSortBuilder sort = SortBuilders.geoDistanceSort("location", lat, lng).geoDistance(GeoDistance.PLANE).unit(DistanceUnit.KILOMETERS).order(SortOrder.ASC);
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                .withSourceFilter(fetchSourceFilter)
                .withPageable(pageRequest);
//                .withSort(sort)
        IndexCoordinates index = IndexCoordinates.of("dongtaii");
        SearchHits<es_dynamic> search = operations.search(nativeSearchQueryBuilder.build(), es_dynamic.class, index);
        List<SearchHit<es_dynamic>> searchHits = search.getSearchHits();
        System.out.println(nativeSearchQueryBuilder.build().getQuery().toString());
        System.out.println("领域内的点个数: "+search.getTotalHits());
        Iterator<SearchHit<es_dynamic>> iterator = searchHits.iterator();
        while (iterator.hasNext()){
            System.out.println(iterator.next().getContent());
        }
        return searchHits;
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
                    // 需要验证一下！！
                    /**
                     * BigDecimal old_lng = tmp.getLng();
                     *                         BigDecimal old_lat = tmp.getLat();
                     *                         BigDecimal land_course = tmp.getLandCourse();
                     *                         BigDecimal land_speed = tmp.getLandSpeed();
                     *                         //行驶的航速
                     *                         BigDecimal x_sp = land_speed.multiply(BigDecimal.valueOf(Math.sin(land_course.doubleValue()*Math.PI/180))).setScale(5, RoundingMode.HALF_UP);
                     *                         BigDecimal y_sp = land_speed.multiply(BigDecimal.valueOf(Math.cos(land_course.doubleValue()*Math.PI/180))).setScale(5, RoundingMode.HALF_UP);
                     *                         System.out.println("x_sp:"+x_sp+"y_sp"+y_sp);
                     *                         //行驶了多少海里,0.010小时
                     *                         BigDecimal x_distance = x_sp.multiply(new BigDecimal(0.01)).divide(new BigDecimal(60),6, RoundingMode.HALF_UP);
                     *                         BigDecimal y_distance = y_sp.multiply(new BigDecimal(0.01)).divide(new BigDecimal(60),6, RoundingMode.HALF_UP);
                     *                         BigDecimal add_lng = old_lng.add(x_distance);
                     *                         BigDecimal add_lat = old_lat.add(y_distance);
                     */
                    BigDecimal landCourse = collection2.get(0).getLandCourse();
                    BigDecimal landSpeed = collection2.get(0).getLandSpeed();
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

                            System.out.println("不相交");

                        } else {
                            System.out.println("相交");
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

                            System.out.println("不相交");

                        } else {
                            System.out.println("相交");
                            collection1.add(left.get(lflag));
                        }
                    }else {
                        //判断轨迹点都在一侧的情况下，船舶行驶五分钟能不能通过断面
                        es_dynamic tmp = collection2.get(0);
                        for (es_dynamic dy : collection2){

//                            if(tmp.getTime().before(dy.getTime())){
                            if(tmp.getTime().compareTo(dy.getTime())>0){
                                tmp = dy;
                            }
                        }
                        BigDecimal old_lng = tmp.location[0];
                        BigDecimal old_lat = tmp.location[1];
                        BigDecimal land_course = tmp.getLandCourse();
                        BigDecimal land_speed = tmp.getLandSpeed();
                        //行驶的航速
                        BigDecimal x_sp = land_speed.multiply(BigDecimal.valueOf(Math.sin(Math.toRadians(land_course.doubleValue())))).setScale(5, RoundingMode.HALF_UP);
                        BigDecimal y_sp = land_speed.multiply(BigDecimal.valueOf(Math.cos(Math.toRadians(land_course.doubleValue())))).setScale(5, RoundingMode.HALF_UP);
                        System.out.println("x_sp:"+x_sp+"y_sp"+y_sp);
                        //行驶了多少海里,0.03小时
                        BigDecimal x_distance = x_sp.multiply(new BigDecimal(0.08)).divide(new BigDecimal(60),6, RoundingMode.HALF_UP);
                        BigDecimal y_distance = y_sp.multiply(new BigDecimal(0.08)).divide(new BigDecimal(60),6, RoundingMode.HALF_UP);
                        BigDecimal add_lng = old_lng.add(x_distance);
                        BigDecimal add_lat = old_lat.add(y_distance);
                        //相交判断无误
                        if (!intersection(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), add_lng.doubleValue(), add_lat.doubleValue(), old_lng.doubleValue(), old_lat.doubleValue())) {
                            System.out.println("不相交");
                        } else {
                            System.out.println("相交");
                            collection1.add(tmp);
                        }
                    }
                }
            }
        }
        return collection1;
    }
    public List<DynamicEntity> queryByArea(List<DynamicEntity> collection, BigDecimal lng1, BigDecimal lat1, BigDecimal lng2, BigDecimal lat2){
        //第二步判断船舶是否过断面
        //通过断面的船舶集合
        List<DynamicEntity> collection1= new ArrayList<>();
        List<DynamicEntity> collection3 = new ArrayList<>();
//原collection里需要把以筛选的Mmsi别开
        for (int i = 0; i < collection.size(); i++) {
            int flag = 0;
            if (!collection3.isEmpty()) {
                for (int o = 0; o < collection3.size(); o++) {
                    if (collection3.get(o).getMmsi()==collection.get(i).getMmsi()) {
                        flag = 1;
                        break;
                    }
                }
            }
            if (flag == 0) {
                List<DynamicEntity> collection2 = new ArrayList<>();
                collection2.add(collection.get(i));
                collection3.add(collection.get(i));
                for (int j = i + 1; j < collection.size(); j++) {
                    if (collection.get(j).getMmsi()==collection.get(i).getMmsi()) {
                        collection2.add(collection.get(j));
                    }
                }

                if (collection2.size() < 2) {
                    //按现有速度判断五分钟后，该船的位置，把两个点相连，判断是否过断面
                    // 需要验证一下！！
                    /**
                     * BigDecimal old_lng = tmp.getLng();
                     *                         BigDecimal old_lat = tmp.getLat();
                     *                         BigDecimal land_course = tmp.getLandCourse();
                     *                         BigDecimal land_speed = tmp.getLandSpeed();
                     *                         //行驶的航速
                     *                         BigDecimal x_sp = land_speed.multiply(BigDecimal.valueOf(Math.sin(land_course.doubleValue()*Math.PI/180))).setScale(5, RoundingMode.HALF_UP);
                     *                         BigDecimal y_sp = land_speed.multiply(BigDecimal.valueOf(Math.cos(land_course.doubleValue()*Math.PI/180))).setScale(5, RoundingMode.HALF_UP);
                     *                         System.out.println("x_sp:"+x_sp+"y_sp"+y_sp);
                     *                         //行驶了多少海里,0.010小时
                     *                         BigDecimal x_distance = x_sp.multiply(new BigDecimal(0.01)).divide(new BigDecimal(60),6, RoundingMode.HALF_UP);
                     *                         BigDecimal y_distance = y_sp.multiply(new BigDecimal(0.01)).divide(new BigDecimal(60),6, RoundingMode.HALF_UP);
                     *                         BigDecimal add_lng = old_lng.add(x_distance);
                     *                         BigDecimal add_lat = old_lat.add(y_distance);
                     */
                    BigDecimal landCourse = collection2.get(0).getLandCourse();
                    BigDecimal landSpeed = collection2.get(0).getLandSpeed();
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
                        lng_new = lng_new.add(collection2.get(0).getLng());
                        lat_new = lat_new.add(collection2.get(0).getLat());
                        if (!intersection(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), collection2.get(0).getLng().doubleValue(), collection2.get(0).getLat().doubleValue(), lng_new.doubleValue(), lat_new.doubleValue())) {

                            System.out.println("不相交");

                        } else {
                            System.out.println("相交");
                            collection1.add(collection2.get(0));
                        }
                    }
                } else {
                    //此处左右点筛选无误
                    List<DynamicEntity> left = new ArrayList<>();
                    List<DynamicEntity> right = new ArrayList<>();
                    for (int k = 0; k < collection2.size(); k++) {
                        Double tmp = (lat1.doubleValue() - lat2.doubleValue()) * collection2.get(k).getLng().doubleValue() + (lng2.doubleValue() - lng1.doubleValue()) * collection2.get(k).getLat().doubleValue() + lng1.doubleValue() * lat2.doubleValue() - lng2.doubleValue() * lat1.doubleValue();
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
                            leftlength[lf] = getLength(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), left.get(lf).getLng().doubleValue(), left.get(lf).getLat().doubleValue());
                            leftnum[lf] = lf;
                        }
                        for (int rf = 0; rf < right.size(); rf++) {
                            rightlength[rf] = getLength(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), right.get(rf).getLng().doubleValue(), right.get(rf).getLat().doubleValue());
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
                        if (!intersection(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), left.get(lflag).getLng().doubleValue(), left.get(lflag).getLat().doubleValue(), right.get(rflag).getLng().doubleValue(), right.get(rflag).getLat().doubleValue())) {

                            System.out.println("不相交");

                        } else {
                            System.out.println("相交");
                            collection1.add(left.get(lflag));
                        }
                    }else {
                        //判断轨迹点都在一侧的情况下，船舶行驶五分钟能不能通过断面
                        DynamicEntity tmp = collection2.get(0);
                        for (DynamicEntity dy : collection2){

//                            if(tmp.getTime().before(dy.getTime())){
                            if(tmp.getTime().compareTo(dy.getTime())>0){
                                tmp = dy;
                            }
                        }
                        BigDecimal old_lng = tmp.getLng();
                        BigDecimal old_lat = tmp.getLat();
                        BigDecimal land_course = tmp.getLandCourse();
                        BigDecimal land_speed = tmp.getLandSpeed();
                        //行驶的航速
                        BigDecimal x_sp = land_speed.multiply(BigDecimal.valueOf(Math.sin(Math.toRadians(land_course.doubleValue())))).setScale(5, RoundingMode.HALF_UP);
                        BigDecimal y_sp = land_speed.multiply(BigDecimal.valueOf(Math.cos(Math.toRadians(land_course.doubleValue())))).setScale(5, RoundingMode.HALF_UP);
                        System.out.println("x_sp:"+x_sp+"y_sp"+y_sp);
                        //行驶了多少海里,0.03小时
                        BigDecimal x_distance = x_sp.multiply(new BigDecimal(0.08)).divide(new BigDecimal(60),6, RoundingMode.HALF_UP);
                        BigDecimal y_distance = y_sp.multiply(new BigDecimal(0.08)).divide(new BigDecimal(60),6, RoundingMode.HALF_UP);
                        BigDecimal add_lng = old_lng.add(x_distance);
                        BigDecimal add_lat = old_lat.add(y_distance);
                        System.out.println(add_lng);
                        System.out.println(add_lat);
                        //相交判断无误
                        if (!intersection(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue(), add_lng.doubleValue(), add_lat.doubleValue(), old_lng.doubleValue(), old_lat.doubleValue())) {
                            System.out.println("不相交");
                        } else {
                            System.out.println("相交");
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
