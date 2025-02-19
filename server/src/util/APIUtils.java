package util;

import javax.servlet.http.HttpServletResponse;

public class APIUtils {

    public static void initCosmosDB() {
        CosmosUtil.init();
    }

    public static void setResponseHeader(HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
    }

}
