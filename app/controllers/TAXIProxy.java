package controllers;

import java.io.IOException;
import java.lang.System;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.DOMException;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import play.utils.UriEncoding;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * If no license is here then you can whatever you like!
 * and of course I am not liable
 *
 * Created by kostandin on 05/07/15.
 */
 
public class TAXIProxy extends Controller {

    public static F.Promise<Result> index(String query) {

        if(StringUtils.isEmpty(query)){

            F.Promise.promise(new F.Function0<Object>() {
                @Override
                public Object apply() throws Throwable {
                    return ok(Json.toJson("Query parameter (q) not provided "));
                }

            });
        }

        final String baseUrl = "http://designtaxi.com/";

        F.Promise<WSResponse> wsResponsePromise = WS.url(baseUrl + "news-search.php").setQueryParameter("news_keyword",query).get();

        return wsResponsePromise.map(new F.Function<WSResponse, Result>() {
            @Override
            public Result apply(WSResponse wsResponse) throws Throwable {

                String body = wsResponse.getBody();
                List<Map<String,String>> results  = new ArrayList<Map<String, String>>();

                try {
                    // Insert into map
                    org.jsoup.nodes.Document doc = Jsoup.parse(body);
                    Elements items = doc.select("div.news-cover");
                    
                    // Iterate through results
                    for (Element item : items) {
                        Map<String,String> keyValue = new LinkedHashMap<String, String>();
                        
                        keyValue.put("image", baseUrl + item.select("img").attr("src"));
                        keyValue.put("title", item.select("a.addthis_button_expanded").attr("addthis:title"));
                        
                        
                        // Connect to each and every article to get date and first sentence as content
                        try {
                            org.jsoup.nodes.Document articleDoc = Jsoup.connect(item.select("a.addthis_button_expanded").attr("addthis:url")).userAgent("Mozilla").get();
                            
                            // If connection successful(STATUS 200), the add content and date keys to map
                            keyValue.put("content", articleDoc.select("div#news-content").text().substring(0, articleDoc.select("div#news-content").text().indexOf(".") + 1) + ".");
                            keyValue.put("date", articleDoc.select("span.date").text());
                            
                        } catch (IOException e) {
                            System.out.println(e);
                        }
                        
                        keyValue.put("url", item.select("a.addthis_button_expanded").attr("addthis:url"));
                        
                        results.add(keyValue);
                    }
                    
                } catch (DOMException e) {
                    e.printStackTrace();
                }

                return ok(Json.toJson(results));
            }
        });
    }
}
