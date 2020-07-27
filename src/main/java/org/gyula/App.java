package org.gyula;

import felhivasok.FelhivasLekerdezes;
import felhivasok.FelhivasParser;
import felhivasok.RSSParser;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import palyazatkezelo.MongoAccess;

import java.io.IOException;


public class App extends Application {
    FelhivasParser felhivasParser = new FelhivasParser();
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Pályázatkezelő program");
        scene = new Scene(loadFXML("kezdooldal"));
        stage.setScene(scene);
        stage.getIcons().add(new Image("/org/gyula/images/egyetemlogo.png"));
        stage.setResizable(false);
        stage.show();
        kezdoFigyelmezetetes();
        Runnable hatterben = () -> {
            Platform.runLater(() -> {
                try {
                    felhivasParser.felhivasKeszito(new RSSParser().rssListaKeszito());
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }
            });
        };
        new Thread(hatterben).start();

        try {
            MongoAccess.getConnection();
        } catch (Exception e) {
            System.out.println("Problema a kapcsolattal");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Hiba!");
            alert.setHeaderText("Sajnos valami probléma van az internet kapcsolattal");
            alert.setContentText("Az alkalmazás most leáll, kérem, próbálja meg később!");
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(MongoAccess.class.getResource("/org/gyula/dialogCSS.css").toExternalForm());
            dialogPane.getStyleClass().add("/org/gyula/dialogCSS.css");
            alert.showAndWait();
            System.exit(0);
        }
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    public static Parent loadFXML(String fxml) throws IOException {
        InternetEllenorzo.ellenoriz();
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

    public static void exit() {
        Runnable kilepes = () -> Platform.runLater(FelhivasLekerdezes::automatikusTorles);
        new Thread(kilepes).start();
    }

    public void kezdoFigyelmezetetes() throws IOException {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Türelmet kérek!");
        alert.setHeight(320);
        alert.setHeaderText("Az alkalmazás ellenőrzi, hogy vannak-e új felhívások az interneten");
        alert.setContentText("Ez egy kis időbe telik, kérem a türelmét. \nHa a frissítés lezárult, megjelenik az OK gomb");
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getButtonTypes().remove(0); //ez tunteti el az OK gombot
        dialogPane.getStylesheets().add(MongoAccess.class.getResource("/org/gyula/dialogCSS.css").toExternalForm());
        dialogPane.getStyleClass().add("/org/gyula/dialogCSS.css");
        alert.show();
        PauseTransition delay = new PauseTransition(Duration.seconds(2)); //1 masodperc a kesleletetes
        delay.setOnFinished( event -> dialogPane.getButtonTypes().add(0, ButtonType.OK));
        delay.play();
    }

}