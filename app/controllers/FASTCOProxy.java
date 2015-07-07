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

import java.io.IOException;
import java.lang.System;

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
 * Created by kostandin on 06/07/15.
 *
 */
 
public class FASTCOProxy extends Controller {

    public static F.Promise<Result> index(String query) {

        if(StringUtils.isEmpty(query)){

            F.Promise.promise(new F.Function0<Object>() {
                @Override
                public Object apply() throws Throwable {
                    return ok(Json.toJson("Query parameter (q) not provided "));
                }

            });
        }
        
        String basicUrl = "http://www.fastcodesign.com/api/v1/search";
        
        // Additional query parameters
        String paged = "0";
        int limit = 10;
        
        F.Promise<WSResponse> wsResponsePromise = WS.url(basicUrl).setQueryParameter("q", query)
                                                                    .setQueryParameter("paged", paged)
                                                                    .setQueryParameter("limit", Integer.toString(limit)).get();
                                                                    
        return wsResponsePromise.map(new F.Function<WSResponse, Result>() {
            @Override
            public Result apply(WSResponse wsResponse) throws Throwable {

                String body = wsResponse.getBody();
                List<Map<String,String>> results  = new ArrayList<Map<String, String>>();

                try {
                    
                    JSONObject initialBody = new JSONObject(body);
                    JSONArray resultArray = (JSONArray)initialBody.get("items");
                    
                    for (int i = 0; i < resultArray.length(); i++) {
                        
                        Map<String,String> keyValue = new LinkedHashMap<String, String>();
                        
                        JSONObject element = (JSONObject)resultArray.get(i);
                        
                        // Get title
                        keyValue.put("title", element.get("title").toString());
                        
                        // Get content
                        keyValue.put("content", element.get("deck").toString());
                        
                        // Get date
                        element = (JSONObject)resultArray.get(i);
                        element = (JSONObject)element.get("date");
                        element = (JSONObject)element.get("updated");
                        
                        keyValue.put("date", element.get("long").toString());
                        
                        // Get url
                        element = (JSONObject)resultArray.get(i);
                        
                        keyValue.put("url", "www." + element.get("url").toString().substring(2));
                        
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
