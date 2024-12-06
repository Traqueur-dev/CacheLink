package fr.traqueur.cachelink.updating;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class UpdateMessageTypeAdapter extends TypeAdapter<UpdateMessage> {
    @Override
    public void write(JsonWriter jsonWriter, UpdateMessage updateMessage) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("cacheName").value(updateMessage.cacheName());
        jsonWriter.name("cacheId").value(updateMessage.cacheId());
        jsonWriter.name("key").value(updateMessage.key());
        jsonWriter.name("value").value(updateMessage.value());
        jsonWriter.name("operation").value(updateMessage.operation().name());
        jsonWriter.endObject();
    }

    @Override
    public UpdateMessage read(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        String cacheName = null;
        String cacheId = null;
        String key = null;
        String value = null;
        Operation operation = null;
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            switch (name) {
                case "cacheName":
                    cacheName = jsonReader.nextString();
                    break;
                case "cacheId":
                    cacheId = jsonReader.nextString();
                    break;
                case "key":
                    key = jsonReader.nextString();
                    break;
                case "value":
                    value = jsonReader.nextString();
                    break;
                case "operation":
                    operation = Operation.valueOf(jsonReader.nextString());
                    break;
            }
        }
        jsonReader.endObject();
        return new UpdateMessage(cacheId,cacheName, operation,key, value);
    }
}
