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
public class Lianjia_Resold {



	private static String BJ_NEWHOUSE = "NEW";
	private static String BJ_RENTOUT = "RENTOUT";
	private static String BJ_RESOLDS = "RESOLD";

	public static String FOLDER1= "/home/gir/crawldata/beijing/lianjia/resold/lianjia_resold0428.txt";
	public static String FOLDER2= "/home/gir/crawldata/beijing/lianjia/resold/lianjia_resold0428_zhoubian.txt";
	public static String LOG=FOLDER1; 
	public static String regions[] = {
		"/dongcheng/","/xicheng/","/chaoyang/","/haidian/","/fengtai/",
		"/shijingshan/","/tongzhou/","/changping/","/daxing/",
		"/yizhuangkaifaqu/","/shunyi/","/fangshan/","/mentougou/",
		"/pinggu/","/huairou/","/miyun/","/yanqing/","/yanjiao/"
	};

	public static String RESOLDAPARTMENT_URL = "http://bj.lianjia.com/ershoufang";
	public static String DOMAIN_URL = "http://bj.lianjia.com";
	private static boolean first = true; //由于楼盘页数由js动态生成，所以用此充当flag，仅第一次从第一页获取总页数
	public static void main(String[] args) {

		for(int k=0;k< regions.length;k++){
			String tempurl="http://bj.lianjia.com/ershoufang"+regions[k];
			String pages=getTotalPage(tempurl);
			int total=Integer.parseInt(pages);
			for(int i=1;i<=total;i++){
				String url=tempurl+"pg"+i+"/";
				System.out.println("第"+i+"页");
				getResoldApartmentInfo(url);// 租房
			}
		}

	}
	public static String getTotalPage(String url){
		String pages="";
		Parser parser = new Parser();
		String content = HTMLTool.fetchURL(url, "utf-8","get");
		if(content!=null){
			try {
				parser.setInputHTML(content);
				parser.setEncoding("utf-8");

				NodeFilter filter=new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","page-box house-lst-page-box"));

				NodeList nodes = parser.extractAllNodesThatMatch(filter);

				if (nodes != null&& nodes.size()==1) {
					TagNode tni = (TagNode) nodes.elementAt(0);
					String totalPage =tni.getAttribute("page-data");
					if ( totalPage != null&& totalPage.startsWith("{")) {
						totalPage=totalPage.substring(totalPage.indexOf("{")+"\" totalPage\":".length(),totalPage.indexOf(",")).trim();
						pages=totalPage;
					}
				}
			}catch (ParserException e1) {

				e1.printStackTrace();
		 }
	}
	return pages;
}

