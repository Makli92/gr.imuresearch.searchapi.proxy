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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * If no license is here then you can whatever you like!
 * and of course I am not liable
 *
 * Created by fotis on 30/06/14.
 */
public class BPProxy extends Controller {

    public static F.Promise<Result> index(String query) {

        if(StringUtils.isEmpty(query)){

            F.Promise.promise(new F.Function0<Object>() {
                @Override
                public Object apply() throws Throwable {
                    return ok(Json.toJson("Query parameter  (q) not provided "));
                }

            });
        }

            F.Promise<WSResponse> wsResponsePromise = WS.url("http://www.brainpickings.org").setQueryParameter("s",query).get();

        return wsResponsePromise.map(new F.Function<WSResponse, Result>() {
            @Override
            public Result apply(WSResponse wsResponse) throws Throwable {


                String body = wsResponse.getBody();

                List<Map<String,String>> ret  = new ArrayList<Map<String, String>>();

                try {


                    org.jsoup.nodes.Document parse = Jsoup.parse(body);
                    Elements itemContent = parse.select(".post");
                    for (Element next : itemContent) {
                        Map<String,String> keyValue = new LinkedHashMap<String, String>();
                        Elements a = next.select(".newsImage").select("a");
                        keyValue.put("image", "");
                        Elements link = next.select(".holder").select("h2").select("a");
                        keyValue.put("title", link.html());
                        keyValue.put("content",next.select(".holder").select("p").html());
                        keyValue.put("date",next.select(".date").html());
                        keyValue.put("url", link.attr("href"));
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
