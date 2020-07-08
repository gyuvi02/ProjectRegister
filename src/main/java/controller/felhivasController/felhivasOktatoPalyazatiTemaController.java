package controller.felhivasController;

import felhivasok.FelhivasLekerdezes;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;

public class felhivasOktatoPalyazatiTemaController {

    @FXML
    private ListView felhivasLista;

    @FXML
    private Button kilepesGomb;

    @FXML
    private void kilep() {
        Stage ablak = (Stage) kilepesGomb.getScene().getWindow();
        ablak.close();
    }


    public void adatTranszfer(String nev) {
        FelhivasLekerdezes felhivasLekerdezes = new FelhivasLekerdezes();
        felhivasLista.getItems().setAll(felhivasLekerdezes.resztvevokAlapjan(nev));
        felhivasLista.getSelectionModel().select(0);
    }

    @FXML
    private void felhivasValaszto(ActionEvent event) throws IOException {
        String kivalasztottFelhivas = felhivasLista.getSelectionModel().getSelectedItem().toString();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/org/gyula/felhivasFXML/felhivasReszletek.fxml"));
        Parent felhivasValasztoParent = loader.load();
        Scene felhivasValasztoScene = new Scene(felhivasValasztoParent);
        felhivasReszletekController controller = loader.getController();
        controller.adatTranszfer(kivalasztottFelhivas);
//        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        Stage stage = new Stage();
        stage.setTitle("A kiválasztott felhívás részletei");
//        stage.setX(280);//ezzel kezilg allitom nagyjabol kozepre, de kell lenni mas megoldasnak, hogy ne az elozo ablak bal szelehez igazitsa, hanem kozepre, mint a tobbi ablakot
        stage.setScene(felhivasValasztoScene);
        stage.show();



    }
}
