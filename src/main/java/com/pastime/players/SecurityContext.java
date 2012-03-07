package com.pastime.players;

public final class SecurityContext {

    private static final ThreadLocal<Player> currentPlayer = new ThreadLocal<Player>();

    public static Player getCurrentPlayer() {
        return currentPlayer.get();
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
