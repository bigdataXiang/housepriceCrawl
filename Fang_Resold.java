package com.svail.crawl.fang;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasChildFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import com.svail.util.FileTool;
import com.svail.util.HTMLTool;

import net.sf.json.JSONObject;
public class Fang_Resold {


	private static Random random = new Random();
	/**
	 * java.util.Random类中实现的随机算法是伪随机，也就是有规则的随机，
	 * 所谓有规则的就是在给定种(seed)的区间内随机生成数字；相同种子数的Random对象，
	 * 相同次数生成的随机数字是完全相同的；Random类中各方法生成的随机数字都是均匀分布的，
	 * 也就是说区间内部的数字生成的几率均等；
	 * Random()：创建一个新的随机数生成器。
	 */
	private static String BJ_NEWHOUSE = "NEW";
	private static String BJ_RENTINFO = "RENTINFO";
	private static String BJ_RENTOUT = "RENTOUT";
	private static String BJ_RESOLDS = "RESOLD";
	public static String FOLDER1 = "/home/gir/crawldata/beijing/fang/resold/fang_resold0414.txt";
	public static String FOLDER2 = "/home/gir/crawldata/beijing/fang/resold/fang_resold0414_zhoubian.txt";
    public static void main(String[] args) {
		
		for(int i=0;i<regions.length;i++)
		 getResoldApartmentInfo(regions[i]);
		
	}
    static JSONObject jsonObjArr = new JSONObject();
	
	/* 解析二手房页面 */
	private static String parseResold(String url)
	{
		String content = HTMLTool.fetchURL(url, "gb2312", "get");

		Parser parser = new Parser();
		if (content == null)
		{
			return null;
		}
		
		String poi = null;
		try {
			
			parser.setInputHTML(content);
			parser.setEncoding("gb2312");
			// 获取发布时间
			NodeFilter filter = new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "mainBoxL"));//class mainBoxL
			NodeList nodes = parser.extractAllNodesThatMatch(filter);
			
			if (nodes != null && nodes.size() == 1)
			{
				String str = nodes.elementAt(0).toPlainTextString().replace("\r\n", "").replace("\t", "").replace("\n","").replace("\b","").trim();
				
				int n = str.indexOf("房源编号：");
				int m = str.indexOf("发布时间：");
				/*
				 * Java中字符串中子串的查找共有四种方法，如下：
                   1、int indexOf(String str) ：返回第一次出现的指定子字符串在此字符串中的索引。 
                   2、int indexOf(String str, int startIndex)：从指定的索引处开始，返回第一次出现的指定子字符串在此字符串中的索引。 
                   3、int lastIndexOf(String str) ：返回在此字符串中最右边出现的指定子字符串的索引。 
                   4、int lastIndexOf(String str, int startIndex) ：从指定的索引处开始向后搜索，返回在此字符串中最后一次出现的指定子字符串的索引。
				 */
				
				if (n != -1)
				{
					String str1=str.substring(0, n);
					//poi = "<TITLE>" + str1.replace("\r\n", "").replace("\t", "").replace("\n","").replace("\b","").trim() + "</TITLE>";
					jsonObjArr.put("title",str1.replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n","").replace("\b","").trim());
					if (m != -1)
					{
						
						int k = str.indexOf("总", m + "发布时间：".length());
						if (k != -1)
						{
							
							String str2=str.substring(m + "发布时间：".length(), k);
							poi += "<TIME>" + str2.replace("\r\n", "").replace("\t", "").replace("\n","").replace("\b","").replace("-", "/")+ "</TIME>";
							jsonObjArr.put("time",str2.replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n","").replace("\b","").replace("-", "/"));
						}
					}
				}
			}
			if (poi == null)
				return poi;
			
			parser.reset();
			filter = new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "inforTxt"));
			nodes = parser.extractAllNodesThatMatch(filter);
			
