package pro.gmall.realtime.publisher.service.impl;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.support.ValueType;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;
import pro.gmall.realtime.publisher.service.ESService;
import pro.gmall.realtime.publisher.utils.MyESUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author DangHao
 */
@Service
public class ESServiceImpl implements ESService {

	/**
	 * GET /gmall_dau_info_2020-10-24-query/_search <br>
	 * {<br>
	 * "query": {<br>
	 * "match_all": {}<br>
	 * }<br>
	 * }
	 *
	 * @param date 日期
	 * @return
	 */
	@Override
	public Long getDauTotal(String date) {
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		JestClient jestClient = new MyESUtil().getJestClient();
		sourceBuilder.query(new MatchAllQueryBuilder());

		String query = sourceBuilder.toString();
		String indexName = "gmall_dau_info_" + date + "-query";

		Search search = new Search.Builder(query).addIndex(indexName).build();

		Long total = 0L;
		try {
			SearchResult result = jestClient.execute(search);
			total = result.getTotal();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("ES查询失败");
		} finally {
			new MyESUtil().closeJestClient(jestClient);
		}

		return total;
	}

	@Override
	public Map<String, Long> getDauHour(String date) {
		HashMap<String, Long> hourMap = new HashMap<>(16);
		JestClient jestClient = new MyESUtil().getJestClient();

		String aggName = "groupBy_hr";
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		TermsAggregationBuilder termsAggregationBuilder = new TermsAggregationBuilder(aggName, ValueType.LONG).field("hr").size(10);

		sourceBuilder.aggregation(termsAggregationBuilder);

		String query = sourceBuilder.toString();
		String indexName = "gmall_dau_info_"+ date + "-query" ;
		Search search = new Search.Builder(query).addIndex(indexName).build();

		try {
			SearchResult result = jestClient.execute(search);
			TermsAggregation termsAgg = result.getAggregations().getTermsAggregation(aggName);

			if (termsAgg != null) {
				List<TermsAggregation.Entry> buckets = termsAgg.getBuckets();
				for (TermsAggregation.Entry bucket : buckets) {
					hourMap.put(bucket.getKey(), bucket.getCount());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("ES查询异常");
		} finally {
			new MyESUtil().closeJestClient(jestClient);
		}
		return hourMap;
	}

	public static void main(String[] args) {
		ESServiceImpl esService = new ESServiceImpl();

		Long dauTotal = esService.getDauTotal("2021-05-12");
		System.out.println("esService.getDauHour(\"2021-05-12\") = " + esService.getDauHour("2021-05-12"));
//		System.out.println("dauTotal = " + dauTotal);
	}
}
