package com.pastime.players;

public final class SecurityContext {

    private static final ThreadLocal<PlayerPrincipal> currentPlayer = new ThreadLocal<PlayerPrincipal>();

    public static PlayerPrincipal getPrincipal() {
        return currentPlayer.get();
    }

    public static void setCurrentPlayer(PlayerPrincipal player) {
        currentPlayer.set(player);
    }

    public static boolean signedIn() {
        return currentPlayer.get() != null;
    }

    public static void remove() {
        currentPlayer.remove();
    }

}
