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
 * Created by kostandin on 04/07/15.
 */
 
public class WDCDProxy extends Controller {

    public static F.Promise<Result> index(String query) {

        if(StringUtils.isEmpty(query)){

            F.Promise.promise(new F.Function0<Object>() {
                @Override
                public Object apply() throws Throwable {
                    return ok(Json.toJson("Query parameter (q) not provided "));
                }

            });
        }

        F.Promise<WSResponse> wsResponsePromise = WS.url("http://www.whatdesigncando.com/").setQueryParameter("s",query).get();

        return wsResponsePromise.map(new F.Function<WSResponse, Result>() {
            @Override
            public Result apply(WSResponse wsResponse) throws Throwable {

                String body = wsResponse.getBody();
                List<Map<String,String>> results  = new ArrayList<Map<String, String>>();

                try {
                    // Insert into map
                    org.jsoup.nodes.Document doc = Jsoup.parse(body);
                    Elements items = doc.select("div.item");
                    
                    // Iterate through results
                    for (Element item : items) {
                        Map<String,String> keyValue = new LinkedHashMap<String, String>();
                        
                        String imageUrl = item.select("a").attr("style");
                        
                        keyValue.put("image", imageUrl.substring(imageUrl.indexOf("'") + 1, imageUrl.indexOf("'", imageUrl.indexOf("'") + 1)));
                        keyValue.put("title", item.select("h3").text());
                        
                        // Get date and the first sentence as "content" from each article separately (or the "sub-title" tag)
                        org.jsoup.nodes.Document articleDoc = Jsoup.connect(item.select("a").attr("href")).get();
                        
                        String datePublished = articleDoc.select("div#maincontent p.metainfo").text().substring(0, articleDoc.select("div#maincontent p.metainfo").text().indexOf("Published"));
                        String firstSentence;
                        
                        if (articleDoc.select("div#maincontent p.sub-title").text().length() == 0) {
                            firstSentence = articleDoc.select("div#maincontent p:not(.metainfo)").text().substring(0, articleDoc.select("div#maincontent p:not(.metainfo)").text().indexOf(".") + 1);
                            firstSentence = firstSentence + ".";
                        } else {
                            firstSentence = articleDoc.select("div#maincontent p.sub-title").text();
                            firstSentence = firstSentence + "..";
                        }
                        
                        keyValue.put("content", firstSentence);
                        keyValue.put("date", datePublished);
                        keyValue.put("url", item.select("a").attr("href"));
                        
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
