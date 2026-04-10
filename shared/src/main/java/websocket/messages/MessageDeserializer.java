package websocket.messages;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class MessageDeserializer implements JsonDeserializer<ServerMessage> {

    @Override
    public ServerMessage deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String serverMessageType = jsonElement.getAsJsonObject().get("serverMessageType").getAsString();

        return switch (serverMessageType) {
            case "LOAD_GAME" ->
                    jsonDeserializationContext.deserialize(jsonElement, LoadGameMessage.class);
            case "ERROR" ->
                    jsonDeserializationContext.deserialize(jsonElement, ErrorMessage.class);
            case "NOTIFICATION" ->
                    jsonDeserializationContext.deserialize(jsonElement, NotificationMessage.class);
            default ->
                    jsonDeserializationContext.deserialize(jsonElement, ServerMessage.class);
        };
    }
}
