//package com.glory.springboot.reposity;
//
//import com.glory.springboot.entities.*;
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//import org.elasticsearch.common.geo.GeoDistance;
//import org.elasticsearch.common.geo.builders.CoordinatesBuilder;
//import org.elasticsearch.common.geo.builders.MultiPointBuilder;
//import org.elasticsearch.common.unit.DistanceUnit;
//import org.elasticsearch.geometry.Geometry;
//import org.elasticsearch.geometry.GeometryVisitor;
//import org.elasticsearch.geometry.Point;
//import org.elasticsearch.geometry.ShapeType;
//import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.index.query.GeoShapeQueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
//import org.springframework.data.elasticsearch.core.SearchHit;
//import org.springframework.data.elasticsearch.core.SearchHits;
//import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
//import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
//import org.springframework.data.elasticsearch.core.query.IndexQuery;
//import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
//import org.springframework.data.redis.core.RedisTemplate;
//
//import java.io.IOException;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//
//import static org.elasticsearch.index.query.QueryBuilders.*;
//import static org.junit.jupiter.api.Assertions.*;
//import org.elasticsearch.common.geo.ShapeRelation;
//import org.elasticsearch.common.geo.builders.ShapeBuilder;
//
//@SpringBootTest
//class dynamicRespTest {
//
//    @Autowired
//    dynamicResp dynamicResp;
//    @Autowired
//    private ElasticsearchOperations operations;
//    @Autowired
//    private RedisTemplate<String,String> redisTemplate;
//
//    @Test
//    public void set(){
//        try {
//            Set<String> keys = redisTemplate.keys("*");
//            Iterator<String> it1 = keys.iterator();
//            while (it1.hasNext()) {
//                Boolean delete = redisTemplate.delete(it1.next());
//                System.out.println(delete);
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//    @Test
//    public void add() throws ParseException {
//        List<DynamicEntity> all = (List<DynamicEntity>) dynamicResp.findBetweenTime("2017-02-06","2017-02-08");
//        System.out.println(new Date());
//        Gson gson = new Gson();
//        List<String> keys = new ArrayList<>();
//        IndexCoordinates test = IndexCoordinates.of("test");
//        for (DynamicEntity d : all){
//            String[] s = d.getTime().split(" ");
//            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder().must(termQuery("lon3", d.getLng())).must(termQuery("lat3", d.getLat()));
//            NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder);
//            IndexCoordinates index = IndexCoordinates.of("place");
//            SearchHits<place> search = operations.search(nativeSearchQueryBuilder.build(), place.class, index);
//            List<SearchHit<place>> searchHits = search.getSearchHits();
//            String placeId = searchHits.get(0).getContent().placeId;
//            String[] da = s[0].split("-");
//            String[] hr = s[1].split(":");
//            String key = da[0].substring(2,4)+placeId+da[1]+da[2]+ (Integer.parseInt(hr[0]) / 4 + 1)+d.getMmsi();
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//yyyy-MM-dd HH:mm:ss
//            Date date = simpleDateFormat.parse(d.getTime());
//            long score = date.getTime();
//            System.out.println(key);
//            keys.add(key);
//            es_dynamic es=new es_dynamic();
//            es.location[0] = d.getLng();
//            es.location[1] = d.getLat();
//            es.landSpeed = d.getLandSpeed();
//            es.landCourse = d.getLandCourse();
//            es.aisType = d.getAistype();
//            es.precion = d.getPrecion();
//            es.shipCourse = d.getShipCourse();
//            es.shipState = d.getShipState();
//            es.time =d.getTime();
//
//            redisTemplate.opsForZSet().add(key,gson.toJson(es),score); //存放轨迹数据 key yyyyMMddHHMMSI
//            redisTemplate.expire(key,5,TimeUnit.MINUTES);
//        }
//        for(String key:keys){
//            Set<String> set = redisTemplate.opsForZSet().range(key, 0, -1);
//            Iterator<String> iterator = set.iterator();
//            IndexQuery indexQuery = new IndexQuery();
//            String did = key.substring(0,10);
//
//            String mmsi = key.substring(10);
//            if (mmsi.equals("0")||mmsi.equals(null)){
//                return;
//            }
//            List<es_dynamic> trajectories = new ArrayList<>();
//            while (iterator.hasNext()){
//                es_dynamic dynamic = gson.fromJson(iterator.next(), es_dynamic.class);
//                trajectories.add(dynamic);
//            }
//            redis_dynamic rd = new redis_dynamic();
//            rd.did = Long.valueOf(did);
//            rd.mmsi = Integer.parseInt(mmsi);
//            rd.trajectory = trajectories;
//            String s = gson.toJson(rd);
//            indexQuery.setSource(s);
//            operations.index(indexQuery,test);
//            System.out.println(mmsi);
//        }
//        System.out.println(new Date());
////        Set<String> keys = redisTemplate.keys("*");
////        Iterator<String> it1 = keys.iterator();
////        while (it1.hasNext()) {
//////            redisTemplate.delete(it1.next());
////            System.out.println(it1);
////        }
//    }
//    @Test
//    void search(){
//        Gson gson = new Gson();//20170501
////        System.out.println(new Date());
//        Date dNow = new Date( );
//        SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss:SS");
//
//        System.out.println("当前时间为: " + ft.format(dNow));
//        Set<String> range = redisTemplate.opsForZSet().range("201750413791796", 0, -1);
//        for (String s : range){
//            DynamicEntity json = gson.fromJson(s, new TypeToken<DynamicEntity>() {
//            }.getType());
//            System.out.println(json.toString());
//        }
//        System.out.println("查询结果数量:"+range.size());
//        dNow = new Date( );
//        System.out.println("当前时间为: " + ft.format(dNow));
//    }
////    @Test
////    void test1() {
////        List<DynamicEntity> all = dynamicResp.findAll();
////        IndexCoordinates index = IndexCoordinates.of("trajectory");
////        List<IndexQuery> queries = new ArrayList<>();
////        Gson gson = new Gson();
////        int count=0;
////        for (DynamicEntity d:all){
////            es_dynamic es=new es_dynamic();
////            es.location[0] = d.getLng();
////            es.location[1] = d.getLat();
////            es.landSpeed = d.getLandSpeed();
////            es.landCourse = d.getLandCourse();
////            es.did = String.valueOf(d.getDid());
////            es.aisType = d.getAistype();
////            es.mmsi = d.getMmsi();
////            es.precion = d.getPrecion();
////            es.shipCourse = d.getShipCourse();
////            es.shipState = d.getShipState();
////            es.time =d.getTime();
////            IndexQuery indexQuery = new IndexQuery();
////            indexQuery.setId(String.valueOf(d.getDid()));
////            indexQuery.setSource(gson.toJson(es));
////            queries.add(indexQuery);
////            count++;
////            if (count==500){
////                count=0;
////                System.out.println("inserting..");
////                operations.bulkIndex(queries, index);
////                queries.clear();
////            }
////        }
////        System.out.println(count);
////        operations.bulkIndex(queries, index);
//////        if (count>1){
//////            operations.bulkIndex(queries, index);
//////            queries.clear();
//////        }
////    }
//    @Test
//    void test03() throws ParseException {
////        try {
////            String time = "2111/07/29 14:50:11";
////            Date date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(time);
////            long unixTimestamp = date.getTime()/1000;
////            System.out.println(unixTimestamp);
////        } catch (ParseException e) {
////            e.printStackTrace();
////        }
//
//        List<DynamicEntity> all = dynamicResp.findAll();
//        IndexCoordinates index = IndexCoordinates.of("test");
//        List<IndexQuery> queries = new ArrayList<>();
//        Gson gson = new Gson();
//        int count=0;
//        for (DynamicEntity d:all){
//            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(d.getTime());
//            long unixTimestamp = date.getTime()/1000;
//            es_test es=new es_test();
//            es.location[0] = d.getLng();
//            es.location[1] = d.getLat();
//            es.landSpeed = d.getLandSpeed();
//            es.landCourse = d.getLandCourse();
//            es.id = String.valueOf(d.getMmsi()).concat(String.valueOf(unixTimestamp));
//            es.aisType = d.getAistype();
//            es.mmsi = d.getMmsi();
//            es.precion = d.getPrecion();
//            es.shipCourse = d.getShipCourse();
//            es.shipState = d.getShipState();
//            es.time =d.getTime();
//            IndexQuery indexQuery = new IndexQuery();
//            indexQuery.setId(String.valueOf(d.getDid()));
//            indexQuery.setSource(gson.toJson(es));
//            queries.add(indexQuery);
//            count++;
//            if (count==500){
//                count=0;
//                System.out.println("inserting..");
//                operations.bulkIndex(queries, index);
//                queries.clear();
//            }
//        }
//        System.out.println(count);
//        operations.bulkIndex(queries, index);
//    }
//
//    /**
//     *{
//     * 	"_source":["mmsi","location","time"],
//     * 	"query":{
//     * 		"bool":{
//     * 			"filter":[{
//     * 				"range":{
//     * 					"time":{
//     * 						"gte":"2017-02-05",
//     * 						"lte":"2017-02-08"
//     *                                        }* 				}},{
//     * 					"geo_distance":{
//     * 						"distance": "10km",
//     * 						"distance_type": "plane",
//     * 						"location":{
//     * 							"lat":31.92,
//     * 							"lon":120.855
//     *
//     *                        }
//     *                    }
//     *                }
//     *
//     * 			]
//     * 		}
//     * 	}
//     * }
//     */
//    @Test
//    void test02(){
//        String[] include = {"mmsi","location"};
//        FetchSourceFilter fetchSourceFilter = new FetchSourceFilter(include,null);
//        PageRequest pageRequest = PageRequest.of(0,100);
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
//                .filter(rangeQuery("time").gte("2017-02-05").lte("2017-02-08")) //must。filter,none
//                .filter(geoDistanceQuery("location").distance(70, DistanceUnit.KILOMETERS).point(31.90,120.0).geoDistance(GeoDistance.PLANE)); //must ,filter ,none
//        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
//                .withSourceFilter(fetchSourceFilter)
//                .withPageable(pageRequest);
//        IndexCoordinates index = IndexCoordinates.of("dongtaii");
//        SearchHits<es_dynamic> search = operations.search(nativeSearchQueryBuilder.build(), es_dynamic.class, index);
//        System.out.println(nativeSearchQueryBuilder.build().getQuery().toString());
//        System.out.println(search.getTotalHits());
//        for (int i=0;i<10;i++){
//            System.out.println(search.getSearchHit(i));
//        }
//    }
//}