	//抓取二手房数据
	public static void getResoldApartmentInfo(String url) {


		SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");//

		java.util.Date latestdate = null;
		Date newest = null;


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

				NodeFilter filter=new AndFilter(new TagNameFilter("h2"),new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","info-panel"))));
				NodeList nodes = parser.extractAllNodesThatMatch(filter);

				if (nodes != null&& nodes.size()>0) {
					for (int n = 0; n < nodes.size(); n ++) {

						TagNode tn=(TagNode)nodes.elementAt(n);

						String purl=((TagNode)tn.getChildren().elementAt(0)).getAttribute("href");
						if(purl.indexOf("/ershoufang")!=-1) {
							String poi2 = parseResold(purl);
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
										synchronized(BJ_RESOLDS)

										{
											poi.replace(" ", "").replace("\r\n","").replace("\n","").replace("\b","").replace("\t","").trim();
											if(url.indexOf("/yanjiao/")!=-1)
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
							} catch (NullPointerException e1) {

								e1.printStackTrace();
							}
						}
					}
				}
				if (quit)
					break;
			} catch (ParserException e1) {

				e1.printStackTrace();
			}
		}

	}

	static JSONObject jsonObjArr = new JSONObject();
	private static String parseResold(String url) {

		//System.out.println("Cralwed->"+url);
		String content = HTMLTool.fetchURL(url, "utf-8","get");//GB2312是汉字书写国家标准。
		//System.out.println("url.ok!");

		Parser parser = new Parser();
		if (content == null) {
			return null;
		}

		String poi ="";
		try {

			parser.setInputHTML(content);
			parser.setEncoding("utf-8");

			NodeFilter filter=new AndFilter(new TagNameFilter("h1"),new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","line01"))));
			NodeList nodes= parser.extractAllNodesThatMatch(filter);
			if (nodes != null && nodes.size() == 1) {

				String str = nodes.elementAt(0).toPlainTextString().replace("&nbsp","").replace("随时", "").replace("入住", "").replace("看房", "").replace("\r\n", "").replace("\t", "").replace("\n", "").replace("拎包", "").replace("随时看%", "").replace("有钥匙%", "").trim();

				String tt=str.replace("，","").replace("、","").replace("《","【").replace("》","】").replace("[","【").replace("]","】").replace("=", "").replace("-", "").replace(",", "").replace("！", "").trim();

				int ix = tt.indexOf("【");
				int ix2=tt.indexOf("】");
				if(ix!=-1&&ix2!=-1) {
					String t1=tt.substring(0,ix);
					String t2=tt.substring(ix2+"】".length());
					//poi += "<TITLE>" + t1.replace(" ", "").trim() + t2.replace(" ", "").trim() +"</TITLE>";//大标题
					jsonObjArr.put("title",t1.replace(" ", "").trim() + t2.replace(" ", "").trim());
				} else {
					//poi += "<TITLE>" + tt.replace(" ", "").trim() + "</TITLE>";//大标题
					jsonObjArr.put("title",tt.replace(" ", "").trim());
				}
				parser.reset();

				Date d = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
				jsonObjArr.put("time",sdf.format(d));

				filter = new AndFilter(new TagNameFilter("dl"), new HasParentFilter(new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "desc-text clear"))));
				nodes = parser.extractAllNodesThatMatch(filter);
				if (nodes != null &&nodes.size()>0) {

					for (int mm = 0; mm < 8; mm ++) {
						Node ni = nodes.elementAt(mm);
						tt = ni.toPlainTextString().trim();
						tt = tt.replace("（", "(").replace("）", ")").replace("、", "");
						if (mm==0) {

							ix = tt.indexOf("售价：");
							ix2 = tt.indexOf("/");

							if (ix != -1) {
								String sub =tt.substring(ix+"售价：".length(),ix2)+"</PRICE><AREA>"+tt.substring(ix2).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
								if (sub!=null && sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1){
										jsonObjArr.put("price",tt.substring(ix+"售价：".length(),ix2));
										jsonObjArr.put("area",tt.substring(ix2).replace("\r\n", "").replace("\t", "").replace("/", "").trim());
								}
								continue;
                            }
						}else{

							if (ni instanceof TagNode && ((TagNode)ni).getTagName().equalsIgnoreCase("dl")){
								ix = tt.indexOf("单价：");
								if(ix!=-1){
									jsonObjArr.put("unit_price",tt.replace("单价：", ""));
								}


								ix = tt.indexOf("首付：");
								if (ix != -1) {

									String sub = tt.substring(ix+"首付：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").trim();
									if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
										//poi += "<DOWN_PAYMENT>" + sub.replace(" ", "") + "</DOWN_PAYMENT>";
										jsonObjArr.put("down_payment",sub.replace(" ", ""));
									continue;

								}

								ix = tt.indexOf("月供：");
								if (ix != -1) {

									String sub = tt.substring(ix+"月供：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").trim();
									if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
										//poi += "<MONTH_PAYMENT>" + sub.replace(" ", "") + "</MONTH_PAYMENT>";
										jsonObjArr.put("month_payment",sub.replace(" ", ""));

									continue;

								}

								ix = tt.indexOf("户型");

								if (ix != -1) {
									String sub = tt.substring(ix+"户型：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").trim();
									if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
										//poi += "<HOUSE_TYPE>" + sub.replace(" ", "") + "</HOUSE_TYPE>";
										jsonObjArr.put("house_type",sub.replace(" ", ""));
									continue;
								}
								ix = tt.indexOf("朝向");
								if (ix != -1) {

									String sub = tt.substring(ix+"朝向：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").trim();
									if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
										//poi += "<ORIENTATION>" + sub.replace(" ", "") + "</ORIENTATION>";
										jsonObjArr.put("direction",sub.replace(" ", ""));

									continue;
								}

								ix = tt.indexOf("楼层");
								if (ix != -1) {

									String sub = tt.substring(ix+"楼层：".length()).replace("\r\n", "").replace("\t", "").replace("\n", "").replace(" ", "").trim();
									if (sub!=null&&sub.indexOf("暂无") == -1&&sub.indexOf("待定")==-1)
										//poi += "<FLOOR>" + sub.replace(" ", "") + "</FLOOR>";
										jsonObjArr.put("floor",sub.replace(" ", ""));

									continue;
								}
								ix = tt.indexOf("小区：");
								ix2 = tt.indexOf("(");
								if (ix != -1) {

									String sub = tt.substring(ix+"小区：".length(),ix2)+"<COMMUNITY><BUILT_YEAR>"+tt.substring(tt.indexOf(")")).replace("\r\n", "").replace("\t", "").replace(" ", "").trim();
//小区：中景濠庭(东城&nbsp;安定门)2002年
										if(tt.indexOf("(")!=-1&&tt.indexOf(")")!=-1){
											jsonObjArr.put("community",tt.substring(0,tt.indexOf("(")).replace("小区：", ""));
											String location=tt.substring(tt.indexOf("("),tt.indexOf(")")).replace("(", "").replace("&nbsp;", "");
											jsonObjArr.put("location",location);
											String year=tt.substring(tt.indexOf(")"),tt.indexOf("年")).replace(")", "");
											jsonObjArr.put("built_year",year);
										}else{
											jsonObjArr.put("community",tt.replace("", ""));
										}

									continue;
								}
							}
						}
					}
				}


//没有小区信息#################################

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
		poi=jsonObjArr.toString();
		return  poi ;
	}

}

