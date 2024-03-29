package org.gyula.controller.felhivasController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.gyula.palyazatok.PalyazatiTemak;

import java.io.IOException;

public class felhivasPalyazatiTemaListaController {
    PalyazatiTemak palyazatiTemak = new PalyazatiTemak();

    @FXML
    private ListView<String> kategoriaLista;

    @FXML
    private Button kilepesGomb;

    @FXML
    private Button felhivasValaszto;

    public felhivasPalyazatiTemaListaController() throws InterruptedException {
    }

    @FXML
    public void initialize() {
        kategoriaLista.getItems().setAll(palyazatiTemak.temaLetolt());
        felhivasValaszto.setDisable(true);
        kategoriaLista.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    @FXML
    private void nincsValasztas() {
        boolean disableButtons = kategoriaLista.getSelectionModel().isEmpty();
        felhivasValaszto.setDisable(disableButtons);
    }

    @FXML
    private void kilep() {
        Stage ablak = (Stage) kilepesGomb.getScene().getWindow();
        ablak.close();
    }

    @FXML
    private void kategoriaValaszto() throws IOException {
        String kivalasztottKategoria = kategoriaLista.getSelectionModel().getSelectedItem();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/org/gyula/felhivasFXML/felhivasOktatoPalyazatiTema.fxml"));
        Parent kategoriaValasztoParent = loader.load();
        Scene kategoriaValasztoScene = new Scene(kategoriaValasztoParent);
        felhivasOktatoPalyazatiTemaController controller = loader.getController();
        controller.adatKategoria(kivalasztottKategoria);
//        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        Stage stage = new Stage();
        stage.setTitle("A kategóriába tarozó felhívások - " + kivalasztottKategoria );
        stage.setScene(kategoriaValasztoScene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.getIcons().add(new Image("/org/gyula/images/egyetemlogo.png"));
        stage.show();
    }

}
