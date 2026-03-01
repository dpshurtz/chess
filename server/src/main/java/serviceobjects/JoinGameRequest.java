package serviceobjects;

public record JoinGameRequest(String playerColor, String gameID, String authToken) {
}
