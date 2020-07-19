package felhivasok;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import okatok.Oktato;
import okatok.OktatoLekerdezes;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import palyazatkezelo.MongoAccess;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class FelhivasParser {

//    MongoDatabase palyazatDB = MongoAccess.getConnection().getDatabase("PalyazatDB");

    int[] elemek = {9, 11, 13, 17, 19}; //ezeken a helyeken talalhatok a nekem fontos informaciok a letrehozott feed elemekben

    public boolean felhivasKeszito(ArrayList<RssElemek> feedLista) throws IOException {
        ArrayList<String> legutobbiFelhivasok = new ArrayList<>();
//         feedLista = new RSSParser().rssListaKeszito();
        int k = 1; //ezzel szamolom meg, hany elemet toltottem le

        if (feedLista == null) { //ha nincs uj RSS, vagy kivetel tortent a letoltes kozben, akkor itt ki is lep
            return false;
        }
        ArrayList<RssElemek> aktualizaltFeedLista = new ArrayList<RssElemek>(feedLista); //egy valoban uj lista kell, hogy ne hasson vissza az egyik torlese a masikra

        try {
            for (RssElemek elem : feedLista) {
                String[] adatok = new String[5]; //5 adatra lesz szuksegem, ezek helyet az oldalon belul tarolom majd ebben a tombben
                Document doc = Jsoup.connect(elem.link).get();
                Elements alapAdatok = doc.select("td");
                for (int i = 0; i < 5; i++) {                   // A tombben megadott helyekrol igy szedem ki a szovegeket
                    adatok[i] = alapAdatok.get(elemek[i]).text();
                }
                //az RSS-ekben ujra megjelennek korabbi felhivasok is, ezeket nem akarjuk ujra letolteni
                //itt futtatom le az ellenorzest a link alapjan, hogy mar van-e ilyen felhivas, ha nem (false), akkor folytatom
                if (linkOsszevetes(elem.getLink(), adatok[0])) {
                    return false;
                }
                System.out.println("Elemek szama: " + k++);
                String date = datumKeszito(adatok[2]);
                System.out.println(elem.getCategory());
                String  reszletesLeiras = reszletesLeiras(doc.select("td").get(22).html());
                ArrayList<String> ellenorizendoKategoriak = new ArrayList<>(elem.getCategory());
                Felhivas keszFelhivas = new Felhivas(adatok[0], adatok[1], adatok[3], adatok[4], date, elem.getLink(),
                        reszletesLeiras, elem.getCategory(), lehetsegesResztvevok(ellenorizendoKategoriak),
                        torlesSzamolo(date));
                //elorebb vittem az ellenorzest, hogy gyorsitsam a folyamatot, nem kell peldanyositani sem, ha mar van ilyen felhivas
                //itt ellenorzom a link alapjan, hogy mar van-e ilyen felhivas, ha nem (true), akkor feltoltom
//                if (linkOsszevetes(keszFelhivas)) {
                keszFelhivas.felhivasFeltolto();
                legutobbiFelhivasok.add(keszFelhivas.getFelhivasCim());
//                }
                aktualizaltFeedLista.remove(0); // kitoroljuk, hogy a catch jol mukodjon szukseg eseten, ha nincs kivetel, erre nem lesz szukseg
            }
            //Ennel a tipusu hibanal a weboldal cime nem jol van megadva, 404-es hibat ad. Mast nem tudok tenni, mint hogy kihagyom, es a
            //kovetkezo elemmel folytatjuk, ehhez toroljuk az aktualis elemet, es ujra futtatjuk a metodust, de ez csak akkor jo,
            // ha a tomb elso eleme a hibas, ezert toroljuk fentebb mindig az aktualis elemet
        } catch (HttpStatusException e) {
            System.out.println("Ugy tunik, az oldal jelenleg nem elerheto, probalja meg kesobb\n" + e.getMessage());
            aktualizaltFeedLista.remove(0);
            felhivasKeszito(aktualizaltFeedLista); //a folyamatosan torolt listaval hivjuk meg ujra, ebben mar csak azok vannak, amelyeket meg nem olvastunk be
        }

        LegutobbiFelhivasok legutobbi = new LegutobbiFelhivasok(legutobbiFelhivasok);
        legutobbi.legutobbiListaFeltoltes();
        return true;
    }

    //ha kozvetlenul adunk meg egy pafi.hu linket, mivel a palyazati kategoriak az RSS-ben vannak, itt nekunk kell megadni
    public boolean felhivasLinkbol(String link, ArrayList<String> palyazatiKategoriak) throws IOException {
        try {
            String[] adatok = new String[5]; //5 adatra lesz szuksegem, ezek helyet az oldalon belul tarolom majd ebben a tombben
            Document doc = Jsoup.connect(link).get();
            Elements alapAdatok = doc.select("td");
            for (int i = 0; i < 5; i++) {                   // A tombben megadott helyekrol igy szedem ki a szovegeket
                adatok[i] = alapAdatok.get(elemek[i]).text();
            }
            if (linkOsszevetes(link, adatok[0])) {
                return false;
            }
            String reszletesLeiras = reszletesLeiras(doc.select("td").get(22).html());
            ArrayList<String> ellenorizendoKategoriak = new ArrayList<>(palyazatiKategoriak);

            String date = datumKeszito(adatok[2]);

            Felhivas keszFelhivas = new Felhivas(adatok[0], adatok[1], adatok[3], adatok[4], date, link,
                    reszletesLeiras, palyazatiKategoriak, lehetsegesResztvevok(ellenorizendoKategoriak),
                    torlesSzamolo(date));

//            if (linkOsszevetes(keszFelhivas)) {
            keszFelhivas.felhivasFeltolto();
            return true;
//            }

        } catch (HttpStatusException | IllegalArgumentException e) {
            System.out.println("Ugy tunik, az oldal jelenleg nem elerheto, probalja meg kesobb\n" + e.getMessage());
            return false;
        }
    }

    private static String leirasParser(String html) {  //ez megoldja, hogy a <br> tageket atalakitsuk \n jelekke, es jol tagolja a szoveget
        if(html==null)
            return html;
        Document document = Jsoup.parse(html);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));
        document.select("br").append("\n");
        document.select("p").prepend("\n\n");
        String s = document.html().replaceAll("\n", "\n");
        return Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
    }

    private String reszletesLeiras(String get22) {
        return leirasParser(get22).trim();   //vissza is kaptam a jol tagolt szoveget
    }

    private String datumKeszito(String get2) {
        Date date;
        String datumStr;
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy"); //eloszor a String adott formatumarol kell atalakitani
        SimpleDateFormat formatter2 = new SimpleDateFormat("YYYY. MMMM dd."); //majd mikor mar datum, akkor ugy formazon, ahogy akarom
        try {
            date = formatter.parse(get2);  //itt vigyazni kell, mert nem mindig datum van
            datumStr = formatter2.format(date);
        } catch (Exception e) {
            datumStr = get2; //ha pl. "folyamatosan" szerepel a szovegben
        }
        return datumStr;
    }
    //itt szamoljuk ki, mikor torolheto a felhivas, es ez bekerul minden dokumentumba
    private LocalDate torlesSzamolo(String datum) {
        return FelhivasLekerdezes.parseDate(datum).plusDays(14);
    }

    private ArrayList<String> lehetsegesResztvevok(ArrayList<String> fixKategoriak) { //itt valogatjuk le, kinek a palyazati temaja egyezik a kategoriakkal
        ArrayList<String> lehetsegesOktatok = new ArrayList<>();
        for (Oktato iterOktato :  OktatoLekerdezes.oktatoTeljesDok("Minden tanszék")) {
            ArrayList<String> kategoriak = new ArrayList<>(fixKategoriak);
            kategoriak.retainAll(iterOktato.getPalyazatiTema());    //ezek utan a kategoriak valtozo a metszetnek felel meg
            if (!kategoriak.isEmpty()){ //ha a metszet ures, nem kell hozzaadni az oktatot
                lehetsegesOktatok.add(iterOktato.getNev());
//                System.out.println(lehetsegesOktatok.toString()); //csak ellenorzes celjabol
            }
        }
        return lehetsegesOktatok;
    }

    private boolean linkOsszevetes(String ellenorizendoLink, String ellenorizendoCim) {
        Felhivas felhivasLista = new Felhivas();
        ArrayList<Felhivas> osszehasonlitoLista = felhivasLista.felhivasLetolto(ellenorizendoCim); //csak azokkal a felhivasokkal vetjuk ossze, amelyeknek azonos a cime
        if (!osszehasonlitoLista.isEmpty()) { //ha ures, vagyis nem volt korabban ilyen cimu felhivas, akkor NullPointer lenne, de akkor nem is kell ellemoriznem
            for (Felhivas felhivas : osszehasonlitoLista) {
                if (felhivas.getFelhivasLink().equals(ellenorizendoLink)) {
                    return true; //akkor adunk vissza true-t, ha mar van ilyen, es nem kell letolteni
                }
            }
        }
        return false;
    }

//    private boolean linkOsszevetes(Felhivas ellenorizendo) {
//        Felhivas felhivasLista = new Felhivas();
//        ArrayList<Felhivas> osszehasonlitoLista = felhivasLista.felhivasLetolto(ellenorizendo.getFelhivasCim()); //csak azokkal a felhivasokkal vetjuk ossze, amelyeknek azonos a cime
//        if (!osszehasonlitoLista.isEmpty()) { //ha ures, akkor NullPointer lenne, de akkor nem is kell ellemoriznem
//            for (Felhivas felhivas : osszehasonlitoLista) {
//                if (felhivas.getFelhivasLink().equals(ellenorizendo.getFelhivasLink())) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
}