			if (nodes != null)
			{
				for (int rds = 0; rds < nodes.size(); rds ++)
				{
					NodeList hs = nodes.elementAt(rds).getChildren();//NodeList getChildren ()：取得子节点的列表
					if (hs == null)
						continue;
					
					for (int jk = 0; jk < hs.size(); jk ++)
					{
						if (hs.elementAt(jk) instanceof TagNode)//instanceof 测试它左边的对象是否是它右边的类的实例，返回boolean类型的数据。
						{
							TagNode tn = (TagNode) hs.elementAt(jk);
							if (tn.getTagName().equalsIgnoreCase("dl"))
							{
								NodeList chld = tn.getChildren();
								
								if (chld != null)
								{
									for (int cnt = 0; cnt < chld.size(); cnt ++)
									{
										if (chld.elementAt(cnt) instanceof TagNode)
										//elementAt() 函数把字符串划分为元素，并返回位于指定下标位置的元素。
									    //n = String.elementAt(string, index, separator):n从函数返回的字符串,string要解析的字符串。index一个整数，定义被返回的部分;separator分隔字符串的分隔符。
										//如果 index 是负数，则返回第一个元素。如果 index 的值太大，则返回最后一个元素。
										{
											String str = ((TagNode) chld.elementAt(cnt)).toPlainTextString().replace("\r\n", "").replace("\t", "").replace("\n","").replace("\b","").trim();
											int kk = str.indexOf("总价：");
											if (kk != -1)
											{
												int kk1 = str.indexOf("(", kk + "总价：".length());
												/**
												 *java.lang.String.indexOf(String str, int fromIndex) 方法返回在此字符串中第一次出现的指定子指数，在指定的索引开始。返回的整数是最小的k值: 
                                                  k > = Math.min(fromIndex, this.length()) && this.startsWith(str, k)
                                                                                                                                                     如果不存在这样的k值，则返回-1
                                                  str -- 这是要搜索的子串.fromIndex -- 这是该指数开始搜索.
                                                                                                                                                       此方法返回指数在此字符串中第一次出现的指定子字符串，起始在指定的索引                                                                                                  
                                                                                                                 
												 */
												if (kk1 != -1)
												{
													String substr = str.substring(kk + "总价：".length(), kk1).replace("\r\n", "").replace("\t", "").replace("\n","").replace("\b","");
													//poi += "<PRICE>" + substr + "</PRICE>";
													jsonObjArr.put("price",substr);
												}
												continue;
											}
											kk = str.indexOf("户型：");
											if (kk != -1)
											{
												String substr = str.substring(kk + "户型：".length()).replace("\r\n", "").replace("\t", "").replace("\n","").replace("\b","");
												if (substr.indexOf("暂无") == -1)
													//poi += "<HOUSE_TYPE>" + substr + "</HOUSE_TYPE>";
													jsonObjArr.put("house_type",substr);
												continue;
											}
											kk = str.indexOf("建筑面积：");
											// returns -1 as substring is not located
											// indexOf()是从字符串的0个位置开始查找的,索引位置也是从0开始计算,一个空格算入一个索引位置
											if (kk != -1)
											{
												/*
												 * str＝str.substring(int beginIndex);截取掉str从首字母起长度为beginIndex的字符串，将剩余字符串赋值给str；
                                                   str＝str.substring(int beginIndex，int endIndex);截取str中从beginIndex开始至endIndex结束时的字符串，并将其赋值给str;
												 */
												int mm=kk + "建筑面积：".length();
												
												String substr = str.replace("\r\n", "").replace("\t", "").replace("\n","").replace("\b","");
												if (substr.indexOf("暂无") == -1)
													//poi += "<BUILDING_AREA>" + substr+"㎡"+"</BUILDING_AREA>";
													jsonObjArr.put("area",substr);
												
												
												continue;
											}
											
											kk = str.indexOf("年代：");
											if (kk != -1)
											{
												String substr = str.substring(kk + "年代：".length()).replace("\r\n", "").replace("\t", "").replace("\n","").replace("\b","");
												if (substr.indexOf("暂无") == -1)
													//poi += "<BUILDING_TIME>" + substr + "</BUILDING_TIME>";
													jsonObjArr.put("built_year",substr);
												continue;
											}
											
											kk = str.indexOf("朝向：");
											if (kk != -1)
											{
												String substr = str.substring(kk + "朝向：".length()).replace("\r\n", "").replace("\t", "").replace("\n","").replace("\b","");
												if (substr.indexOf("暂无") == -1)
													//poi += "<BUILDING_DIR>" + substr + "</BUILDING_DIR>";
													jsonObjArr.put("direction",substr);
												continue;
											}
											kk = str.indexOf("楼层：");
											if (kk != -1)
											{
												String substr = str.substring(kk + "楼层：".length()).replace("\r\n", "").replace("\t", "").replace("\n","").replace("\b","");
												if (substr.indexOf("暂无") == -1)
													//poi += "<BUILDING_FLOOR>" + substr + "</BUILDING_FLOOR>";
													jsonObjArr.put("floor",substr);
												continue;
											}
											kk = str.indexOf("结构：");
											if (kk != -1)
											{
												String substr = str.substring(kk + "结构：".length()).replace("\r\n", "").replace("\t", "").replace("\n","").replace("\b","");
												if (substr.indexOf("暂无") == -1)
													//poi += "<BUILDING_STRUCT>" + substr + "</BUILDING_STRUCT>";
													jsonObjArr.put("structure",substr);
												continue;
											}
											kk = str.indexOf("装修：");
											if (kk != -1)
											{
												String substr = str.substring(kk + "装修：".length()).replace("\r\n", "").replace("\t", "").replace("\n","").replace("\b","");
												if (substr.indexOf("暂无") == -1)
													//poi += "<BUILDING_CONDITION>" + substr + "</BUILDING_CONDITION>";
													jsonObjArr.put("fitment",substr);
												continue;
											}
											
											kk = str.indexOf("住宅类别：");
											if (kk != -1)
											{
												String substr = str.substring(kk + "住宅类别：".length()).replace("\r\n", "").replace("\t", "").replace("\n","").replace("\b","");
												if (substr.indexOf("暂无") == -1)
													//poi += "<BUILDING_TYPE>" + substr + "</BUILDING_TYPE>";
													jsonObjArr.put("property",substr);
												continue;
											}
											
											kk = str.indexOf("楼盘名称：");
											if (kk != -1)
											{
												String substr = str.substring(kk + "名称：".length()).replace("\r\n", "").replace("\t", "").replace("\n","").replace("\b","");
												if (substr.indexOf("暂无") == -1)
													//poi += "<BUILDING_NAME>" + substr.replace("[街景地图]","").replace("称：","")+ "</BUILDING_NAME>";
													jsonObjArr.put("community",substr.replace(" ", "").replace("[街景地图]","").replace("称：",""));
												continue;
											}
											kk = str.indexOf("学  校：");
											if (kk != -1)
											{
												String substr = str.substring(kk + "学  校：".length()).replace("\r\n", "").replace("\t", "").replace("\n","").replace("\b","");
												if (substr.indexOf("暂无") == -1)
													//poi += "<BUILDING_SERVICE>" + substr + "</BUILDING_SERVICE>";
													jsonObjArr.put("school",substr);
												continue;
											}
											kk = str.indexOf("配套设施：");
											if (kk != -1)
											{
												String substr = str.substring(kk + "配套设施：".length()).replace("\r\n", "").replace("\t", "").replace("\n","").replace("\b","");
												if (substr.indexOf("暂无") == -1)
													poi += "<BUILDING_SERVICE>" + substr + "</BUILDING_SERVICE>";
													jsonObjArr.put("eqiupment",substr);
												continue;
											}
										}
									}
								}
							}
						}
					}
				}
			}
			
			
			parser.reset();
			filter = new AndFilter(new  TagNameFilter("p"), new HasParentFilter(new HasAttributeFilter("class", "traffic mt10")));
			nodes = parser.extractAllNodesThatMatch(filter);
			int m=nodes.size();
			if (nodes != null)
			{
				for (int cnt = 0; cnt < m; cnt ++)
				{
					String str = nodes.elementAt(cnt).toPlainTextString().trim();
					
					int si = str.indexOf("址：");
					if (str.indexOf("地") != -1 && si != -1)
					{
						if (str.indexOf("暂无") == -1)
						{
							String str1=str.substring(si + "址：".length()).replace("\r\n", "").replace("\t", "").replace("\n","").replace("\b","");
							//poi += "<ADDRESS>" +str1+ "</ADDRESS>";
							jsonObjArr.put("address",str1);
						}
					}
				}
			}
			jsonObjArr.put("url",url);
			poi = jsonObjArr.toString();
			int ss = poi.indexOf("[");
			while (ss != -1)
			{
				int ee = poi.indexOf("]", ss + 1);
				if (ee != -1)
				{
					String sub = poi.substring(ss, ee + 1);
					poi = poi.replace(sub, "");
				}
				else
					break;
				ss = poi.indexOf("[", ss);
			}

		} catch (ParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		return poi;
	}
	
	public static String LOG = "/home/gir/crawldata/beijing/fang/resold";
	/*
	 * 二手房 http://esf.cq.fang.com/
	 * 出租房  http://zu.cq.fang.com/
     * */
	public static String regions[] = {
		"/house-a01/", "/house-a00/", "/house-a06/", "/house-a02/", "/house-a03/", "/house-a04/",
		"/house-a05/", "/house-a07/", "/house-a012/", "/house-a0585/", "/house-a010/", "/house-a011/",
		"/house-a08/", "/house-a013/", "/house-a09/", "/house-a014/", "/house-a015/", "/house-a016/",
		"/house-a0987/", "/house-a011817/",
	};
	
	/* 抓取二手房数据
	 * */
	public static String RESOLDAPARTMENT_URL = "http://esf.fang.com";
	
	public static void getResoldApartmentInfo(String region)
	{
		// 首先加载
		Vector<String> log = null;
		//Vector是一个集合，用数组实现的。所以vector的数据结构是数组。vector里面包含的可以是int，string等任何类型，包括一个自定义的对象。
		synchronized(BJ_RESOLDS)
		//无论synchronized关键字加在方法上还是对象上，它取得的锁都是对象，而不是把一段代码或函数当作锁
		{
			log = FileTool.Load(LOG + File.separator + region + "_resold.log", "UTF-8");
		}
		// 2014/12/8 17:16:42
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd");//小写的mm表示的是分钟
		
		java.util.Date latestdate = null;
		Date newest = null;
		
		if (log != null)
		{
			try {
				latestdate = sdf.parse(log.elementAt(0));
				latestdate = new Date(latestdate.getTime() - 1);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		String url = RESOLDAPARTMENT_URL + region;
		Vector<String> urls = new Vector<String>();
		
		Set<String> visited = new TreeSet<String>();
		urls.add(url);
		
		Parser parser = new Parser();
		boolean quit = false;
		
		while (urls.size() > 0)
		{
			// 解析页面
			url = urls.get(0);
			
			urls.remove(0);
			visited.add(url);
			
			String content = HTMLTool.fetchURL(url, "gb2312", "get");

			if (content == null)
			{
				continue;
			}
			try {
				
				parser.setInputHTML(content);
				parser.setEncoding("gb18030");
				// <dd class="info rel floatr">
				// <p class="title"><a href="/chushou/3_153703104.htm" target="_blank" title="冉家坝龙山小学旁 光宇阳光地中海精装三房急售 无营业税拎包入住">冉家坝龙山小学旁 光宇阳光地中海精装三房急售 无营业税拎包入住</a></p>					
				// <p class="title"
				HasParentFilter parentFilter = new HasParentFilter(new AndFilter(new TagNameFilter("p"), new HasAttributeFilter("class", "title")));
				//HasChildFilter：是返回有符合条件的子节点的节点，需要另外一个Filter作为过滤子节点的参数。HasParentFilter和HasSiblingFilter的功能与HasChildFilter类似。
				//TagNameFilter：是最容易理解的一个Filter，根据Tag的名字进行过滤
				//HasAttributeFilter:可以匹配出包含制定名字的属性，或者制定属性为指定值的节点。
				NodeFilter filter = new AndFilter(new TagNameFilter("a"), new AndFilter(new AndFilter(parentFilter, new HasAttributeFilter("title")), new HasAttributeFilter("href"))); 
				
				NodeList nodes = parser.extractAllNodesThatMatch(filter);
				
				if (nodes != null)
				{
					for (int n = 0; n < nodes.size(); n ++)
					{
						TagNode tn = (TagNode)nodes.elementAt(n);
						String purl = tn.getAttribute("href");
						if (purl.startsWith("/chushou"))
						{
							String poi = parseResold("http://esf.fang.com" + purl);
							if (poi != null)
							{
								// 获取时间
								JSONObject jsonObject = JSONObject.fromObject(poi);
								String tm=jsonObject.get("time").toString();
									try {
										Date date = sdf.parse(tm);
										if (latestdate != null)
										{
											if (date.before(latestdate))
											{
												quit = true;
											}
											else if (newest == null)
											{
												newest = date;
											}
											else{
												if (newest.before(date))
													newest = date;
											}
												
										}
									} catch (ParseException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
									if (quit)
									{
										break;
									}
									else
									{
										synchronized(BJ_RESOLDS)
										{
											poi.replace(" ", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
											if(url.indexOf("/house-a0987/")!=-1||url.indexOf("/house-a011817/")!=-1)
												FileTool.Dump(poi.replace(" ", "").trim(),FOLDER2, "UTF-8");
											else
												FileTool.Dump(poi.replace(" ", "").trim(),FOLDER1, "UTF-8");
												System.out.println(poi);
										}
									}
							}
							
							try {
								Thread.sleep(500 * ((int) (Math
									.max(1, Math.random() * 3))));
							} catch (final InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
				}
				
				parser.reset();
				
				// <div class="fanye gray6">  <a class="pageNow">PageControl1_hlk_next
				filter = new AndFilter(new TagNameFilter("a"),new HasAttributeFilter("id","PageControl1_hlk_next") );
				/* filter = new AndFilter(new TagNameFilter("div"), 
					new HasChildFilter(new AndFilter(new TagNameFilter("a"), new HasAttributeFilter("class", "pageNow")))); 
				*/
				nodes = parser.extractAllNodesThatMatch(filter);
				
				if (nodes != null)
				{
					String turl = ((TagNode)nodes.elementAt(0)).getAttribute("href");
					if (!visited.contains("http://esf.fang.com" + turl))
					{
						int kk = 0;
						for (; kk < urls.size(); kk ++)
						{
							if (urls.elementAt(kk).equalsIgnoreCase("http://esf.fang.com" + turl))
							{
								break;
							}
						}
						
						if (kk == urls.size())
							urls.add("http://esf.fang.com" + turl);
					}
					
				}
				
				if (quit)
					break;
			}
			catch (ParserException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}catch(NullPointerException e){
				System.out.println(e.getMessage());
			}
		}
		
		synchronized(BJ_RESOLDS)
		{
			File f = new File(LOG + File.separator + region + "_resold.log");
			f.delete();
			if (newest != null)
			{			
				FileTool.Dump(sdf.format(newest), LOG + File.separator + region + "_resold.log", "UTF-8");
			}
		}
	}
}
