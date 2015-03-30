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
 
public class BBProxy extends Controller {

    public static F.Promise<Result> index(String query) {

        if(StringUtils.isEmpty(query)){

            F.Promise.promise(new F.Function0<Object>() {
                @Override
                public Object apply() throws Throwable {
                    return ok(Json.toJson("Query parameter (q) not provided "));
                }

            });
        }

            F.Promise<WSResponse> wsResponsePromise = WS.url("http://www.bloomberg.com/search").setQueryParameter("query",query).get();

        return wsResponsePromise.map(new F.Function<WSResponse, Result>() {
            @Override
            public Result apply(WSResponse wsResponse) throws Throwable {


                String body = wsResponse.getBody();

                List<Map<String,String>> ret  = new ArrayList<Map<String, String>>();

                try {
                    // Insert into map
                    
                    org.jsoup.nodes.Document doc = Jsoup.parse(body);
                    Elements items = doc.getElementsByClass("search-result");
                    
                    for (Element item : items) {
                        Map<String,String> keyValue = new LinkedHashMap<String, String>();
                        
                        keyValue.put("image", item.getElementsByClass("search-result-story__thumbnail__image").attr("src"));
                        keyValue.put("title", item.getElementsByClass("search-result-story__headline").text());
                        keyValue.put("content", item.getElementsByClass("search-result-story__body").text().substring(0, item.getElementsByClass("search-result-story__body").text().indexOf(" (Source: Bloomberg")));
                        keyValue.put("date", item.getElementsByClass("published-at").text());
                        keyValue.put("url", "www.bloomberg.com/" + item.getElementsByClass("search-result-story__thumbnail__link").attr("href"));
                        
                        ret.add(keyValue);
                    }


                } catch (DOMException e) {
                    e.printStackTrace();
                }

                return ok(Json.toJson(ret));

            }
        });


    }

}
