package serviceobjects;

import model.GameData;

import java.util.Collection;

public record ListGamesResult(Collection<ListGameData> games) {
}
