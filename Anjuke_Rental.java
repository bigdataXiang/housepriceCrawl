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
public class Anjuke_Rental {


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
	public static String FOLDER = "/home/gir/crawldata/beijing/anjuke/rental/anjuke_rental0426.txt";
	
   public static void main(String[] args) {
		
		
		getRentalInfo();//929_ok!
		
		
	}

	
	/* 解析求租页面 */
	private static String parseRental(String url)
	{
		String content = HTMLTool.fetchURL(url, "utf-8","get");
		Parser parser = new Parser();
		JSONObject jsonObjArr = new JSONObject();
		if (content == null)
		{
			return null;
		}
		
		String poi ="";
		try {
			
			parser.setInputHTML(content);
			parser.setEncoding("utf-8");


			NodeFilter filter=new AndFilter(new TagNameFilter("h1"),new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","w headline"))));
			NodeList nodes= parser.extractAllNodesThatMatch(filter); 
//						if(nodes==null)
//						{
//							filter=new AndFilter(new TagNameFilter("h1"),new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","bigtitle"))));
//							nodes= parser.extractAllNodesThatMatch(filter);  
//						}
			if (nodes != null && nodes.size() == 1)
			{
				
				String str = nodes.elementAt(0).toPlainTextString().replace("\r\n", "").replace("\t", "").trim();
				
				String tt=str.replace("，",",").replace("、",",").replace("《","【").replace("》","】").replace("=", "").replace("-", "");
				tt=tt.replace(",", "");
				int ix = tt.indexOf("【");
				int ix2=tt.indexOf("】");
				if(ix!=-1&&ix2!=-1)
				{
					String t1=tt.substring(0,ix);
					String t2=tt.substring(ix2+"】".length());
					String sub=t1.replace(" ", "").trim() + t2.replace(" ", "").trim();
					jsonObjArr.put("title",sub);
					//poi += "<TITLE>" +  +"</TITLE>";//大标题
				}
				else
				{
					String sub= tt.replace(" ", "").trim();
					jsonObjArr.put("title",sub);
				}
				
				parser.reset();
					
			}		

			filter=new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","other"));
			nodes=parser.extractAllNodesThatMatch(filter);  
			if (nodes != null && nodes.size() == 1)
			{
				
				String str2 = nodes.elementAt(0).toPlainTextString().replace("\r\n", "").replace("\t", "").replace(" ", "").trim();	
				int ix=str2.indexOf("发布时间：");
				int ix2=str2.indexOf("浏览");
				String str=null;
				if(ix!=-1&&ix!=-2)
				{
					str=str2.substring(ix+"发布时间：".length(), ix2);
					
				}		
				if (str.replace(" ", "").trim().startsWith("vardate=newDate()")||str==null)
				{
					Date d = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
					jsonObjArr.put("time",sdf.format(d));
					//poi += "<TIME>" + sdf.format(d) + "</TIME>";//发布时间
					
				}
				else{
					jsonObjArr.put("time",str.replace(" ", "").trim());
				}			
				parser.reset();
					
			}	



				filter = new AndFilter(new TagNameFilter("li"), new HasParentFilter(new AndFilter(new TagNameFilter("ul"), new HasAttributeFilter("class", "info")))); 
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
							tt = tt.replace("（", "(").replace("）", ")");
							
							
							
							int ix = tt.indexOf("区域：");
							
							if (ix != -1)
							{
								String sub = tt.substring(ix + "区域：".length()).replace("\r\n", "").replace("\t", "").trim();
								if (sub.indexOf("暂无") == -1)
									jsonObjArr.put("location", sub);
									//poi += "<DISTRICT>" + sub.replace(" ", "") + "</DISTRICT>";
								continue;
							}

							ix = tt.indexOf("居室：");
							int ix2=tt.indexOf(";document");
							
							if (ix != -1&&ix2!=-1)
							{

								String sub = tt.substring(ix + "居室：".length(),ix2).replace("\"", "").replace("vartmp=", "").replace("\r\n", "").replace("\t", "").replace(" ", "").trim();//"整租两室"
								if (sub.indexOf("暂无") == -1)
									jsonObjArr.put("house_type", sub.replace("vartmp=", ""));
									//poi += "<TYPE>" + sub.replace(" ", "") + "</TYPE>";
								continue;
							}
							
							

							ix = tt.indexOf("租金：");
							
							if (ix != -1)
							{

								String sub = tt.substring(ix + "租金：".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace("看中了，一键搬家", "").trim();
								if (sub.indexOf("暂无") == -1)
									//poi += "<PRICE>" + sub.replace(" ", "") + "</PRICE>";
									jsonObjArr.put("price", sub);
								
								continue;
							}


							ix = tt.indexOf("入住：");
							
							if (ix != -1)
							{

								String sub = tt.substring(ix + "入住：".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
								if (sub.indexOf("暂无") == -1)
									//poi += "<CHECKIN>" + sub.replace(" ", "") + "</CHECKIN>";
									jsonObjArr.put("checkin", sub);
									

								continue;
							}

						
						}
					}
					jsonObjArr.put("url",url);
					
					poi=jsonObjArr.toString();
		     }
		} catch (ParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
	/* 抓取求租数据
	 * */
	public static String RENTAL_URL = "http://BJ.58.com";
	
	public static void getRentalInfo()
	{
		// 首先加载
		Vector<String> log = null;
		synchronized(BJ_RENTINFO)
		{
			log = FileTool.Load(LOG + File.separator + "rental.log", "UTF-8");
		}
		
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
		
		String url = RENTAL_URL+"/qiuzu/";
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
				// <dd class="info rel floatr">
				// <p class="title"><a href="/qiuzu/3_153703104.htm" target="_blank" title="冉家坝龙山小学旁 光宇阳光地中海精装三房急售 无营业税拎包入住">冉家坝龙山小学旁 光宇阳光地中海精装三房急售 无营业税拎包入住</a></p>					
				// <p class="title"
				NodeFilter filter = new AndFilter(new TagNameFilter("a"), new HasAttributeFilter("class","t") ); 
				NodeList nodes = parser.extractAllNodesThatMatch(filter);
				if (nodes != null)
				{
					for (int n = 0; n < nodes.size(); n ++)
					{
						TagNode tn = (TagNode)nodes.elementAt(n);
						String purl = tn.getAttribute("href");
						
						if(purl.startsWith("http"))
						{
							//System.out.println(purl);
						String poi2 = parseRental( purl);
						String poi=poi2.replace("&nbsp;", "").replace("&nbsp", "").replace("vartmp=", "").replace("", "");
						System.out.println(poi);
						// 获取时间
						if (poi != null)
						{
							// 获取时间
							JSONObject jsonObject = JSONObject.fromObject(poi);
                            String tm=jsonObject.get("time").toString();
								if(tm.indexOf("-")!=-1)
									tm=tm.replace("-", "/");
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
									synchronized(BJ_RENTOUT)
									//synchronized是Java语言的关键字，当它用来修饰一个方法或者一个代码块的时候，能够保证在同一时刻最多只有一个线程执行该段代码。
									{
										poi.replace(" ", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
										FileTool.Dump(poi, FOLDER, "UTF-8");
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
				filter = new AndFilter(new TagNameFilter("a"),new HasAttributeFilter("class","next")); 
				
				nodes = parser.extractAllNodesThatMatch(filter);
				
				if (nodes != null&& nodes.size()==1)
				{
					TagNode tni = (TagNode) nodes.elementAt(0);
					String href = tni.getAttribute("href");
					if ( href != null)
					{
						if (!visited.contains("http://BJ.58.com" + href))
						{
							int kk = 0;
							for (; kk < urls.size(); kk ++)
							{
								if (urls.elementAt(kk).equalsIgnoreCase("http://BJ.58.com" + href))
								{
									break;
								}
							}
							
							if (kk == urls.size())
								urls.add("http://BJ.58.com" + href);
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
		
		synchronized(BJ_RENTINFO)
		{
			File f = new File(LOG + File.separator + "rental.log");
			f.delete();
			if (newest != null)
			{			
				FileTool.Dump(sdf.format(newest), LOG + File.separator + "rental.log", "UTF-8");
			}
		}
		
	}
	
	/* 求租 http://zu.cq.fang.com/qiuzu/
	 * */	
	/**
	 * @param args
	 */
	
}
