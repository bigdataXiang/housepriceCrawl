package com.svail.crawl.lianjia;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import com.svail.util.FileTool;
import com.svail.util.HTMLTool;

import net.sf.json.JSONObject;
public class Lianjia_Newhouse {



	private static String BJ_NEWHOUSE = "NEW";
	private static String BJ_RENTOUT = "RENTOUT";
	private static String BJ_RESOLDS = "RESOLD";

	public static String FOLDER=  "/home/gir/crawldata/beijing/lianjia/newhouse/lianjia_newhouse0428.txt";
	public static String LOG=FOLDER; 
	public static String regions[] = {
		"/dongcheng/","/xicheng/","/chaoyang/","/haidian/","/fengtai/",
		"/shijingshan/","/tongzhou/","/changping/","/daxing/",
		"/yizhuangkaifaqu/","/shunyi/","/fangshan/","/mentougou/",
		"/pinggu/","/huairou/","/miyun/","/yanqing/","/yanjiao/"
	};

	static JSONObject jsonObjArr = new JSONObject();
	public static String RENTOUT_URL = "http://www.zufangzi.com/area";
	public static String RESOLDAPARTMENT_URL = "http://bj.lianjia.com/ershoufang";
	public static String NEWBUILDING_URL = "http://bj.lianjia.com/loupan/";
	public static String DOMAIN_URL = "http://bj.lianjia.com";
	private static boolean firstloupan = true; //由于楼盘页数由js动态生成，所以用此充当flag，仅第一次从第一页获取总页数
	private static boolean firstershoufang =true; //同上
	public static void main(String[] args) {
		getNewBuildingInfo(); //新楼盘
	}



//抓取新房数据

	public static void getNewBuildingInfo() {
		Vector<String> log = null;
		synchronized(BJ_NEWHOUSE) {
			log = FileTool.Load(LOG + File.separator  + "_new.log", "UTF-8");
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

		String url = NEWBUILDING_URL;
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


				NodeFilter filter=new AndFilter(new TagNameFilter("h2"),new HasParentFilter(new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class","col-1"))));
				NodeList nodes= parser.extractAllNodesThatMatch(filter);

				if (nodes != null&&nodes.size()>0) {
					for (int n = 0; n < nodes.size(); n ++) {

						TagNode tn =  (TagNode)nodes.elementAt(n).getChildren().elementAt(1);

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
											FileTool.Dump(poi,FOLDER, "UTF-8");
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


				if (firstloupan) {

					firstloupan=false;
					filter=new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","page-box house-lst-page-box"));

					nodes = parser.extractAllNodesThatMatch(filter);

					if (nodes != null&& nodes.size()==1) {
						TagNode tni = (TagNode) nodes.elementAt(0);
						String totalPage =tni.getAttribute("page-data");
						if ( totalPage != null&& totalPage.startsWith("{")) {
							totalPage=totalPage.substring(totalPage.indexOf("{")+"\" totalPage\":".length(),totalPage.indexOf(",")).trim();
							int a= Integer.parseInt(totalPage);
							String[] pg= new String[a-1];
							for (int i = 2; i<=a; i++ ) {
								pg[i-2]="pg"+new Integer(i).toString();
								if (!visited.contains(NEWBUILDING_URL+pg[i-2])) {
									int kk = 0;
									for (; kk < urls.size(); kk ++) {
										if (urls.elementAt(kk).equalsIgnoreCase(NEWBUILDING_URL+pg[i-2])) {
											break;
										}
									}

									if (kk == urls.size())
										urls.add(NEWBUILDING_URL+pg[i-2]);
								}
							}
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
			File f = new File(LOG + File.separator  + ".log");
			f.delete();
			if (newest != null) {
				FileTool.Dump(sdf.format(newest), LOG + File.separator + ".log", "UTF-8");
			}
		}
	}

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
			Date d = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
			jsonObjArr.put("time",sdf.format(d));
			//poi += "<TIME>" + sdf.format(d) + "</TIME>";

			parser.reset();
			NodeFilter	filter = new AndFilter(new TagNameFilter("p"), new HasAttributeFilter("class", "desc-p clear"));
			NodeList	nodes = parser.extractAllNodesThatMatch(filter);
			if (nodes != null&&nodes.size()>0) {
				for (int mm = 0; mm < nodes.size(); mm ++) {
					Node ni = nodes.elementAt(mm);

					String tt = ni.toPlainTextString().trim();
					tt = tt.replace(" ", "").replace("（", "(").replace("）", ")").replace("、", ",").replace("&nbsp;","").replace(":","").replace("\r\n", "").replace("\t", "").replace("\n", "").replace("m²", "平米").replace("㎡", "平米");

                    if(tt.indexOf("项目地址：")!=-1){
                    	jsonObjArr.put("address",tt.replace("项目地址：", ""));
                    }
                    if(tt.indexOf("售楼处地址：")!=-1){
                    	jsonObjArr.put("sales_address",tt.replace("售楼处地址：", ""));
                    }
                    if(tt.indexOf("开发商：")!=-1){
                    	jsonObjArr.put("developer",tt.replace("开发商：", ""));
                    }
                    if(tt.indexOf("物业公司：")!=-1){
                    	jsonObjArr.put("property_company",tt.replace("物业公司：", ""));
                    }
                    if(tt.indexOf("最新开盘：")!=-1){
                    	jsonObjArr.put("open_time",tt.replace("最新开盘：", ""));
                    }
                    if(tt.indexOf("最早交房：")!=-1){
                    	jsonObjArr.put("completed_time",tt.replace("最早交房：", ""));
                    }
                    if(tt.indexOf("产权年限：")!=-1){
                    	jsonObjArr.put("term",tt.replace("产权年限：", ""));
                    }
                    if(tt.indexOf("规划户数：")!=-1){
                    	jsonObjArr.put("households",tt.replace("规划户数：", ""));
                    }
                    if(tt.indexOf("车位情况：")!=-1){
                    	jsonObjArr.put("park",tt.replace("车位情况：", ""));
                    }
                    if(tt.indexOf("装修状况：")!=-1){
                    	jsonObjArr.put("fitment",tt.replace("装修状况：", ""));
                    }
                    if(tt.indexOf("建筑类型：")!=-1){
                    	jsonObjArr.put("biult_type",tt.replace("建筑类型：", ""));
                    }
                    if(tt.indexOf("物业类型：")!=-1){
                    	jsonObjArr.put("property",tt.replace("物业类型：", ""));
                    }
                    if(tt.indexOf("容积率：")!=-1){
                    	jsonObjArr.put("volume_rate",tt.replace("容积率：", ""));
                    }
                    if(tt.indexOf("绿化率：")!=-1){
                    	jsonObjArr.put("green_rate",tt.replace("绿化率：", ""));
                    }
                    if(tt.indexOf("物业费用：")!=-1){
                    	jsonObjArr.put("property_fee",tt.replace("物业费用：", ""));
                    }
                    if(tt.indexOf("供暖方式：")!=-1){
                    	jsonObjArr.put("heat_supply",tt.replace("供暖方式：", ""));
                    }
                    if(tt.indexOf("建筑面积：")!=-1){
                    	jsonObjArr.put("totalarea",tt.replace("建筑面积：", ""));
                    }

				}
           }
			
			jsonObjArr.put("totalarea",url);
            poi=jsonObjArr.toString();

		} catch (ParserException e1) {

			e1.printStackTrace();
		}
		return poi;
	}

}

