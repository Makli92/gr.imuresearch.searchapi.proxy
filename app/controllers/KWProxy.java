package controllers;

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
 * Created by kostandin on 05/04/15.
 */
 
public class KWProxy extends Controller {

    public static F.Promise<Result> index(String query) {

        if(StringUtils.isEmpty(query)){

            F.Promise.promise(new F.Function0<Object>() {
                @Override
                public Object apply() throws Throwable {
                    return ok(Json.toJson("Query parameter (q) not provided "));
                }

            });
        }

        F.Promise<WSResponse> wsResponsePromise = WS.url("http://knowledge.wharton.upenn.edu/").setQueryParameter("s",query).get();

        return wsResponsePromise.map(new F.Function<WSResponse, Result>() {
            @Override
            public Result apply(WSResponse wsResponse) throws Throwable {


                String body = wsResponse.getBody();

                List<Map<String,String>> results  = new ArrayList<Map<String, String>>();

                try {
                    
                    // Insert into map
                    org.jsoup.nodes.Document doc = Jsoup.parse(body);
                    Elements items = doc.select("div.article.type-article.status-publish");         // All articles belong to this classes
                    
                    for (Element item : items) {
                        Map<String,String> keyValue = new LinkedHashMap<String, String>();
                        
                        // Check if specific article belongs to "has-post-thumbnail" class (therefore it contains an image)
                        if (item.hasClass("has-post-thumbnail")) {
                            // Add image key and value to map
                            keyValue.put("image", item.select("img").attr("src"));
                        }
                        
                        // Add the rest of keys and values
                        /*
                        keyValue.put("title", item.select("h2").select("a").text());
                        keyValue.put("content", item.select("p").first().ownText());
                        keyValue.put("date", item.select("time").text());
                        keyValue.put("url", item.select("h2").select("a").attr("href"));
                        */ 
                        
                        keyValue.put("title", item.select("h2").select("a").text());
                        keyValue.put("content", item.select("div.attribute.categorythumbs").first().text());
                        keyValue.put("date", item.select("ul.datestamp").select("li").first().text());
                        keyValue.put("url", item.select("h2").select("a").attr("href"));
                        
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
