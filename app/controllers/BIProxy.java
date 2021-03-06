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
 * Created by kostandin on 29/03/15.
 */
 
public class BIProxy extends Controller {

    public static F.Promise<Result> index(String query) {

        if(StringUtils.isEmpty(query)){

            F.Promise.promise(new F.Function0<Object>() {
                @Override
                public Object apply() throws Throwable {
                    return ok(Json.toJson("Query parameter (q) not provided "));
                }

            });
        }

        F.Promise<WSResponse> wsResponsePromise = WS.url("http://www.businessinsider.com/s").setQueryParameter("q",query).get();

        return wsResponsePromise.map(new F.Function<WSResponse, Result>() {
            @Override
            public Result apply(WSResponse wsResponse) throws Throwable {

                String body = wsResponse.getBody();
                List<Map<String,String>> results  = new ArrayList<Map<String, String>>();

                try {
                    // Insert into map
                    org.jsoup.nodes.Document doc = Jsoup.parse(body);
                    Elements items = doc.select("div.search-result");
                    
                    // Iterate through results
                    for (Element item : items) {
                        Map<String,String> keyValue = new LinkedHashMap<String, String>();
                        
                        keyValue.put("image", item.select("img").attr("src"));
                        keyValue.put("title", item.select("h3").text());
                        keyValue.put("content", item.select("div.excerpt").first().text());
                        keyValue.put("date", item.select("li.date").text());
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
