# spring-boot-vue
vue-learn-ol的后端

前段使用vuecli搭建，地址https://github.com/glory6ms/vue-learn-ol.git

mysql--jpa

elasticsearch -- springData ElasticSearch 版本7.60

es学习--目前不记录es的概念和设计思路，只记录理解的内容

配置：es的版本需要与springData ElasticSearch相对应，spirng boot内设置连接参数

	 spring: 
	  elasticsearch:  
	    rest: 
	      uris: [http://localhost:9200,http://localhost:9201,http://localhost:9202] 
然后建立bean与repository extends ElasticsearchRepository。但是这个接口只提供了储存索引和基本的search接口，开发中与es的原生语法有一些差异。

所以推荐使用ElasticsearchOperations而不是repository，通过自动注入的方法初始化之后结合实体类可以完成es官方文档列出的所有功能.	

	@Autowired  
	    private ElasticsearchOperations operations;  

1.查询表达式
查询trajectory索引中船舶航行状态字段中带有机和动这些字段的记录。。省略了ip，实践中需要加上

	GET trajectory/_search
	{
	    "query": {
		"match": {
		    "shipState": "机动"
		}
	    }
	}

java:

	MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("shipState","机动"); //创建查询条件这里将java与DSl对着看 matchQuery==》match，里面的参数由k:v改为k,v  
	NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(matchQueryBuilder); //创建查询体 withQuery==>query  
	IndexCoordinates index = IndexCoordinates.of("trajectory"); // trajectory索引 
	SearchHits<es_dynamic> search = operations.search(nativeSearchQueryBuilder.build(), es_dynamic.class, index); // 发出请求 
// search.getSearchHits().get(i).getContent就可以得到一个文档了 

2.显示输出与排序的实现 

  	String[] include = {"mmsi","location","landCourse","landSpeed","time"}; //输出中打印的字段  
  	String exclude = null; //输出中排除的字段 
  	FetchSourceFilter fetchSourceFilter = new FetchSourceFilter(include,exclude); //过滤  
  	PageRequest pageRequest = PageRequest.of(0,1000); //分页，参数(页面，页面大小)  
  	// 实现 new NativeSearchQueryBuilder().withQuery(boolQueryBuilder).withSourceFilter(fetchSourceFilter).withPageable(pageRequest); 

3.查询与过滤

通常的规则是，使用查询（query）语句来进行 全文 搜索或者其它任何需要影响 相关性得分 的搜索。除此以外的情况都使用过滤（filters)。
查询trajectory中时间范围在[]内的文档

	GET trajectory/_search
	  {
	   "query": {
		"bool": {
		    "filter": {
			"range": {
			    "time": {
				"gte": "2017-10-20",
				"lte": "2017-11-10"
			    }
			}
		    }
		}
	    }
	｝

java:

	BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
			  .filter(rangeQuery("time").gte(timein).lte(timeout));//创建查询条件,查询体与上文类似，不再重复
4.组合查询

boolquery可以实现多条件组合查询,理解为sql里面的连接查询

    - must == AND;  
    - must_not == NOT;  
    - should ==OR;  
查询trajectory在时间范围内并且在以一个点为圆域范围内的文档

	"query":{
		"bool":{
			"filter":{
				"bool":{
					"must":[
						{
						"geo_distance":{
							"distance": "0.4km",
							"distance_type": "plane",
							"location":{
								"lat":31.9164,
								"lon":120.8769
								}
							}
						},
						{
							"range" : {
								"time" : {
									"from" : "2017-02-06",
									"to" : "2017-02-07",
									"include_lower" : true,
									"include_upper" : true,
									"boost" : 1.0
								}
								}
						}
					]
				}
					
				}
		}
	}
  
java:

	BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(boolQuery().must(geoDistanceQuery("location").distance(lineLength, DistanceUnit.KILOMETERS).point(lat, lng).geoDistance(GeoDistance.PLANE))
                .must(rangeQuery("time").gte(timein).lte(timeout)));
                
	将Java与DSL相对应看查询条件最外层bool==>boolQuery(),然后过滤，因为有两个条件，所以需要组合就再加上一个bool==>boolQuery()，AND==》must连接两个条件，  
	geo_distance==>geoDistanceQuery()，内部参数照着写，可能有些方法名不完全一样，但是看简介应该也能成功找到对应的方法.rangeQuery与其是AND的关系，所以再加上一个must()，lte(),gte()地位一样

5.地理位置

  官方文档讲得比较详细，如何初始化以及四种过滤方法。唯一指定的坑就是geo_point结构里面是经度在前纬度在后，查询体和Java里面是lat,lon，不然会出现geo_point类型的点无法初始化的问题。

6.聚合
  聚合官方文档里也很详细，实现起来也不难，比较难的就是结果集的获取，单步调试了很多次才得到统计结果，聚合得到的seach里面是个k:v嵌套怪,java里面部分字段还获取不到 
例子，桶聚合里面统计一个字段的值落在等距区间内的个数  

	AbstractAggregationBuilder aggregationBuilder= AggregationBuilders.histogram("histogram_speed").field("landSpeed").interval(2.0).minDocCount(1);  
		// 两个参数分别是要显示的和不显示的   
	NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder()    
			.addAggregation(aggregationBuilder);    
	SearchHits<dynamic> search = operations.search(nativeSearchQueryBuilder.build(), dynamic.class, index);   
	Aggregations aggregations = search.getAggregations();   
	Map<String, Aggregation> asMap = aggregations.asMap();    
	for (String s:asMap.keySet()){    
	      ParsedHistogram ag=aggregations.get(s);   
	      List<? extends Histogram.Bucket> buckets = ag.getBuckets();   
	      for (Histogram.Bucket b:buckets){   
		  System.out.println("key is "+ b.getKeyAsString()+"---and value is "+ b.getDocCount());  
		  }    
		map.put("speed",buckets);   
	      }   
//buckets  k:v ==>区间:个数     


                

