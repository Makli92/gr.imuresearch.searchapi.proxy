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
 * Created by kostandin on 04/07/15.
 *
 */
 
public class TREEProxy extends Controller {

    public static F.Promise<Result> index(String query) {

        if(StringUtils.isEmpty(query)){

            F.Promise.promise(new F.Function0<Object>() {
                @Override
                public Object apply() throws Throwable {
                    return ok(Json.toJson("Query parameter (q) not provided "));
                }

            });
        }
        
        String basicUrl = "https://www.googleapis.com/customsearch/v1element";
        
        // Additional query parameters
        String key = "AIzaSyCVAXiUzRYsML1Pv6RwSG1gunmMikTzQqY";
        String rsz = "filtered_cse";
        final int num = 10;
        String hl = "el";
        String cx = "017401606067716418337:btpggki1yw8";
        
        F.Promise<WSResponse> wsResponsePromise = WS.url(basicUrl).setQueryParameter("key", key)
                                                                    .setQueryParameter("rsz", rsz)
                                                                    .setQueryParameter("num", Integer.toString(num))
                                                                    .setQueryParameter("hl", hl)
                                                                    .setQueryParameter("cx", cx)
                                                                    .setQueryParameter("q", query).get();
                                                                    
        return wsResponsePromise.map(new F.Function<WSResponse, Result>() {
            @Override
            public Result apply(WSResponse wsResponse) throws Throwable {

                String body = wsResponse.getBody();
                List<Map<String,String>> results  = new ArrayList<Map<String, String>>();

                try {
                    
                    JSONObject initialBody = new JSONObject(body);
                    JSONArray resultArray = (JSONArray)initialBody.get("results");
                    
                    for (int i = 0; i < num; i++) {
                        
                        Map<String,String> keyValue = new LinkedHashMap<String, String>();
                        
                        JSONObject innerObj = new JSONObject(resultArray.get(i).toString());
                        JSONObject metaTags = (JSONObject)innerObj.get("richSnippet");
                        JSONObject ogTags = (JSONObject)metaTags.get("metatags");
                        
                        keyValue.put("image", ogTags.get("ogImage").toString());
                        keyValue.put("title", ogTags.get("ogTitle").toString());
                        keyValue.put("content", ogTags.get("ogDescription").toString());
                        keyValue.put("date", innerObj.get("contentNoFormatting").toString().substring(0, innerObj.get("contentNoFormatting").toString().indexOf("..")));
                        keyValue.put("url", ogTags.get("ogUrl").toString());
                        
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
