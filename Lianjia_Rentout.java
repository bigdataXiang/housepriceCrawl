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

public class Lianjia_Rentout {


	private static String BJ_RENTOUT = "RENTOUT";


	public static String FOLDER1 = "/home/gir/crawldata/beijing/lianjia/rentout/lianjia_rentout0414.txt";
	public static String FOLDER2 = "/home/gir/crawldata/beijing/lianjia/rentout/lianjia_rentout0414_zhoubian.txt";
	public static String LOG = FOLDER1;
	public static String regions[] = { "/dongcheng/", "/xicheng/", "/chaoyang/", "/haidian/", "/fengtai/",
			"/shijingshan/", "/tongzhou/", "/changping/", "/daxing/", "/yizhuangkaifaqu/", "/shunyi/", "/fangshan/",
			"/mentougou/", "/pinggu/", "/huairou/", "/miyun/", "/yanqing/", "/yanjiao/" };

	public static String RENTOUT_URL = "http://bj.lianjia.com/zufang/";
	public static String DOMAIN_URL = "http://bj.lianjia.com";
	private static boolean firstloupan = true; // 由于楼盘页数由js动态生成，所以用此充当flag，仅第一次从第一页获取总页数
	private static boolean firstershoufang = true; // 同上

	public static void main(String[] args) {
		for(int k=0;k< regions.length;k++){
			String tempurl="http://bj.lianjia.com/zufang"+regions[k];
			for(int i=1;i<=100;i++){
				String url=tempurl+"pg"+i+"/";
				System.out.println("第"+i+"页");
				getRentOutInfo(url);// 租房
			}
		}
		
    }

	// 抓取出租数据
	public static void getRentOutInfo(String url) {

		Vector<String> log = null;
		synchronized (BJ_RENTOUT) {
			log = FileTool.Load(LOG + File.separator + "_rentout.log", "UTF-8");
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");

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

		Vector<String> urls = new Vector<String>();

		Set<String> visited = new TreeSet<String>();
		urls.add(url);

		Parser parser = new Parser();
		boolean quit = false;

		while (urls.size() > 0) {

			url = urls.get(0);

			urls.remove(0);
			visited.add(url);

			String content = HTMLTool.fetchURL(url, "utf-8", "get");
			if (content == null) {
				continue;
			}
			try {

				parser.setInputHTML(content);
				parser.setEncoding("utf-8");

				HasParentFilter parent1 = new HasParentFilter(
						new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "info-panel")));
				HasParentFilter parent2 = new HasParentFilter(new AndFilter(new TagNameFilter("h2"), parent1));
				NodeFilter filter = new AndFilter(new TagNameFilter("a"), parent2);
				NodeList nodes = parser.extractAllNodesThatMatch(filter);

				if (nodes != null && nodes.size() > 0) {
					for (int n = 0; n < nodes.size(); n++) {

						TagNode tn = (TagNode) nodes.elementAt(n);

						String purl = tn.getAttribute("href");
						if (purl.startsWith("http")) {
							String poi2 = parseRentOut(purl);
							if (poi2 == null)
								continue;
							String poi = poi2.replace("&nbsp;", "").replace("&nbsp", "").replace("()", "");
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
										synchronized (BJ_RENTOUT) {
											poi.replace(" ", "").replace("\r\n", "").replace("\n", "").replace("\b", "")
													.replace("\t", "").trim();
											if(url.indexOf("yanjiao")!=-1){
												FileTool.Dump(poi, FOLDER2, "UTF-8");
											}else{
												FileTool.Dump(poi, FOLDER1, "UTF-8");
											}
											
										}
									}
							}

							try {
								Thread.sleep(500 * ((int) (Math.max(1, Math.random() * 3))));
							} catch (final InterruptedException e1) {
								e1.printStackTrace();
							}
						}
					}
				}
			}catch(ParserException e){
				System.out.println(e.getMessage());
			}
		}
	}



	static JSONObject jsonObjArr = new JSONObject();

	private static String parseRentOut(String url) {


		//System.out.println("Crawled->"+url);
		String content = HTMLTool.fetchURL(url, "utf-8","get");//GB2312是汉字书写国家标准。
		//System.out.println("url.ok!");

		Parser parser = new Parser();//获取解析器
		if (content == null) {
			return null;
		}

		String poi ="";
		String tt="";
		int ix;
		int ix2;
		try {

			parser.setInputHTML(content);
			parser.setEncoding("utf-8");
			NodeFilter filter=new AndFilter(new TagNameFilter("h2"),new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class","line01"))));
			NodeList nodes= parser.extractAllNodesThatMatch(filter);

			if (nodes != null && nodes.size() == 1) {

				String str = nodes.elementAt(0).toPlainTextString().replace("&nbsp","").replace("\r\n", "").replace("\t", "").replace("\n", "").trim();
				jsonObjArr.put("title",str);
			}
            parser.reset();
            Date d = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
			jsonObjArr.put("time",sdf.format(d));

			filter = new AndFilter(new TagNameFilter("dl"), new HasParentFilter(new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "desc-text clear"))));
			nodes = parser.extractAllNodesThatMatch(filter);
			if (nodes != null && nodes.size()>0) {
				for(int i=0;i<nodes.size();i++){
					TagNode no = (TagNode) nodes.elementAt(i);
					String str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "").trim();
					if(str.indexOf("租金：")!=-1){
						if(str.indexOf("/")!=-1){
							jsonObjArr.put("price",str.substring(0, str.indexOf("/")).replace("租金：", ""));
							jsonObjArr.put("area",str.substring(str.indexOf("/")).replace("/", ""));
						}else{
							jsonObjArr.put("price",str.replace("租金：", ""));
						}
					}
					if(str.indexOf("户型：")!=-1){
						jsonObjArr.put("house_type",str.replace("", ""));
					}
					if(str.indexOf("朝向：")!=-1){
						jsonObjArr.put("direction",str);
					}
					if(str.indexOf("楼层：")!=-1){
						jsonObjArr.put("floor",str);
					}
					if(str.indexOf("小区：")!=-1){
						if(str.indexOf("（")!=-1&&str.indexOf("）")!=-1){
							jsonObjArr.put("community",str.substring(0,str.indexOf("（")).replace("小区：", ""));
							String location=str.substring(str.indexOf("（"),str.indexOf("）")).replace("（", "").replace("&nbsp;", "");
							jsonObjArr.put("location",location);
							String year=str.substring(str.indexOf("）"),str.indexOf("年")).replace("）", "");
							jsonObjArr.put("built_year",year);
						}else{
							jsonObjArr.put("community",str.replace("", ""));
						}
						
					}
					if(str.indexOf("更新：")!=-1){
						jsonObjArr.put("time",str.replace("更新：", ""));
					}
				}
			}

		}catch(ParserException e1){

		e1.printStackTrace();
	} 
	
	jsonObjArr.put("url",url);
	poi=jsonObjArr.toString();
	return poi;
}}
