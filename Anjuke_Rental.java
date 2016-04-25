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
	public static String FOLDER = "/home/gir/crawldata/beijing/anjuke/rental/anjuke_rental0414.txt";
	
   public static void main(String[] args) {
		
		
		getRentalInfo();//929_ok!
		
		
	}
	/* 解析出租页面 */
	private static String parseRentOut(String url)
	{
		

		System.out.println(url);
		String content = HTMLTool.fetchURL(url, "utf-8","get");//GB2312是汉字书写国家标准。
		System.out.println("url.ok!");
	
	
		Parser parser = new Parser();//获取解析器
		if (content == null)
		{
			return null;
		}
		
		String poi ="";
		try {
			
			parser.setInputHTML(content);
			parser.setEncoding("utf-8");


			NodeFilter filter=new AndFilter(new TagNameFilter("h3"),new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","tit cf"))));
			NodeList nodes= parser.extractAllNodesThatMatch(filter); 	
			if (nodes != null && nodes.size() == 1)
			{
				
				String str = nodes.elementAt(0).toPlainTextString().replace("随时", "").replace("入住", "").replace("看房", "").replace("\r\n", "").replace("\t", "").replace("爱屋吉屋", "").replace("佣金1%", "").trim();

				String tt=str.replace("，","").replace("、","").replace("《","【").replace("》","】").replace("[","【").replace("]","】").replace("=", "").replace("-", "").replace(",", "").replace("！", "").trim();

				int ix = tt.indexOf("【");
				int ix2=tt.indexOf("】");
				if(ix!=-1&&ix2!=-1)
				{
					String t1=tt.substring(0,ix);
					String t2=tt.substring(ix2+"】".length());
					poi += "<TITLE>" + t1.replace(" ", "").trim() + t2.replace(" ", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim() +"</TITLE>";//大标题
				}
				else
				{
					poi += "<TITLE>" + tt.replace(" ", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim() + "</TITLE>";//大标题
				}
				parser.reset();
				
				
				Date d = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
				poi += "<TIME>" + sdf.format(d) + "</TIME>";//发布时间
			
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
							tt = ni.toPlainTextString().trim();
							tt = tt.replace("（", "(").replace("）", ")").replace("、", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","");
							
							
							

							 ix = tt.indexOf("租金押付");
							if (ix != -1)
							{

								String sub = tt.substring(ix + "租金押付".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1)
									poi += "<DEPOSIT>" + sub.replace(" ", "") + "</DEPOSIT>";
								continue;
							}
							
							ix = tt.indexOf("租金");
							
							if (ix != -1)
							{
								String sub = tt.substring(ix + "租金".length()).replace("\r\n", "").replace("\t", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1)
									poi += "<PRICE>" + sub.replace(" ", "") + "</PRICE>";
								continue;
							}
							ix = tt.indexOf("房型");						
							if (ix != -1)
							{
								String sub = tt.substring(ix + "房型".length()).replace("\r\n", "").replace("\t", "").replace("开盘通知我", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<HOUSE_TYPE>" + sub.replace(" ", "") + "</HOUSE_TYPE>";
								continue;
							}
							
							ix = tt.indexOf("租赁方式");						
							if (ix != -1)
							{
								String sub = tt.substring(ix + "租赁方式".length()).replace("\r\n", "").replace("\t", "").replace("开盘通知我", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<RENT_TYPE>" + sub.replace(" ", "") + "</RENT_TYPE>";
								continue;
							}


								
							ix = tt.indexOf("所在小区");	
							ix2=tt.indexOf("(");
							if (ix != -1&&ix2>ix)
							{

								String sub = tt.substring(ix + "所在小区".length(),ix2).replace("\r\n", "").replace("\t", "").replace(" ", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<COMMUNITY>" + sub.replace(" ", "") + "</COMMUNITY>";

								continue;
							}

							ix = tt.indexOf("位置");						
							if (ix != -1)
							{

								String sub = tt.substring(ix + "位置".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<LOCATION>" + sub.replace(" ", "") + "</LOCATION>";

								continue;
							}
							ix = tt.indexOf("小区名");						
							if (ix != -1)
							{

								String sub = tt.substring(ix + "小区名".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<COMMUNITY>" + sub.replace(" ", "") + "</COMMUNITY>";

								continue;
							}
							ix = tt.indexOf("地址");
							if (ix != -1)
							{

								String sub = tt.substring(ix + "地址".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<ADDRESS>" + sub.replace(" ", "") + "</ADDRESS>";

								continue;
							}
							ix = tt.indexOf("开发商");						
							if (ix != -1)
							{

								String sub = tt.substring(ix + "开发商".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<DEVELOPER>" + sub.replace(" ", "") + "</DEVELOPER>";

								continue;
							}
							ix = tt.indexOf("物业公司");						
							if (ix != -1)
							{

								String sub = tt.substring(ix + "物业公司".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<PROPERTY>" + sub.replace(" ", "") + "</PROPERTY>";

								continue;
							}
							ix = tt.indexOf("物业类型");						
							if (ix != -1)
							{

								String sub = tt.substring(ix + "物业类型".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<PROPERTY_TYPE>" + sub.replace(" ", "") + "</PROPERTY_TYPE>";

								continue;
							}
							ix = tt.indexOf("物业费用");						
							if (ix != -1)
							{

								String sub = tt.substring(ix + "物业费用".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace("(绿化率高)","").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<PROPERTY_FEE>" + sub.replace(" ", "") + "</PROPERTY_FEE>";

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
					for (int mm = 0; mm <nodes.size(); mm ++)
					{
						Node ni = nodes.elementAt(mm);
							
						if (ni instanceof TagNode && ((TagNode)ni).getTagName().equalsIgnoreCase("dl"))
							//equals是比较2个字符串是否相等的，区分大小写 equalsignorecase功能一样，但是不区分大小写
							//instanceof是Java的一个二元操作符,它的作用是测试它左边的对象是否是它右边的类的实例，返回boolean类型的数据。
						{
							tt = ni.toPlainTextString().trim();
							tt = tt.replace("（", "(").replace("）", ")").replace("、", ",");
					
							 ix = tt.indexOf("装修");	
							if (ix != -1)
							{

								String sub = tt.substring(ix + "装修".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<FITMENT>" + sub.replace(" ", "") + "</FITMENT>";
								
								continue;
							}

							ix = tt.indexOf("面积");
							int iy=tt.indexOf("总建");
							if (ix != -1&& iy==-1)
							{

								String sub = tt.substring(ix + "面积".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<HOUSE_AREA>" + sub.replace(" ", "") + "</HOUSE_AREA>";
								
								continue;
							}
							
							ix = tt.indexOf("总建面积");	
							if (ix != -1)
							{

								String sub = tt.substring(ix + "总建面积".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").replace("平方米", "").replace("(中型小区)", "").replace("(大型小区)", "").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<TOTAL_AREA>" + sub.replace(" ", "") + "</TOTAL_AREA>";
								
								continue;
							}					

							ix = tt.indexOf("朝向");	
							if (ix != -1)
							{

								String sub = tt.substring(ix + "朝向".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<ORIENTATION>" + sub.replace(" ", "") + "</ORIENTATION>";
								
								continue;
							}

							ix = tt.indexOf("楼层");	
							if (ix != -1)
							{

								String sub = tt.substring(ix + "楼层".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<FLOOR>" + sub.replace(" ", "") + "</FLOOR>";
								
								continue;
							}

							
							ix = tt.indexOf("类型");	
							if (ix != -1)
							{

								String sub = tt.substring(ix + "类型".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<TYPE>" + sub.replace(" ", "") + "</TYPE>";
								
								continue;
							}
							ix = tt.indexOf("总户数");	
							if (ix != -1)
							{

								String sub = tt.substring(ix + "总户数".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<HOUSEHOLDS>" + sub.replace(" ", "") + "</HOUSEHOLDS>";
								
								continue;
							}
							ix = tt.indexOf("建造年代");	
							if (ix != -1)
							{

								String sub = tt.substring(ix + "建造年代".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<BUILT_YEAR>" + sub.replace(" ", "") + "</BUILT_YEAR>";
								
								continue;
							}
							ix = tt.indexOf("容积率");	
							if (ix != -1)
							{

								String sub = tt.substring(ix + "容积率".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<VOLUME_RATE>" + sub.replace(" ", "") + "</VOLUME_RATE>";
								
								continue;
							}
							
							ix = tt.indexOf("停车位");	
							if (ix != -1)
							{

								String sub = tt.substring(ix + "停车位".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<PARK>" + sub.replace(" ", "") + "</PARK>";
								
								continue;
							}
							ix = tt.indexOf("绿化率");	
							if (ix != -1)
							{

								String sub = tt.substring(ix + "绿化率".length(),tt.indexOf("%")+"%".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").replace("(绿化率高)","").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<GREEN_RATE>" + sub.replace(" ", "") + "</GREEN_RATE>";
								
								continue;
							}

							
						}
					}
				
												
					
					System.out.println("poi.ok!");
					return "<POI>" + poi + "<URL>" + url + "</URL></POI>";
		}
				
				
			}	
			else
			{
				parser.reset();
				 filter=new AndFilter(new TagNameFilter("h1"),new HasAttributeFilter("class","title f16 txt_c"));
				 nodes= parser.extractAllNodesThatMatch(filter); 	
				if (nodes != null && nodes.size() == 1)
				{
					TagNode tn=(TagNode)nodes.elementAt(0);
					
					String str = tn.getAttribute("title").replace("随时", "").replace("入住", "").replace("看房", "").replace("\r\n", "").replace("\t", "").replace("爱屋吉屋", "").replace("佣金1%", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
					String tt=str.replace("，","").replace("、","").replace("《","【").replace("》","】").replace("[","【").replace("]","】").replace("=", "").replace("-", "").replace(",", "").replace("！", "").trim();

					int ix = tt.indexOf("【");
					int ix2=tt.indexOf("】");
					if(ix!=-1&&ix2!=-1)
					{
						String t1=tt.substring(0,ix);
						String t2=tt.substring(ix2+"】".length());
						poi += "<TITLE>" + t1.replace(" ", "").trim() + t2.replace(" ", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim() +"</TITLE>";//大标题
					}
					else
					{
						poi += "<TITLE>" + tt.replace(" ", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim() + "</TITLE>";//大标题
					}
					parser.reset();
					
					
					Date d = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
					poi += "<TIME>" + sdf.format(d) + "</TIME>";//发布时间
				
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
								tt = ni.toPlainTextString().trim();
								tt = tt.replace("（", "(").replace("）", ")").replace("、", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","");
								
								
								

								 ix = tt.indexOf("租金押付");
								if (ix != -1)
								{

									String sub = tt.substring(ix + "租金押付".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
									if (sub!=null&&sub.indexOf("暂无") == -1)
										poi += "<DEPOSIT>" + sub.replace(" ", "") + "</DEPOSIT>";
									continue;
								}
								
								ix = tt.indexOf("租金");
								
								if (ix != -1)
								{
									String sub = tt.substring(ix + "租金".length()).replace("\r\n", "").replace("\t", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
									if (sub!=null&&sub.indexOf("暂无") == -1)
										poi += "<PRICE>" + sub.replace(" ", "") + "</PRICE>";
									continue;
								}
								ix = tt.indexOf("房型");						
								if (ix != -1)
								{
									String sub = tt.substring(ix + "房型".length()).replace("\r\n", "").replace("\t", "").replace("开盘通知我", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
									if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
										poi += "<HOUSE_TYPE>" + sub.replace(" ", "") + "</HOUSE_TYPE>";
									continue;
								}
								
								ix = tt.indexOf("租赁方式");						
								if (ix != -1)
								{
									String sub = tt.substring(ix + "租赁方式".length()).replace("\r\n", "").replace("\t", "").replace("开盘通知我", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
									if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
										poi += "<RENT_TYPE>" + sub.replace(" ", "") + "</RENT_TYPE>";
									continue;
								}


									
								ix = tt.indexOf("所在小区");	
								ix2=tt.indexOf("(");
								if (ix != -1&&ix2>ix)
								{

									String sub = tt.substring(ix + "所在小区".length(),ix2).replace("\r\n", "").replace("\t", "").replace(" ", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
									if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
										poi += "<COMMUNITY>" + sub.replace(" ", "") + "</COMMUNITY>";

									continue;
								}

								ix = tt.indexOf("位置");						
								if (ix != -1)
								{

									String sub = tt.substring(ix + "位置".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
									if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
										poi += "<LOCATION>" + sub.replace(" ", "") + "</LOCATION>";

									continue;
								}
								ix = tt.indexOf("小区名");						
								if (ix != -1)
								{

									String sub = tt.substring(ix + "小区名".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
									if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
										poi += "<COMMUNITY>" + sub.replace(" ", "") + "</COMMUNITY>";

									continue;
								}
								ix = tt.indexOf("地址");
								if (ix != -1)
								{

									String sub = tt.substring(ix + "地址".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
									if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
										poi += "<ADDRESS>" + sub.replace(" ", "") + "</ADDRESS>";

									continue;
								}
								ix = tt.indexOf("开发商");						
								if (ix != -1)
								{

									String sub = tt.substring(ix + "开发商".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
									if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
										poi += "<DEVELOPER>" + sub.replace(" ", "") + "</DEVELOPER>";

									continue;
								}
								ix = tt.indexOf("物业公司");						
								if (ix != -1)
								{

									String sub = tt.substring(ix + "物业公司".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
									if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
										poi += "<PROPERTY>" + sub.replace(" ", "") + "</PROPERTY>";

									continue;
								}
								ix = tt.indexOf("物业类型");						
								if (ix != -1)
								{

									String sub = tt.substring(ix + "物业类型".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
									if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
										poi += "<PROPERTY_TYPE>" + sub.replace(" ", "") + "</PROPERTY_TYPE>";

									continue;
								}
								ix = tt.indexOf("物业费用");						
								if (ix != -1)
								{

									String sub = tt.substring(ix + "物业费用".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace("(绿化率高)","").trim();
									if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
										poi += "<PROPERTY_FEE>" + sub.replace(" ", "") + "</PROPERTY_FEE>";

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
						for (int mm = 0; mm <nodes.size(); mm ++)
						{
							Node ni = nodes.elementAt(mm);
								
							if (ni instanceof TagNode && ((TagNode)ni).getTagName().equalsIgnoreCase("dl"))
								//equals是比较2个字符串是否相等的，区分大小写 equalsignorecase功能一样，但是不区分大小写
								//instanceof是Java的一个二元操作符,它的作用是测试它左边的对象是否是它右边的类的实例，返回boolean类型的数据。
							{
								tt = ni.toPlainTextString().trim();
								tt = tt.replace("（", "(").replace("）", ")").replace("、", ",").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","");
						
								 ix = tt.indexOf("装修");	
									if (ix != -1)
									{

										String sub = tt.substring(ix + "装修".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
										if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
											poi += "<FITMENT>" + sub.replace(" ", "") + "</FITMENT>";
										
										continue;
									}

									ix = tt.indexOf("面积");
									int iy=tt.indexOf("总建");
									if (ix != -1&& iy==-1)
									{

										String sub = tt.substring(ix + "面积".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
										if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
											poi += "<HOUSE_AREA>" + sub.replace(" ", "") + "</HOUSE_AREA>";
										
										continue;
									}
									
									ix = tt.indexOf("总建面积");	
									if (ix != -1)
									{

										String sub = tt.substring(ix + "总建面积".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").replace("平方米", "").replace("(中型小区)", "").replace("(大型小区)", "").trim();
										if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
											poi += "<TOTAL_AREA>" + sub.replace(" ", "") + "</TOTAL_AREA>";
										
										continue;
									}					

									ix = tt.indexOf("朝向");	
									if (ix != -1)
									{

										String sub = tt.substring(ix + "朝向".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
										if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
											poi += "<ORIENTATION>" + sub.replace(" ", "") + "</ORIENTATION>";
										
										continue;
									}

									ix = tt.indexOf("楼层");	
									if (ix != -1)
									{

										String sub = tt.substring(ix + "楼层".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
										if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
											poi += "<FLOOR>" + sub.replace(" ", "") + "</FLOOR>";
										
										continue;
									}

									
									ix = tt.indexOf("类型");	
									if (ix != -1)
									{

										String sub = tt.substring(ix + "类型".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
										if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
											poi += "<TYPE>" + sub.replace(" ", "") + "</TYPE>";
										
										continue;
									}
									ix = tt.indexOf("总户数");	
									if (ix != -1)
									{

										String sub = tt.substring(ix + "总户数".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
										if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
											poi += "<HOUSEHOLDS>" + sub.replace(" ", "") + "</HOUSEHOLDS>";
										
										continue;
									}
									ix = tt.indexOf("建造年代");	
									if (ix != -1)
									{

										String sub = tt.substring(ix + "建造年代".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
										if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
											poi += "<BUILT_YEAR>" + sub.replace(" ", "") + "</BUILT_YEAR>";
										
										continue;
									}
									ix = tt.indexOf("容积率");	
									if (ix != -1)
									{

										String sub = tt.substring(ix + "容积率".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
										if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
											poi += "<VOLUME_RATE>" + sub.replace(" ", "") + "</VOLUME_RATE>";
										
										continue;
									}
									
									ix = tt.indexOf("停车位");	
									if (ix != -1)
									{

										String sub = tt.substring(ix + "停车位".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
										if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
											poi += "<PARK>" + sub.replace(" ", "") + "</PARK>";
										
										continue;
									}
									ix = tt.indexOf("绿化率");	
									if (ix != -1)
									{

										String sub = tt.substring(ix + "绿化率".length(),tt.indexOf("%")+"%".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").replace("(绿化率高)","").trim();
										if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
											poi += "<GREEN_RATE>" + sub.replace(" ", "") + "</GREEN_RATE>";
										
										continue;
									}
							}
						}
					
													
						
						System.out.println("poi.ok!");
						return "<POI>" + poi + "<URL>" + url + "</URL></POI>";
			       }
		        }	
			 }
			} catch (ParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
	
	/* 解析二手房页面 */
	private static String parseResold(String url)
	{
		System.out.println(url);
		String content = HTMLTool.fetchURL(url, "utf-8","get");//GB2312是汉字书写国家标准。
		System.out.println("url.ok!");

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
					poi += "<TITLE>" + t1.replace(" ", "").trim() + t2.replace(" ", "").trim() +"</TITLE>";//大标题
				}
				else
				{
					poi += "<TITLE>" + tt.replace(" ", "").trim() + "</TITLE>";//大标题
				}
				
				parser.reset();
					
			}	
			parser.reset();
			
			Date d = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
			poi += "<TIME>" + sdf.format(d) + "</TIME>";//发布时间
		
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
								poi += "<PRICE>" + sub.replace(" ", "") + "</PRICE>";
							continue;
						}

						ix = tt.indexOf("参考首付");
						if (ix != -1)
						{

							String sub = tt.substring(ix + "参考首付".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1)
								poi += "<DOWN_PAYMENT>" + sub.replace(" ", "") + "</DOWN_PAYMENT>";
							continue;
						}
						ix = tt.indexOf("单价");
						if (ix != -1)
						{

							String sub = tt.substring(ix + "单价".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								poi += "<UNIT_PRICE>" + sub.replace(" ", "") + "</UNIT_PRICE>";

							continue;
						}
						
						ix = tt.indexOf("所在小区");	
						int ix2=tt.indexOf("(");
						if (ix != -1&&ix2>ix)
						{

							String sub = tt.substring(ix + "所在小区".length(),ix2).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								poi += "<COMMUNITY>" + sub.replace(" ", "") + "</COMMUNITY>";

							continue;
						}

						ix = tt.indexOf("位置");						
						if (ix != -1)
						{

							String sub = tt.substring(ix + "位置".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								poi += "<LOCATION>" + sub.replace(" ", "") + "</LOCATION>";

							continue;
						}
						ix = tt.indexOf("地址");						
						if (ix != -1)
						{

							String sub = tt.substring(ix + "地址".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace("","").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								poi += "<ADDRESS>" + sub.replace(" ", "") + "</ADDRESS>";

							continue;
						}
						ix = tt.indexOf("开发商");						
						if (ix != -1)
						{

							String sub = tt.substring(ix + "开发商".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								poi += "<DEVELOPER>" + sub.replace(" ", "") + "</DEVELOPER>";

							continue;
						}
						ix = tt.indexOf("物业公司");						
						if (ix != -1)
						{

							String sub = tt.substring(ix + "物业公司".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								poi += "<PROPERTY>" + sub.replace(" ", "") + "</PROPERTY>";

							continue;
						}
						ix = tt.indexOf("物业类型");						
						if (ix != -1)
						{

							String sub = tt.substring(ix + "物业类型".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								poi += "<PROPERTY_TYPE>" + sub.replace(" ", "") + "</PROPERTY_TYPE>";

							continue;
						}
						ix = tt.indexOf("物业费用");						
						if (ix != -1)
						{

							String sub = tt.substring(ix + "物业费用".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace("(绿化率高)","").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								poi += "<PROPERTY_FEE>" + sub.replace(" ", "") + "</PROPERTY_FEE>";

							continue;
						}
						
						
					
					}
				}
			}
			parser.reset();
			
			
//			filter=new AndFilter(new TagNameFilter("dd"),new HasAttributeFilter("id","reference_monthpay"));
//			nodes= parser.extractAllNodesThatMatch(filter); 	
//			if (nodes != null && nodes.size() == 1)
//			{
//				String str = nodes.elementAt(0).toPlainTextString().replace("\r\n", "").replace("\t", "").trim();
//				poi += "<MONTHLY_PAYMENT>" + str.replace(" ", "") + "</MONTHLY_PAYMENT>";					
//			}	
//			parser.reset();
					
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
								poi += "<HOUSE_TYPE>" + sub.replace(" ", "") + "</HOUSE_TYPE>";
							continue;
						}

						ix = tt.indexOf("面积");
						iy=tt.indexOf("总建");
						if (ix != -1&& iy==-1)
						{

							String sub = tt.substring(ix + "面积".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								poi += "<HOUSE_AREA>" + sub.replace(" ", "") + "</HOUSE_AREA>";
							
							continue;
						}
						
						ix = tt.indexOf("总建面积");	
						if (ix != -1)
						{

							String sub = tt.substring(ix + "总建面积".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").replace("平方米", "").replace("(中型小区)", "").replace("(大型小区)", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								poi += "<TOTAL_AREA>" + sub.replace(" ", "") + "</TOTAL_AREA>";
							
							continue;
						}
						

						ix = tt.indexOf("朝向");	
						if (ix != -1)
						{

							String sub = tt.substring(ix + "朝向".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								poi += "<ORIENTATION>" + sub.replace(" ", "") + "</ORIENTATION>";
							
							continue;
						}

						ix = tt.indexOf("楼层");	
						if (ix != -1)
						{

							String sub = tt.substring(ix + "楼层".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								poi += "<FLOOR>" + sub.replace(" ", "") + "</FLOOR>";
							
							continue;
						}

						ix = tt.indexOf("装修");	
						if (ix != -1)
						{

							String sub = tt.substring(ix + "装修".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								poi += "<FITMENT>" + sub.replace(" ", "") + "</FITMENT>";
							
							continue;
						}

						ix = tt.indexOf("类型");	
						if (ix != -1)
						{

							String sub = tt.substring(ix + "类型".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								poi += "<TYPE>" + sub.replace(" ", "") + "</TYPE>";
							
							continue;
						}
						
						ix = tt.indexOf("总户数");	
						if (ix != -1)
						{

							String sub = tt.substring(ix + "总户数".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								poi += "<HOUSEHOLDS>" + sub.replace(" ", "") + "</HOUSEHOLDS>";
							
							continue;
						}
						ix = tt.indexOf("建造年代");	
						if (ix != -1)
						{

							String sub = tt.substring(ix + "建造年代".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								poi += "<BUILT_YEAR>" + sub.replace(" ", "") + "</BUILT_YEAR>";
							
							continue;
						}
						ix = tt.indexOf("容积率");	
						if (ix != -1)
						{

							String sub = tt.substring(ix + "容积率".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								poi += "<VOLUME_RATE>" + sub.replace(" ", "") + "</VOLUME_RATE>";
							
							continue;
						}
						
						ix = tt.indexOf("停车位");	
						if (ix != -1)
						{

							String sub = tt.substring(ix + "停车位".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								poi += "<PARK>" + sub.replace(" ", "") + "</PARK>";
							
							continue;
						}
						ix = tt.indexOf("绿化率");	
						if (ix != -1)
						{

							String sub = tt.substring(ix + "绿化率".length(),tt.indexOf("%")+"%".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").replace("(绿化率高)","").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								poi += "<GREEN_RATE>" + sub.replace(" ", "") + "</GREEN_RATE>";
							
							continue;
						}

						
					}
				}
			
				
				
				System.out.println("poi.ok!");
				//函数定义在920行
				return "<POI>" + poi + "<URL>" + url + "</URL></POI>";
	}
					
							
				
			
		} catch (ParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
	
	/* 解析新房 */
	private static String parseNewBuilding(String url)
	{
		System.out.println(url);
		String content = HTMLTool.fetchURL(url, "utf-8","get");//GB2312是汉字书写国家标准。
		Parser parser = new Parser();//获取解析器
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
				
				String str = nodes.elementAt(0).toPlainTextString().replace("\r\n", "").replace("\t", "").trim();
				if(str!=null)
					poi += "<TITLE>" + str.replace(" ", "").trim() +"</TITLE>";//大标题
				
								
			}	
			parser.reset();	
			
			Date d = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
			poi += "<TIME>" + sdf.format(d) + "</TIME>";//发布时间
			
//						filter=new AndFilter(new TagNameFilter("i"),new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","fl"))));
//						nodes=parser.extractAllNodesThatMatch(filter);  
//						if (nodes != null)
//						{
//							
//							String str = nodes.elementAt(0).toPlainTextString().replace("\r\n", "").replace("\t", "").trim();			
//						
//							if(str!=null)
//								poi += "<STATUS>" + str.replace(" ", "").trim() + "</STATUS>";//状态
//												
//						}	
			
			parser.reset();

			
				filter = new AndFilter(new TagNameFilter("dl"), new HasAttributeFilter("class", "basic-parms"));
				nodes = parser.extractAllNodesThatMatch(filter);
				if(nodes!=null&&nodes.size()==1)
				{
					String str = nodes.elementAt(0).toPlainTextString().replace("\r\n", "").replace("\t", "").replace("，", "").replace(",", "").trim();
					
					int ix=str.indexOf("参考均价");
					int ix2=str.indexOf("优惠折扣");
					if(ix!=-1&&ix2!=-1)
					{
						String sub=str.substring(ix+"参考均价".length(), ix2).replace("变价通知我", "").replace(" ", "").trim();
						if(sub.indexOf("元")!=-1)
							poi+="<PRICE>"+sub+"</PRICE>";
					}
					
					 ix=str.indexOf("优惠折扣");
					 ix2=str.indexOf("楼盘户型");
					if(ix!=-1&&ix2!=-1)
					{
						String sub=str.substring(ix+"优惠折扣".length(), ix2).replace("获取优惠", "").replace(" ", "").trim();
						if(sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
							poi+="<DISCOUNT>"+sub+"</DISCOUNT>";
					}
					
					 ix=str.indexOf("楼盘户型");
					 ix2=str.indexOf("楼盘地址");
					if(ix!=-1&&ix2!=-1)
					{
						String sub=str.substring(ix+"楼盘户型".length(), ix2).replace("全部户型", "").replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\b", "").replace("\n", "").replace("装修预算点击预估装修预算（点我省钱）","").trim();
						if(sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
							poi+="<TYPE>"+sub+"</TYPE>";
					}
					
					 ix=str.indexOf("楼盘地址");
					if(ix!=-1)
					{
						String sub=str.substring(ix+"楼盘地址".length()).replace("查看地图", "").replace(" ", "").replace("[", "").replace("]", "").replace("（", "").replace("）", "").replace("-", "").trim();
						
						if(sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
							poi+="<ADDRESS>"+sub+"</ADDRESS>";
					}					
					
				}
				parser.reset();
				
				filter = new AndFilter(new TagNameFilter("li"), new HasParentFilter(new AndFilter(new TagNameFilter("ul"), new HasAttributeFilter("class", "info-left"))));
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
							tt = tt.replace("（", "(").replace("）", ")").replace("、", ",");
							
							
							int ix = tt.indexOf("最新开盘");
							
							if (ix != -1)
							{
								String sub = tt.substring(ix + "最新开盘".length()).replace("\r\n", "").replace("\t", "").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<OPENING_DATE>" + sub.replace(" ", "") + "</OPENING_DATE>";
								continue;
							}

							
							
							ix = tt.indexOf("装修标准");
							if (ix != -1)
							{

								String sub = tt.substring(ix + "装修标准".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<FITMENT>" + sub.replace(" ", "") + "</FITMENT>";
								continue;
							}
							

							ix = tt.indexOf("产权年限");
							
							if (ix != -1)
							{

								String sub = tt.substring(ix + "产权年限".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<EQUILT_TIME>" + sub.replace(" ", "") + "</EQUILT_TIME>";

								continue;
							}

						
						}
					}
				}
				parser.reset();
						
				filter = new AndFilter(new TagNameFilter("li"), new HasParentFilter(new AndFilter(new TagNameFilter("ul"), new HasAttributeFilter("class", "info-right"))));
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
							tt = tt.replace("（", "(").replace("）", ")").replace("、", ",");
					

							int ix = tt.indexOf("交房时间");
							
							if (ix != -1)
							{
								String sub = tt.substring(ix + "交房时间".length()).replace("\r\n", "").replace("\t", "").replace("开盘通知我", "").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<PROSSESSION_DATE>" + sub.replace(" ", "") + "</PROSSESSION_DATE>";
								continue;
							}

							ix = tt.indexOf("建筑类型");
							
							if (ix != -1)
							{

								String sub = tt.substring(ix + "建筑类型".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace(",", "").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									poi += "<BUILDING_TYPE>" + sub.replace(" ", "") + "</BUILDING_TYPE>";
								
								continue;
							}

						
						}
					}
				
												
					
					System.out.println("poi.ok!");
					//函数定义在920行
					return "<POI>" + poi + "<URL>" + url + "</URL></POI>";
		}
						
							
				
			
		} catch (ParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
	
	/* 解析求租页面 */
	private static String parseRental(String url)
	{
		String content = HTMLTool.fetchURL(url, "utf-8","get");
		Parser parser = new Parser();
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
					poi += "<TITLE>" + t1.replace(" ", "").trim() + t2.replace(" ", "").trim() +"</TITLE>";//大标题
				}
				else
				{
					poi += "<TITLE>" + tt.replace(" ", "").trim() + "</TITLE>";//大标题
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
					poi += "<TIME>" + sdf.format(d) + "</TIME>";//发布时间
					
				}
				else
					
					poi += "<TIME>" + str.replace(" ", "").trim() + "</TIME>";//发布时间
				
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
									poi += "<DISTRICT>" + sub.replace(" ", "") + "</DISTRICT>";
								continue;
							}

							ix = tt.indexOf("居室：");
							int ix2=tt.indexOf(";document");
							
							if (ix != -1&&ix2!=-1)
							{

								String sub = tt.substring(ix + "居室：".length(),ix2).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
								if (sub.indexOf("暂无") == -1)
									poi += "<TYPE>" + sub.replace(" ", "") + "</TYPE>";
								continue;
							}
							
							

							ix = tt.indexOf("租金：");
							
							if (ix != -1)
							{

								String sub = tt.substring(ix + "租金：".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").replace("看中了，一键搬家", "").trim();
								if (sub.indexOf("暂无") == -1)
									poi += "<PRICE>" + sub.replace(" ", "") + "</PRICE>";
								
								continue;
							}


							ix = tt.indexOf("入住：");
							
							if (ix != -1)
							{

								String sub = tt.substring(ix + "入住：".length()).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
								if (sub.indexOf("暂无") == -1)
									poi += "<CHECKIN>" + sub.replace(" ", "") + "</CHECKIN>";

								continue;
							}

						
						}
					}
				
												
					System.out.println("poi.ok!");
					//函数定义在920行

						return "<POI>" + poi + "<URL>" + url + "</URL></POI>";
		}
						
							
				
			
		} catch (ParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
	
	
	
	private static String parseLngLat(String url, String content)		//这个函数是干嘛的？？解析坐标？？
	{
		if (content == null)
			return null;


		
		int s = content.indexOf("src=\"/newsecond/map/NewMapDetail.aspx?newcode=");
		
		String ref = null;
		if (s != -1)
		{
			int e = content.indexOf("\"", s + "src=\"/newsecond/map/NewMapDetail.aspx?newcode=".length());
			if (e != -1)
			{
				ref = "http://esf.cq.fang.com" + content.substring(s + "src=\"".length(), e);
			}
		}
		else
		{
			s = content.indexOf("src=\"http://zu.cq.fang.com/map/NewMapDetail.aspx?newcode=");
			if (s != -1)
			{
				int e = content.indexOf("\"", s + "src=\"http://zu.cq.fang.com/map/NewMapDetail.aspx?newcode=".length());
				if (e != -1)
				{
					ref = content.substring(s + "src=\"".length(), e);
				}
			}
		}
		
		if (ref != null)
		{
			String contentd = HTMLTool.fetchURL(ref, "gb2312","get");

			if (contentd == null)
				return null;
			
			String px = null, py = null;
			if (ref.startsWith("http://esf.cq.fang.com/newsecond/map/NewMapDetail.aspx?newcode")
				|| ref.startsWith("http://zu.cq.fang.com/map/NewMapDetail.aspx?newcode="))
			{
				// px:"116.19629669189453125000",py:"39.91481781005859375000"
				
				s = contentd.indexOf("px:\"");
				if (s != -1)
				{
					int e = contentd.indexOf("\"", s + "px:\"".length());
					if (e != -1)
					{
						px = contentd.substring(s + "px:\"".length(), e);
						
						s = contentd.indexOf("py:\"");
						if (s != -1)
						{
							e = contentd.indexOf("\"", s + "py:\"".length());
							if (e != -1)
							{
								py = contentd.substring(s + "py:\"".length(), e);
								
							}
							else
							{
								System.out.println("解析y坐标出错:" + url);
							}
						}
						else
							System.out.println("解析y坐标出错:" + url);
					}
					else
					{
						System.out.println("解析x坐标出错:" + url);
					}
				}
				else
					System.out.println("解析x坐标出错:" + url);
			}
			else
			{
				/* var cityx=121.07670593261718750000;
				 * var cityy=31.31203269958496093750;
				 */
				s = contentd.indexOf("var cityx=");
				if (s != -1)
				{
					int e = contentd.indexOf(";", s + "var cityx=".length());
					if (e != -1)
					{
						px = contentd.substring(s + "var cityx=".length(), e);

						s = contentd.indexOf("var cityy=");
						if (s != -1)
						{
							e = contentd.indexOf(";", s + "var cityy=".length());
							if (e != -1)
							{
								py = contentd.substring(s + "var cityy=".length(), e);

							}
							else
							{
								System.out.println("解析y坐标出错:" + url);
							}
						}
						else
							System.out.println("解析y坐标出错:" + url);
					}
					else
					{
						System.out.println("解析x坐标出错:" + url);
					}
				}
				else
					System.out.println("解析x坐标出错:" + url);
			}
			
			if (px != null)
			{
				return "<LNGLAT>" + px + ";" + py + "</LNGLAT>";
			} 
		} 
		
		return null;
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
										synchronized(BJ_RESOLDS)
										//synchronized是Java语言的关键字，当它用来修饰一个方法或者一个代码块的时候，能够保证在同一时刻最多只有一个线程执行该段代码。
										{
											
											FileTool.Dump(poi,FOLDER+ "/bj_Rentoutinfo.csv", "UTF-8");
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
	
	/* 抓取出租数据
	 * */
	public static String RENTOUT_URL = "http://bj.zu.anjuke.com/fangyuan";
	
	public static void getRentOutInfo(String region)
	{
		// 首先加载
		Vector<String> log = null;
		synchronized(BJ_RENTOUT)
		{
			log = FileTool.Load(LOG + File.separator + region + "_rentout.log", "UTF-8");
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
		
		String url = RENTOUT_URL + region;
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
	

				NodeFilter filter=new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","zu-itemmod"));
				NodeList nodes = parser.extractAllNodesThatMatch(filter);
				
				if (nodes != null)
				{
					for (int n = 0; n < nodes.size(); n ++)
					{
				
						TagNode tn=(TagNode)nodes.elementAt(n);
						String purl = tn.getAttribute("link");
						
						if(purl.startsWith("http"))
						{
						
						String poi2 = parseRentOut(purl);
						if (poi2 == null)
							continue;
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
										synchronized(BJ_RENTOUT)
										//synchronized是Java语言的关键字，当它用来修饰一个方法或者一个代码块的时候，能够保证在同一时刻最多只有一个线程执行该段代码。
										{
											
											FileTool.Dump(poi, FOLDER+"/Anjuke_RentoutInfo.txt", "UTF-8");
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
		
		synchronized(BJ_RENTOUT)
		{
			File f = new File(LOG + File.separator + region + ".log");
			f.delete();
			if (newest != null)
			{			
				FileTool.Dump(sdf.format(newest), LOG + File.separator + region + ".log", "UTF-8");
			}
		}
	}

	
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
							System.out.println(purl);
						String poi2 = parseRental( purl);
						String poi=poi2.replace("&nbsp;", "").replace("&nbsp", "").replace("vartmp=", "").replace("\"", "");
						System.out.println(poi);
						// 获取时间
						if (poi != null)
						{
							// 获取时间
							int m = poi.indexOf("<TIME>");
							int k = poi.indexOf("</TIME>");
							
							if (m != -1 && k != -1)
							{
								assert(m < k);
								String tm = poi.substring(m + "<TIME>".length(), k);
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
						System.out.println("purl");
						String poi2 = parseNewBuilding(purl);
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
											
											FileTool.Dump(poi, "D:\\新房_安居客_北京.csv", "UTF-8");
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
	
	/* 求租 http://zu.cq.fang.com/qiuzu/
	 * */	
	/**
	 * @param args
	 */
	
}
