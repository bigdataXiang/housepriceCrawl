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

public class Fang_Rental {


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
	
	public static String FOLDER = "/home/gir/crawldata/beijing/fang/rental/fang_rental0414.txt";
	public static void main(String[] args) {

		getRentalInfo();//已经调试好917
	
	}
	
	/* 解析求租页面 */
	private static String parseRental(String url)
	{
		String content = HTMLTool.fetchURL(url, "gb2312", "get");

		Parser parser = new Parser();
		JSONObject jsonObjArr = new JSONObject();
		if (content == null)
		{
			return null;
		}
		
		String poi = null;
		try {
			
			parser.setInputHTML(content);
			parser.setEncoding("gb2312");
			// 获取发布时间
			// div class="title"
			NodeFilter filter = new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "title"));
			NodeList nodes = parser.extractAllNodesThatMatch(filter);
			
			if (nodes != null && nodes.size() == 1)
			{
				nodes = nodes.elementAt(0).getChildren();
				for (int n = 0; n < nodes.size(); n ++)
				{
					String str = nodes.elementAt(n).toPlainTextString().trim();
					String substr="";
					int mm = str.indexOf("]");
					if (mm != -1)
					{
						if (poi == null){
							substr=str.substring(mm + 1).replace("\r\n", "").replace("\n", "").replace("\b", "").replace("\t", "");
							jsonObjArr.put("title", substr);
						}else{
							substr=str.substring(mm + 1);
							jsonObjArr.put("title", substr);
						}
						continue;
					}
					
					mm = str.indexOf("发布");
					if (mm != -1)
					{
						if (poi == null){
							substr=str.replace("发布", "").replace("-", "/").replace("\r\n", "").replace("\n", "").replace("\b", "").replace("\t", "");
							jsonObjArr.put("time", substr);
						}else{
							substr=str.replace("发布", "").replace("-", "/").replace("\r\n", "").replace("\n", "").replace("\b", "").replace("\t", "");
							jsonObjArr.put("time", substr);
						}
							
					}
				}
			}
			parser.reset();
			// class="house"
			filter = new AndFilter(new TagNameFilter("dl"), new HasAttributeFilter("class", "house"));
			// 期望租金：1500元/月 期望面积： 不小于65平米 租赁方式：整租 求租地点：九龙坡，石桥铺   期望户型：二居房屋配套：暂无资料
			nodes = parser.extractAllNodesThatMatch(filter);
			
			if (nodes != null)
			{
				for (int n = 0; n < nodes.size(); n ++)
				{
					Node no = nodes.elementAt(n);
					if (no instanceof TagNode)
					{
						TagNode tno = (TagNode) no;
						String str = tno.toPlainTextString().replace(" ", "").replace("\t", "").replace("\r\n", "").replace("\n", "").replace("\b", "").replace("\t", "").trim();
						/**
						 * stringObj.split([separator，[limit]]) 
						 * stringObj:必选项。要被分解的 String 对象或文字。该对象不会被 split 方法修改。 
						 * separator :可选项。字符串或 正则表达式对象，它标识了分隔字符串时使用的是一个还是多个字符。如果忽略该选项，返回包含整个字符串的单一元素数组。
						 * limit:可选项。该值用来限制返回数组中的元素个数。
						 * split 方法的结果是一个字符串数组，在 stingObj 中每个出现 separator 的位置都要进行分解
						 */
						int kk=str.indexOf("期望租金：");
						if (kk != -1)
						{
							String substr = str.substring(kk + "期望租金：".length(),str.indexOf("面积：")-"期望".length());
								//poi += "<PRICE>" + substr + "</PRICE>";
							jsonObjArr.put("price", substr);
						}
					   kk=str.indexOf("期望面积：");
						if (kk != -1)
						{
							String substr = str.substring(kk + "期望面积：".length(),str.indexOf("租赁方式："));
							//poi += "<AREA>" + substr + "</AREA>";
							jsonObjArr.put("area", substr);
						}
						kk=str.indexOf("租赁方式：");
						if (kk != -1)
						{
							String substr = str.substring(kk + "租赁方式：".length(),str.indexOf("求租地点："));
							//poi += "<SCHEMA>" + substr + "</SCHEMA>";
							jsonObjArr.put("rent_type", substr);
							
						}
						kk=str.indexOf("求租地点：");
						if (kk != -1)
						{
							String substr = str.substring(kk + "求租地点：".length(),str.indexOf("户型")-"期望".length());
							//poi += "<ADDRESS>" + substr + "</ADDRESS>";
							jsonObjArr.put("location", substr);
						
						}
						kk=str.indexOf("期望户型：");
						if (kk != -1)
						{
							String substr = str.substring(kk + "期望户型：".length(),str.indexOf("房屋配套："));
							//poi += "<BUILDTYPE>" + substr + "</BUILDTYPE>";
							jsonObjArr.put("house_type", substr);
						}
						kk=str.indexOf("房屋配套：");
						if (kk != -1)
						{
							String substr = str.substring(kk + "房屋配套：".length());
							//poi += "<EQUITMENT>" + substr + "</EQUITMENT>";
							jsonObjArr.put("facility", substr);
						}
						
						}						
					}
				}
			
