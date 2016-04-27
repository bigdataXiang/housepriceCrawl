package com.svail.crawl.anjuke;
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
public class Anjuke_Resold{


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
	public static String FOLDER1 = "/home/gir/crawldata/beijing/anjuke/resold/anjuke_resold0426.txt";
	public static String FOLDER2= "/home/gir/crawldata/beijing/anjuke/resold/anjuke_resold0426_zhoubian.txt";
	public static void main(String[] args) {
		   for(int i=0;i<regions.length;i++)
		   {
		   getResoldApartmentInfo(regions[i]);
		   }
		  
		}

	
	/* 解析二手房页面 */
	private static String parseResold(String url)
	{
		//System.out.println(url);
		String content = HTMLTool.fetchURL(url, "utf-8","get");//GB2312是汉字书写国家标准。
		//System.out.println("url.ok!");
		JSONObject jsonObjArr = new JSONObject();

		Parser parser = new Parser();//获取解析器
		if (content == null)
		{
			return null;
		}
		
		String poi ="";
		try {
			
			parser.setInputHTML(content);
			parser.setEncoding("utf-8");


			NodeFilter filter=new AndFilter(new TagNameFilter("h3"),new HasAttributeFilter("class","fl"));
			NodeList nodes= parser.extractAllNodesThatMatch(filter); 	
			if (nodes != null && nodes.size() == 1)
			{
				
				String str = nodes.elementAt(0).toPlainTextString().replace("急售", "").replace("总价", "").replace("\r\n", "").replace("\t", "").replace("爱屋吉屋", "").replace("佣金1%", "").trim();

				String tt=str.replace("，","").replace("、","").replace("《","【").replace("》","】").replace("[","【").replace("]","】").replace("=", "").replace("-", "").replace(",", "").replace("！", "").trim();

				int ix = tt.indexOf("【");
				int ix2=tt.indexOf("】");
				if(ix!=-1&&ix2!=-1)
				{
					String t1=tt.substring(0,ix);
					String t2=tt.substring(ix2+"】".length());
					String substr=t1.replace(" ", "").trim() + t2.replace(" ", "").trim();
					jsonObjArr.put("title", substr);
					
				}
				else
				{
					String substr=  tt.replace(" ", "").trim();
					jsonObjArr.put("title", substr);
				}				
				parser.reset();					
			}	
			parser.reset();
			
			Date d = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
			jsonObjArr.put("time",sdf.format(d));
		
			filter = new AndFilter(new TagNameFilter("dl"), new HasParentFilter(new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "litem fl"))));
			nodes = parser.extractAllNodesThatMatch(filter);
			if (nodes != null)
			{
				for (int mm = 0; mm < nodes.size(); mm ++)
				{
					Node ni = nodes.elementAt(mm);
						
					if (ni instanceof TagNode && ((TagNode)ni).getTagName().equalsIgnoreCase("dl"))
						//equals是比较2个字符串是否相等的，区分大小写 equalsignorecase功能一样，但是不区分大小写
						//instanceof是Java的一个二元操作符,它的作用是测试它左边的对象是否是它右边的类的实例，返回boolean类型的数据。
					{
						String tt = ni.toPlainTextString().trim();
						tt = tt.replace("（", "(").replace("）", ")").replace("、", "");
						
						
						int ix = tt.indexOf("售价");
						
						if (ix != -1)
						{
							String sub = tt.substring(ix + "售价".length()).replace("\r\n", "").replace("\t", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1)
								jsonObjArr.put("price",sub);
								//poi += "<PRICE>" + sub.replace(" ", "") + "</PRICE>";
							continue;
						}

						ix = tt.indexOf("参考首付");
						if (ix != -1)
						{

							String sub = tt.substring(ix + "参考首付".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1)
								jsonObjArr.put("down_payment",sub);
								//poi += "<DOWN_PAYMENT>" + sub.replace(" ", "") + "</DOWN_PAYMENT>";
							continue;
						}
						ix = tt.indexOf("单价");
						if (ix != -1)
						{

							String sub = tt.substring(ix + "单价".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<UNIT_PRICE>" + sub.replace(" ", "") + "</UNIT_PRICE>";
								jsonObjArr.put("unit_price",sub);

							continue;
						}
						
						ix = tt.indexOf("所在小区");	
						int ix2=tt.indexOf("(");
						if (ix != -1&&ix2>ix)
						{

							String sub = tt.substring(ix + "所在小区".length(),ix2).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<COMMUNITY>" + sub.replace(" ", "") + "</COMMUNITY>";
								jsonObjArr.put("community",sub);

							continue;
						}

						ix = tt.indexOf("位置");						
						if (ix != -1)
						{

							String sub = tt.substring(ix + "位置".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<LOCATION>" + sub.replace("\r\n", "").replace("\t", "").replace(" ", "").replace("\n", "").trim()+ "</LOCATION>";
								jsonObjArr.put("location",sub);
							

							continue;
						}
						ix = tt.indexOf("地址");						
						if (ix != -1)
						{

							String sub = tt.substring(ix + "地址".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace("","").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<ADDRESS>" + sub.replace(" ", "") + "</ADDRESS>";
								jsonObjArr.put("address",sub);

							continue;
						}
						ix = tt.indexOf("开发商");						
						if (ix != -1)
						{

							String sub = tt.substring(ix + "开发商".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<DEVELOPER>" + sub.replace(" ", "") + "</DEVELOPER>";
								jsonObjArr.put("developer",sub);

							continue;
						}
						ix = tt.indexOf("物业公司");						
						if (ix != -1)
						{

							String sub = tt.substring(ix + "物业公司".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<PROPERTY>" + sub.replace(" ", "") + "</PROPERTY>";
								jsonObjArr.put("property_company",sub);
                                continue;
						}
						ix = tt.indexOf("物业类型");						
						if (ix != -1)
						{

							String sub = tt.substring(ix + "物业类型".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<PROPERTY_TYPE>" + sub.replace(" ", "") + "</PROPERTY_TYPE>";
								jsonObjArr.put("property",sub);

							continue;
						}
						ix = tt.indexOf("物业费用");						
						if (ix != -1)
						{

							String sub = tt.substring(ix + "物业费用".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace("(绿化率高)","").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<PROPERTY_FEE>" + sub.replace(" ", "") + "</PROPERTY_FEE>";
								jsonObjArr.put("property_fee",sub);

							continue;
						}
						
						
					
					}
				}
			}
			parser.reset();
			
					
			filter = new AndFilter(new TagNameFilter("dl"), new HasParentFilter(new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "ritem fr"))));
			nodes = parser.extractAllNodesThatMatch(filter);
			if (nodes != null)
			{
				for (int mm = 0; mm < nodes.size(); mm ++)
				{
					Node ni = nodes.elementAt(mm);
						
					if (ni instanceof TagNode && ((TagNode)ni).getTagName().equalsIgnoreCase("dl"))
						//equals是比较2个字符串是否相等的，区分大小写 equalsignorecase功能一样，但是不区分大小写
						//instanceof是Java的一个二元操作符,它的作用是测试它左边的对象是否是它右边的类的实例，返回boolean类型的数据。
					{
						String tt = ni.toPlainTextString().trim();
						tt = tt.replace("（", "(").replace("）", ")").replace("、", ",");
				

						int ix = tt.indexOf("房型");
						int iy;
						
						if (ix != -1)
						{
							String sub = tt.substring(ix + "房型".length()).replace("\r\n", "").replace("\t", "").replace("开盘通知我", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<HOUSE_TYPE>" + sub.replace(" ", "") + "</HOUSE_TYPE>";
								jsonObjArr.put("house_type",sub);
							continue;
						}

						ix = tt.indexOf("面积");
						iy=tt.indexOf("总建");
						if (ix != -1&& iy==-1)
						{

							String sub = tt.substring(ix + "面积".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<HOUSE_AREA>" + sub.replace(" ", "") + "</HOUSE_AREA>";
								jsonObjArr.put("area",sub);
							
							continue;
						}
						
						ix = tt.indexOf("总建面积");	
						if (ix != -1)
						{

							String sub = tt.substring(ix + "总建面积".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").replace("平方米", "").replace("(中型小区)", "").replace("(大型小区)", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<TOTAL_AREA>" + sub.replace(" ", "") + "</TOTAL_AREA>";
								jsonObjArr.put("totalarea",sub);
							
							continue;
						}
						

						ix = tt.indexOf("朝向");	
						if (ix != -1)
						{

							String sub = tt.substring(ix + "朝向".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<ORIENTATION>" + sub.replace(" ", "") + "</ORIENTATION>";
								jsonObjArr.put("direction",sub);
							
							continue;
						}

						ix = tt.indexOf("楼层");	
						if (ix != -1)
						{

							String sub = tt.substring(ix + "楼层".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<FLOOR>" + sub.replace(" ", "") + "</FLOOR>";
								jsonObjArr.put("floor",sub);
							
							continue;
						}

						ix = tt.indexOf("装修");	
						if (ix != -1)
						{

							String sub = tt.substring(ix + "装修".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<FITMENT>" + sub.replace(" ", "") + "</FITMENT>";
								jsonObjArr.put("fitment",sub);
							
							continue;
						}

						ix = tt.indexOf("类型");	
						if (ix != -1)
						{

							String sub = tt.substring(ix + "类型".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<TYPE>" + sub.replace(" ", "") + "</TYPE>";
								jsonObjArr.put("property",sub);
							
							continue;
						}
						
						ix = tt.indexOf("总户数");	
						if (ix != -1)
						{

							String sub = tt.substring(ix + "总户数".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<HOUSEHOLDS>" + sub.replace(" ", "") + "</HOUSEHOLDS>";
								jsonObjArr.put("households",sub);
							
							continue;
						}
						ix = tt.indexOf("建造年代");	
						if (ix != -1)
						{

							String sub = tt.substring(ix + "建造年代".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<BUILT_YEAR>" + sub.replace(" ", "") + "</BUILT_YEAR>";
								jsonObjArr.put("built_year",sub);
							
							continue;
						}
						ix = tt.indexOf("容积率");	
						if (ix != -1)
						{

							String sub = tt.substring(ix + "容积率".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<VOLUME_RATE>" + sub.replace(" ", "") + "</VOLUME_RATE>";
								jsonObjArr.put("volume_rate",sub);
							continue;
						}
						
						ix = tt.indexOf("停车位");	
						if (ix != -1)
						{

							String sub = tt.substring(ix + "停车位".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<PARK>" + sub.replace(" ", "") + "</PARK>";
								jsonObjArr.put("park",sub);
							
							continue;
						}
						ix = tt.indexOf("绿化率");	
						if (ix != -1)
						{

							String sub = tt.substring(ix + "绿化率".length(),tt.indexOf("%")+"%".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").replace("(绿化率高)","").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<GREEN_RATE>" + sub.replace(" ", "") + "</GREEN_RATE>";
								jsonObjArr.put("green_rate",sub);
							continue;
						}

						
					}
				}
			
				
				jsonObjArr.put("url",url);
				jsonObjArr.put("heat_supply","null");
				poi=jsonObjArr.toString().replace("&nbsp;", "").trim();
				return  poi;
	}
					
							
				
			
		} catch (ParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch(NullPointerException e){
			System.out.println(e.getMessage());
			
		}

		if (poi != null)
		{
			poi = poi.replace("&nbsp;", "").replace("&nbsp", "");
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

	public static String LOG = "/home/gir/crawldata/beijing/anjuke/resold";
	/*
	 * 二手房 http://esf.cq.fang.com/
	 * 出租房  http://zu.cq.fang.com/
     * */
	public static String regions[] = {
			"/chaoyang/","/haidian/","/fengtai/","/dongchenga/","/xicheng/",
			"/chongwen/","/xuanwu/","/shijingshan/","/changping/","/tongzhou/",
			"/daxing/","/shunyi/","/huairou/","/fangshan/","/mentougou/","/miyun/",
			"/pinggua/","/yanqing/","/yanjiao/","/zhoubiana/",};
	
	/* 抓取二手房数据
	 * */
	public static String RESOLDAPARTMENT_URL = "http://beijing.anjuke.com/sale";
	
	public static void getResoldApartmentInfo(String region)
	{
		// 首先加载
		Vector<String> log = null;
		synchronized(BJ_RESOLDS)
		{
			log = FileTool.Load(LOG + File.separator + region + "_resold.log", "UTF-8");
		}
		// 2014/12/8 17:16:42
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");//
		
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
			
			String content = HTMLTool.fetchURL(url, "utf-8","get");
			
			if (content == null)
			{
				continue;
			}
			try {
				
				parser.setInputHTML(content);
				parser.setEncoding("utf-8");
	

				NodeFilter filter=new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","house-title"));
				NodeList nodes = parser.extractAllNodesThatMatch(filter);
				
				if (nodes != null)
				{
					for (int n = 0; n < nodes.size(); n ++)
					{
						NodeList nn= nodes.elementAt(n).getChildren();
				
						TagNode tn=(TagNode)nn.elementAt(1);
						String purl = tn.getAttribute("href");
						
						if(purl.startsWith("http"))
						{
						
						String poi2 = parseResold(purl);
						String poi=poi2.replace("&nbsp;", "").replace("&nbsp", "").replace("()", "");						
						System.out.println(poi);
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
										
										newest = new Date();
									}
									
									if (quit)
									{
										break;
									}
									else
									{
										synchronized(BJ_RESOLDS)
										//synchronized是Java语言的关键字，当它用来修饰一个方法或者一个代码块的时候，能够保证在同一时刻最多只有一个线程执行该段代码。
										{
											poi.replace(" ", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").replace("(地图)", "").trim();
											if(url.indexOf("/yanjiao/")!=-1||url.indexOf("/zhoubiana/")!=-1)
												FileTool.Dump(poi,FOLDER2, "UTF-8");
											else
												FileTool.Dump(poi,FOLDER1, "UTF-8");
										}
									}
							}
							
							try {
								Thread.sleep(500 * ((int) (Math
									.max(1, Math.random() * 3))));
							} catch (final InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}catch (NullPointerException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
				}
				
				parser.reset();
				
				// <div class="fanye gray6">  <a class="pageNow">
				 filter = new AndFilter(new TagNameFilter("a"), new HasAttributeFilter("class","aNxt") ); 
				
				nodes = parser.extractAllNodesThatMatch(filter);
				
				if (nodes != null&& nodes.size()==1)
				{
					TagNode tni = (TagNode) nodes.elementAt(0);
					String href = tni.getAttribute("href");
					if ( href != null&&href.startsWith("http:"))
					{
						if (!visited.contains( href))
						{
							int kk = 0;
							for (; kk < urls.size(); kk ++)
							{
								if (urls.elementAt(kk).equalsIgnoreCase( href))
								{
									break;
								}
							}
							
							if (kk == urls.size())
								urls.add(href);
						}
					}
				
								
					
				}
				parser.reset();
				if (quit)
					break;
			}
			catch (ParserException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
		}
		
		synchronized(BJ_RESOLDS)
		{
			File f = new File(LOG + File.separator + region + ".log");
			f.delete();
			if (newest != null)
			{			
				FileTool.Dump(sdf.format(newest), LOG + File.separator + region + ".log", "UTF-8");
			}
		}
	}

	
}
