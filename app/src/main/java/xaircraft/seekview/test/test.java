package xaircraft.seekview.test;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by chenyulong on 2017/7/18.
 */

public class test {
    public static void main(String []args){
        String str = "1;2;3;4;5;6;7;8;9;";
        List<String> ls = new ArrayList<>();
        for (String s : str.split(";")) {
            if(!s.contains("000"))
                ls.add(s);
        }
        String s = "";
        if (s==""){
            System.out.printf("string is \"\"");

        }else if (s.equals("")){
            System.out.printf("string is null");

        }


//        System.out.printf(ls.toString());

    }

}
