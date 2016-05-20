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

public class Fang_Rentout {


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
	//D:/Crawldata_BeiJing/fang/rentout/0520/
	///home/gir/crawldata/beijing/fang/rentout/
	public static String FOLDER1 = "D:/Crawldata_BeiJing/fang/rentout/0520/fang_rentout"+"0520.txt";
	public static String FOLDER2 = "D:/Crawldata_BeiJing/fang/rentout/0520/fang_rentout"+"0520_zhoubian.txt";
	public static String MONITOR="D:/Crawldata_BeiJing/fang/rentout/0520/";
	public static void main(String[] args) {
		int pages=0;
			for(int i=0;i<regions.length;i++)
			{
				try{
					
					String content = HTMLTool.fetchURL("http://zu.fang.com"+regions[i]+"i31/", "gb2312", "get");
					Parser parser = new Parser();
					try {
						parser.setInputHTML(content);
						parser.setEncoding("gb2312");

						HasParentFilter parentFilter=new HasParentFilter(new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "fanye")));
						NodeFilter filter = new AndFilter(new TagNameFilter("span"),parentFilter);
						NodeList nodes = parser.extractAllNodesThatMatch(filter);
						
						if (nodes != null && nodes.size() == 1)
						{
							String str = nodes.elementAt(0).toPlainTextString().replace("共", "").replace("页", "").replace("\r\n", "").replace("\t", "").replace("\b","").replace("\n","").replace("\r","");
							System.out.println(str);
							pages=Integer.parseInt(str);
						}
						
					}catch (ParserException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} 
				String url="";				
				for(int j=10;j<=pages;j++){
					url="http://zu.fang.com"+regions[i]+"i3"+j+"/";
					getRentOutInfo(url);
					String str="完成"+regions[i]+"区"+"第"+j+"页的抓取！";
					System.out.println(str);
					FileTool.Dump(str, MONITOR+"monitor.txt", "utf-8");
				}  
			
		}catch(ArrayIndexOutOfBoundsException e){
			System.out.println(e.getMessage());
			e.getStackTrace();
		}
	}
		
}
	static JSONObject jsonObjArr = new JSONObject();
	/* 解析出租页面 */
	private static String parseRentOut(String url)
	{
		String content = HTMLTool.fetchURL(url, "gb2312", "get");//GB2312是汉字书写国家标准。

		Parser parser = new Parser();//获取解析器
		if (content == null)
		{
			return null;
		}
		
		String poi = "";
		try {
			
			parser.setInputHTML(content);
			parser.setEncoding("gb2312");
			// 获取发布时间
			NodeFilter filter = new AndFilter(new TagNameFilter("dl"), new HasAttributeFilter("class", "title"));//通过创建如下NodeFilter对象，提取其间的文本
			//NodeFilter:过滤某个节点以及其子节点,NodeFilter是一个接口,接口是不能被实例化的,被实例化的是那个接口的实现类   那个实现类指针指向的是那个接口
			//TagNameFilter意思是根据节点名称来过滤。TagNameFilter("dl")是过滤"dl"
			//HasAttributeFilter("class", "title")可以匹配出包含制定名字的属性，或者制定属性为指定值的节点。
			NodeList nodes = parser.extractAllNodesThatMatch(filter);
			//遍历所有的节点
			if (nodes != null && nodes.size() == 1)
			{
				String str = nodes.elementAt(0).toPlainTextString().replace("免费发布出租", "").replace("\r\n", "").replace("\t", "").replace("\b","").replace("\n","").replace("\r","").replace("&nbsp;", "").trim();
				//elementAt(int index) 方法用于获取组件的向量的指定索引/位置。
				//String toPlainTextString()：取得纯文本信息。
				//replace(char oldChar, char newChar)返回一个新的字符串，它是通过用 newChar 替换此字符串中出现的所有 oldChar 而生成的。
				//trim()去掉字符串中的空格
				//int n = str.indexOf("房源编号：");//indexOf()返回指定字符在此字符串中第一次出现处的索引
				int m = str.indexOf("发布时间：");
				int i= str.indexOf("房源编号");
				int s=str.indexOf("]");
				if(i!=-1){
					String substr=str.substring(0, i).replace(" ", "").replace("\r\n", "").replace("\t", "").trim();
					jsonObjArr.put("title",substr);
				}
					

				if(s!=-1){
					String substr=str.substring(s+"]".length(), m).replace(" ", "").replace("\r\n", "").replace("\t", "").trim();
					jsonObjArr.put("title",substr);
				}
					if (m != -1)
					{
						int k = str.indexOf("(", m + "发布时间：".length());
						if (k != -1)
						{
							//poi += "<TIME>" + str.substring(m + "发布时间：".length(), k).trim() + "</TIME>";
							jsonObjArr.put("time", str.substring(m + "发布时间：".length(), k).trim());
							parser.reset();//解析器重置清零的意思?
							
							// Huxing floatl
							filter = new AndFilter(new TagNameFilter("span"), new HasAttributeFilter("class", "num red"));
							
							nodes = parser.extractAllNodesThatMatch(filter);
							if (nodes != null)
							{
								for (int cnt = 0; cnt < nodes.size(); cnt ++)
								{
									Node cni = nodes.elementAt(cnt);
									
									str = cni.getParent().toPlainTextString().trim();
									//java.io.File.getParent() 方法返回的路径字符串，如果此抽象路径名的父或如果此路径名没有指定父目录则为null。
									//Node getParent ()：取得父节点
									int n=str.indexOf("元/月");
									if ( n!= -1)
									{
										//poi += "<PRICE>" + str.substring(0,n).replace("\r\n", "") +"元/月"+ "</PRICE>";
										jsonObjArr.put("price",str.substring(0,n).replace("\r\n", ""));
										break;
									}
								}
							
							parser.reset();
							
							// <span class="num red">
							filter = new AndFilter(new TagNameFilter("li"), new HasParentFilter(new AndFilter(new TagNameFilter("ul"), new HasAttributeFilter("class", "Huxing floatl"))));
							nodes = parser.extractAllNodesThatMatch(filter);

							if (nodes != null)
							{
								for (int mm = 0; mm < nodes.size(); mm ++)
								{
									Node ni = nodes.elementAt(mm);
										
									if (ni instanceof TagNode && ((TagNode)ni).getTagName().equalsIgnoreCase("li"))
										//equals是比较2个字符串是否相等的，区分大小写 equalsignorecase功能一样，但是不区分大小写
										//instanceof是Java的一个二元操作符,它的作用是测试它左边的对象是否是它右边的类的实例，返回boolean类型的数据。
									{
										String tt = ni.toPlainTextString().trim();
										
										
										int ix = tt.indexOf("物业类型：");
										if (ix != -1)
										{
											String sub = tt.substring(ix + "物业类型：".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
											if (sub.indexOf("暂无") == -1)
												//poi += "<PROPERTY_TYPE>" + sub + "</PROPERTY_TYPE>";
												jsonObjArr.put("property",sub);
											continue;
										}
										
										ix = tt.indexOf("小 区：");
										
										if (ix != -1)
										{
											String sub = tt.substring(ix + "小 区：".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
											if (sub.indexOf("暂无") == -1)
												//poi += "<COMMUNITY>" + sub + "</COMMUNITY>";
												jsonObjArr.put("community",sub);
											continue;
										}

										ix = tt.indexOf("地 址：");
										
										if (ix != -1)
										{

											String sub = tt.substring(ix + "地 址：".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
											if (sub.indexOf("暂无") == -1)
												//poi += "<ADDRESS>" + sub + "</ADDRESS>";
												jsonObjArr.put("address",sub);
											continue;
										}
										
										ix = tt.indexOf("户 型：");
										
										if (ix != -1)
										{

											String sub = tt.substring(ix + "户 型：".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
											if (sub.indexOf("暂无") == -1)
												//poi += "<HOUSE_TYPE>" + sub + "</HOUSE_TYPE>";
												jsonObjArr.put("house_type",sub);
											
											continue;
										}

										ix = tt.indexOf("出租间：");
										
										if (ix != -1)
										{

											String sub = tt.substring(ix + "出租间：".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
											if (sub.indexOf("暂无") == -1)
												//poi += "<PARTMENT>" + sub + "</PARTMENT>";
												jsonObjArr.put("rent_type",sub);
											continue;
										}


										ix = tt.indexOf("面 积：");
										
										if (ix != -1)
										{

											String sub = tt.substring(ix + "面 积：".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
											if (sub.indexOf("暂无") == -1)
												//poi += "<AREA>" + sub + "</AREA>";
												jsonObjArr.put("area",sub);

											continue;
										}

										ix = tt.indexOf("朝 向：");
										
										if (ix != -1)
										{

											String sub = tt.substring(ix + "朝 向：".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
											if (sub.indexOf("暂无") == -1)
												//poi += "<DIRECTION>" + sub + "</DIRECTION>";
												jsonObjArr.put("direction",sub);
											continue;
										}

										ix = tt.indexOf("楼 层：");
										
										if (ix != -1)
										{

											String sub = tt.substring(ix + "楼 层：".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
											if (sub.indexOf("暂无") == -1)
												//poi += "<FLOOR>" + sub + "</FLOOR>";
												jsonObjArr.put("floor",sub);

											continue;
										}
										

										ix = tt.indexOf("装 修：");
										
										if (ix != -1)
										{

											String sub = tt.substring(ix + "装 修：".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
											if (sub.indexOf("暂无") == -1)
												//poi += "<DECORATION>" + sub + "</DECORATION>";
												jsonObjArr.put("fitment",sub);

											continue;
										}
									}
								}
								jsonObjArr.put("url",url);
							}
						}
					}		
				}
			}else{
				parser.reset();
				HasParentFilter parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "h1-tit rel")));
				filter = new AndFilter(new TagNameFilter("h1"), parentFilter1);//通过创建如下NodeFilter对象，提取其间的文本
				nodes = parser.extractAllNodesThatMatch(filter);
				if(nodes.size()!=0){
					TagNode no = (TagNode) nodes.elementAt(0);
					String str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "").trim();
					jsonObjArr.put("title",str);
				}
				parser.reset();
				parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "h1-tit rel")));
				filter = new AndFilter(new TagNameFilter("p"), parentFilter1);//通过创建如下NodeFilter对象，提取其间的文本
				nodes = parser.extractAllNodesThatMatch(filter);
				if(nodes.size()!=0){
					TagNode no = (TagNode) nodes.elementAt(0);
					String str=no.toPlainTextString().replace("\r\n", "").replace("\t", "").replace("\n", "").trim();
					if(str.indexOf("时间：")!=-1){
						str=str.substring(str.indexOf("时间：")+"时间：".length());
						jsonObjArr.put("time",str);
					}
					
				}
				parser.reset();
				parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("ul"),new HasAttributeFilter("class", "house-info")));
				filter = new AndFilter(new TagNameFilter("li"),parentFilter1);//通过创建如下NodeFilter对象，提取其间的文本
				nodes = parser.extractAllNodesThatMatch(filter);
				if(nodes.size()!=0){
					for(int i=0;i<nodes.size();i++){
						TagNode no = (TagNode) nodes.elementAt(i);
						String str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "").replace("[面议]", "").replace("m&sup2;", "").trim();
						if(str.indexOf("租金")!=-1){
							jsonObjArr.put("price",str);
						}
						if(str.indexOf("房屋概况")!=-1){
							str=str.replace("房屋概况：", "");//住宅|1室0厅1卫|55|南|精装修
							String[] arry=str.split("\\|");
							jsonObjArr.put("property",arry[0]);
							jsonObjArr.put("house_type",arry[1]);
							jsonObjArr.put("area",arry[2]);
							jsonObjArr.put("direction",arry[3]);
							jsonObjArr.put("fitment",arry[4]);
						}
						if(str.indexOf("小区")!=-1){
							jsonObjArr.put("community",str);
						}
                        if(str.indexOf("合租")!=-1){
                        	String[] arry=str.split("\\|");
                        	jsonObjArr.put("rent_type",arry[0]);
						}
					}
				}
				
			}
		} catch (ParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		jsonObjArr.put("url",url);
		poi=jsonObjArr.toString();
		if (poi != null)
		{
			poi = poi.replace("&nbsp;", "");
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
		}
		
		return poi;
	}

	
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
	

	
	/* 抓取出租数据
	 * */
	public static String RENTOUT_URL = "http://zu.fang.com";
	
	public static void getRentOutInfo(String url)
	{
		// 首先加载
		Vector<String> log = null;
		
		// 2014/12/8 17:16:42
	    SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
	    //
		
		java.util.Date latestdate = null;
		Date newest = null;
		

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
				parser.setEncoding("gb2312");
				// <dd class="info rel floatr">
				// <p class="title"><a href="/chuzu/3_153703104.htm" target="_blank" title="冉家坝龙山小学旁 光宇阳光地中海精装三房急售 无营业税拎包入住">冉家坝龙山小学旁 光宇阳光地中海精装三房急售 无营业税拎包入住</a></p>					
				// <p class="title"
				HasParentFilter parentFilter = new HasParentFilter(new AndFilter(new TagNameFilter("p"), new HasAttributeFilter("class", "title")));
				NodeFilter filter = new AndFilter(new TagNameFilter("a"), new AndFilter(new AndFilter(parentFilter, new HasAttributeFilter("target")), new HasAttributeFilter("href"))); 
				
				NodeList nodes = parser.extractAllNodesThatMatch(filter);
				
				if (nodes != null)
				{
					int size=nodes.size();
					for (int n = 0; n < nodes.size(); n ++)
					{
						TagNode tn = (TagNode)nodes.elementAt(n);
						String purl = tn.getAttribute("href");
						if (purl.indexOf("/chuzu")!=-1)
						{
							String poi="";
							if(purl.startsWith("/chuzu")){
								poi = parseRentOut("http://zu.fang.com" + purl);
							}else{
								poi = parseRentOut(purl);
							}
							
							if (poi != null)
							{
								// 获取时间
								JSONObject jsonObject = JSONObject.fromObject(poi);
								String tm=jsonObject.get("time").toString();
									try {
										if(tm.indexOf('-')!=-1)
											tm=tm.replace("-", "/");	
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
										synchronized(BJ_RENTOUT)
										//synchronized是Java语言的关键字，当它用来修饰一个方法或者一个代码块的时候，能够保证在同一时刻最多只有一个线程执行该段代码。
										{
											poi.replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
											if(url.indexOf("/house-a0987/")!=-1||url.indexOf("/house-a011817/")!=-1)
												FileTool.Dump(poi,FOLDER2, "UTF-8");
											else
												FileTool.Dump(poi,FOLDER1, "UTF-8");
											System.out.println(poi);
											//"/house-a0987/", "/house-a011817/"
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
			}catch (ParserException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
		}
	}
	
}
