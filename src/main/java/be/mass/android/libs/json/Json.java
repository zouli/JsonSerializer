package be.mass.android.libs.json;

final class Json {
    public static void serialize(Object obj, JavaScriptSerializer jss, StringBuilder output) {
        JsonSerializer js = new JsonSerializer();
        output.append(js.serialize(obj));
        js = null;
    }

//    public static void Serialize(object obj, JavaScriptSerializer jss, TextWriter output) {
//        JsonSerializer js = new JsonSerializer(jss);
//        js.Serialize(obj, output);
//        js = null;
//    }

    //    public static object Deserialize(string input, JavaScriptSerializer jss) {
//        if (jss == null)
//            throw new ArgumentNullException("jss");
//
//        return Deserialize(new StringReader(input), jss);
//    }
//
    public static <T> T[] deserializeArray(String input, JavaScriptSerializer jss, Class<T> clazz) {
        if (jss == null)
            throw new NullPointerException("jss");

        JsonDeserializer ser = new JsonDeserializer();
        return ser.parseArray(input, clazz);
    }

    public static <T> T deserialize(String input, JavaScriptSerializer jss, Class<T> clazz) {
        if (jss == null)
            throw new NullPointerException("jss");

        JsonDeserializer ser = new JsonDeserializer();
        return ser.parseObject(input, clazz);
    }
}
