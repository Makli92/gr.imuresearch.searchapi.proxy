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
import java.util.*;
import java.lang.StringBuilder;

/**
 * If no license is here then you can whatever you like!
 * and of course I am not liable
 *
 * Created by kostandin on 21/04/15.
 */
 
public class CNBCProxy extends Controller {

    public static F.Promise<Result> index(String query) {

        if(StringUtils.isEmpty(query)){

            F.Promise.promise(new F.Function0<Object>() {
                @Override
                public Object apply() throws Throwable {
                    return ok(Json.toJson("Query parameter (q) not provided "));
                }

            });
        }

        String target = "all";
        String categories = "exclude";
        String partnerId = "2000";
                                                                                                // ?target=all&categories=exclude&partnerId=2000&keywords=apple
        F.Promise<WSResponse> wsResponsePromise = WS.url("http://search.cnbc.com/main.do").setQueryParameter("target", target)
                                                                                          .setQueryParameter("categories", categories)
                                                                                          .setQueryParameter("partnerId", partnerId)
                                                                                          .setQueryParameter("keywords", query).get();

        return wsResponsePromise.map(new F.Function<WSResponse, Result>() {
            @Override
            public Result apply(WSResponse wsResponse) throws Throwable {

                String body = wsResponse.getBody();

                List<Map<String,String>> results  = new ArrayList<Map<String, String>>();

                try {
                    // Parse html document
                    org.jsoup.nodes.Document doc = Jsoup.parse(body);
                    Elements items = doc.select("div:not(.clr).padL.padR");     // Choose elements that contain classes "padL" and "padR", but not "clr"
                    
                    // Iterate through results
                    for (Element item : items) {
                        
                        Map<String,String> keyValue = new LinkedHashMap<String, String>();
                        
                        // Add the keys and values
                        keyValue.put("title", item.select("a").text());
                        keyValue.put("content", item.select("span.cnbc_bio_content").text());
                        keyValue.put("date", CalculateDateFormat(Long.parseLong(item.getElementsByTag("script").html().replaceAll("[^0-9]", ""), 10)));     // Edit the date format
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
    
    private static String CalculateDateFormat(long millis) {
        // Set helping arrays
        String[] monthArr = new String[12];
        String[] dayArr = new String[7];
        
        // Set month names
        monthArr[0] = "January";
        monthArr[1] = "February";
        monthArr[2] = "March";
        monthArr[3] = "April";
        monthArr[4] = "May";
        monthArr[5] = "June";
        monthArr[6] = "July";
        monthArr[7] = "August";
        monthArr[8] = "September";
        monthArr[9] = "October";
        monthArr[10] = "November";
        monthArr[11] = "December";
        
        // Set day names
        dayArr[0] = "Sunday";
        dayArr[1] = "Monday";
        dayArr[2] = "Tuesday";
        dayArr[3] = "Wednesday";
        dayArr[4] = "Thursday";
        dayArr[5] = "Friday";
        dayArr[6] = "Saturday";
        
        /** Create Date object with random date, in order to calculate date of article like 
            the "setTime()" function in Javascript (date since January 1, 1970 00:00:00 gmt)
        **/
        
        Date dateObj = new Date(92, 1, 10);
        
        // Add long number to get article's date
        dateObj.setTime(millis);
        
        StringBuilder date = new StringBuilder();
        date.append(dayArr[dateObj.getDay()]).append(", ").append(Integer.toString(dateObj.getDate())).append(" ")
            .append(monthArr[dateObj.getMonth()]).append(" ").append(Integer.toString(dateObj.getYear() + 1900)).append(" ");        // 1900 added to year(see getYear() method's documentation)

        String hours;
        String midnight;
        
        if (dateObj.getHours() > 12) {
            if (dateObj.getHours() - 12 < 10) {
                hours = "0" + Integer.toString(dateObj.getHours() - 12);
            } else {
                hours = Integer.toString(dateObj.getHours() - 12); 
            }
            midnight = "PM";
        } else {
            if (dateObj.getHours() < 10) {
                hours = "0" + Integer.toString(dateObj.getHours());
            } else {
                hours = Integer.toString(dateObj.getHours()); 
            }
            midnight = "AM";
        }
        
        date.append(hours).append(":").append(Integer.toString(dateObj.getMinutes())).append(" " + midnight + " ET");
        
        return date.toString();
    }
}
