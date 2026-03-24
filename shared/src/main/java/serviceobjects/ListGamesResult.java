package serviceobjects;

import java.util.Collection;

public record ListGamesResult(Collection<ListGameData> games) {
}
