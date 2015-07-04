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
 * Created by kostandin on 16/04/15.
 */
 
public class NWProxy extends Controller {

    public static F.Promise<Result> index(String query) {

        if(StringUtils.isEmpty(query)){

            F.Promise.promise(new F.Function0<Object>() {
                @Override
                public Object apply() throws Throwable {
                    return ok(Json.toJson("Query parameter (q) not provided "));
                }

            });
        }

        final String officialUrl = "http://www.newsweek.com";

        F.Promise<WSResponse> wsResponsePromise = WS.url(officialUrl + "/search/site/" + query).get();

        return wsResponsePromise.map(new F.Function<WSResponse, Result>() {
            @Override
            public Result apply(WSResponse wsResponse) throws Throwable {


                String body = wsResponse.getBody();

                List<Map<String,String>> results  = new ArrayList<Map<String, String>>();

                try {
                    
                    // Insert into map
                    org.jsoup.nodes.Document doc = Jsoup.parse(body);
                    Elements items = doc.select("li.search-result");            // All articles belong to this class
                    
                    for (Element item : items) {
                        Map<String,String> keyValue = new LinkedHashMap<String, String>();
                        
                        keyValue.put("image", item.select("img").attr("src"));
                        keyValue.put("title", item.select("h2").select("a").text());
                        keyValue.put("content", item.select("div.article-summary").first().text());
                        
                        // Get date from each article separately
                        org.jsoup.nodes.Document articleDoc = RedirectionHandler(officialUrl + item.select("a").attr("href"));
                        
                        keyValue.put("date", articleDoc.select("span.timedate").text());
                        keyValue.put("url", officialUrl + item.select("a").attr("href"));
                        
                        results.add(keyValue);
                    }
                } catch (DOMException e) {
                    e.printStackTrace();
                }

                return ok(Json.toJson(results));
            }
        });
    }
    
    private static org.jsoup.nodes.Document RedirectionHandler(String url) throws IOException {

        org.jsoup.nodes.Document articleDoc = Jsoup.connect(url).get();
        String officialUrl = "http://www.newsweek.com";
        String redirectedUrl = null;
        
        Elements meta = articleDoc.select("html head meta");
        
        if (meta.attr("http-equiv").contains("refresh")) {
            redirectedUrl = officialUrl + meta.attr("content").substring(meta.attr("content").indexOf("=") + 1).replaceAll("'", "");
            return RedirectionHandler(redirectedUrl);
        }
    
        return articleDoc;
    }
}
