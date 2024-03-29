package org.gyula.palyazatok;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.TextSearchOptions;
import org.gyula.okatok.OktatoLekerdezes;
import org.gyula.palyazatkezelo.MongoAccess;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

import static com.mongodb.client.model.Filters.*;

public class PalyazatLekerdezesek{

   static MongoDatabase palyazatDB;

    static {
        try {
            palyazatDB = MongoAccess.getConnection().getDatabase("PalyazatDB");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static MongoCollection<Palyazat> palyazatokColl = palyazatDB.getCollection("Palyazatok", Palyazat.class);


//levalogatja a megadott fazisban levo palyazatok cimet
    public ArrayList<Palyazat> fazisLekerdezes(String fazis) throws InterruptedException{
        return palyazatokColl.find(eq("aktualisFazis", fazis)).into(new ArrayList<>());
    }

    //minden resztvevo szerepben kulon kereshetunk vele, parameterkent adjuk at, ha barmelyikben mezoben keresunk, akkor "osszes"
    public ArrayList<Palyazat> resztvevoKereso(String pozicio, String nev) {
        if (pozicio.equals("összes")) {
            return palyazatokColl.find(or(eq("resztvevok.kezelo", nev), eq("resztvevok.projektmenedzser", nev),
                    eq("resztvevok.szakmaiVezeto", nev), eq("resztvevok.resztvevoEmberek", nev)))
                    .into(new ArrayList<>());
        }
        return palyazatokColl.find(eq("resztvevok." + pozicio, nev)).into(new ArrayList<>());
    }

    public ArrayList<String> resztvevoKeresoCim(String pozicio, String nev) {
        if (pozicio.equals("összes")) {
            return palyazatokColl.find(or(eq("resztvevok.kezelo", nev), eq("resztvevok.projektmenedzser", nev),
                    eq("resztvevok.szakmaiVezeto", nev), eq("resztvevok.resztvevoEmberek", nev))).map(Palyazat::getPalyazatCim)
                    .into(new ArrayList<>());
        }
        return palyazatokColl.find(eq("resztvevok." + pozicio, nev)).map(Palyazat::getPalyazatCim).into(new ArrayList<>());
    }

    public ArrayList<String> osszesResztvevo(String palyazatCim) throws InterruptedException{//csak a resztvevok, a menedzser es a tobbiek nem
        return palyazatokColl.find(eq("palyazatCim", palyazatCim))
                .map(Palyazat::getResztvevok).into(new ArrayList<>()).get(0).getResztvevoEmberek();
    }

    //viszaadja az osszes palyazatot
    public ArrayList<Palyazat> osszesPalyazat() throws InterruptedException{
        ArrayList<String> palyazatLista = new ArrayList<>();
        return palyazatokColl.find().into(new ArrayList<>());
    }

    //levalogatja a K+F palyazatokat
    public static ArrayList<String> kPlusFPalyazatok() throws InterruptedException {
                return palyazatokColl.find(eq("kplusF", "Igen")).map(Palyazat::getPalyazatCim).into(new ArrayList<>());
    }

    //levalogatja azokat a palyazatokat, ahol nem kellett onero
    public static ArrayList<String> oneroNelkul() throws InterruptedException{
        return palyazatokColl.find(eq("onero", 0.0)).map(Palyazat::getPalyazatCim).into(new ArrayList<>());
    }

    //min es max - a ket datum kozott kezdodott palyazatokat adja vissza
    public static ArrayList<String> kezdoEvPeriodus(LocalDate min, LocalDate max) throws InterruptedException {
        return palyazatokColl.aggregate(Arrays.asList(
                Aggregates.match(Filters.gte("kezdet", min)),
                Aggregates.match(Filters.lte("kezdet", max))))
                .map(Palyazat::getPalyazatCim).into(new ArrayList<>());

    }

    //min es max - a ket datum kozott befejezodott palyazatokat adja vissza
    public static ArrayList<String> vegeEvPeriodus(LocalDate min, LocalDate max) throws InterruptedException {
        return palyazatokColl.aggregate(Arrays.asList(
                Aggregates.match(Filters.gte("veg", min)),
                Aggregates.match(Filters.lte("veg", max))))
                .map(Palyazat::getPalyazatCim).into(new ArrayList<>());

    }

    //3 adatbol lehet valasztani (onero, igenyeltTamogatas, tervezettOsszkoltseg - legordulo menu)
    //a ket megadott osszeg koze eso palyazatokat adja vissza
    public ArrayList<Palyazat> osszegHatarok(String kategoria, double min, double max) throws InterruptedException{
        return palyazatokColl.aggregate(Arrays.asList(
                Aggregates.match(Filters.gte(kategoria, min)),
                Aggregates.match(Filters.lte(kategoria, max))))
                .into(new ArrayList<>());
    }

    //kulcsszavak alapjan keres a palyazat cimeben, a leirasban es a megjegyzesekben
    public static ArrayList<String> kulcsszavakPalyazat(String kulcsszo) throws InterruptedException{
        return palyazatokColl.find(Filters.text(kulcsszo, new TextSearchOptions().language("hu")))
                .map(Palyazat::getPalyazatCim).into(new ArrayList<>());
    }

    //megsem akarom szetvalasztani a kategoriakat, akkor eleg ennyi a resztvevok megkeresesehez
    public static ArrayList<String> oktatoAktivitasCimek(String aktivOktato) throws InterruptedException{
        return palyazatokColl.find(or(eq("resztvevok.kezelo", aktivOktato),
                (eq("resztvevok.projektmenedzser", aktivOktato)),
                (eq("resztvevok.szakmaiVezeto", aktivOktato)),
                (eq("resztvevok.resztvevoEmberek", aktivOktato))))
                .map(Palyazat::getPalyazatCim).into(new ArrayList<>());
    }

    //elso korben az oktatoLekerdezes segitsegevel a tanszek oktatoit, azutan ezen a listan hasznaljuk az oktatoAktivitasCimek metodust
    public ArrayList<String> tanszekiAktivitasCimek(String tanszek) throws InterruptedException{
        HashSet<String> palyazatok = new HashSet<>();
        OktatoLekerdezes oktatoLekerdezes = new OktatoLekerdezes();
        for (String oktato : oktatoLekerdezes.oktatoNevsor(tanszek)) {
            palyazatok.addAll(oktatoAktivitasCimek(oktato));
        }
        ArrayList<String> rendezettPalyazatok = new ArrayList<>(palyazatok);
        return nevRendezo(rendezettPalyazatok);
    }

    //ezzel kerdezzuk le egy palyazat osszes resztvevojet
    public ArrayList<String> resztvevoHash(String palyazat) throws InterruptedException{
        PalyazatiResztvevok palyazatiResztvevok = palyazatokColl.find(eq("palyazatCim", palyazat))
                .map(Palyazat::getResztvevok).first();
        HashSet<String> resztvevok = new HashSet<>();
        resztvevok.add(palyazatiResztvevok.getKezelo());
        resztvevok.add(palyazatiResztvevok.getProjektmenedzser());
        resztvevok.add(palyazatiResztvevok.getSzakmaiVezeto());
        resztvevok.addAll(palyazatiResztvevok.getResztvevoEmberek());
        ArrayList<String> rendezettNevek = new ArrayList<>(resztvevok);
        return nevRendezo(rendezettNevek);
    }

    private ArrayList<String> nevRendezo(ArrayList<String> lista) throws InterruptedException{
        lista.sort(Comparator.comparing(String::trim)); //nev alapjan rendezve kuldi vissza
        return lista;
    }



}

