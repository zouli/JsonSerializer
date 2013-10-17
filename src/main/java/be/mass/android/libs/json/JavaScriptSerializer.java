package be.mass.android.libs.json;

public class JavaScriptSerializer {
    public String serialize(Object obj) {
        StringBuilder b = new StringBuilder();
        serialize(obj, b);
        return b.toString();
    }

    public void serialize(Object obj, StringBuilder output) {
        Json.serialize(obj, this, output);
    }

    public Object deserialize(String input, Class<?> clazz) {
        if (input.matches("^\\[(.*)\\]$")) {
            return Json.deserializeArray(input, this, clazz);
        } else {
            return Json.deserialize(input, this, clazz);
        }
    }
}
