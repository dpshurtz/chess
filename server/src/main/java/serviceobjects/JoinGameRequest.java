package serviceobjects;

public record JoinGameRequest(String playerColor, int gameID, String authToken) {
}
