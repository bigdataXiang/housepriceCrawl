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

public class Fang_Newhouse {


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
	
	public static String FOLDER = "/home/gir/crawldata/beijing/fang/newhouse/fang_NewBuilding2014_10.txt";
    public static void main(String[] args) {
		
		getNewBuildingInfo(2016,1);//已经调试好917
		
	
	}
    static JSONObject jsonObjArr = new JSONObject();
	/* 解析本月开盘页面 */
	private static String parseNewBuilding(String url)
	{
		String content = HTMLTool.fetchURL(url, "gb2312", "get");

		Parser parser = new Parser();
		if (content == null)
		{
			return null;
		}
		
		String poi = "";
		try {
			
			parser.setInputHTML(content);
			parser.setEncoding("gb2312");
			// <a id="xfxq_C03_14" target="_blank" href="http://haitangwanld023.fang.com/house/3111064820/housedetail.htm">更多详细信息&gt;&gt;</a>
			NodeFilter filter = new AndFilter(new TagNameFilter("a"), new AndFilter(new HasAttributeFilter("id", "xfdsxq_B04_14"), new HasAttributeFilter("href")));//xfxq_C03_14
			//AndFilter可以把两种Filter进行组合，只有同时满足条件的Node才会被过滤。
			//NodeFilter filter = new HasAttributeFilter( "id", "logoindex" );getText:div id="logoindex"
			NodeList nodes = parser.extractAllNodesThatMatch(filter);
			
			if (nodes.size()!=0)
			{
				for (int n = 0; n < nodes.size(); n ++)
				{
					if (nodes.elementAt(n) instanceof TagNode)
					//elementAt(int index) 方法用于获取组件的向量的指定索引/位置。index--这是一个索引该向量,它返回指定索引处的组件。
					{
						TagNode tn = (TagNode) nodes.elementAt(n);
						
						if (tn.getAttribute("href").endsWith("housedetail.htm"))
							//getAttribute这个方法是提取放置在某个共享区间的对象的，他对应了setAttribute方法，比如在session中，使用setAttribute将一个数据放入session区间，那么在一个会话区间内，便可以在其他页面中用getAttribute将数据提取并使用
							//Java String.endsWith()方法用法实例教程, 该方法返回一个true，如果参数表示的字符序列是由该对象表示的字符序列的后缀，否则返回false
							//java.lang.String.endsWith() 方法返回的测试该字符串是否以指定后缀结束
						{
							content = HTMLTool.fetchURL(tn.getAttribute("href"), "gb18030", "get");
							
							if (content == null)
							{
								return null;
							}
							try {
								
								parser.setInputHTML(content);
								parser.setEncoding("gb2312");
								
								// filter = new AndFilter(new TagNameFilter("table"), new HasParentFilter(new HasAttributeFilter("class", "besic_inform")));
								
								nodes = parser.extractAllNodesThatMatch(new HasAttributeFilter("class", "besic_inform"));
								
								//for (int jk = 0; jk < nodes.size(); jk ++)
								
									String tt = nodes.elementAt(0).toPlainTextString().replace("提示：页中所涉面积，如无特殊说明，均为建筑面积；所涉及装修状况、标准以最终合同为准。", "").replace("\r\n", "").replace("\n", "").replace("\t", "").replace(" ", "").replace("&nbsp;", "").trim();
			
									if (tt.indexOf("[更多]") != -1&&tt.indexOf("房价")!=-1)
									{
										jsonObjArr.put("community",tt.substring(tt.indexOf("[更多]")+"[更多]".length(),tt.indexOf("房价")));
											
										//poi += "<COMMUNITY>" +  tt.substring(tt.indexOf("[更多]")+"[更多]".length(),tt.indexOf("房价")) + "</COMMUNITY>";
									}
									if (tt.indexOf("物业类别") != -1&&tt.indexOf("项目特色")!=-1)
									{
										
											//poi += "<BUILDING_USAGE>" + tt.substring(tt.indexOf("物业类别")+"物业类别".length(),tt.indexOf("项目特色")) + "</BUILDING_USAGE>";
										jsonObjArr.put("property",tt.substring(tt.indexOf("物业类别")+"物业类别".length(),tt.indexOf("项目特色")));
									}
									 if (tt.indexOf("项目特色") != -1&&tt.indexOf("建筑类别")!=-1)
									{
										
											//poi += "<CHARACTER>" + tt.substring(tt.indexOf("项目特色")+"项目特色".length(),tt.indexOf("建筑类别"))+ "</CHARACTER>";
										 jsonObjArr.put("character",tt.substring(tt.indexOf("物业类别")+"物业类别".length(),tt.indexOf("项目特色")));
									}
									 if (tt.indexOf("建筑类别") != -1&&tt.indexOf("装修状况")!=-1)
									{
										    int xx=tt.indexOf("建筑类别")+"建筑类别".length();
										    int yy=tt.indexOf("装修状况");
											//poi += "<BUILDING_TYPE>" + tt.substring(tt.indexOf("建筑类别")+"建筑类别".length(),tt.indexOf("装修状况")) + "</BUILDING_TYPE>";
										    jsonObjArr.put("biult_type",tt.substring(tt.indexOf("建筑类别")+"建筑类别".length(),tt.indexOf("装修状况")));
									}
									 if (tt.indexOf("装修状况") != -1&&tt.indexOf("环线位置")!=-1)
									{
										
											//poi += "<BUILDING_CONDITION>" + tt.substring(tt.indexOf("装修状况")+"装修状况".length(),tt.indexOf("环线位置")) + "</BUILDING_CONDITION>";
										 jsonObjArr.put("fitment",tt.substring(tt.indexOf("装修状况")+"装修状况".length(),tt.indexOf("环线位置")));
									}
									 if (tt.indexOf("容积率") != -1&&tt.indexOf("绿化率")!=-1)
									{
										
											//poi += "<FAR>" + tt.substring(tt.indexOf("容积率")+"容积率".length(),tt.indexOf("绿化率")) + "</FAR>";
										 jsonObjArr.put("volume_rate",tt.substring(tt.indexOf("容积率")+"容积率".length(),tt.indexOf("绿化率")));
									}
									if (tt.indexOf("绿化率") != -1&&tt.indexOf("开盘时间")!=-1)
									{
										
											//poi += "<GREEN>" +tt.substring(tt.indexOf("绿化率")+"绿化率".length(),tt.indexOf("开盘时间")) + "</GREEN>";
										 jsonObjArr.put("green_rate",tt.substring(tt.indexOf("绿化率")+"绿化率".length(),tt.indexOf("开盘时间")));
									}
									 if (tt.indexOf("开盘时间") != -1&&tt.indexOf("交房时间")!=-1)
									{
										
											//poi += "<SALE_TIME>" + tt.substring(tt.indexOf("开盘时间")+"开盘时间".length(),tt.indexOf("交房时间")) + "</SALE_TIME>";
										 jsonObjArr.put("open_time",tt.substring(tt.indexOf("开盘时间")+"开盘时间".length(),tt.indexOf("交房时间")));
									}
									 if (tt.indexOf("交房时间") != -1&&tt.indexOf("物业费")!=-1)
									{
										
											//poi += "<SUBMIT_TIME>" + tt.substring(tt.indexOf("交房时间")+"交房时间".length(),tt.indexOf("物业费")) + "</SUBMIT_TIME>";
										 jsonObjArr.put("completed_time",tt.substring(tt.indexOf("交房时间")+"交房时间".length(),tt.indexOf("物业费")));
									}
									 if (tt.indexOf("物业费") != -1&&tt.indexOf("物业公司")!=-1)
									{
										
											//poi += "<PROPERTY_FEE>" + tt.substring(tt.indexOf("物业费")+"物业费".length(),tt.indexOf("物业公司")) + "</PROPERTY_FEE>";
										 jsonObjArr.put("property_fee",tt.substring(tt.indexOf("物业费")+"物业费".length(),tt.indexOf("物业公司")));
									}
									 if (tt.indexOf("物业公司") != -1&&tt.indexOf("开发商")!=-1)
									{
										
											//poi += "<SERVER>" + tt.substring(tt.indexOf("物业公司")+"物业公司".length(),tt.indexOf("开发商"))+ "</SERVER>";
										 jsonObjArr.put("property_company",tt.substring(tt.indexOf("物业公司")+"物业公司".length(),tt.indexOf("开发商")));
									}
									 if (tt.indexOf("开发商") != -1&&tt.indexOf("预售许可证")!=-1)
									{
										
											//poi += "<DEVELOPER>" + tt.substring(tt.indexOf("开发商")+"开发商".length(),tt.indexOf("预售许可证"))+ "</DEVELOPER>";
										 jsonObjArr.put("developer",tt.substring(tt.indexOf("开发商")+"开发商".length(),tt.indexOf("预售许可证")));
									}

									 if (tt.indexOf("售楼地址") != -1&&tt.indexOf("物业地址")!=-1)
									{
										
											//poi += "<SALE_ADDRESS>" + tt.substring(tt.indexOf("售楼地址")+"售楼地址".length(),tt.indexOf("物业地址")) + "</SALE_ADDRESS>";
										 jsonObjArr.put("sales_address",tt.substring(tt.indexOf("售楼地址")+"售楼地址".length(),tt.indexOf("物业地址")));
									}
									 if (tt.indexOf("价") != -1&&tt.indexOf("走势")!=-1)
									{
										//poi += "<PRICE>" + tt.substring(tt.indexOf("价")+"价".length(),tt.indexOf("走势")).replace("[房价", "") + "</PRICE>";
										 jsonObjArr.put("price",tt.substring(tt.indexOf("价")+"价".length(),tt.indexOf("走势")).replace("[房价", ""));
									}
								 jsonObjArr.put("url",url);
								 poi=jsonObjArr.toString();
							
							} catch (ParserException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} 
						}
					}
				}
			}else{
				 parser.reset();
                 filter = new AndFilter(new TagNameFilter("a"), new AndFilter(new HasAttributeFilter("id", "xfptxq_B04_14"), new HasAttributeFilter("href")));
                 nodes = parser.extractAllNodesThatMatch(filter);
                 if (nodes != null)
     			{
     				for (int n = 0; n < nodes.size(); n ++)
    				{
    					if (nodes.elementAt(n) instanceof TagNode)
    					//elementAt(int index) 方法用于获取组件的向量的指定索引/位置。index--这是一个索引该向量,它返回指定索引处的组件。
    					{
    						TagNode tn = (TagNode) nodes.elementAt(n);
    						
    						if (tn.getAttribute("href").endsWith("housedetail.htm"))
    							//getAttribute这个方法是提取放置在某个共享区间的对象的，他对应了setAttribute方法，比如在session中，使用setAttribute将一个数据放入session区间，那么在一个会话区间内，便可以在其他页面中用getAttribute将数据提取并使用
    							//Java String.endsWith()方法用法实例教程, 该方法返回一个true，如果参数表示的字符序列是由该对象表示的字符序列的后缀，否则返回false
    							//java.lang.String.endsWith() 方法返回的测试该字符串是否以指定后缀结束
    						{
    							content = HTMLTool.fetchURL(tn.getAttribute("href"), "gb18030", "get");
    							
    							if (content == null)
    							{
    								return null;
    							}
    							try {
    								
    								parser.setInputHTML(content);
    								parser.setEncoding("gb2312");
    								
    								// filter = new AndFilter(new TagNameFilter("table"), new HasParentFilter(new HasAttributeFilter("class", "besic_inform")));
    								
    								nodes = parser.extractAllNodesThatMatch(new HasAttributeFilter("class", "besic_inform"));
    								
    								//for (int jk = 0; jk < nodes.size(); jk ++)
    								
    									String tt = nodes.elementAt(0).toPlainTextString().replace("提示：页中所涉面积，如无特殊说明，均为建筑面积；所涉及装修状况、标准以最终合同为准。", "").replace("\r\n", "").replace("\n", "").replace("\t", "").replace(" ", "").replace("&nbsp;", "").trim();
    			
    									if (tt.indexOf("[更多]") != -1&&tt.indexOf("房价")!=-1)
    									{
    										jsonObjArr.put("community",tt.substring(tt.indexOf("[更多]")+"[更多]".length(),tt.indexOf("房价")));
    											
    										//poi += "<COMMUNITY>" +  tt.substring(tt.indexOf("[更多]")+"[更多]".length(),tt.indexOf("房价")) + "</COMMUNITY>";
    									}
    									if (tt.indexOf("物业类别") != -1&&tt.indexOf("项目特色")!=-1)
    									{
    										
    											//poi += "<BUILDING_USAGE>" + tt.substring(tt.indexOf("物业类别")+"物业类别".length(),tt.indexOf("项目特色")) + "</BUILDING_USAGE>";
    										jsonObjArr.put("property",tt.substring(tt.indexOf("物业类别")+"物业类别".length(),tt.indexOf("项目特色")));
    									}
    									 if (tt.indexOf("项目特色") != -1&&tt.indexOf("建筑类别")!=-1)
    									{
    										
    											//poi += "<CHARACTER>" + tt.substring(tt.indexOf("项目特色")+"项目特色".length(),tt.indexOf("建筑类别"))+ "</CHARACTER>";
    										 jsonObjArr.put("character",tt.substring(tt.indexOf("物业类别")+"物业类别".length(),tt.indexOf("项目特色")));
    									}
    									 if (tt.indexOf("建筑类别") != -1&&tt.indexOf("装修状况")!=-1)
    									{
    										    int xx=tt.indexOf("建筑类别")+"建筑类别".length();
    										    int yy=tt.indexOf("装修状况");
    											//poi += "<BUILDING_TYPE>" + tt.substring(tt.indexOf("建筑类别")+"建筑类别".length(),tt.indexOf("装修状况")) + "</BUILDING_TYPE>";
    										    jsonObjArr.put("biult_type",tt.substring(tt.indexOf("建筑类别")+"建筑类别".length(),tt.indexOf("装修状况")));
    									}
    									 if (tt.indexOf("装修状况") != -1&&tt.indexOf("环线位置")!=-1)
    									{
    										
    											//poi += "<BUILDING_CONDITION>" + tt.substring(tt.indexOf("装修状况")+"装修状况".length(),tt.indexOf("环线位置")) + "</BUILDING_CONDITION>";
    										 jsonObjArr.put("fitment",tt.substring(tt.indexOf("装修状况")+"装修状况".length(),tt.indexOf("环线位置")));
    									}
    									 if (tt.indexOf("容积率") != -1&&tt.indexOf("绿化率")!=-1)
    									{
    										
    											//poi += "<FAR>" + tt.substring(tt.indexOf("容积率")+"容积率".length(),tt.indexOf("绿化率")) + "</FAR>";
    										 jsonObjArr.put("volume_rate",tt.substring(tt.indexOf("容积率")+"容积率".length(),tt.indexOf("绿化率")));
    									}
    									if (tt.indexOf("绿化率") != -1&&tt.indexOf("开盘时间")!=-1)
    									{
    										
    											//poi += "<GREEN>" +tt.substring(tt.indexOf("绿化率")+"绿化率".length(),tt.indexOf("开盘时间")) + "</GREEN>";
    										 jsonObjArr.put("green_rate",tt.substring(tt.indexOf("绿化率")+"绿化率".length(),tt.indexOf("开盘时间")));
    									}
    									 if (tt.indexOf("开盘时间") != -1&&tt.indexOf("交房时间")!=-1)
    									{
    										
    											//poi += "<SALE_TIME>" + tt.substring(tt.indexOf("开盘时间")+"开盘时间".length(),tt.indexOf("交房时间")) + "</SALE_TIME>";
    										 jsonObjArr.put("open_time",tt.substring(tt.indexOf("开盘时间")+"开盘时间".length(),tt.indexOf("交房时间")));
    									}
    									 if (tt.indexOf("交房时间") != -1&&tt.indexOf("物业费")!=-1)
    									{
    										
    											//poi += "<SUBMIT_TIME>" + tt.substring(tt.indexOf("交房时间")+"交房时间".length(),tt.indexOf("物业费")) + "</SUBMIT_TIME>";
    										 jsonObjArr.put("completed_time",tt.substring(tt.indexOf("交房时间")+"交房时间".length(),tt.indexOf("物业费")));
    									}
    									 if (tt.indexOf("物业费") != -1&&tt.indexOf("物业公司")!=-1)
    									{
    										
    											//poi += "<PROPERTY_FEE>" + tt.substring(tt.indexOf("物业费")+"物业费".length(),tt.indexOf("物业公司")) + "</PROPERTY_FEE>";
    										 jsonObjArr.put("property_fee",tt.substring(tt.indexOf("物业费")+"物业费".length(),tt.indexOf("物业公司")));
    									}
    									 if (tt.indexOf("物业公司") != -1&&tt.indexOf("开发商")!=-1)
    									{
    										
    											//poi += "<SERVER>" + tt.substring(tt.indexOf("物业公司")+"物业公司".length(),tt.indexOf("开发商"))+ "</SERVER>";
    										 jsonObjArr.put("property_company",tt.substring(tt.indexOf("物业公司")+"物业公司".length(),tt.indexOf("开发商")));
    									}
    									 if (tt.indexOf("开发商") != -1&&tt.indexOf("预售许可证")!=-1)
    									{
    										
    											//poi += "<DEVELOPER>" + tt.substring(tt.indexOf("开发商")+"开发商".length(),tt.indexOf("预售许可证"))+ "</DEVELOPER>";
    										 jsonObjArr.put("developer",tt.substring(tt.indexOf("开发商")+"开发商".length(),tt.indexOf("预售许可证")));
    									}

    									 if (tt.indexOf("售楼地址") != -1&&tt.indexOf("物业地址")!=-1)
    									{
    										
    											//poi += "<SALE_ADDRESS>" + tt.substring(tt.indexOf("售楼地址")+"售楼地址".length(),tt.indexOf("物业地址")) + "</SALE_ADDRESS>";
    										 jsonObjArr.put("sales_address",tt.substring(tt.indexOf("售楼地址")+"售楼地址".length(),tt.indexOf("物业地址")));
    									}
    									 if (tt.indexOf("价") != -1&&tt.indexOf("走势")!=-1)
    									{
    										//poi += "<PRICE>" + tt.substring(tt.indexOf("价")+"价".length(),tt.indexOf("走势")).replace("[房价", "") + "</PRICE>";
    										 jsonObjArr.put("price",tt.substring(tt.indexOf("价")+"价".length(),tt.indexOf("走势")).replace("[房价", ""));
    									}
    								jsonObjArr.put("url",url);
									poi=jsonObjArr.toString();
    							
    							} catch (ParserException e1) {
    								// TODO Auto-generated catch block
    								e1.printStackTrace();
    							} 
    						}
    					}
    				}
    			}
				
			}

		} catch (ParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
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
	/*
	 * 新盘
	 *  本月开盘  http://newhouse.cq.fang.com/house/saledate/201502.htm
	 *  top100楼盘 http://newhouse.cq.fang.com/house/asp/trans/buynewhouse/default.htm
	 * */
	public static String NEWBUILDING_URL = "http://newhouse.fang.com/house/saledate/";
	
	public static void getNewBuildingInfo(int year, int month)
	{

		String url = NEWBUILDING_URL;
		
		if (month < 10)
			url += year + "0" + month;
		else
			url += year + month;
		url += ".htm";
		
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
				//System.out.println(content);
				int ss = content.indexOf("<strong class=\"f14 fb_blue\">");
				
				while (ss != -1)
				{
					int en = content.indexOf("</strong>", ss + "<strong class=\"f14 fb_blue\">".length());
					if (en != -1)
					{
						String sub = content.substring(ss, en);
						
						int rfs = sub.indexOf("href=\"");
						if (rfs != -1)
						{
							int rfe = sub.indexOf("\"", rfs + "href=\"".length());
							if (rfe != -1)
							{
								String purl = sub.substring(rfs + "href=\"".length(), rfe);
								System.out.println(purl);
								String poi = parseNewBuilding(purl);
								//String poi = "test";
								if (poi != null)
								{
									synchronized(BJ_NEWHOUSE)
									{
										FileTool.Dump(poi, FOLDER, "UTF-8");
										System.out.println(poi);
									}
								}
							else
								break;
						}
						else
							break;
					}
					else
						break;
					
					ss = content.indexOf("<strong class=\"f14 fb_blue\">", en + "</strong>".length());
					
					try {
						Thread.sleep(500 * ((int) (Math
							.max(1, Math.random() * 3))));
					} catch (final InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
		}
				//parser.reset();
				// <div class="fanye gray6">  <a class="pageNow">
				
				
				NodeFilter filter = new AndFilter(new TagNameFilter("div"), new HasParentFilter(new HasAttributeFilter("class", "searchListPage"))); 
				
				NodeList nodes = parser.extractAllNodesThatMatch(filter);
				
				if (nodes != null)
				{
					for (int nn = 0; nn < nodes.size(); nn ++)
					{
						Node ni = nodes.elementAt(nn);
						NodeList cld = ni.getChildren();
						if (cld != null)
						{
							for (int kkk = 0; kkk < cld.size(); kkk ++)
							{
								if (cld.elementAt(kkk) instanceof TagNode)
								{
									String href = ((TagNode)cld.elementAt(kkk)).getAttribute("href");
									if (href != null)
									{
										if (!href.startsWith("http://"))
										{
											if (href.startsWith("/house"))
												href = "http://newhouse.fang.com" + href;
											else
												continue;
										}
										
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
