## 介绍
基于SpringBoot + SpringSecurity + MyBatis构建的一个web应用，采集Arxiv预印本网站下的论文摘要信息到本地Mysql数据库中，利用MyBatis构建持久化层，使用SpringSecurity实现认证和授权操作，通过TF-IDF算法生成对用户传来的检索关键词的论文推荐序列，并反馈给用户。

## 软件包描述
```properties
--- com.snail.arxiv
      |
       --- config
            |
             --- MvcConfig  自定义一些SpringMVC的配置项
             --- MyBatisConfig  自定义MyBatis的一些配置项
             --- WebSecurityConfig  自定义Security的一些配置项
       --- controller
            |
             --- MainController  主控制器
       --- entity
            |
             --- PaperRecord  封装论文记录数据的实体类
       --- exception
            |
             --- PaperRecordsIsNullException  论文记录在数据库中不存在异常
       --- mapper
            |
             --- PaperRecordMapper  MyBatis的映射接口,使用注解来注入sql语句
       --- utils
            |
             --- GeneralKits  通用工具类,注解为一个Sping容器的配置类
             --- MyLocaleResolver  自定义区域解析器,为项目提供国际化功能
             --- PaperId2WordArrayMap  向容器中注入论文对应的单词数组
--- arxiv.sql  构建数据库及相应的数据表的sql可执行文件
--- AtomXmlExample  atom格式的xml文件示例
```

## MainController下主要url访问地址介绍
```properties
/test  测试连接,返回一个json格式的"Hello World!"
/  主页,从数据库中取出最新的记录在主页显示
/search  检索,通过TF-IDF算法得到一个推荐的论文序列,反馈给用户
/rebuild  重新获取Arxiv上最新的论文记录,存放到本地数据库中,若原本没有则创建新记录
/purge  删除数据库上所有的记录
/reindex  重新生成单词索引,当数据发生变化时手动执行
```
其中，/rebuild、/purge、/reindex操作需要管理员权限，项目的认证与授权由Spring Security来提供支持。

## TF-IDF算法介绍
**TF：关键词频率(Term Frequency)**，在搜索引擎工作时，需要将搜索的关键词与相关出现这些关键词的网页对应起来，一般而言，可以将关键词出现次数最多的网页推荐给用户，但是，这样明显存在一个问题，就是篇幅长的网页比篇幅短的网页要占优势的多，因此需要根据网页的长度对关键词的次数进行归一化，用关键词出现的次数除以网页的总字数，这个商就是**关键词频率**。

不同的词预测主题的能力各不相同，比如**原子能的应用**这个词，**应用**是个很通用的词，而**原子能**则是个很专业的词，因而，需要对每一个词赋予一个权重，一个词预测主题的能力越大，则权重越大，反之，权重越小。在信息检索中使用最多的权重是**逆文本频率指数**，简称**IDF**，其公式描述为：

![](http://latex.codecogs.com/gif.latex?log(\frac{D}{D_w}))

D为所有网页的数量，D_w是含有关键词w的网页数量，若关键词“的”在所有网页都出现了，则它的IDF值为0，因而，某个网页与所有被检索的关键词之间相关性可以描述为：

![](http://latex.codecogs.com/gif.latex?TF_1%20\cdot%20IDF_1%20+%20TF_2%20\cdot%20IDF_2%20+%20\cdots%20+%20TF_N%20\cdot%20IDF_N)

在本项目中，将一篇论文的标题和摘要放在一起作为一个网页，用一个字符串数组保存一篇论文的所有单词，以这个数组为基础来计算每个被检索的关键词的TF值，然后通过计算数据库中已知论文总数与关键词对应出现这个关键词的论文数量的商来统计关键词的IDF值。然后可以使用上述网页相关性公式来计算论文对应被检索关键词的相关性。

## Arxiv API 调用接口
Arxiv为程序开发者提供了一套访问其海量的论文数据摘要信息的API调用接口，开发者通过调用这些接口，就可以得到一个Atom格式的xml文档内容，并且，开发者可以指定各种查询条件获取对应的论文，在本项目中，仅对人工智能(cs.AI)领域下最新的论文进行抓取。
1. 查询cs.AI(人工智能)学科类别下的论文，返回100条记录
```html
http://export.arxiv.org/api/query?search_query=cat:cs.AI&start=0&max_results=100
```

2. 查询cs.AI,cs.CL和cs.CV，默认只返回10条记录
```html
http://export.arxiv.org/api/query?search_query=cat:cs.AI+OR+cat:cs.CL+OR+cat:cs.CV
```

3. 查询cs.AI(人工智能)学科类别下最新的论文，返回100条记录
```html
http://export.arxiv.org/api/query?search_query=cat:cs.AI&sortBy=lastUpdatedDate&start=0&max_results=100
```
