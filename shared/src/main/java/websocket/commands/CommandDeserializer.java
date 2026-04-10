package websocket.commands;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class CommandDeserializer implements JsonDeserializer<UserGameCommand> {

    @Override
    public UserGameCommand deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
        String commandType = jsonElement.getAsJsonObject().get("commandType").getAsString();

        return switch (commandType) {
            case "CONNECT", "LEAVE", "RESIGN" ->
                    jsonDeserializationContext.deserialize(jsonElement, StandardGameCommand.class);
            case "MAKE_MOVE" ->
                    jsonDeserializationContext.deserialize(jsonElement, MakeMoveCommand.class);
            case "GET_VALID_MOVES" ->
                    jsonDeserializationContext.deserialize(jsonElement, GetValidMovesCommand.class);
            default ->
                    jsonDeserializationContext.deserialize(jsonElement, UserGameCommand.class);
        };
    }
}
