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

    public <T> T deserialize(String input, Class<T> clazz) {
        if (input.matches("^\\[(.*)\\]$")) {
            return (T) Json.deserializeArray(input, this, clazz.getComponentType());
        } else {
            return Json.deserialize(input, this, clazz);
        }
    }
}
