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
 * Created by fotis on 30/06/14.
 */
public class ANProxy extends Controller {

    public static F.Promise<Result> index(String query) {

        if(StringUtils.isEmpty(query)){

            F.Promise.promise(new F.Function0<Object>() {
                @Override
                public Object apply() throws Throwable {
                    return ok(Json.toJson("Query parameter  (q) not provided "));
                }

            });
        }

            F.Promise<WSResponse> wsResponsePromise = WS.url("http://europe.autonews.com/apps/pbcs.dll/search?Category=SEARCH")
                    .post("crit="+ UriEncoding.encodePathSegment(query, "UTF-8")+"&SearchCategory=%25&ExclCat=DATACENTER%3BOEM%3BOEM01%3BOEM02%3BOEM03%3BOEM04%3BOEM05%3BOEM06%3BOEM07%3BOEM08%3BOEM09%3BOEM10%3BBLOG06%3BRETAIL01%3BRETAIL02%3BRETAIL03%3BRETAIL04%3BRETAIL05%3BRETAIL06%3BRETAIL07%3BRETAIL08%3BRETAIL09%3BGLOBAL01%3BGLOBAL02%3BGLOBAL03%3BCOPY01%3BCUTAWAY%3BANA%3BCHEVY100%3BELECTRIC100%3BFINANCE_AND_INSURANCE%3BLEGALFILE%3BLEADING_WOMEN01%3BLEADING_WOMEN%3BVIDEO%3BEY%3BFEATURES%3BBP%3BBP03%3BHOLD%3BCOPY01%3BCOPY02%3BZZZ_SPECIAL%3BTEASE%3BWEBINAR%3BANEVENTS%3BANEVENTSOUT%3BANEPOLL%3BEVENTS%3BDESKTOP%3BNEWSLETTER%3BNEWSLETTERANE%3BNEWSLETTERLITE%3BNEWSLETTERANELITE%3BNEWSLETTERSPECIAL%3BSEO%3BANECLASS%3BTEASERS%3BSAP%3BPRESS_RELEASES%3BGLOBAL_MONTHLY");


        return wsResponsePromise.map(new F.Function<WSResponse, Result>() {
            @Override
            public Result apply(WSResponse wsResponse) throws Throwable {


                String body = wsResponse.getBody();

                List<Map<String,String>> ret  = new ArrayList<Map<String, String>>();

                try {


                    org.jsoup.nodes.Document parse = Jsoup.parse(body);
                    Elements itemContent = parse.select("#left_rail").select("> table").select("> tbody").select("tr:eq(1)").select("td").select("table");



                    for (Element next : itemContent) {

                        Map<String,String> keyValue = new LinkedHashMap<String, String>();

                        Element td = next.select("tbody").select("tr").select("td").first();

                        if(td.hasClass("searchhits")) continue;

                        Elements a = td.select("a");
                        keyValue.put("image", "");
                        keyValue.put("title", a.select("h3").html());
                        keyValue.put("content",td.select("p").first().html());
                        keyValue.put("date", td.select("div").first().select("strong").html());
                        keyValue.put("url", a.attr("href"));
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
