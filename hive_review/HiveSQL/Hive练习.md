- [找出所有科目成绩都大于某一学科平均成绩的学生](#找出所有科目成绩都大于某一学科平均成绩的学生)
- [统计每个月访问量和累计月访问量](#统计每个月访问量和累计月访问量)
- [所有用户中在今年10月份第一次购买商品的金额](#所有用户中在今年10月份第一次购买商品的金额)
- [查询前20%时间的订单信息](#查询前20%时间的订单信息)
- [统计出每个用户的累积访问次数](#统计出每个用户的累积访问次数)
- [TopN](#TopN)
    - [TopN-2](#TopN2)
    - [TopN-3](#TopN3)
- [第一笔订单](#第一笔订单)
    - [第一笔订单2](#第一笔订单2)
- [区间求值](#区间求值)
    - [区间求值2](#区间求值2)
    - [区间匹配](#区间匹配3)
    - [条件匹配](#条件匹配)
- [所有用户和活跃用户](#所有用户和活跃用户)
- [图书馆的故事](#图书馆的故事)
- [窗口函数案例](#窗口函数案例)

---

## 找出所有科目成绩都大于某一学科平均成绩的学生

```sql
CREATE TABLE score_1
(
    uid STRING,
    subject_id STRING,
    score int
) ROW FORMAT delimited FIELDS TERMINATED BY '\t';

INSERT overwrite TABLE score_1
VALUES ("1001", "01", 100),
    ("1001", "02", 100),
    ("1001", "03", 100),
    ("1002", "01", 90),
    ("1002", "02", 70),
    ("1002", "03", 50),
    ("1003", "01", 80),
    ("1003", "02", 60),
    ("1003", "03", 40);

DESC function extended cast;
DESC function extended round; -- 四舍五入保留指定位数小数
DESC function extended ceil; -- 向上取整，123.3 -> 124
DESC function extended floor; -- 向下取整，123.3 -> 123
DESC function extended cast; -- 类型转换，四舍五入，cast('12.2' as double);cast(12.2 as decimal(10, 2))
DESC function extended regexp_extract;
-- 正则匹配截取，不做四舍五入
-- 求出每个科目的平均分
SELECT uid,
       subject_id,
       score,
       CAST(AVG(score) OVER (PARTITION BY subject_id ) AS int) avg_score
FROM score_1;

-- 拿所有分数和平均分比较，大于平均分的标0，否则标1
SELECT uid,
       subject_id,
       score,
       IF(score > avg_score, 0, 1) flag
FROM (SELECT uid,
             subject_id,
             score,
             CAST(AVG(score) OVER (PARTITION BY subject_id ) AS int) avg_score
      FROM score_1) t1;
-- 根据uid进行分组，如果此uid的所有学科分数都大于该学科的平均分，则sum（flag）= 0
SELECT t2.uid
FROM (SELECT uid,
             subject_id,
             score,
             IF(score > avg_score, 0, 1) flag
      FROM (SELECT uid,
                   subject_id,
                   score,
                   CAST(AVG(score) OVER (PARTITION BY subject_id ) AS int) avg_score
            FROM score_1) t1) t2
GROUP BY uid
HAVING SUM(flag) = 0;
```

## 统计每个月访问量和累计月访问量

```sql
CREATE TABLE visit
(
    userId STRING,
    visitDate STRING,
    visitCount INT
) ROW FORMAT delimited FIELDS TERMINATED BY "\t";
INSERT INTO TABLE visit
VALUES ('u01', '2017/1/21', 5),
    ('u02', '2017/1/23', 6),
    ('u03', '2017/1/22', 8),
    ('u04', '2017/1/20', 3),
    ('u01', '2017/1/23', 6),
    ('u01', '2017/2/21', 8),
    ('u02', '2017/1/23', 6),
    ('u01', '2017/2/22', 4);

-- 转换日期格式，根据用户和访问时间统计用户月访问量
SELECT userId,
       DATE_FORMAT(REPLACE(visitDate, '/', '-'), 'yyyy-MM') moth,
       SUM(visitCount)                                      total_month
FROM visit
GROUP BY userId, moth
ORDER BY userId, moth;

-- 开窗，统计用户的累计访问总量
SELECT t1.userId,
       t1.moth,
       t1.total_month,
--        sum(total_month) over(partition by moth) total_visit -- rows between unbounded preceding and unbounded following
       SUM(total_month) OVER (PARTITION BY userId ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW ) total_visit
FROM (SELECT userId,
             DATE_FORMAT(REPLACE(visitDate, '/', '-'), 'yyyy-MM') moth,
             SUM(visitCount)                                      total_month
      FROM visit
      GROUP BY userId, moth
      ORDER BY userId, moth) t1;

-- 开窗，统计累计访问总量
SELECT t1.userId,
       t1.moth,
       t1.total_month,
       SUM(total_month) OVER (ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW ) total_visit
FROM (SELECT userId,
             DATE_FORMAT(REPLACE(visitDate, '/', '-'), 'yyyy-MM') moth,
             SUM(visitCount)                                      total_month
      FROM visit
      GROUP BY userId, moth
      ORDER BY userId, moth) t1;
```

## 所有用户中在今年10月份第一次购买商品的金额

```sql
CREATE TABLE `order`
(
    userid STRING,
    money int,
    paymenttime STRING,
    orderid STRING
) ROW FORMAT delimited FIELDS TERMINATED BY "\t";

INSERT overwrite TABLE `order`
VALUES ('001', 200, '2021-09-01', '121'),
    ('002', 300, '2021-09-01', '122'),
    ('001', 100, '2021-10-01', '123'),
    ('001', 200, '2021-10-02', '124'),
    ('002', 500, '2021-10-01', '125'),
    ('001', 100, '2021-11-01', '126');

-- 筛选出10月份所有订单
SELECT userid,
       money,
       paymenttime,
       orderid
FROM `order`
WHERE DATE_FORMAT(paymenttime, 'yyyy-MM') = DATE_FORMAT(CURRENT_DATE, 'yyyy-10');
-- t1
-- 按用户分组取第一条订单记录
SELECT userid,
       money,
       paymenttime
FROM (SELECT userid,
             money,
             paymenttime,
             orderid,
             RANK() OVER (PARTITION BY userid ORDER BY paymenttime) RANK
      FROM (SELECT userid,
          money,
          paymenttime,
          orderid
          FROM `order`
          WHERE date_format(paymenttime, 'yyyy-MM') = date_format(CURRENT_DATE, 'yyyy-10')) t1) t2
WHERE RANK = 1;

-- TODO 取出按照时间轴顺序发生了状态变化的数据行；
CREATE TABLE shop
(
    id STRING,
    rate STRING,
    rq STRING
);
INSERT INTO shop
VALUES (100, 0.1, "2021-03-02"),
       (100, 0.1, "2021-02-02"),
       (100, 0.2, "2021-03-05"),
       (100, 0.2, "2021-03-06"),
       (100, 0.3, "2021-03-07"),
       (100, 0.1, "2021-03-09"),
       (100, 0.1, "2021-03-10"),
       (100, 0.1, "2021-03-10"),
       (200, 0.1, "2021-03-10"),
       (200, 0.1, "2021-02-02"),
       (200, 0.2, "2021-03-05"),
       (200, 0.2, "2021-03-06"),
       (200, 0.3, "2021-03-07"),
       (200, 0.1, "2021-03-09"),
       (200, 0.1, "2021-03-10"),
       (200, 0.1, "2021-03-10");


SELECT id,
       rate,
       rq,
       lag_test
FROM (SELECT id,
             rate,
             rq,
             LAG(rate, 1, 0) OVER (PARTITION BY id ORDER BY rq) lag_test
      FROM shop
      ORDER BY id) t1
WHERE rate != lag_test;
```

## 查询前20%时间的订单信息

```sql
CREATE TABLE business
(
    name STRING,
    orderdate STRING,
    cost int
) ROW FORMAT delimited FIELDS TERMINATED BY '\t'
;
INSERT overwrite TABLE business
VALUES
    ("jack", "2017-01-01", 10),
    ("tony", "2017-01-02", 15),
    ("jack", "2017-02-03", 23),
    ("tony", "2017-01-04", 29),
    ("jack", "2017-01-05", 46),
    ("jack", "2017-04-06", 42),
    ("tony", "2017-01-07", 50),
    ("jack", "2017-01-08", 55),
    ("mart", "2017-04-08", 62),
    ("mart", "2017-04-09", 68),
    ("neil", "2017-05-10", 12),
    ("mart", "2017-04-11", 75),
    ("neil", "2017-06-12", 80),
    ("mart", "2017-04-13", 94);
SELECT name,
       orderdate,
       cost,
       NTILE(5) OVER (ORDER BY orderdate)
FROM business;
```

> 20%,ntile(5)<p>
NTILE(n)：将每个窗口分区的行划分为n个范围的桶，各个组有编号，编号从 1 开始，

## 统计出每个用户的累积访问次数

    (userId  visitDate   visitCount)==> 用户id 月份 小计 累积

```sql
CREATE TABLE test_sql.test1
(
    userId STRING,
    visitDate STRING,
    visitCount INT
) ROW FORMAT DELIMITED FIELDS TERMINATED BY "\t";
INSERT INTO TABLE test_sql.test1
VALUES ('u01', '2017/1/21', 5),
    ('u02', '2017/1/23', 6),
    ('u03', '2017/1/22', 8),
    ('u04', '2017/1/20', 3),
    ('u01', '2017/1/23', 6),
    ('u01', '2017/2/21', 8),
    ('u02', '2017/1/23', 6),
    ('u01', '2017/2/22', 4);

SELECT t2.userId,
       t2.visit_Month,
       sum_visit subtotal,
       SUM(t2.sum_visit) OVER (PARTITION BY userId ORDER BY visit_Month ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT
           ROW)  total_visit
FROM (SELECT t1.userId,
             t1.visit_Month,
             SUM(visitCount) sum_visit
      FROM (SELECT userId,
                   MONTH(REPLACE(visitDate, '/', '-')) visit_Month,
                   visitCount
            FROM test1
           ) t1
      GROUP BY t1.userId, t1.visit_Month) t2
ORDER BY t2.visit_Month, t2.userId;
```

## TopN

    统计：(1)每个店铺的UV（访客数） (2)每个店铺访问次数top3的访客信息。
    输出店铺名称、访客id、访问次数（表名为Visit，访客的用户id为user_id，被访问的店铺名称为shop, ）

```sql
CREATE TABLE test_sql.test2
(
    user_id STRING,
    shop STRING
) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t';
INSERT INTO TABLE test_sql.test2
VALUES ('u1', 'a'),
    ('u2', 'b'),
    ('u1', 'b'),
    ('u1', 'a'),
    ('u3', 'c'),
    ('u4', 'b'),
    ('u1', 'a'),
    ('u2', 'c'),
    ('u5', 'b'),
    ('u4', 'b'),
    ('u6', 'c'),
    ('u2', 'c'),
    ('u1', 'b'),
    ('u2', 'a'),
    ('u2', 'a'),
    ('u3', 'a'),
    ('u5', 'a'),
    ('u5', 'a'),
    ('u5', 'a');
-- 统计每个店的UV
SELECT shop,
       COUNT(DISTINCT user_id) uv
FROM test2
GROUP BY shop
ORDER BY shop;
-- 每个店访问次数的Top3的用户信息
SELECT t2.shop,
       t2.user_id,
       t2.cnt
FROM (SELECT shop,
             user_id,
             cnt,
             DENSE_RANK() OVER (PARTITION BY shop ORDER BY cnt DESC ) RANK
      FROM (SELECT user_id,
          shop,
          COUNT (*) cnt
          FROM test2
          GROUP BY user_id, shop) t1) t2
WHERE t2.rank <= 3
ORDER BY shop;
```

### TopN2

    查询各自区组的money排名前十的账号（分组取前10）

```sql
CREATE TABLE test_sql.test10
(
    `dist_id` STRING COMMENT '区组id',
    `account` STRING COMMENT '账号',
    `gold` INT COMMENT '金币'
);
INSERT INTO TABLE test_sql.test10
VALUES ('1', '77', 18),
    ('1', '88', 106),
    ('1', '99', 10),
    ('1', '12', 13),
    ('1', '13', 14),
    ('1', '14', 25),
    ('1', '15', 36),
    ('1', '16', 12),
    ('1', '17', 158),
    ('2', '18', 12),
    ('2', '19', 44),
    ('2', '10', 66),
    ('2', '45', 80),
    ('2', '78', 98);

SELECT dist_id, account, gold
FROM (SELECT dist_id,
             account,
             gold,
             ROW_NUMBER() OVER (PARTITION BY dist_id ORDER BY gold
                 DESC ) RANK
      FROM (SELECT dist_id, ACCOUNT, SUM(gold) gold FROM test10 GROUP BY dist_id, ACCOUNT) t1) t2
WHERE RANK <=
      10;
```

### TopN3

    查询充值日志表2019年01月02号每个区组下充值额最大的账号，要求结果： -- 区组id，账号，金额，充值时间

```sql

CREATE TABLE test_sql.test9
(
    dist_id STRING COMMENT '区组id',
    ACCOUNT STRING COMMENT '账号',
    `money` DECIMAL(10, 2) COMMENT '充值金额',
    create_time STRING COMMENT '订单时间'
);

INSERT INTO TABLE test_sql.test9
VALUES ('1', '11', 100006, '2019-01-02 13:00:01'),
    ('1', '22', 110000, '2019-01-02 13:00:02'),
    ('1', '33', 102000, '2019-01-02 13:00:03'),
    ('1', '44', 100300, '2019-01-02 13:00:04'),
    ('1', '55', 100040, '2019-01-02 13:00:05'),
    ('1', '66', 100005, '2019-01-02 13:00:06'),
    ('1', '77', 180000, '2019-01-03 13:00:07'),
    ('1', '88', 106000, '2019-01-02 13:00:08'),
    ('1', '99', 100400, '2019-01-02 13:00:09'),
    ('1', '12', 100030, '2019-01-02 13:00:10'),
    ('1', '13', 100003, '2019-01-02 13:00:20'),
    ('1', '14', 100020, '2019-01-02 13:00:30'),
    ('1', '15', 100500, '2019-01-02 13:00:40'),
    ('1', '16', 106000, '2019-01-02 13:00:50'),
    ('1', '17', 100800, '2019-01-02 13:00:59'),
    ('2', '18', 100800, '2019-01-02 13:00:11'),
    ('2', '19', 100030, '2019-01-02 13:00:12'),
    ('2', '10', 100000, '2019-01-02 13:00:13'),
    ('2', '45', 100010, '2019-01-02 13:00:14'),
    ('2', '78', 100070, '2019-01-02 13:00:15');

WITH TEMP AS
         (SELECT dist_id, account, SUM(`money`) sum_money, create_time
          FROM test_sql.test9
          WHERE DATE_FORMAT(create_time, '
yyyy-MM-dd') = '2019-01-02'
          GROUP BY dist_id, account)
SELECT t1.dist_id, t1.account, t1.sum_money, t1.create_time
FROM (SELECT temp.dist_id,
             temp.account,
             temp.sum_money,
             create_time,
             RANK() OVER (PARTITION BY temp.dist_id ORDER BY temp.sum_money DESC) ranks
      FROM TEMP) t1
WHERE ranks = 1;
```

---

## 第一笔订单

    统计:(1)给出 2017年每个月的订单数、用户数、总成交金额。(2)给出2017年11月的新客数(指在11月才有第一笔订单)
    (表STG.ORDER，有如下字段:Date，Order_id，User_id，amount。数据样例:2017-01-01,10029028,1000003251,33.57)

```sql
CREATE TABLE test_sql.test3
(
    dt STRING,
    order_id STRING,
    user_id STRING,
    amount DECIMAL(10, 2)
) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t';
INSERT INTO TABLE test_sql.test3
VALUES ('2017-11-01', '10029128', '1000003251', 33.57),
    ('2017-01-01', '10029028', '1000003251', 33.57),
    ('2017-01-01', '10029029', '1000003251', 33.57),
    ('2017-01-01', '100290288', '1000003252', 33.57),
    ('2017-02-02', '10029088', '1000003251', 33.57),
    ('2017-02-02', '100290281', '1000003251', 33.57),
    ('2017-02-02', '100290282', '1000003253', 33.57),
    ('2017-11-02', '10290282', '100003253', 234),
    ('2018-11-02', '10290284', '100003243', 234);

-- 2017年每个月的订单数、用户数、总成交金额
SELECT DATE_FORMAT(dt, 'yyyy-MM') Mon,
       COUNT(*)                   order_cnt,
       COUNT(DISTINCT user_id)    user_cnt,
       SUM(amount)                mon_amount
FROM test3
WHERE YEAR(dt) = '2017'
GROUP BY DATE_FORMAT(dt, 'yyyy-MM');

-- 给出2017年11月的新客数(指在11月才有第一笔订单)
SELECT COUNT(user_id) new_user_cnt
FROM test3
GROUP BY user_id
HAVING DATE_FORMAT(MIN(dt), 'yyyy-MM') = '2017-11';
```

### 第一笔订单2

    所有用户中在今年10月份第一次购买商品的金额 
    (表ordertable字段:(购买用户：userid，金额：money，购买时间：paymenttime(格式：2017-10-01)，订单id：orderid)

```sql
CREATE TABLE test_sql.test6
(
    userid STRING,
    money decimal(10, 2),
    paymenttime STRING,
    orderid STRING
);

INSERT INTO TABLE test_sql.test6
VALUES ('001', 120, '2017-10-01', '123'),
    ('001', 100, '2017-10-01', '123'),
    ('001', 200, '2017-10-02', '124'),
    ('002', 500, '2017-10-01', '125'),
    ('001', 100, '2017-11-01', '126');
SELECT userid, paymenttime, SUM(money) mony, orderid
FROM (SELECT userid,
             money,
             paymenttime,
             orderid,
             RANK() OVER (PARTITION BY userid ORDER BY paymenttime) RANK
      FROM test6
      WHERE MONTH (paymenttime)
          = '10') t1
WHERE RANK = 1
GROUP BY userid, paymenttime, orderid;
```

---

## 区间求值

    根据年龄段观看电影的次数进行排序;(用户文件(user_id，name，age), 用户看电影的记录文件(user_id，url))

```sql
CREATE TABLE test_sql.test4user
(
    user_id STRING,
    name STRING,
    age int
);

CREATE TABLE test_sql.test4log
(
    user_id STRING,
    url STRING
);

INSERT INTO TABLE test_sql.test4user
VALUES ('001', 'u1', 10),
    ('002', 'u2', 15),
    ('003', 'u3', 15),
    ('004', 'u4', 20),
    ('005', 'u5', 25),
    ('006', 'u6', 35),
    ('007', 'u7', 40),
    ('008', 'u8', 45),
    ('009', 'u9', 50),
    ('0010', 'u10', 65);
INSERT INTO TABLE test_sql.test4log
VALUES ('001', 'url1'),
    ('002', 'url1'),
    ('003', 'url2'),
    ('004', 'url3'),
    ('005', 'url3'),
    ('006', 'url1'),
    ('007', 'url5'),
    ('008', 'url7'),
    ('009', 'url5'),
    ('0010', 'url1');

SELECT t4u.age_phase,
       SUM(t4l.visit_cnt) view_cnt
FROM (SELECT user_id,
             COUNT(*) visit_cnt
      FROM test4log
      GROUP BY user_id) t4l
         JOIN
     (SELECT user_id,
             CASE
                 WHEN age <= 10 THEN '0-10'
                 WHEN age > 10 AND age <= 20 THEN '10-20'
                 WHEN age > 20 AND age <= 30 THEN '20-30'
                 WHEN age > 30 AND age <= 40 THEN '30-40'
                 WHEN age > 40 AND age <= 50 THEN '40-50'
                 WHEN age > 50 AND age <= 60 THEN '50-60'
                 ELSE '70以上'
                 END AS age_phase
      FROM test4user) t4u
     ON t4l.user_id = t4u.user_id
GROUP BY t4u.age_phase
ORDER BY view_cnt DESC;
```

### 区间求值2

    求11月9号下午14点（14-15点），访问/api/user/login接口的top10的ip地址(时间 2016-11-09 14:22:05 接口 /api/user/login ip地址 110.23.5.33)

```sql

CREATE TABLE test_sql.test8
(
    `date` STRING,
    interface STRING,
    ip STRING
);

INSERT INTO TABLE test_sql.test8
VALUES ('2016-11-09 11:22:05', '/api/user/login', '110.23.5.23'),
    ('2016-11-09 11:23:10', '/api/user/detail', '57.3.2.16'),
    ('2016-11-09 23:59:40', '/api/user/login', '200.6.5.166'),
    ('2016-11-09 11:14:23', '/api/user/login', '136.79.47.70'),
    ('2016-11-09 11:15:23', '/api/user/detail', '94.144.143.141'),
    ('2016-11-09 11:16:23', '/api/user/login', '197.161.8.206'),
    ('2016-11-09 12:14:23', '/api/user/detail', '240.227.107.145'),
    ('2016-11-09 13:14:23', '/api/user/login', '79.130.122.205'),
    ('2016-11-09 14:14:23', '/api/user/detail', '65.228.251.189'),
    ('2016-11-09 14:15:23', '/api/user/detail', '245.23.122.44'),
    ('2016-11-09 14:17:23', '/api/user/detail', '22.74.142.137'),
    ('2016-11-09 14:19:23', '/api/user/detail', '54.93.212.87'),
    ('2016-11-09 14:20:23', '/api/user/detail', '218.15.167.248'),
    ('2016-11-09 14:24:23', '/api/user/detail', '20.117.19.75'),
    ('2016-11-09 15:14:23', '/api/user/login', '183.162.66.97'),
    ('2016-11-09 16:14:23', '/api/user/login', '108.181.245.147'),
    ('2016-11-09 14:17:23', '/api/user/login', '22.74.142.137'),
    ('2016-11-09 14:19:23', '/api/user/login', '22.74.142.137');

SELECT ip, COUNT(*) AS cnt
FROM test_sql.test8
WHERE DATE_FORMAT(`date`, 'yyyy-MM-dd HH') >= '2016-11-09 14'
  AND DATE_FORMAT(`date`, 'yyyy-MM-dd HH') < '2016-11-09 15'
  AND interface = '/api/user/login'
GROUP BY ip
ORDER BY cnt DESC
LIMIT 10;
```

---

## 所有用户和活跃用户

    有日志(日期 用户 年龄),求所有用户和活跃用户的总数及平均年龄。（活跃用户指连续两天都有访问记录的用户）

```sql
CREATE TABLE test5
(
    dt STRING,
    user_id STRING,
    age int
) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',';
INSERT INTO TABLE test_sql.test5
VALUES ('2019-02-11', 'test_1', 23),
    ('2019-02-11', 'test_2', 19),
    ('2019-02-11', 'test_3', 39),
    ('2019-02-11', 'test_1', 23),
    ('2019-02-11', 'test_3', 39),
    ('2019-02-11', 'test_1', 23),
    ('2019-02-12', 'test_2', 19),
    ('2019-02-13', 'test_1', 23),
    ('2019-02-15', 'test_2', 19),
    ('2019-02-16', 'test_2', 19);

-- 求活跃用户信息
SELECT COUNT(*)                                       activ_user_cnt,
       CAST(SUM(t4.age) / COUNT(*) AS decimal(10, 2)) activ_user_avg_age
FROM (SELECT t3.user_id,
             t3.age
      FROM (SELECT t2.user_id,
                   MIN(t2.age) age
            FROM (SELECT t1.user_id,
                         t1.age,
                         DATE_SUB(dt, RANK) flag
                  FROM (SELECT dt,
                               user_id,
                               MIN(age) age,
                               ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY dt) RANK
                        FROM test5
                        GROUP BY dt, user_id) t1
                 ) t2
            GROUP BY t2.flag, t2.user_id
            HAVING COUNT(*) >= 2
           ) t3
      GROUP BY t3.user_id, t3.age) t4;
--求所有用户信息
SELECT COUNT(*)                                    all_user_cnt,
       CAST(SUM(age) / COUNT(*) AS decimal(10, 2)) all_user_avg_age
FROM (SELECT user_id,
             MIN(age) age
      FROM test5
      GROUP BY user_id) t5;
-- 拼接
SELECT SUM(total_user_cnt)      total_user_cnt,
       SUM(totoal_user_avg_age) totoal_user_avg_age,
       SUM(activ_user_cnt)      activ_user_cnt,
       SUM(activ_user_avg_age)  activ_user_avg_age
FROM (SELECT 0                                              total_user_cnt,
             0                                              totoal_user_avg_age,
             COUNT(*)                                       activ_user_cnt,
             CAST(SUM(t4.age) / COUNT(*) AS decimal(10, 2)) activ_user_avg_age
      FROM (SELECT t3.user_id,
                   t3.age
            FROM (SELECT t2.user_id,
                         MIN(t2.age) age
                  FROM (SELECT t1.user_id,
                               t1.age,
                               DATE_SUB(dt, RANK) flag
                        FROM (SELECT dt,
                                     user_id,
                                     MIN(age) age,
                                     ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY dt) RANK
                              FROM test5
                              GROUP BY dt, user_id) t1
                       ) t2
                  GROUP BY t2.flag, t2.user_id
                  HAVING COUNT(*) >= 2
                 ) t3
            GROUP BY t3.user_id, t3.age) t4
      UNION ALL
      SELECT COUNT(*)                                    total_user_cnt,
             CAST(SUM(age) / COUNT(*) AS decimal(10, 2)) totoal_user_avg_age,
             0,
             0
      FROM (SELECT user_id,
                   MIN(age) age
            FROM test5
            GROUP BY user_id) t5) t6;
```

## 图书馆的故事

```text
现有图书管理数据库的三个数据模型如下：
图书（数据表名：BOOK）
	序号  	字段名称    字段描述    字段类型
	1   	BOOK_ID 	总编号 		文本
	2   	SORT    	分类号 		文本
	3  	 	BOOK_NAME   书名  		文本
	4   	WRITER  	作者  		文本
	5   	OUTPUT  	出版单位    文本
	6   	PRICE   	单价  		数值（保留小数点后2位）
读者（数据表名：READER）
	序号  	字段名称    字段描述    字段类型
	1   	READER_ID   借书证号    文本
	2   	COMPANY 	单位  		文本
	3   	NAME    	姓名  		文本
	4   	SEX 		性别  		文本
	5   	GRADE   	职称  		文本
	6   	ADDR    	地址  		文本
借阅记录（数据表名：BORROW LOG）
	序号  	字段名称    	字段描述    字段类型
	1   	READER_ID   	借书证号    文本
	2   	BOOK_ID  		总编号 		文本
	3   	BORROW_DATE  	借书日期    日期
（1）创建图书管理库的图书、读者和借阅三个基本表的表结构。请写出建表语句。
（2）找出姓李的读者姓名（NAME）和所在单位（COMPANY）。
（3）查找“高等教育出版社”的所有图书名称（BOOK_NAME）及单价（PRICE），结果按单价降序排序。
（4）查找价格介于10元和20元之间的图书种类(SORT）出版单位（OUTPUT）和单价（PRICE），结果按出版单位（OUTPUT）和单价（PRICE）升序排序。
（5）查找所有借了书的读者的姓名（NAME）及所在单位（COMPANY）。
（6）求”科学出版社”图书的最高单价、最低单价、平均单价。
（7）找出当前至少借阅了2本图书（大于等于2本）的读者姓名及其所在单位。
（8）考虑到数据安全的需要，需定时将“借阅记录”中数据进行备份，请使用一条SQL语句，在备份用户bak下创建与“借阅记录”表结构完全一致的数据表BORROW_LOG_BAK.井且将“借阅记录”中现有数据全部复制到BORROW_L0G_ BAK中。
（9）现在需要将原Oracle数据库中数据迁移至Hive仓库，请写出“图书”在Hive中的建表语句（Hive实现，提示：列分隔符|；数据表数据需要外部导入：分区分别以month＿part、day＿part 命名）
（10）Hive中有表A，现在需要将表A的月分区　201505　中　user＿id为20000的user＿dinner字段更新为bonc8920，其他用户user＿dinner字段数据不变，请列出更新的方法步骤。（Hive实现，提示：Hive中无update语法，请通过其他办法进行数据更新）
```

```sql
-- 创建图书表book

CREATE TABLE test_sql.book
(
    book_id STRING,
    `SORT` STRING,
    book_name STRING,
    writer STRING,
    OUTPUT STRING,
    price decimal(10, 2)
);
INSERT INTO TABLE test_sql.book
VALUES ('001', 'TP391', '信息处理', 'author1', '机械工业出版社', '20'),
    ('002', 'TP392', '数据库', 'author12', '科学出版社', '15'),
    ('003', 'TP393', '计算机网络', 'author3', '机械工业出版社', '29'),
    ('004', 'TP399', '微机原理', 'author4', '科学出版社', '39'),
    ('005', 'C931', '管理信息系统', 'author5', '机械工业出版社', '40'),
    ('006', 'C932', '运筹学', 'author6', '科学出版社', '55');

-- 创建读者表reader

CREATE TABLE test_sql.reader
(
    reader_id STRING,
    company STRING,
    name STRING,
    sex STRING,
    grade STRING,
    addr STRING
);
INSERT INTO TABLE test_sql.reader
VALUES ('0007', '搜狐', '李梅', '女', 'ceo', 'addr6'),
    ('0001', '阿里巴巴', 'jack', '男', 'vp', 'addr1'),
    ('0002', '百度', 'robin', '男', 'vp', 'addr2'),
    ('0003', '腾讯', 'tony', '男', 'vp', 'addr3'),
    ('0004', '京东', 'jasper', '男', 'cfo', 'addr4'),
    ('0005', '网易', 'zhangsan', '女', 'ceo', 'addr5'),
    ('0006', '搜狐', 'lisi', '女', 'ceo', 'addr6');

-- 创建借阅记录表borrow_log

CREATE TABLE test_sql.borrow_log
(
    reader_id STRING,
    book_id STRING,
    borrow_date STRING
);

INSERT INTO TABLE test_sql.borrow_log
VALUES ('0001', '002', '2019-10-14'),
    ('0002', '001', '2019-10-13'),
    ('0003', '005', '2019-09-14'),
    ('0004', '006', '2019-08-15'),
    ('0005', '003', '2019-10-10'),
    ('0006', '004', '2019-17-13');
SELECT name, company
FROM reader
WHERE name LIKE '李%';

SELECT book_name, price
FROM test_sql.book
WHERE OUTPUT = "高等教育出版社"
ORDER BY price DESC; -- (4)
SELECT sort, output, price
FROM test_sql.book
WHERE price >= 10
  AND price <= 20
ORDER BY output, price; -- (5)
SELECT b.name, b.company
FROM test_sql.borrow_log a
         JOIN test_sql.reader b ON a.reader_id = b.reader_id; -- (6)
SELECT MAX(price), MIN(price), AVG(price)
FROM test_sql.book
WHERE OUTPUT = '科学出版社'; -- (7)
SELECT b.name, b.company
FROM (SELECT reader_id FROM test_sql.borrow_log GROUP BY reader_id HAVING COUNT(*) >= 2) a
         JOIN
     test_sql.reader b ON a.reader_id = b.reader_id;

-- (8)
CREATE TABLE test_sql.borrow_log_bak AS
SELECT *
FROM test_sql.borrow_log; -- (9)
CREATE TABLE book_hive
(
    book_id STRING,
    SORT STRING,
    book_name STRING,
    writer STRING,
    OUTPUT STRING,
    price DECIMAL(10, 2)
) PARTITIONED BY ( month_part STRING, day_part STRING )
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\|' STORED AS TEXTFILE; -- (10)
--方式1：配置hive支持事务操作，分桶表，orc存储格式 --方式2：第一步找到要更新的数据，将要更改的字段替换为新的值，第二步找到不需要更新的数据，第三步将上两步的数据插入一张新表中。
```

---

### 条件匹配

    需求：根据聚合在一起的编码转换成聚合在一起的码值
    一个字段是   “1,2,3,4” -->  “原因1  原因2 原因3  原因4”

```sql
CREATE TABLE wangyou1
(
    codeStr STRING
);

INSERT OVERWRITE TABLE wangyou1
VALUES ("1,2,3,4"),
    ("1,2"),
    ("2,3"),
    ("2,3,4");

WITH t1 AS (
    (SELECT codeStr,
            code_id
     FROM wangyou1 LATERAL VIEW EXPLODE(SPLIT(codestr, ',')) tmp AS code_id)
)
SELECT t2.codeStr,
       CONCAT_WS(',', COLLECT_LIST(t2.code_value)) code_value
FROM (SELECT t1.codeStr,
             CASE t1.code_id
                 WHEN '1' THEN '原因1'
                 WHEN '2' THEN '原因2'
                 WHEN '3' THEN '原因3'
                 WHEN '4' THEN '原因4'
                 END AS code_value
      FROM t1) t2
GROUP BY t2.codeStr;

SELECT t2.codeStr,
       CONCAT_WS(',', collect_list(code_value)) code_value
FROM (SELECT t1.codeStr,
             MAP('1', '原因1', '2', '原因2', '3', '原因3', '4', '原因4')[code_id] AS code_value
      FROM (SELECT codeStr,
                   code_id
            FROM wangyou1 LATERAL VIEW EXPLODE(SPLIT(codestr, ',')) tmp AS code_id) t1) t2
GROUP BY t2.codeStr;


SELECT map("1", "原因1", "2", "原因2", "3", "原因3", "4", "原因4")

SELECT str_to_map('1:"原因1", 1:"原因2"')[1]
```

### 区间匹配3
```sql
-- 统计各年龄段统计人数[0,10),[10,20)...
CREATE TABLE age_interval
(
    age int
);

INSERT INTO age_interval
VALUES (1),
       (2),
       (10),
       (11),
       (20),
       (21),
       (30),
       (31),
       (40),
       (51);

SELECT CONCAT('[', age_interval, '-', `intercal`, ')') age_inter,
       num
FROM (SELECT age_interval,
             num,
             LEAD(age_interval, 1, age_interval) OVER (ORDER BY age_interval) `intercal`
      FROM (SELECT FLOOR(age / 10) * 10 AS age_interval,
                   COUNT(*)                num
            FROM age_interval
            GROUP BY FLOOR(age / 10)) t1) t2

```

---

## 窗口函数案例

    窗口函数基本语法:操作函数（窗口、聚合、排序等）+窗口函数基本内容【 over + partition分区+排序+窗口内区域】
    1.LEAD(字段,位移数,默认值) ：向下位移N行取值
    2.LAG(字段,位移数,默认值) ：向上位移N行取值
    3.FIRST_VALUE：当前分组第一个值
    4.LAST_VALUE：当前分组最后一个值

    1.ROW_NUMBER：从1开始，按照顺序编号
    2.RANK：生成排名，相同得分排名相同，并留空位。
    3.DENSE_RANK：生成排名，相同得分排名相同，并不留空位。
    4.CUME_DIST：小于等于当前值的行数/分组内总行数
    5.PERCENT_RANK：分组内当前排名占总排名的百分比
    6.NTILE：分桶，将分组内的数据均匀分N桶
    7.PERCENT_RANK:计算给定行的百分比排名。可以用来计算超过了百分之多少的人。如360小助手开机速度超过了百分之多少的人。

---

    统计每个业务员的每个月的业绩，包括当月业绩、当年最大单月业绩、当年累计业绩

```sql
CREATE TABLE sale_record
(
    order_id STRING COMMENT '订单id',
    shop_id STRING COMMENT '商家id',
    sale_user STRING COMMENT '业务员',
    goods_id STRING COMMENT '商品id',
    dt STRING COMMENT '日期',
    amount int COMMENT '商品金额'
);

INSERT INTO sale_record
VALUES (101, '零食店', '老王', 10001, '2020-01-01', 10),
       (102, '零食店', '老王', 10001, '2020-02-01', 40),
       (103, '零食店', '老王', 10001, '2020-03-01', 20),
       (104, '零食店', '老王', 10002, '2020-04-01', 110),
       (105, '零食店', '老王', 10002, '2020-05-01', 150),
       (106, '烟酒店', '老王', 10002, '2020-06-01', 50),
       (107, '零食店', '老二', 10001, '2020-07-01', 60),
       (108, '零食店', '老二', 10001, '2020-08-01', 10),
       (109, '烟酒店', '老二', 10003, '2020-09-01', 110),
       (110, '烟酒店', '老王', 10003, '2020-01-01', 170),
       (111, '烟酒店', '老张', 10001, '2020-02-01', 140),
       (112, '零食店', '老张', 10004, '2020-03-01', 190),
       (113, '零食店', '老王', 10004, '2020-04-01', 101);

SELECT t1.dt_month,
       t1.sale_user,
       t1.mon_amount,
       SUM(mon_amount) OVER (PARTITION BY sale_user) total_amount,
       MAX(mon_amount) OVER (PARTITION BY sale_user) max_mon_amount
FROM (SELECT sale_user,
             DATE_FORMAT(dt, 'yyyy-MM') dt_month,
             SUM(amount)                mon_amount
      FROM sale_record
      GROUP BY sale_user, DATE_FORMAT(dt, 'yyyy-MM')) t1
ORDER BY dt_month;

SELECT dt_month,
       sale_user,
       mon_amount,
--        下个月业绩
       LEAD(mon_amount, 1, -99999) OVER (PARTITION BY sale_user ORDER BY dt_month) next_amount,
--        上个月业绩
       LAG(mon_amount, 1, -99999) OVER (PARTITION BY sale_user ORDER BY dt_month)  last_amount
FROM (SELECT sale_user,
             DATE_FORMAT(dt, 'yyyy-MM') dt_month,
             SUM(amount)                mon_amount
      FROM sale_record
      GROUP BY sale_user, DATE_FORMAT(dt, 'yyyy-MM')) t1;

SELECT dt_month,
       sale_user,
       mon_amount,
       ROW_NUMBER() OVER (PARTITION BY sale_user ORDER BY mon_amount DESC ) ROW_NUMBER,
RANK() OVER (PARTITION BY sale_user ORDER BY mon_amount DESC)        RANK,
DENSE_RANK() OVER (PARTITION BY sale_user ORDER BY mon_amount DESC)  DENSE_RANK,
--        CUME_DIST：小于等于当前值的行数/分组内总行数
CUME_DIST() OVER (PARTITION BY sale_user ORDER BY mon_amount DESC)   CUME_DIST
FROM (SELECT sale_user,
    DATE_FORMAT(dt, 'yyyy-MM') dt_month,
    SUM(amount) mon_amount
    FROM sale_record
    GROUP BY sale_user, DATE_FORMAT(dt, 'yyyy-MM')) t1
```


