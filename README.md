# shardingjdbcDemo
* shardingJdbc+mybatis实现分库分表
注意：shardingJdbc只支持等号、between、in维度分片，所以建议在数据中增加一个业务无关的字段来区别分区，是的分区选择尽量走 等号和 in
列如用户表，如果用用户id作为分库分表字段的话 id=X和id in（x,y,z）的查询性能都还可以,id>x的查询就会很难处理处理不好就会有性能问题！
决绝这个问题的思路可以这样使用一个分区字段zone 以前的查询就可以变成id=x ==> id=x and zone=将x按照规则生成 如果是id>x 就可以改写成
id>x and zone in (这里的这可以根据业务和算法生成) 这样避免去走shardingjdbc的between