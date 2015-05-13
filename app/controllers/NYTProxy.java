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
import java.util.regex.*;

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
 
public class NYTProxy extends Controller {

    public static F.Promise<Result> index(String query) {

        if(StringUtils.isEmpty(query)){

            F.Promise.promise(new F.Function0<Object>() {
                @Override
                public Object apply() throws Throwable {
                    return ok(Json.toJson("Query parameter (q) not provided "));
                }

            });
        }
        
        String basicUrl = "http://query.nytimes.com/svc/add/v1/sitesearch.json";

        // Additional query parameters
        String spotlight = "true";
        String facet = "true";
        
        F.Promise<WSResponse> wsResponsePromise = WS.url(basicUrl).setQueryParameter("q", query)
                                                                    .setQueryParameter("spotlight", spotlight)
                                                                    .setQueryParameter("facet", facet).get();

        return wsResponsePromise.map(new F.Function<WSResponse, Result>() {
            @Override
            public Result apply(WSResponse wsResponse) throws Throwable {

                String body = wsResponse.getBody();
                List<Map<String,String>> results  = new ArrayList<Map<String, String>>();

                try {
                    // Reach json code into html response from ajax call
                    org.jsoup.nodes.Document doc = Jsoup.parse(body);
                    String resultJSONbody = doc.select("body").first().text();
                    
                    // Parse the json code
                    JSONObject resultJSONobj = new JSONObject(resultJSONbody);
                    resultJSONobj = (JSONObject)resultJSONobj.get("response");
                    
                    // Reach array of results and set to JSONArray
                    JSONArray resultJSONarray = new JSONArray(resultJSONobj.get("docs").toString());
                    
                    // Insert each result's elements into map with corresponding key
                    for (int i = 0; i < resultJSONarray.length(); i++) {
                        // Set internal map
                        Map<String,String> keyValue = new LinkedHashMap<String, String>();
                        
                        // Set basic image url where image exists
                        String basicImgUrl = "http://static01.nyt.com/";
                        resultJSONobj = (JSONObject)resultJSONarray.get(i);
                        
                        // Check if article contains "multimedia" key (image), else it shall be empty list
                        if (resultJSONobj.getJSONArray("multimedia").length() != 0) {
                            // Iterate through multimedia list of values and get the thumbnail image's url
                            for (int j = 0; j < resultJSONobj.getJSONArray("multimedia").length(); j++) {
                                if (resultJSONobj.getJSONArray("multimedia").getJSONObject(j).getString("subtype").equals("thumbnail")) {
                                    // Prepend the basic image url
                                    keyValue.put("image", basicImgUrl + resultJSONobj.getJSONArray("multimedia").getJSONObject(j).getString("url"));
                                    break;
                                }
                            }
                            
                        }
                        
                        keyValue.put("title", resultJSONobj.getJSONObject("headline").getString("main"));
                        keyValue.put("content", resultJSONobj.getString("snippet").replace("</strong>", ""));
                        
                        // Format date
                        String date = resultJSONobj.getString("pub_date").substring(0, resultJSONobj.getString("pub_date").length() - 1);
                        Pattern pattern = Pattern.compile("[A-Z]");
                        Matcher matcher = pattern.matcher(date);
                        
                        if (matcher.find()) {
                            date = date.substring(0, date.indexOf(matcher.group(0))) + " " + matcher.group(0) + " " + date.substring(date.indexOf(matcher.group(0)) + 1, date.length());
                        }
                        
                        keyValue.put("date", date);
                        keyValue.put("url", resultJSONobj.getString("web_url"));
                        
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