			parser.reset();
			// <span class="tel"> <span>15922777917</span><span class="font14 gray6 master">小李</span> </span>
			filter = new AndFilter(new TagNameFilter("span"), new HasAttributeFilter("class", "qiuzutel"));
			nodes = parser.extractAllNodesThatMatch(filter);
			if (nodes != null)
			{
				String s = nodes.elementAt(0).toPlainTextString().replace(" ", "").replace("\t", "").replace("\r\n", "").trim();
				//poi += "<CONTACT>" + s + "</CONTACT>";
				jsonObjArr.put("contact", s);
			}
						
			parser.reset();
			// <p class="beizhu">家电齐全，价钱看房后面议</p>
			filter = new AndFilter(new TagNameFilter("p"), new HasAttributeFilter("class", "beizhu"));
			nodes = parser.extractAllNodesThatMatch(filter);
			if (nodes != null)
			{
				String s = nodes.elementAt(0).toPlainTextString().replace(" ", "").replace("\t", "").replace("\r\n", "").trim();
				//poi += "<NOTATION>" + s + "</NOTATION>";
				jsonObjArr.put("notice", s);
				
			}
			
		} catch (ParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		jsonObjArr.put("url",url);
		poi=jsonObjArr.toString().replace("", "").replace("&nbsp;", "").trim();
		System.out.println(poi);
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

	
	/* 抓取求租数据
	 * */
	public static String RENTAL_URL = "http://zu.fang.com/qiuzu/h316/";
	
	public static void getRentalInfo()
	{
		// 首先加载
		Vector<String> log = null;
		synchronized(BJ_RENTINFO)
		{
			log = FileTool.Load(FOLDER + File.separator + "rental.log", "UTF-8");
		}
		
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");//小写的mm表示的是分钟
		
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
		
		String url = RENTAL_URL;
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
				// <p class="title"><a href="/qiuzu/3_153703104.htm" target="_blank" title="冉家坝龙山小学旁 光宇阳光地中海精装三房急售 无营业税拎包入住">冉家坝龙山小学旁 光宇阳光地中海精装三房急售 无营业税拎包入住</a></p>					
				// <p class="title"
				HasParentFilter parentFilter = new HasParentFilter(new AndFilter(new TagNameFilter("p"), new HasAttributeFilter("class", "title")));
				NodeFilter filter = new AndFilter(new TagNameFilter("a"), new AndFilter(new AndFilter(parentFilter, new HasAttributeFilter("target")), new HasAttributeFilter("href"))); 
				
				NodeList nodes = parser.extractAllNodesThatMatch(filter);
				
				if (nodes != null)
				{
					for (int n = 0; n < nodes.size(); n ++)
					{
						TagNode tn = (TagNode)nodes.elementAt(n);
						String purl = tn.getAttribute("href");
						if (purl.startsWith("/qiuzu"))
						{
							String poi = parseRental("http://zu.fang.com" + purl);
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
										synchronized(BJ_RENTINFO)
										{
											
											poi.replace(" ", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
											FileTool.Dump(poi, FOLDER , "UTF-8");
											//System.out.println(poi);
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
				filter = new AndFilter(new TagNameFilter("div"), 
					new HasChildFilter(new AndFilter(new TagNameFilter("a"), new HasAttributeFilter("class", "pageNow")))); 
				
				nodes = parser.extractAllNodesThatMatch(filter);
				
				if (nodes != null)
				{
					for (int nn = 0; nn < nodes.size(); nn ++)
					{
						NodeList cld = nodes.elementAt(nn).getChildren();
						if (cld == null)
							continue;
						
						for (int jj = 0; jj < cld.size(); jj ++)
						{
							if (cld.elementAt(jj) instanceof TagNode)
							{
								TagNode tni = (TagNode) cld.elementAt(jj);
								String href = tni.getAttribute("href");
								if (tni.getTagName().equalsIgnoreCase("a") && tni.toPlainTextString().indexOf("下一页") == -1 &&  tni.toPlainTextString().indexOf("末页") == -1 && href != null)
								{
									if (!visited.contains("http://zu.fang.com" + href))
									{
										int kk = 0;
										for (; kk < urls.size(); kk ++)
										{
											if (urls.elementAt(kk).equalsIgnoreCase("http://zu.fang.com" + href))
											{
												break;
											}
										}
										
										if (kk == urls.size())
											urls.add("http://zu.fang.com" + href);
									}
								}
							}
						}
					}
				}
				
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
			File f = new File(FOLDER + File.separator + "rental.log");
			f.delete();
			if (newest != null)
			{			
				FileTool.Dump(sdf.format(newest), FOLDER + File.separator + "rental.log", "UTF-8");
			}
		}
		
	}
	
}
