package com.pastime.players;

public final class SecurityContext {

    private static final ThreadLocal<Player> currentPlayer = new ThreadLocal<Player>();

    public static Player getCurrentPlayer() {
        Player player = currentPlayer.get();
        if (player == null) {
            throw new IllegalStateException("No player is signed in");
        }
        return player;
    }

    public static void setCurrentPlayer(Player player) {
        currentPlayer.set(player);
    }

    public static boolean playerSignedIn() {
        return currentPlayer.get() != null;
    }

    public static void remove() {
        currentPlayer.remove();
    }

}
