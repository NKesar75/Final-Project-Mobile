package domain.hackathon.personal_assistant;

/**
 * Created by darkness7245 on 4/24/2018.
 */

public class searchresults {
    private String mtitle, msnippet, murl;
    public searchresults(){}
    public searchresults(String title, String snippet, String url) {
        mtitle = title;
        msnippet = snippet;
        murl = url;
    }
    public String getMtitle(){
        return mtitle;
    }
    public String getMsnippet() {
        return mtitle;
    }
    public String getMurl() {
        return murl;
    }
}