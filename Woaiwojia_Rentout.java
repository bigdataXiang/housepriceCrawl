package com.svail.crawl.woaiwojia;

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
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import com.svail.util.FileTool;
import com.svail.util.HTMLTool;

import net.sf.json.JSONObject;
public class Woaiwojia_Rentout {


	private static String BJ_NEWHOUSE = "NEW";
	private static String BJ_RENTOUT = "RENTOUT";
	private static String BJ_RESOLDS = "RESOLD";

	public static String LOG = "/home/gir/crawldata/beijing/woaiwojia/rentout";
	public static String FOLDER1="/home/gir/crawldata/beijing/woaiwojia/rentout/woaiwojia_rentout0414.txt";
	public static String FOLDER2="/home/gir/crawldata/beijing/woaiwojia/rentout/woaiwojia_rentout0414_zhoubian.txt";		
	public static String regions[] = {
		"/anzhen/","/aolinpikegongyuan/","/beishatan/","/beiyuan/","/baiziwan/","/changying/",
		"/cbd/","/chaoqing/","/chaoyangbeilu/","/chaoyanggongyuan/","/chaoyangmen/","/dashanzi/",
		"/dongba/","/dongbalizhuang/","/dingfuzhuang/","/dongdaqiao/","/dawanglu/","/dougezhuang/",
		"/fatou/","/ganluyuan/","/gaobeidian/","/guanzhuang/","/gongti/","/guomao/","/guozhan/",
		"/huajiadi/","/hujialou/","/hongmiao/","/huixinxijie/","/hepingjie/","/huaweiqiao/","/jianxiangqiao/",
		"/jiuxianqiao/","/jingsong/","/jianguomenwai/","/laiguangying/","/liufang/","/madian/","/panjiayuan/",
		"/shaoyaoju/","/shifoying/","/sihui/","/shuangqiao/","/shibalidian/","/sanlitun/","/shilipu/",
		"/sifangqiao/","/taiyanggong/","/tuanjiehu/","/tianshuiyuan/","/wangjing/","/xibahe/","/yaao/",
		"/yayuncun/","/yayuncunxiaoying/","/yansha/","/zuojiazhuang/","/haidian/","/fengtai/","/dongcheng/",
		"/xicheng/","/shijingshan/","/daxing/","/tongzhou/","/shunyi/","/changping/","/beijingzhoubian/",
	};
	public static String loupan[]= {"/p1/","/p2/","/p3/","/p4/","/p5/","/p6/"};

	public static String RENTOUT_URL = "http://bj.5i5j.com/rent";
	public static String RESOLDAPARTMENT_URL = "http://bj.5i5j.com/exchange";
	public static String NEWBUILDING_URL = "http://bj.5i5j.com/loupan/list";
	public static String DOMAIN_URL = "http://bj.5i5j.com";


