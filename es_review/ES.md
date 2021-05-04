# ES小记
---
- [查看所有index](#查看所有index)
- [查看某个索引](#查看某个索引)
- [创建index](#创建index)
- [自定义mapping创建索引](#自定义mapping创建索引)
- [添加、删除别名,可用于索引分组](#添加、删除别名,可用于索引分组)
- [查看别名列表](#查看别名列表)
- [查询某个索引分片状态](#查询某个索引分片状态)
- [删除索引](#删除索引)
- [创建文档](#创建文档：/索引名/类型(type)名/文档id)
- [PUT和POST区别](#PUT和POST区别)
- [查看指定索引，文档Id的内容](#查看指定索引，文档Id的内容)
- [删除指定文档Id内容](#删除指定文档Id内容)
- [更新指定文档ID中指定字段内容](#更新指定文档ID中指定字段内容,doc为固定写法)
- [创建索引模板](#创建索引模板)
- [查看已有索引模板](#查看已有索引模板)
- [查询自定义索引模板的索引数据](#查询自定义索引模板的索引数据)

---

## 查看所有index
    (?v:代表头)

    GET /_cat/indices?v

## 查看某个索引

    GET /test_index
    GET /test_index_1
    GET /test_index_cus

## 创建index

    PUT test_index
    PUT test_index_1

### 自定义mapping创建索引

```json
PUT test_index_cus
{
  "aliases": {
    "test_aliases": {}
  },
  "mappings": {
    "movie": {
      "properties": {
        "id": {
          "type": "long"
        },
        "name": {
          "type": "text",
          "analyzer": "ik_smart"
        },
        "doubanScore": {
          "type": "double"
        },
        "actorList": {
          "properties": {
            "id": {
              "type": "long"
            },
            "name": {
              "type": "keyword"
            }
          }
        }
      }
    }
  }
}
```

### 添加、删除别名,可用于索引分组

```json
POST _aliases
{
  "actions": [
    {
      "add": {
        "index": "test_index_cus",
        "alias": "test_add_alias"
      }
    }
  ]
}
```

```json
POST _aliases
{
  "actions": [
    {
      "remove": {
        "index": "test_index_cus",
        "alias": "test_aliases"
      }
    }
  ]
}
```

### 查看别名列表

    GET _cat/aliases?v

### 查询某个索引分片状态

    GET /_cat/shards/test_index

### 删除索引

    DELETE /test_index_1

## 创建文档：/索引名/类型(type)名/文档id

```json
PUT /test_index/movie/1
{
  "id": 100,
  "name": "operation red sea",
  "doubanScore": 8.5,
  "actorList": [
    {
      "id": 1,
      "name": "zhang yi"
    },
    {
      "id": 2,
      "name": "hai qing"
    },
    {
      "id": 3,
      "name": "zhang han yu"
    }
  ]
}

PUT /test_index/movie/2
{
  "id": 200,
  "name": "operation meigong river",
  "doubanScore": 8,
  "actorList": [
    {
      "id": 3,
      "name": "zhang han yu"
    }
  ]
}

POST /test_index/movie/3
{
  "id": 300,
  "name": "incident red sea",
  "doubanScore": 6.6,
  "actorList": [
    {
      "id": 4,
      "name": "zhang san feng"
    }
  ]
}
```

### PUT和POST区别

    PUT为幂等性操作，执行时候需要指定type名称和文档ID，如果id存在则会替换同ID内容，version+1
    POST则不会，文档ID为可选，如果未指定会自己随机生成文档ID

### 查看指定索引，文档Id的内容
    GET /索引名/类型名/文档 id
    GET /test_index/movie/3
    GET /test_index/_search

### 删除指定文档Id内容
    删除索引会立即释放空间，删除文档则只会标记“删除”，在只从POST /_forcemerge才会释放空间
    DELETE /test_index/movie/3

### 更新指定文档ID中指定字段内容,doc为固定写法

```json
POST /test_index/movie/3/_update?pretty
{
"doc": {
"name": "红海事件"
}
}
```

### 创建索引模板
```json
PUT _template/template_test
{
  "index_patterns": [
    "temp_test*"
  ],
  "settings": {
    "number_of_shards": 1
  },
  "aliases": {
    "{index}-query": {},
    "temp_test-query": {}
  },
  "mappings": {
    "_doc": {
      "properties": {
        "id": {
          "type": "keyword"
        },
        "movie_name": {
          "type": "text",
          "analyzer": "ik_smart"
        }
      }
    }
  }
}
```
### 查看已有索引模板
GET _cat/templates?v

```json
 根据自定义索引模板导入数据，自动创建索引
POST temp_test_add/_doc
{
  "id": "333",
  "name": "zhang3"
}

PUT temp_test_put/_doc/1
{
  "id":1,
  "name":"put"
}

PUT temp_test_add/_doc/1
{
  "id":1,
  "name":"put"
}
```


### 查询自定义索引模板的索引数据
    GET temp_test_add-query/_mapping
    GET temp_test-query/_mapping
    GET temp_test_add/_mapping
    
    GET temp_test_put/_search
    GET temp_test_add/_search
    
    GET temp_test_put-query/_search
    
    查看以temp_test开头索引的全部数据
    GET temp_test-query/_search
