package com.pwc.commsgaze.customview;

public class ViewUtil {


   static int parseInt(String floatString){
        if(floatString!= null && !floatString.isEmpty()){
            floatString = floatString.replaceAll("[^\\d.]", "");
            return !floatString.isEmpty()?(int)(Float.parseFloat(floatString)):0;}
        return 0;
    }
}