	public static void main(String[] args) {
		for(int i=0; i<regions.length; i++)
			getRentOutInfo(regions[i]);//租房

	}
	static JSONObject jsonObjArr = new JSONObject();
	//抓取出租数据
	public static void getRentOutInfo(String region) {

		Vector<String> log = null;
		synchronized(BJ_RENTOUT) {
			log = FileTool.Load(LOG + File.separator + region + "_rentout.log", "UTF-8");
		}

		SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");

		java.util.Date latestdate = null;
		Date newest = null;

		if (log != null) {
			try {
				latestdate = sdf.parse(log.elementAt(0));
				latestdate = new Date(latestdate.getTime() - 1);
			} catch (ParseException e) {

				e.printStackTrace();
			}
		}

		String url = RENTOUT_URL + region;
		Vector<String> urls = new Vector<String>();

		Set<String> visited = new TreeSet<String>();
		urls.add(url);

		Parser parser = new Parser();
		boolean quit = false;

		while (urls.size() > 0) {

			url = urls.get(0);

			urls.remove(0);
			visited.add(url);

			String content = HTMLTool.fetchURL(url, "utf-8","get");
			//System.out.println("下一页-->"+url);
			if (content == null) {
				continue;
			}
			try {

				parser.setInputHTML(content);
				parser.setEncoding("utf-8");


				NodeFilter filter=new OrFilter(new AndFilter(new TagNameFilter("h2"),new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","list-info")))),new AndFilter(new TagNameFilter("li"),new HasAttributeFilter("class","publish")));
				
				NodeList nodes = parser.extractAllNodesThatMatch(filter);
                //这样提取的nodes[i=2n]是链接，nodes[i=2n+1]是发布时间
				if (nodes != null && nodes.size()>0) {
					for (int n = 0; n < nodes.size(); n+=2) {//n每次加上2

						TagNode tn=(TagNode)nodes.elementAt(n);

						String purl=((TagNode)tn.getChildren().elementAt(0)).getAttribute("href");
						if(purl.startsWith("/rent")) {
							purl=DOMAIN_URL+purl;
							String poi2 = parseRentOut(purl);
							if (poi2 == null)
								continue;
							String poi=poi2.replace("&nbsp;", "").replace("&nbsp", "").replace("()", "");
							System.out.println(poi);
						
							if (poi != null) {

								JSONObject jsonObject = JSONObject.fromObject(poi);
								String tm=jsonObject.get("time").toString();
									try {
										Date date = sdf.parse(tm);
										if (latestdate != null) {
											if (date.before(latestdate)) {
												quit = true;
											} else if (newest == null) {
												newest = date;
											} else {
												if (newest.before(date))
													newest = date;
											}

										}
									} catch (ParseException e) {

										e.printStackTrace();

										newest = new Date();
									}
					

									if (quit) {
										break;
									} else {
										synchronized(BJ_RENTOUT) {

											poi.replace(" ", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
											if(url.indexOf("/beijingzhoubian/")!=-1)
												FileTool.Dump(poi,FOLDER2, "UTF-8");
											else
												FileTool.Dump(poi,FOLDER1, "UTF-8");
												//System.out.println(poi);
										}
								}
							}

							try {
								Thread.sleep(500 * ((int) (Math.max(1, Math.random()*3))));
							} catch (final InterruptedException e1) {
								e1.printStackTrace();
							}catch(StringIndexOutOfBoundsException e){
								e.getMessage();
								
							}
						}
					}
				}

				parser.reset();

				
				filter=new AndFilter(new TagNameFilter("a"),new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","rent-page"))));
				nodes = parser.extractAllNodesThatMatch(filter);

				if (nodes != null&&nodes.size()>0) {
					TagNode tni = (TagNode) nodes.elementAt(nodes.size()-1);
					String href =DOMAIN_URL+ tni.getAttribute("href");
					if ( href != null&&href.startsWith("http")) {
						if (!visited.contains( href)) {
							int kk = 0;
							for (; kk < urls.size(); kk ++) {
								if (urls.elementAt(kk).equalsIgnoreCase( href)) {
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
			} catch (ParserException e1) {

				e1.printStackTrace();
			}
		}

		synchronized(BJ_RENTOUT) {
			File f = new File(LOG + File.separator + region + ".log");
			f.delete();
			if (newest != null) {
				FileTool.Dump(sdf.format(newest), LOG + File.separator + region + ".log", "UTF-8");
			}
		}
	}


	private static String parseRentOut(String url) {


		//System.out.println("Crawled->"+url);
		String content = HTMLTool.fetchURL(url, "utf-8","get");//GB2312是汉字书写国家标准。
		//System.out.println("url.ok!");


		Parser parser = new Parser();//获取解析器
		if (content == null) {
			return null;
		}

		String poi ="";
		try {

			parser.setInputHTML(content);
			parser.setEncoding("utf-8");


			NodeFilter filter=new AndFilter(new TagNameFilter("h2"),new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","house-main"))));
			NodeList nodes= parser.extractAllNodesThatMatch(filter);
			if (nodes != null && nodes.size() == 1) {

				String str = nodes.elementAt(0).toPlainTextString().replace("&nbsp","").replace("随时", "").replace("入住", "").replace("看房", "").replace("\r\n", "").replace("\t", "").replace("\n", "").replace("拎包", "").replace("新上房源", "").trim();

				String tt=str.replace("，","").replace("、","").replace("《","【").replace("》","】").replace("[","【").replace("]","】").replace("=", "").replace("-", "").replace(",", "").replace("！", "").trim();

				int ix = tt.indexOf("【");
				int ix2=tt.indexOf("】");
				if(ix!=-1&&ix2!=-1) {
					String t1=tt.substring(0,ix);
					String t2=tt.substring(ix2+"】".length());
					jsonObjArr.put("title",t1.replace(" ", "").trim() + t2.replace(" ", "").trim());
				} else {
					jsonObjArr.put("title",tt.replace(" ", "").trim());
				}
				parser.reset();


				Date d = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
				jsonObjArr.put("time",sdf.format(d));

				filter = new AndFilter(new TagNameFilter("li"), new HasParentFilter(new AndFilter(new TagNameFilter("ul"), new HasAttributeFilter("class", "house-info"))));
				nodes = parser.extractAllNodesThatMatch(filter);
				if (nodes != null && nodes.size()>0) {
					for (int mm = 0; mm < 3; mm ++) {
						Node ni = nodes.elementAt(mm);

						if (ni instanceof TagNode && ((TagNode)ni).getTagName().equalsIgnoreCase("li"))

						{
							tt = ni.toPlainTextString().trim();
							tt = tt.replace("（", "(").replace("）", ")").replace("、", "");


							ix = tt.indexOf("租金");

							if (ix != -1) {
								String sub = tt.substring(ix + "租金：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1)
									if(sub.contains("整租"))
										//poi+="<RENT_TYPE>整租</RENT_TYPE>";
								        jsonObjArr.put("rent_type","整租");
									else if(sub.contains("合租"))
										jsonObjArr.put("rent_type","合租");
									else
										jsonObjArr.put("rent_type","暂无");
								
								
								jsonObjArr.put("price",sub.replace(" ", "").replace("整租", "").replace("合租",""));
								continue;
							}

							ix = tt.indexOf("小区");

							if (ix != -1) {

								String sub = tt.substring(ix + "小区：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									//poi += "<COMMUNITY>" + sub.replace(" ", "") + "</COMMUNITY>";
									jsonObjArr.put("community",sub.replace(" ", ""));
								continue;

							}
						}

					}
				}
				parser.reset();
				filter = new AndFilter(new TagNameFilter("li"), new HasParentFilter(new AndFilter(new TagNameFilter("ul"), new HasAttributeFilter("class", "house-info-2"))));
				nodes = parser.extractAllNodesThatMatch(filter);
				if (nodes != null && nodes.size()>0) {
					for (int mm = 0; mm < 6; mm ++) {
						Node ni = nodes.elementAt(mm);

						if (ni instanceof TagNode && ((TagNode)ni).getTagName().equalsIgnoreCase("li"))

						{
							tt = ni.toPlainTextString().trim();
							tt = tt.replace("（", "(").replace("）", ")").replace("、", "");

							ix = tt.indexOf("户型");

							if (ix != -1) {
								String sub = tt.substring(ix + "户型：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace("相似户型", "").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									//poi += "<HOUSE_TYPE>" + sub.replace(" ", "") + "</HOUSE_TYPE>";
									jsonObjArr.put("house_type",sub.replace(" ", ""));
								continue;
							}
							ix = tt.indexOf("装修");
							if (ix != -1) {

								String sub = tt.substring(ix + "装修：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").replace(",", "").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									//poi += "<FITMENT>" + sub.replace(" ", "") + "</FITMENT>";
									jsonObjArr.put("fitment",sub.replace(" ", ""));

								continue;
							}

							ix = tt.indexOf("面积");
							if (ix != -1) {

								String sub = tt.substring(ix + "面积：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").replace(",", "").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									//poi += "<AREA>" + sub.replace(" ", "") + "</AREA>";
									jsonObjArr.put("area",sub.replace(" ", ""));

								continue;
							}

							ix = tt.indexOf("朝向");
							if (ix != -1) {

								String sub = tt.substring(ix + "朝向：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").replace(",", "").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									//poi += "<ORIENTATION>" + sub.replace(" ", "") + "</ORIENTATION>";
									jsonObjArr.put("direction",sub.replace(" ", ""));

								continue;
							}

							ix = tt.indexOf("楼层");
							if (ix != -1) {

								String sub = tt.substring(ix + "楼层：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").replace(",", "").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									//poi += "<FLOOR>" + sub.replace(" ", "") + "</FLOOR>";
									jsonObjArr.put("floor",sub.replace(" ", ""));

								continue;
							}


							ix = tt.indexOf("年代：");
							if (ix != -1) {

								String sub = tt.substring(ix + "年代：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").replace(",", "").trim();
								if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
									//poi += "<BUILT_YEAR>" + sub.replace(" ", "") + "</BUILT_YEAR>";
									jsonObjArr.put("built_year",sub.replace(" ", ""));

								continue;
							}
						}
					}
				}
				parser.reset();

				filter = new AndFilter(new TagNameFilter("ul"), new HasParentFilter(new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "xq-intro-info"))));
				nodes = parser.extractAllNodesThatMatch(filter);
				if (nodes != null && nodes.size()>0) {

					Node ni = nodes.elementAt(0).getFirstChild();

					do {

						tt = ni.toPlainTextString().trim();
						tt = tt.replace("（", "(").replace("）", ")").replace("、", "");

						ix = tt.indexOf("建筑年代：");

						if (ix != -1) {
							String sub = tt.substring(ix + "建筑年代：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace("相似户型", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<BUILT_Date>" + sub.replace(" ", "") + "</BUILT_Date>";
								jsonObjArr.put("built_year",sub.replace(" ", ""));
							ni=ni.getNextSibling();
							continue;
						}
						ix = tt.indexOf("建筑面积：");
						if (ix != -1) {

							String sub = tt.substring(ix + "建筑面积：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<TOTAL_AREA>" + sub.replace(" ", "") + "</TOTAL_AREA>";
								jsonObjArr.put("totalarea",sub.replace(" ", ""));
							ni=ni.getNextSibling();
							continue;
						}

						ix = tt.indexOf("所在版块：");
						if (ix != -1) {

							String sub = tt.substring(ix + "所在版块：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<REGION>" + sub.replace(" ", "") + "</REGION>";
								jsonObjArr.put("location",sub.replace(" ", ""));
							ni=ni.getNextSibling();
							continue;
						}

						ix = tt.indexOf("总户数：");
						if (ix != -1) {

							String sub = tt.substring(ix + "总户数：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<HOUSEHOLDS>" + sub.replace(" ", "") + "</HOUSEHOLDS>";
								jsonObjArr.put("households",sub.replace(" ", ""));
							ni=ni.getNextSibling();
							continue;
						}

						ix = tt.indexOf("容积率：");
						if (ix != -1) {

							String sub = tt.substring(ix + "容积率：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<VOLUME_RATE>" + sub.replace(" ", "") + "</VOLUME_RATE>";
								jsonObjArr.put("volume_rate",sub.replace(" ", ""));
							ni=ni.getNextSibling();
							continue;
						}


						ix = tt.indexOf("绿化率：");
						if (ix != -1) {

							String sub = tt.substring(ix + "绿化率：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<GREEN_RATE>" + sub.replace(" ", "") + "</GREEN_RATE>";
								jsonObjArr.put("green_rate",sub.replace(" ", ""));
							ni=ni.getNextSibling();
							continue;
						}

						ix = tt.indexOf("物业费用：");

						if (ix != -1) {

							String sub = tt.substring(ix + "物业费用：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<PROPERTY_FEE>" + sub.replace(" ", "") + "</PROPERTY_FEE>";
								jsonObjArr.put("property_fee",sub.replace(" ", ""));
							ni=ni.getNextSibling();
							continue;

						}
						ix = tt.indexOf("物业公司：");

						if (ix != -1) {

							String sub = tt.substring(ix + "物业公司：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<PROPERTY>" + sub.replace(" ", "") + "</PROPERTY>";
								jsonObjArr.put("property_company",sub.replace(" ", ""));
							ni=ni.getNextSibling();
							continue;

						}
						ix = tt.indexOf("物业类型：");
						if (ix != -1) {

							String sub = tt.substring(ix + "物业类型：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<PROPERTY_TYPE>" + sub.replace(" ", "") + "</PROPERTY_TYPE>";
								jsonObjArr.put("property",sub.replace(" ", ""));
							ni=ni.getNextSibling();
							continue;

						}
						ix = tt.indexOf("供暖方式：");

						if (ix != -1) {

							String sub = tt.substring(ix + "供暖方式：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<HEAT_SUPPLY>" + sub.replace(" ", "") + "</HEAT_SUPPLY>";
								jsonObjArr.put("heat_supply",sub.replace(" ", ""));
							ni=ni.getNextSibling();
							continue;

						}
						ix = tt.indexOf("开发商：");

						if (ix != -1) {

							String sub = tt.substring(ix + "开发商：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<DEVELOPER>" + sub.replace(" ", "") + "</DEVELOPER>";
								jsonObjArr.put("developer",sub.replace(" ", ""));
							ni=ni.getNextSibling();
							continue;

						}

						ni=ni.getNextSibling();
					} while(ni!=null);


				}
			}
		} catch (ParserException e1) {
// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//System.out.println("poi.ok!");
		jsonObjArr.put("url",url);
		poi=jsonObjArr.toString();
		return  poi;
	}



}


