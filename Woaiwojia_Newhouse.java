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
public class Woaiwojia_Newhouse {


	private static String BJ_NEWHOUSE = "NEW";
	private static String BJ_RENTOUT = "RENTOUT";
	private static String BJ_RESOLDS = "RESOLD";

	public static String LOG = "/home/gir/crawldata/beijing/woaiwojia/newhouse";
	public static String FOLDER1= "/home/gir/crawldata/beijing/woaiwojia/newhouse/woaiwojia_newhouse0428.txt";
	public static String FOLDER2= "/home/gir/crawldata/beijing/woaiwojia/newhouse/woaiwojia_newhouse0428_zhoubian.txt";
	
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
		


		for(int i=0; i<loupan.length; i++)
			getNewBuildingInfo(loupan[i]);//新楼盘

	

	}

	//抓取新房数据

	public static void getNewBuildingInfo(String region) {
		Vector<String> log = null;
		synchronized(BJ_NEWHOUSE) {
			log = FileTool.Load(LOG + File.separator + region + "_new.log", "UTF-8");
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

		String url = NEWBUILDING_URL + region;
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


				NodeFilter filter=new AndFilter(new TagNameFilter("a"),new HasParentFilter(new AndFilter(new TagNameFilter("p"), new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","h-content-m"))))));
				NodeList nodes= parser.extractAllNodesThatMatch(filter);

				if (nodes != null && nodes.size()>0) {
					NodeList ll= nodes.extractAllNodesThatMatch(new TagNameFilter("a"));
					for (int n = 0; n < ll.size(); n ++) {

						TagNode tn =  (TagNode)nodes.elementAt(n);

						String purl = tn.getAttribute("href");

						if(purl.startsWith("/loupan")) {
							purl=DOMAIN_URL+purl;
							String poi2 = parseNewBuilding(purl);
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
										synchronized(BJ_NEWHOUSE) {

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
								Thread.sleep(500 * ((int) (Math
								                           .max(1, Math.random() * 3))));
							} catch (final InterruptedException e1) {

								e1.printStackTrace();
							}

						}
					}
				}

				parser.reset();


				filter=new AndFilter(new TagNameFilter("a"),new HasParentFilter(new AndFilter(new TagNameFilter("li"),new HasAttributeFilter("class","downpage"))));

				nodes = parser.extractAllNodesThatMatch(filter);

				if (nodes != null&& nodes.size()==1) {
					TagNode tni = (TagNode) nodes.elementAt(0);
					String href = DOMAIN_URL+tni.getAttribute("href");
					if ( href != null&&href.startsWith("http")) {
						if (!visited.contains(href)) {
							int kk = 0;
							for (; kk < urls.size(); kk ++) {
								if (urls.elementAt(kk).equalsIgnoreCase(href)) {
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

				e1.printStackTrace();
			}
		}


		synchronized(BJ_NEWHOUSE) {
			File f = new File(LOG + File.separator + region + ".log");
			f.delete();
			if (newest != null) {
				FileTool.Dump(sdf.format(newest), LOG + File.separator + region + ".log", "UTF-8");
			}
		}
	}

	static JSONObject jsonObjArr = new JSONObject();
	private static String parseNewBuilding(String url) {
		//System.out.println("Crawled:"+url);
		String content = HTMLTool.fetchURL(url, "utf-8","get");//GB2312是汉字书写国家标准。
		//System.out.println("url..ok!");

		Parser parser = new Parser();
		if (content == null) {
			return null;
		}

		String poi ="";
		try {

			parser.setInputHTML(content);
			parser.setEncoding("utf-8");

			/*<h3>龙湖双珑原著</h3>*/
			NodeFilter filter = new AndFilter(new TagNameFilter("h3"), new HasParentFilter(new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "h-con"))));
			NodeList nodes= parser.extractAllNodesThatMatch(filter);
			if (nodes != null && nodes.size() == 1) {

				String str = nodes.elementAt(0).toPlainTextString().replace("\r\n", "").replace("\t", "").replace("\n", "").trim();
				if(str!=null)
					//poi += "<TITLE>" + str.replace(" ", "").trim() +"</TITLE>";//标题
					jsonObjArr.put("title",str.replace(" ", "").trim());
			}
			parser.reset();

			Date d = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
			//poi += "<TIME>" + sdf.format(d) + "</TIME>";
			jsonObjArr.put("time",sdf.format(d));

			parser.reset();
			filter = new AndFilter(new TagNameFilter("ul"), new HasParentFilter(new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "h-con-font"))));
			nodes = parser.extractAllNodesThatMatch(filter);
			if(nodes!=null&&nodes.size()==1) {
				String str = nodes.elementAt(0).toPlainTextString().replace("\r\n", "").replace("\t", "").replace("\n", "").replace("，", "").replace(",", "").replace("&nbsp;", "").trim();

				int ix=str.indexOf("总价：");
				int ix2=str.indexOf("均价");
				if(ix!=-1&&ix2!=-1) {
					String sub=str.substring(ix+"总价：".length(), ix2).trim();
					//poi+="<PRICE>"+sub+"</PRICE>";
					jsonObjArr.put("price",sub);
				}
				ix=str.indexOf("均价：");
				ix2=str.indexOf("开盘时间");
				if(ix!=-1&&ix2!=-1) {
					String sub=str.substring(ix+"均价：".length(), ix2).replace("元/平米", "").trim();
					//poi+="<UNIT_PRICE>"+sub+"</UNIT_PRICE>";
					jsonObjArr.put("unit_price",sub);
				}

				ix=str.indexOf("开盘时间：");
				ix2=str.indexOf("主力");
				if(ix!=-1&&ix2!=-1) {
					String sub=str.substring(ix+"开盘时间：".length(), ix2).trim();
					if(sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
						//poi+="<OPENING_DATE>"+sub+"</OPENING_DATE>";
						jsonObjArr.put("open_time",sub);
				}

				ix=str.indexOf("主力户型：");
				ix2=str.indexOf("地址");
				if(ix!=-1&&ix2!=-1) {
					String sub=str.substring(ix+"主力户型".length(), ix2).replace("&nbsp", "").replace(" ", "").trim();
					if(sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
						//poi+="<TYPE>"+sub+"</TYPE>";
						jsonObjArr.put("house_type",sub);
				}

				ix=str.indexOf("地址：");
				ix2=str.indexOf("4008");
				if(ix!=-1&&ix2!=-1) {
					String sub=str.substring(ix+"地址".length(), ix2).trim();
					if(sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
						jsonObjArr.put("address",sub);
				}

			}
			parser.reset();
			filter = new AndFilter(new TagNameFilter("li"), new OrFilter( new HasAttributeFilter("class", "w450"),new HasAttributeFilter("class", "w960")));
			nodes = parser.extractAllNodesThatMatch(filter);
			if (nodes != null && nodes.size()>0) {
				for (int mm = 0; mm < nodes.size(); mm ++) {
					Node ni = nodes.elementAt(mm);

					if (ni instanceof TagNode && ((TagNode)ni).getTagName().equalsIgnoreCase("li"))

					{
						String tt = ni.toPlainTextString().trim();
						tt = tt.replace("（", "(").replace("）", ")").replace("、", ",").replace("&nbsp;","").replace(":","");

						int ix = tt.indexOf("装修情况");
						if (ix != -1) {

							String sub = tt.substring(ix + "装修情况".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<FITMENT>" + sub.replace(" ", "") + "</FITMENT>";
								jsonObjArr.put("fitment",sub);

							continue;
						}
						ix = tt.indexOf("容积率");
						if (ix != -1) {

							String sub = tt.substring(ix + "容积率".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<VOLUME_RATE>" + sub.replace(" ", "") + "</VOLUME_RATE>";volume_rate
								jsonObjArr.put("volume_rate",sub);

							continue;
						}
						ix = tt.indexOf("入住时间");

						if (ix != -1) {
							String sub = tt.substring(ix + "入住时间".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace("开盘通知我", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<PROSSESSION_DATE>" + sub.replace(" ", "") + "</PROSSESSION_DATE>";
								jsonObjArr.put("completed_time",sub);
							continue;
						}
						if (ix != -1) {

							String sub = tt.substring(ix + "产权年限".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<EQUILT_TIME>" + sub.replace(" ", "") + "</EQUILT_TIME>";
								jsonObjArr.put("term",sub);

							continue;
						}

						ix = tt.indexOf("绿化率");
						if (ix != -1) {

							String sub = tt.substring(ix + "绿化率".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").replace(",", "").replace("(绿化率高)","").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<GREEN_RATE>" + sub.replace(" ", "") + "</GREEN_RATE>";
								jsonObjArr.put("green_rate",sub);

							continue;
						}
						ix = tt.indexOf("项目均价");
						if (ix != -1) {

							String sub = tt.substring(ix + "项目均价".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").replace(",", "").replace("(绿化率高)","").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<AVERAGE_PRICE>" + sub.replace(" ", "") + "</AVERAGE_PRICE>";
								jsonObjArr.put("unit_price",sub);

							continue;
						}
						ix = tt.indexOf("开发商");
						if (ix != -1) {

							String sub = tt.substring(ix + "开发商".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<DEVELOPER>" + sub.replace(" ", "") + "</DEVELOPER>";
								jsonObjArr.put("developer",sub);

							continue;

						}
						ix = tt.indexOf("项目地址");
						if (ix != -1) {

							String sub = tt.substring(ix + "项目地址".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<PROJECT_ADDRESS>" + sub.replace(" ", "") + "</PROJECT_ADDRESS>";
								jsonObjArr.put("address",sub);

							continue;

						}
						ix = tt.indexOf("小区配套");
						if (ix != -1) {

							String sub = tt.substring(ix + "小区配套".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<NEARBY_DISTRICT>" + sub.replace(" ", "") + "</NEARBY_DISTRICT>";
								jsonObjArr.put("eqiupment",sub);

							continue;

						}
						ix = tt.indexOf("楼层情况");
						if (ix != -1) {

							String sub = tt.substring(ix + "楼层情况".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<FLOORS_LAYOUT>" + sub.replace(" ", "") + "</FLOORS_LAYOUT>";
								jsonObjArr.put("floor",sub);

							continue;

						}
						ix = tt.indexOf("项目简称");
						if (ix != -1) {

							String sub = tt.substring(ix + "项目简称".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<PROJECT_NAME>" + sub.replace(" ", "") + "</PROJECT_NAME>";
								jsonObjArr.put("community",sub);

							continue;

						}
						ix = tt.indexOf("建筑面积");
						if (ix != -1) {

							String sub = tt.substring(ix + "建筑面积".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<BUILDING_AREA>" + sub.replace(" ", "") + "</BUILDING_AREA>";totalarea
								jsonObjArr.put("totalarea",sub);

							continue;

						}
						ix = tt.indexOf("建筑类型");

						if (ix != -1) {

							String sub = tt.substring(ix + "建筑类型".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").replace(",", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<BUILDING_TYPE>" + sub.replace(" ", "") + "</BUILDING_TYPE>";biult_type
								jsonObjArr.put("biult_type",sub);

							continue;
						}

						ix = tt.indexOf("物业公司");
						if (ix != -1) {

							String sub = tt.substring(ix + "物业公司".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<PROPERTY>" + sub.replace(" ", "") + "</PROPERTY>";
								jsonObjArr.put("property_company",sub);

							continue;

						}
						ix = tt.indexOf("总套数");

						if (ix != -1) {
							String sub = tt.substring(ix + "总套数".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<HOUSEHOLDS>" + sub.replace(" ", "") + "</HOUSEHOLDS>";
								jsonObjArr.put("households",sub);
							continue;
						}
						ix = tt.indexOf("开盘日期");

						if (ix != -1) {
							String sub = tt.substring(ix + "开盘日期".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<OPENING_DATE>" + sub.replace(" ", "") + "</OPENING_DATE>";
								jsonObjArr.put("open_time",sub);
							continue;
						}

						ix = tt.indexOf("车位费");

						if (ix != -1) {
							String sub = tt.substring(ix + "车位费".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<PARKING_FEE>" + sub.replace(" ", "") + "</PARKING_FEE>";
								jsonObjArr.put("park",sub);
							continue;
						}
						ix = tt.indexOf("车位比例");

						if (ix != -1) {
							String sub = tt.substring(ix + "车位比例".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<PARKING_RATIO>" + sub.replace(" ", "") + "</PARKING_RATIO>";
								jsonObjArr.put("park_rate",sub);
							continue;
						}

						ix = tt.indexOf("物业费");

						if (ix != -1) {

							String sub = tt.substring(ix + "物业费".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<PROPERTY_FEE>" + sub.replace(" ", "") + "</PROPERTY_FEE>";
								jsonObjArr.put("property_fee",sub);
							ni=ni.getNextSibling();
							continue;

						}

						ix = tt.indexOf("物业类型");
						if (ix != -1) {

							String sub = tt.substring(ix + "物业类型".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").trim();
							if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
								//poi += "<PROPERTY_TYPE>" + sub.replace(" ", "") + "</PROPERTY_TYPE>";
								jsonObjArr.put("property",sub);
							ni=ni.getNextSibling();
							continue;

						}
					}
				}

			}
		} catch (ParserException e1) {

			e1.printStackTrace();
		}

		if (poi != null) {
			poi = poi.replace("&nbsp;", "").replace("&nbsp", "");
			int ss = poi.indexOf("[");
			while (ss != -1) {
				int ee = poi.indexOf("]", ss + 1);
				if (ee != -1) {
					String sub = poi.substring(ss, ee + 1);
					poi = poi.replace(sub, "");
				} else
					break;
				ss = poi.indexOf("[", ss);
			}
		}
		jsonObjArr.put("url",url);
		poi=jsonObjArr.toString().replace("：", "");
		return poi;
	}

}


