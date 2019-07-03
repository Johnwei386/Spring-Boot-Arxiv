package com.snail.arxiv;

import com.snail.arxiv.entity.PaperRecord;
import com.snail.arxiv.mapper.PaperRecordMapper;
import org.dom4j.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ArxivCollectionApplicationTests {

	@Autowired
	private String rawAtomXmlData;

	@Autowired
	PaperRecordMapper paperRecordMapper;

	@Autowired
	Map<String, String []> paperId2WordArray;

	@Autowired
	Map<String, Set<String>> word2IndexDictionary;

	@Test
	public void contextLoads() {
		Set<String> result = new HashSet<String>();
		Set<String> set1 = new HashSet<String>() {
			{
				add("王者荣耀");
				add("英雄联盟");
				add("穿越火线");
				add("地下城与勇士");
			}
		};

		Set<String> set2 = new HashSet<String>() {
			{
				add("王者荣耀");
				add("地下城与勇士");
				add("魔兽世界");
			}
		};

		result.clear();
		result.addAll(set1);
		result.retainAll(set2);
		System.out.println("交集：" + result);
	}

	@Test
	public void dom4jXmlTest() throws DocumentException {
		Document document = DocumentHelper.parseText(rawAtomXmlData);
		Element root = document.getRootElement();
		System.out.println("根元素: " + root.getName());

		// 得到根元素下所有名称为entry的子元素
//		for (Iterator it = root.elementIterator("entry"); it.hasNext();){
//			Element element = (Element) it.next();
//			System.out.println(element.getName() + ":" +element.getText());
//		}

		// 遍历所有根元素的属性
//		for (Iterator it = root.attributeIterator(); it.hasNext();){
//			Attribute attribute = (Attribute) it.next();
//			System.out.println(attribute.getName() + ":" + attribute.getValue());
//		}

		int count = 1;
		for (Iterator en = root.elementIterator("entry"); en.hasNext();){
			Element entry = (Element) en.next();
			String author = "";
			String category = "";
			int aucount = 1;
			int categoryCount = 1;
			System.out.println(entry.getName() + count + ":");
			for (Iterator it = entry.elementIterator(); it.hasNext();){
				Element element = (Element) it.next();


				if(element.getName().equals("id")){
					System.out.println(element.getName() + ":" + element.getText());
				}

				if(element.getName().equals("published")){
					String time = element.getText();
					time = time.replace("T", " ").replace("Z", " ").trim();
					System.out.println(element.getName() + ":" + time);
				}

				if(element.getName().equals("title")){
					System.out.println(element.getName() + ":" + element.getText());
				}

				if(element.getName().equals("summary")){
					System.out.println(element.getName() + ":" + element.getText());
				}

				if(element.getName().equals("author")){
					for (Iterator au = element.elementIterator(); au.hasNext();){
						Element name = (Element) au.next();
						if (aucount == 1)
							author = name.getText();
						else
						    author = author + "," + name.getText();
					}
					aucount++;
				}

				if(element.getName().equals("link") && element.attribute(0).getName().equals("title")){
					Attribute href = element.attribute("href");
					System.out.println("link: " + href.getValue());
				}

				if(element.getName().equals("category")){
					Attribute term = element.attribute("term");
					if (categoryCount == 1)
						category = term.getValue();
					else
						category = category + "," + term.getValue();
					categoryCount++;
				}
			}
			author = author.trim();
			category = category.trim();
			System.out.println("authors: " + author);
			System.out.println("category: " + category);
			System.out.println("\n");

			count++;
		}
	}

	@Test
	public void dateTransformTest() throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date utilDate = format.parse("2019-06-12 00:00:00");
		// java.util.Data类型数据转换为java.sql.Timestamp类型数据
		Timestamp sqlDate = new Timestamp(utilDate.getTime());
		// 从字符串生成java.sql.Timestamp类型数据
		Timestamp stringDate = Timestamp.valueOf("1993-08-12 12:11:03");
		System.out.println(sqlDate);
		System.out.println(stringDate);
	}

	@Test
	public void mybatisMapperConnectTest(){
		PaperRecord record = paperRecordMapper.getPaperRecordById("http://arxiv.org/abs/cs/9308102v1");
		System.out.println("id:" + record.getId());
		System.out.println("time:" + record.getTime());
		System.out.println("title:" + record.getTitle());
		System.out.println("authors:" + record.getAuthors());
		// 测试查询一条不存在的记录,看返回值是否为false
		Boolean sign = paperRecordMapper.verifyRecordIsExist("null");
		System.out.println(sign);
	}

	@Test
	public void testStringWordSplit(){
		String str1 = "A fundamental computation for statistical inference and accurate\n" +
				"decision-making is to compute the marginal, probabilities or most probable\n" +
				"states of task-relevant variables.";
		str1 = str1.replaceAll("\\pP", "");
		String [] strarr = str1.split("\\s+");
		for(String s:strarr){
			System.out.println(s);
		}
	}

	@Test
	public void testPaperWordList(){
		for (Iterator it = paperId2WordArray.keySet().iterator(); it.hasNext();){
			String key = (String) it.next();
			String [] valarr = paperId2WordArray.get(key);
			String value = "";
			for (String word:valarr)
				value = value + " " + word;
			System.out.println(key + " ==> " + value);
		}
	}

	@Test
	public void testWordDictionary(){
		System.out.println("There are " + word2IndexDictionary.size() + " different word in dictionary.");
		for (Iterator it = word2IndexDictionary.keySet().iterator(); it.hasNext();){
			String word = (String) it.next();
			Set<String> idSet = word2IndexDictionary.get(word);
			System.out.println(word + " => " + idSet);
		}
	}
}
