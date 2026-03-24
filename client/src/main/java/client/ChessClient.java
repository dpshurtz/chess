package client;

import ui.UIOption;

import java.util.ArrayList;
import java.util.Scanner;

public class ChessClient {

    ServerFacade server;

    boolean isActive = true;
    boolean isLoggedIn = false;

    ArrayList<UIOption> preLoginOptions;
    ArrayList<UIOption> postLoginOptions;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

    }

    private void generatePreLoginOptions() {
        preLoginOptions = new ArrayList<>();
        preLoginOptions.add(new UIOption("login", "description", this::login));
        preLoginOptions.add(new UIOption("register", "description", this::register));
        preLoginOptions.add(new UIOption("quit", "description", this::quit));
        preLoginOptions.add(new UIOption("help", "description", this::helpPreLogin));
    }

    private void generatePostLoginOptions() {
        postLoginOptions = new ArrayList<>();
        postLoginOptions.add(new UIOption("play game", "description", this::playGame));
        postLoginOptions.add(new UIOption("create game", "description", this::createGame));
        postLoginOptions.add(new UIOption("list games", "description", this::listGames));
        postLoginOptions.add(new UIOption("observe game", "description", this::observeGame));
        postLoginOptions.add(new UIOption("logout", "description", this::logout));
        postLoginOptions.add(new UIOption("help", "description", this::helpPostLogin));
    }

    private void login(){

    }

    private void register(){

    }

    private void quit(){

    }

    private void helpPreLogin(){

    }

    private void playGame(){

    }

    private void createGame(){

    }

    private void listGames(){

    }

    private void observeGame(){

    }

    private void logout(){

    }

    private void helpPostLogin(){

    }

}
