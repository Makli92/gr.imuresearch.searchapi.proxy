package controllers;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;
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

import org.json.JSONObject;
import org.json.JSONArray;

/*
 * If no license is here then you can do whatever you like!
 * and of course I am not liable
 *
 * Created by kostandin on 13/12/14.
 *
 */
 
public class CNNProxy extends Controller {

    public static F.Promise<Result> index(String query) {

        if(StringUtils.isEmpty(query)){

            F.Promise.promise(new F.Function0<Object>() {
                @Override
                public Object apply() throws Throwable {
                    return ok(Json.toJson("Query parameter  (q) not provided "));
                }

            });
        }

        F.Promise<WSResponse> wsResponsePromise = WS.url("http://searchapp.cnn.com/search/query.jsp").setQueryParameter("query", query).get();

        return wsResponsePromise.map(new F.Function<WSResponse, Result>() {
            @Override
            public Result apply(WSResponse wsResponse) throws Throwable {

                String body = wsResponse.getBody();

                List<Map<String,String>> ret  = new ArrayList<Map<String, String>>();

                try {
                    // Reach json code into html response from ajax call
                    org.jsoup.nodes.Document doc = Jsoup.parse(body);
                    Element resultElement = doc.select("textarea#jsCode").first();
                    String resultString = resultElement.text();
                    
                    // Parse the json code
                    JSONObject obj = new JSONObject(resultString);
                    JSONArray array = new JSONArray(obj.get("results").toString());
                    JSONArray internalArray = new JSONArray(array.get(0).toString());
                    
                    // Insert each result's elements into map with corresponding key
                    for (int i = 0; i < internalArray.length(); i++) {
                        JSONObject elementObj = new JSONObject(internalArray.get(i).toString());
                        
                        String image = elementObj.get("thumbnail").toString();
                        String title = elementObj.get("title").toString();
                        String content = elementObj.get("description").toString();
                        String date = elementObj.get("mediaDateUts").toString();
                        String url = elementObj.get("url").toString();
                        
                        Map<String,String> keyValue = new LinkedHashMap<String, String>();
                        
                        keyValue.put("image", image);
                        keyValue.put("title", title);
                        keyValue.put("url", url);
                        keyValue.put("content", content);
                        keyValue.put("date", date);
                        
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
