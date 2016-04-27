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
public class Anjuke_Newhouse{


	private static Random random = new Random();
	/**
	 * java.util.Random类中实现的随机算法是伪随机，也就是有规则的随机，
	 * 所谓有规则的就是在给定种(seed)的区间内随机生成数字；相同种子数的Random对象，
	 * 相同次数生成的随机数字是完全相同的；Random类中各方法生成的随机数字都是均匀分布的，
	 * 也就是说区间内部的数字生成的几率均等；
	 * Random()：创建一个新的随机数生成器。
	 */
	private static String BJ_NEWHOUSE = "NEW";
	public static String FOLDER1 = "/home/gir/crawldata/beijing/anjuke/newhouse/anjuke_newhouse0426.txt";
	public static String FOLDER2 = "/home/gir/crawldata/beijing/anjuke/newhouse/anjuke_newhouse0426_zhoubian.txt";
	public static void main(String[] args) {
		
		for(int i=0;i<regions.length;i++)	
	     getNewBuildingInfo(regions[i]);
	    }
	/* 解析新房 */
	private static String parseNewBuilding(String url)
	{
		//System.out.println(url);
		String content = HTMLTool.fetchURL(url, "utf-8","get");//GB2312是汉字书写国家标准。
		Parser parser = new Parser();//获取解析器
		
		JSONObject jsonObjArr = new JSONObject();
		if (content == null)
		{
			return null;
		}
		
		String poi ="";
		try {
			
			parser.setInputHTML(content);
			parser.setEncoding("utf-8");


			NodeFilter filter=new AndFilter(new TagNameFilter("h1"),new HasAttributeFilter("id","j-triggerlayer"));
			NodeList nodes= parser.extractAllNodesThatMatch(filter); 	
			if (nodes != null && nodes.size() == 1)
			{
				
				String str = nodes.elementAt(0).toPlainTextString().replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
				if(str!=null){
					jsonObjArr.put("title", str);
				}
			}	
			parser.reset();	
			
			Date d = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
			//poi += "<TIME>" + sdf.format(d) + "</TIME>";//发布时间
			jsonObjArr.put("time",sdf.format(d));
			
			parser.reset();

			
				filter = new AndFilter(new TagNameFilter("ul"), new HasAttributeFilter("class", "list"));
				nodes = parser.extractAllNodesThatMatch(filter);
				if(nodes!=null&&nodes.size()>0)
				{
					int k=nodes.size();
				 for(int i=0;i<nodes.size();i++)
				 {
					String str = nodes.elementAt(i).toPlainTextString().replace("现房在售", "").replace("&emsp;", "").replace("&nbsp;", "").replace("\r\n", "").replace("\t", "").replace("，", "").replace("\n", "").replace(",", "").replace(" ", "").trim();
					if(i==0){
						int ix=str.indexOf("楼盘名称");
						int ix1=str.indexOf("楼盘特点");
						if(ix!=-1&&ix1!=-1)
						{
							String sub=str.substring(ix+"楼盘名称".length(),ix1).replace("期房在售", "").replace("楼盘特点", "").replace(" ", "").trim();
							//poi+="<COMMUNITY>"+sub+"</COMMUNITY>";
							jsonObjArr.put("cmmunity",sub);
						}else{
							jsonObjArr.put("cmmunity","null");
						}
						
						 ix=str.indexOf("楼盘特点");
						 ix1=str.indexOf("参考单价");
						if(ix!=-1&&ix1!=-1)
						{
							String sub=str.substring(ix+"楼盘特点".length(),ix1).replace("参考均价", "").replace(" ", "").replace("\n", "").trim();
							//poi+="<CHARACTER>"+sub+"</CHARACTER>";
							jsonObjArr.put("character",sub);
						}else{
							jsonObjArr.put("character","null");
						}
						
						ix=str.indexOf("参考单价");
						ix1=str.indexOf("楼盘总价");
						if(ix!=-1&&ix1!=-1)
						{
							String sub=str.substring(ix+"参考均价".length(),ix1).replace("[价格走势]", "").replace(" ", "").replace("参考均价", "").replace("\r\n", "").replace("\t", "").replace("\b", "").replace("\n", "").trim();
								//poi+="<AVENRAGE_PRICE>"+sub+"</AVENRAGE_PRICE>";
								jsonObjArr.put("unit_price",sub);
						}
						else
						{
							ix=str.indexOf("参考单价");
							ix1=str.indexOf("物业类型");
							if(ix!=-1&&ix1!=-1)
							{
							String sub=str.substring(ix+"参考均价".length(),ix1).replace("[价格走势]", "").replace(" ", "").replace("参考均价", "").replace("\r\n", "").replace("\t", "").replace("\b", "").replace("\n", "").trim();
							//poi+="<AVENRAGE_PRICE>"+sub+"</AVENRAGE_PRICE>";
							jsonObjArr.put("unit_price",sub);
							}else{
								jsonObjArr.put("unit_price","null");
							}
							
						}
						ix=str.indexOf("楼盘总价");
						ix1=str.indexOf("物业类型");
						if(ix!=-1&&ix1!=-1)
						{
							String sub=str.substring(ix+"楼盘总价".length(),ix1).replace("楼盘总价", "").replace(" ", "").trim();
							//poi+="<PRICE>"+sub+"</PRICE>";
							jsonObjArr.put("price",sub);
						}else{
							jsonObjArr.put("price","null");
						}
						ix=str.indexOf("物业类型");
						ix1=str.indexOf("开发商");
						if(ix!=-1&&ix1!=-1&&ix<ix1)
						{
							String sub=str.substring(ix+"物业类型".length(),ix1).replace("物业类型", "").replace(" ", "").trim();
								//poi+="<WUYE_TYPE>"+sub+"</WUYE_TYPE>";
							jsonObjArr.put("property",sub);
						}else{
							jsonObjArr.put("property","null");
						}
						ix=str.indexOf("开发商");
						ix1=str.indexOf("区域位置");
						if(ix!=-1&&ix1!=-1)
						{
								String sub=str.substring(ix+"开发商".length(),ix1).replace("开发商", "").replace(" ", "").trim();
								//poi+="<KAIFASHANG>"+sub+"</KAIFASHANG>";
								jsonObjArr.put("developer",sub);
						}else{
							jsonObjArr.put("developer","null");
						}
						ix=str.indexOf("区域位置");
						ix1=str.indexOf("楼盘地址");
						if(ix!=-1&&ix1!=-1)
						{
								String sub=str.substring(ix+"区域位置".length(),ix1).replace("开发商", "").replace(" ", "").trim();
								//poi+="<KAIFASHANG>"+sub+"</KAIFASHANG>";
								jsonObjArr.put("location",sub);
						}else{
							jsonObjArr.put("location","null");
						}
						ix=str.indexOf("楼盘地址");
						ix1=str.indexOf("[查看地图]");
						if(ix!=-1&&ix1!=-1)
						{
								String sub=str.substring(ix+"楼盘地址".length(),ix1).replace("楼盘地址", "").replace(" ", "").trim();
								//poi+="<ADDRESS>"+sub+"</ADDRESS>";
								jsonObjArr.put("address",sub);
						}else{
							jsonObjArr.put("address","null");
						}
					}
                    if(i==1){
                    	int ix=str.indexOf("最低首付");
    					int ix1=str.indexOf("[房贷计算器]");
    					if(ix!=-1&&ix1!=-1)
    					{
    							String sub=str.substring(ix+"最低首付".length(),ix1).replace("", "").replace(" ", "").trim();
    							//poi+="<DOWN_PAYMENT>"+sub+"</DOWN_PAYMENT>"; 
    							jsonObjArr.put("down_payment",sub);
    					}else{
    						jsonObjArr.put("down_payment","null");
    					}
    					ix=str.indexOf("月供");
    					ix1=str.indexOf("楼盘户型");
    					if(ix!=-1&&ix1!=-1)
    					{
    							String sub=str.substring(ix+"月供".length(),ix1).replace("", "").replace(" ", "").trim();
    							//poi+="<MONTH_PAYMENT>"+sub+"</MONTH_PAYMENT>";
    							jsonObjArr.put("month_payment",sub);
    							
    					}else{
    						jsonObjArr.put("month_payment","null");
    					}
    					ix=str.indexOf("楼盘户型");
    					ix1=str.indexOf("开盘时间");
    					if(ix!=-1&&ix1!=-1)
    					{
    							String sub=str.substring(ix+"在售户型".length(),ix1).replace("", "").replace(" ", "").trim();
    							//poi+="<HOUSE_TYPE>"+sub+"</HOUSE_TYPE>";
    							jsonObjArr.put("house_type",sub);
    					}else{
    						jsonObjArr.put("house_type","null");
    					}
    					ix=str.indexOf("开盘时间");
    					ix1=str.indexOf("交房时间");
    					if(ix!=-1&&ix1!=-1)
    					{
    							String sub=str.substring(ix+"开盘时间".length(),ix1).replace("&emsp;", "").replace(" ", "").trim();
    							//poi+="<OPEN_TIME>"+sub+"</OPEN_TIME>";
    							jsonObjArr.put("open_time",sub);
    					}else{
    						jsonObjArr.put("open_time","null");
    					}
    					ix=str.indexOf("交房时间");
    					ix1=str.indexOf("售楼处地址");
    					if(ix!=-1&&ix1!=-1)
    					{
    							String sub=str.substring(ix+"交房时间".length(),ix1).replace("&emsp;", "").replace(" ", "").trim();
    							//poi+="<COMPLETED_TIME>"+sub+"</COMPLETED_TIME>";
    							jsonObjArr.put("completed_time",sub);
    					}else{
    						jsonObjArr.put("completed_time","null");
    					}
    					ix=str.indexOf("售楼处地址");
    					if(ix!=-1&&ix1!=-1)
    					{
    							String sub=str.substring(ix+"售楼处地址".length()).replace("", "").replace(" ", "").trim();
    							//poi+="<ADDRESS>"+sub+"</ADDRESS>";
    							jsonObjArr.put("sales_address",sub);
    					}else{
    						jsonObjArr.put("sales_address","null");
    					}
					}
                    if(i==2){
                    	int ix=str.indexOf("建筑类型");
    					int ix1=str.indexOf("产权年限");
    					if(ix!=-1&&ix1!=-1)
    					{
    							String sub=str.substring(ix+"".length(),ix1).replace("[查看详情]", "").replace(" ", "").trim();
    							//poi+="<BUILD_TYPE>"+sub+"</BUILD_TYPE>";
    							jsonObjArr.put("biult_type",sub);
    					}else{
    						jsonObjArr.put("biult_type","null");
    					}
    					ix=str.indexOf("产权年限");
    					ix1=str.indexOf("装修标准");
    					if(ix!=-1&&ix1!=-1)
    					{
    							String sub=str.substring(ix+"".length(),ix1).replace("[查看详情]", "").replace(" ", "").trim();
    							//poi+="<PROPERTY_TIME>"+sub+"</PROPERTY_TIME>";
    							jsonObjArr.put("property_life",sub);
    					}else{
    						jsonObjArr.put("property_life","null");
    					}
    					ix=str.indexOf("装修标准");
    					ix1=str.indexOf("容积率");
    					if(ix!=-1&&ix1!=-1)
    					{
    							String sub=str.substring(ix+"装修标准".length(),ix1).replace("[查看详情]", "").replace(" ", "").trim();
    							//poi+="<FITMENT_TYPE>"+sub+"</FITMENT_TYPE>";
    							jsonObjArr.put("fitment",sub);
    					}else{
    						jsonObjArr.put("fitment","null");
    					}
    					ix=str.indexOf("容积率");
    					ix1=str.indexOf("绿化率");
    					if(ix!=-1&&ix1!=-1)
    					{
    							String sub=str.substring(ix+"容积率".length(),ix1).replace("[查看详情]", "").replace(" ", "").trim();
    							//poi+="<VOL_RATE>"+sub+"</VOL_RATE>";
    							jsonObjArr.put("volume_rate",sub);
    					}else{
    						jsonObjArr.put("volume_rate","null");
    					}
    					ix=str.indexOf("绿化率");
    					ix1=str.indexOf("规划户数");
    					if(ix!=-1&&ix1!=-1)
    					{
    							String sub=str.substring(ix+"绿化率".length(),ix1).replace("[查看详情]", "").replace(" ", "").trim();
    							//poi+="<GREEN>"+sub+"</GREEN>";
    							jsonObjArr.put("green_rate",sub);
    					}else{
    						jsonObjArr.put("green_rate","null");
    					}
    					ix=str.indexOf("规划户数");
    					ix1=str.indexOf("楼层状况");
    					if(ix!=-1&&ix1!=-1)
    					{
    							String sub=str.substring(ix+"规划户数".length(),ix1).replace("[查看详情]", "").replace(" ", "").trim();
    							//poi+="<HOUSE_HOLD>"+sub+"</HOUSE_HOLD>";
    							jsonObjArr.put("households",sub);
    					}else{
    						jsonObjArr.put("households","null");
    					}
    					ix=str.indexOf("楼层状况");
    					ix1=str.indexOf("工程进度");
    					if(ix!=-1&&ix1!=-1)
    					{
    							String sub=str.substring(ix+"楼层状况".length(),ix1).replace("", "").replace(" ", "").trim();
    							//poi+="<FLOOR>"+sub+"</FLOOR>";
    							jsonObjArr.put("floor",sub);
    							
    					}else{
    						jsonObjArr.put("floor","null");
    					}
    					ix=str.indexOf("工程进度");
    					ix1=str.indexOf("物业管理费");
    					if(ix!=-1&&ix1!=-1)
    					{
    							String sub=str.substring(ix+"工程进度".length(),ix1).replace("", "").replace(" ", "").trim();
    							//poi+="<WORK_PROGRESS>"+sub+"</WORK_PROGRESS>";
    							jsonObjArr.put("project_progress",sub);
    					}else{
    						jsonObjArr.put("project_progress","null");
    					}
    					ix=str.indexOf("物业管理费");
    					ix1=str.indexOf("物业公司");
    					if(ix!=-1&&ix1!=-1)
    					{
    							String sub=str.substring(ix+"物业管理费".length(),ix1).replace("", "").replace(" ", "").trim();
    							//poi+="<WUYE_FEE>"+sub+"</WUYE_FEE>";
    							jsonObjArr.put("property_fee",sub);
    					}else{
    						jsonObjArr.put("property_fee","null");
    					}
    					ix=str.indexOf("物业公司");
    					ix1=str.indexOf("车位数");
    					if(ix!=-1&&ix1!=-1)
    					{
    							String sub=str.substring(ix+"物业公司".length(),ix1).replace("", "").replace(" ", "").trim();
    							//poi+="<WUYE_COMPANY>"+sub+"</WUYE_COMPANY>";
    							jsonObjArr.put("property_company",sub);
    					}else{
    						jsonObjArr.put("property_company","null");
    					}
    					ix=str.indexOf("车位数");
    					ix1=str.indexOf("车位比");
    					if(ix!=-1&&ix1!=-1)
    					{
    							String sub=str.substring(ix+"车位数".length(),ix1).replace("", "").replace(" ", "").trim();
    							//poi+="<PARK>"+sub+"</PARK>";
    							jsonObjArr.put("park",sub);
    					}else{
    						jsonObjArr.put("park","null");
    					}
    					ix=str.indexOf("车位比");
    					
    					if(ix!=-1)
    					{
    							String sub=str.substring(ix+"车位比".length()).replace("", "").replace(" ", "").trim();
    							//poi+="<PARK_RATE>"+sub+"</PARK_RATE>";
    							jsonObjArr.put("park_rate",sub);
    					}else{
    						jsonObjArr.put("park_rate","null");
    					}
    					
					}
				}
			}
			//System.out.println("poi.ok!");
			jsonObjArr.put("url",url);
			
			poi=jsonObjArr.toString();
			return poi;	
		} catch (ParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch(StringIndexOutOfBoundsException e){
			e.getMessage();
			
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
	public static String LOG = "D:\\test";
	/*
	 * 二手房 http://esf.cq.fang.com/
	 * 出租房  http://zu.cq.fang.com/
     * */
	public static String regions[] = {
			"/chaoyang/","/haidian/","/fengtai/","/dongchenga/","/xicheng/",
			"/chongwen/","/xuanwu/","/shijingshan/","/changping/","/tongzhou/",
			"/daxing/","/shunyi/","/huairou/","/fangshan/","/mentougou/","/miyun/",
			"/pinggua/","/yanqing/","/yanjiao/","/zhoubiana/",};
	
	/*
	 * 新盘
	 *  本月开盘  http://newhouse.cq.fang.com/house/saledate/201502.htm
	 *  top100楼盘 http://newhouse.cq.fang.com/house/asp/trans/buynewhouse/default.htm
	 * */
	public static String NEWBUILDING_URL = "http://bj.fang.anjuke.com/loupan";
	
	public static void getNewBuildingInfo(String region)
	{
		// 首先加载
		Vector<String> log = null;
		synchronized(BJ_NEWHOUSE)
		{
			log = FileTool.Load(LOG + File.separator + region + "_new.log", "UTF-8");
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
		
		String url = NEWBUILDING_URL + region;
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
			
				
				NodeFilter filter=new AndFilter(new TagNameFilter("div"),new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","key-list"))));

				NodeList nodes = parser.extractAllNodesThatMatch(filter);
				
				if (nodes != null)
				{
					for (int n = 0; n < nodes.size(); n ++)
					{
						TagNode tn = (TagNode)nodes.elementAt(n);
						String purl = tn.getAttribute("data-link");
						
						if(purl.startsWith("http://"))
						{
						int a=purl.indexOf("loupan/");
						String url_can=purl.substring(0,a+"loupan/".length())+"canshu-"+purl.substring(a+"loupan/".length(),purl.length());
						//System.out.println(url_can);
						
						String poi2 = parseNewBuilding(url_can);
						String poi=poi2.replace("&nbsp;", "").replace("&nbsp", "").replace("()", "");
						System.out.println(poi);
							if (poi != null)
							{
								// 获取时间
								int m = poi.indexOf("<TIME>");
								int k = poi.indexOf("</TIME>");
								
								if (m != -1 && k != -1)
								{
									assert(m < k);
									String tm = poi.substring(m + "<TIME>".length(), k);
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
										synchronized(BJ_NEWHOUSE)
										//synchronized是Java语言的关键字，当它用来修饰一个方法或者一个代码块的时候，能够保证在同一时刻最多只有一个线程执行该段代码。
										{
											poi.replace(" ", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
											if(url.indexOf("/yanjiao/")!=-1||url.indexOf("/zhoubiana/")!=-1)
												FileTool.Dump(poi,FOLDER2, "UTF-8");
											else
												FileTool.Dump(poi,FOLDER1, "UTF-8");
												
										}
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
				
				// <div class="fanye gray6">  <a class="pageNow">
				filter = new AndFilter(new TagNameFilter("a"),new HasAttributeFilter("class","next-page next-link")); 
				
				nodes = parser.extractAllNodesThatMatch(filter);
				
				if (nodes != null&& nodes.size()==1)
				{
					TagNode tni = (TagNode) nodes.elementAt(0);
					String href = tni.getAttribute("href");
					if ( href != null&&href.startsWith("http:"))
					{
						if (!visited.contains(href))
						{
							int kk = 0;
							for (; kk < urls.size(); kk ++)
							{
								if (urls.elementAt(kk).equalsIgnoreCase(href))
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
		
		synchronized(BJ_NEWHOUSE)
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
