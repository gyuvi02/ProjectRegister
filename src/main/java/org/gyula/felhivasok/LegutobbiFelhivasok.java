package org.gyula.felhivasok;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.gyula.palyazatkezelo.MongoAccess;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class LegutobbiFelhivasok {
    private ArrayList<String> legutobbi;
    private LocalDateTime most;

    public LegutobbiFelhivasok(ArrayList<String> legutobbi) throws InterruptedException {
        this.legutobbi = legutobbi;
        this.most = LocalDateTime.now();
    }

    MongoDatabase palyazatDB = MongoAccess.getConnection().getDatabase("PalyazatDB");
    MongoCollection<LegutobbiFelhivasok> legutobbikColl = palyazatDB.getCollection("LegutobbiFelhivasok", LegutobbiFelhivasok.class);

    public LegutobbiFelhivasok() throws InterruptedException {
    }

    public LegutobbiFelhivasok legutobbiTeljes() {
        return legutobbikColl.find().sort(new Document("_id", -1)).first();
    }

    public ArrayList<String> legutobbiLekerdezes() {
        return legutobbikColl.find().sort(new Document("_id", -1)).map(LegutobbiFelhivasok::getLegutobbi).first();
    }

    public void legutobbiListaFeltoltes() {
        legutobbikColl.insertOne(this);
    }

    public ArrayList<String> getLegutobbi() {
        return legutobbi;
    }

    public void setLegutobbi(ArrayList<String> legutobbi) {
        this.legutobbi = legutobbi;
    }

    public LocalDateTime getMost() {
        return most;
    }

    public void setMost(LocalDateTime most) {
        this.most = most;
    }
}


