package com.example.config.shardingjdbc;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.alibaba.druid.pool.DruidDataSource;
import com.dangdang.ddframe.rdb.sharding.api.ShardingDataSourceFactory;
import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.SingleKeyDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.SingleKeyTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.example.entity.DataSoruceProperties;
import com.example.util.EnvironmentUtil;
import com.google.common.collect.Range;
import com.mysql.jdbc.Driver;

@Configuration
public class ShardingJDBCConfig implements EnvironmentAware{
	private List<DataSoruceProperties> dsps;
	@Bean
	public DataSource shardingDataSource() {
		//设置分库映射
        Map<String, DataSource> dataSourceMap = new HashMap<>(2);
        //添加两个数据库ds_0,ds_1到map里
        for (DataSoruceProperties p : dsps) {
        		dataSourceMap.put(p.getDatasourceName(), createDataSource(p));
		}
        //设置默认db为ds_0，也就是为那些没有配置分库分表策略的指定的默认库
        //如果只有一个库，也就是不需要分库的话，map里只放一个映射就行了，只有一个库时不需要指定默认库，但2个及以上时必须指定默认库，否则那些没有配置策略的表将无法操作数据
        DataSourceRule dataSourceRule = new DataSourceRule(dataSourceMap, dsps.get(0).getDatasourceName());
       
        //设置分表映射，将t_0和t_1两个实际的表映射到t逻辑表
        //0和1两个表是真实的表，t是个虚拟不存在的表，只是供使用。如查询所有数据就是select * from t就能查完0和1表的
        TableRule orderTableRule = TableRule.builder("t")
                .actualTables(Arrays.asList("t_0", "t_1"))
                .dataSourceRule(dataSourceRule)
                .build();
        //具体分库分表策略，按什么规则来分
        ShardingRule shardingRule = ShardingRule.builder()
                .dataSourceRule(dataSourceRule)
                .tableRules(Arrays.asList(orderTableRule))
                .databaseShardingStrategy(new DatabaseShardingStrategy("id", new SingleKeyDatabaseShardingAlgorithm<Long>() {

					@Override
					public String doEqualSharding(Collection<String> availableTargetNames,
							ShardingValue<Long> shardingValue) {
						return "DEFAULT";
					}

					@Override
					public Collection<String> doInSharding(Collection<String> availableTargetNames,
							ShardingValue<Long> shardingValue) {
						Collection<String> result = new LinkedHashSet<>(availableTargetNames.size());
						result.add("DEFAULT");
						return result;
					}

					@Override
					public Collection<String> doBetweenSharding(Collection<String> availableTargetNames,
							ShardingValue<Long> shardingValue) {
						 Collection<String> result = new LinkedHashSet<>(availableTargetNames.size());
						 result.add("DEFAULT");
					     return result;
					}
				}))
                .tableShardingStrategy(new TableShardingStrategy("id", new SingleKeyTableShardingAlgorithm<Long>() {

					@Override
					public String doEqualSharding(Collection<String> availableTargetNames,
							ShardingValue<Long> shardingValue) {
						for (String dsname : availableTargetNames) {
							Long index=shardingValue.getValue()%2;
							if(dsname.equals("t_"+index)) {
								return dsname;
							}
						}
						throw new IllegalArgumentException();
					}

					@Override
					public Collection<String> doInSharding(Collection<String> availableTargetNames,
							ShardingValue<Long> shardingValue) {
						Collection<String> result = new LinkedHashSet<>(availableTargetNames.size());
						for (String dsname : availableTargetNames) {
							for (Long x : shardingValue.getValues()) {
								Long index=x%2;
								if(dsname.equals("ds_"+index)) {
									result.add( dsname);
								}
							}
						}
						return result;
					}

					@Override
					public Collection<String> doBetweenSharding(Collection<String> availableTargetNames,
							ShardingValue<Long> shardingValue) {
						Collection<String> result = new LinkedHashSet<>(availableTargetNames.size());
				        Range<Long> range = shardingValue.getValueRange();
				        for (Long i = range.lowerEndpoint(); i <= range.upperEndpoint(); i++) {
				            for (String each : availableTargetNames) {
				                if (each.endsWith(i % 2 + "")) {
				                    result.add(each);
				                }
				            }
				        }
				        return result;
					}
				})).build();
		DataSource ds=ShardingDataSourceFactory.createDataSource(shardingRule);
		return ds;
	}
	
	private static DataSource createDataSource(DataSoruceProperties p) {
        //使用druid连接数据库
        DruidDataSource result = new DruidDataSource();
        result.setDriverClassName(Driver.class.getName());
        result.setUrl(p.getUrl());
        result.setUsername(p.getUsername());
        result.setPassword(p.getPassword());
        return result;
    }
	
	@Override
	public void setEnvironment(Environment evn) {
		dsps=EnvironmentUtil.getList(evn, "spring.datasources", DataSoruceProperties.class);
		
	}

}
