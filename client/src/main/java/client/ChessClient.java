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
        generatePreLoginOptions();
        generatePostLoginOptions();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        while (isActive) {
            executeMenuOptions(preLoginOptions, scanner);

            while (isLoggedIn) {
                executeMenuOptions(postLoginOptions, scanner);
            }
        }
    }

    private void executeMenuOptions(ArrayList<UIOption> options, Scanner scanner) {
        int optionIndex = getMenuInput(options, scanner);
        options.get(optionIndex).action().run();
    }

    private int getMenuInput(ArrayList<UIOption> options, Scanner scanner) {
        for (int i = 0; i < options.size(); i++) {
            System.out.println(i + " - " + options.get(i).name());
        }
        String line = scanner.nextLine();
        return Character.getNumericValue(line.charAt(0));
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
        isActive = false;
    }

    private void helpPreLogin(){
        for (UIOption options : preLoginOptions) {
            System.out.println(options.name() + " - " + options.description());
        }
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
        for (UIOption options : postLoginOptions) {
            System.out.println(options.name() + " - " + options.description());
        }
    }
}
