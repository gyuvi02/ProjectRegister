package org.gyula.controller.palyazatController;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.gyula.App;
import org.gyula.palyazatok.Palyazat;

public class ujPalyazatController {

    @FXML
    private Button hozzaadGomb;

    @FXML
    private Button kilepesGomb;

    @FXML
    private TextField palyazatCim;

    @FXML
    private TextField szakmaiVezeto;

    @FXML
    private ComboBox fazisGomb;

    @FXML
    public void initialize() {
        hozzaadGomb.setDisable(true);
    }

    @FXML
    private void visszaPalyazatOldalra() throws Exception {
        App.setRoot("/org/gyula/palyazatFXML/palyazatFooldal");
    }

    @FXML
    private void uresMezo() {
        boolean disableButtons = palyazatCim.getText().trim().isEmpty() || szakmaiVezeto.getText().trim().isEmpty() ||
                fazisGomb.getSelectionModel().getSelectedIndex() == -1;
        hozzaadGomb.setDisable(disableButtons);
    }

    @FXML
    private void hozzaad(){
        Palyazat ujPalyazat = new Palyazat(palyazatCim.getText(), fazisGomb.getValue().toString(), szakmaiVezeto.getText(),
                                0.0, 0.0, 0.0);
        if (ujPalyazat.PalyazatFeltolto()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Megerősítés");
            alert.setHeaderText("Létrehoztuk a következő pályázatot:");
            alert.setContentText(palyazatCim.getText());
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/org/gyula/dialogCSS.css").toExternalForm());
            dialogPane.getStyleClass().add("/org/gyula/dialogCSS.css");
            alert.showAndWait();
            kilep();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Hibás pályázatcím");
            alert.setHeaderText("Ezzel a címmel már létezik pályázat az adatbázisban");
            alert.setContentText("Az OK gombot megnyomva javíthatja a címet");
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(
                    getClass().getResource("/org/gyula/dialogCSS.css").toExternalForm());
            dialogPane.getStyleClass().add("/org/gyula/dialogCSS.css");
            alert.showAndWait();
        }
    }

    @FXML
    private void kilep() {
        Stage ablak = (Stage) kilepesGomb.getScene().getWindow();
        ablak.close();
    }


}
