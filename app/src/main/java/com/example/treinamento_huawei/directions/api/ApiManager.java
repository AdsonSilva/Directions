package com.example.treinamento_huawei.directions.api;

public class ApiManager {

    private static final String host = "http://192.168.130.171:5000/";

    private static final String postUserResource = "users/";

    private static final String cardapio = "dailyMenu/";

    private static final String weekday = "weekday/";

    private static final String avaliation = "avaliation/";

    private static final String comments = "comments/";

    private static final String comment = "comment/";

    private static final String hascomment = "commentByUser/";

    private static final String hasreported = "reportedByUser/";

    private static final String report = "report/";

    private static final String remove = "remove/";

    private static final String suggestion = "suggestion/";

    private static final String edit = "edit/";

    private static final String firebase = "firebase/";

    private static final String localization = "localization/";

    private static ApiManager instance;


    private ApiManager() {

    }

    public static ApiManager getInstance() {

        if (instance == null) {
            instance = new ApiManager();
        }

        return instance;
    }

    public String postUser() {

        return host +
                postUserResource;
    }

    public String postComment() {

        return host + cardapio +
                comment;
    }

    public String putDailyMenu() {

        return host +
                cardapio;
    }


    public String getDailyMenu() {

        return host +
                cardapio;
    }

    public String getWeekday() {

        return host +
                weekday;
    }

    public String postAvaliation() {

        return host +
                avaliation;
    }

    public String getAvaliation() {

        return host +
                avaliation;
    }

    public String getCardapiosSemana(String menu) {

        return host +
                cardapio +
                menu;
    }

    public String getComments(String day, int page) {
        return host +
                cardapio + day + "/" +
                comments + String.valueOf(page);
    }

    public String getCommentByUser(String id) {
        return host +
                hascomment + id;
    }

    public String postReport() {
        return host +
                cardapio +
                report;
    }

    public String deleteComment() {
        return host +
                cardapio +
                comment +
                remove;
    }

    public String postSuggestion() {
        return host +
                suggestion;
    }

    public String postEditComment() {
        return host +
                cardapio +
                comment +
                edit;
    }

    public String getReportedByUser(String id) {
        return host +
                hasreported + id;
    }

    public String postUserIdFirebase() {
        return host +
                postUserResource + firebase;
    }

    public String sendLocalization(String lat, String log) {
        return host +
                localization + lat + "/" + log;
    }

}
