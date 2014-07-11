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
 * <p/>
 * Created by fotis on 04/07/14.
 */
public class HBRProxy extends Controller {

    public static F.Promise<Result> index(String query) {

        if(StringUtils.isEmpty(query)){

            F.Promise.promise(new F.Function0<Object>() {
                @Override
                public Object apply() throws Throwable {
                    return ok(Json.toJson("Query parameter  (q) not provided "));
                }

            });
        }

        F.Promise<WSResponse> wsResponsePromise = WS.url("http://hbr.org/search/"+ UriEncoding.encodePathSegment(query, "UTF-8")+"/0").get();

        return wsResponsePromise.map(new F.Function<WSResponse, Result>() {
            @Override
            public Result apply(WSResponse wsResponse) throws Throwable {


                String body = wsResponse.getBody();

                List<Map<String,String>> ret  = new ArrayList<Map<String, String>>();

                try {


                    org.jsoup.nodes.Document parse = Jsoup.parse(body);
                    Elements itemContent = parse.select(".search-results").select("ul").select(">li");

                    for (Element next : itemContent) {
                        Map<String,String> keyValue = new LinkedHashMap<String, String>();


                        keyValue.put("title",next.select("h2").select("a").html());
                        keyValue.put("content",next.select(".description").html());
                        keyValue.put("date",next.select(".authors").html());
                        keyValue.put("url", next.select("h2").select("a").attr("href"));

                        ret.add(keyValue);
                    }


                } catch (DOMException e) {
                    e.printStackTrace();
                }

//                return ok(body).as("text/html");
                return ok(Json.toJson(ret));

            }
        });


    }
}
