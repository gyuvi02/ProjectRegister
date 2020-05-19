package okatok;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import palyazatkezelo.MongoAccess;

import java.lang.reflect.Field;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;

public class OktatoLekerdezes{

    public OktatoLekerdezes() {
    }

    MongoDatabase palyazatDB = MongoAccess.getConnection().getDatabase("PalyazatDB");
    MongoCollection<Oktato> oktatokColl = palyazatDB.getCollection("Oktatok", Oktato.class);

    //A kar összes oktatójának vagy egy tanszékhez tartozó oktatók lekérdezése – ennek igazából nincs túl sok gyakorlati jelentősége
    public ArrayList<Oktato> oktatoListak(String tanszek) {//meg kell adni (legordulo menu), hogy melyik tanszek, vagy az osszes
        ArrayList<Oktato> oktatoLista = new ArrayList<>();
        FindIterable<Oktato> iterOktato = oktatokColl.find();
        for (Oktato oktato : iterOktato) {
            if (oktato.getTanszek().equals(tanszek) || tanszek.equals("összes"))
            oktatoLista.add(oktato);
        }
        return nevRendezo(oktatoLista);
    }
    //Egyes oktatók adatlapjának lekérése
    public Oktato oktatoKereso(String oktato) {
        Oktato keresettOktato = oktatokColl.find(eq("nev", oktato)).first();
        if (keresettOktato == null){
            System.out.println("Nincs ilyen oktató");
            return null;
        }
        else
            return keresettOktato;
    }

    //A kar vagy egy tanszék összes kutatási témájának kiíratása (mindegyik csak egyszer szerepeljen)
    public ArrayList<String> kutatasiTemak(String tanszek) { //meg kell adni (legordulo menu), hogy melyik tanszek, vagy az osszes
        for (Oktato oktato : oktatoListak("összes")) {
            oktato.getKutatasiTema()
//            ArrayList<String> kutTemak = oktatokColl
        }
    }

    public void kulcsszavasLekerdezes(String kulcsszo) {

    }

    //Kutatási témák alapján keresés az oktatók között
    public ArrayList<Oktato> kutatasiTemaKereso(String tema) { //a tomb csak kutatasiTema vagy palyazatiTema lehet
        ArrayList<Oktato> oktatoLista = new ArrayList<>();
        for (Oktato oktato : oktatoListak("összes")) {
            if (oktato.getKutatasiTema().contains(tema)) {
                oktatoLista.add(oktato);
            }
        }
        return nevRendezo(oktatoLista);   //Oktato obj-eket kuldok vissza, majd ott levalogatom, hogy mit akarok megjeleniteni
    }
    //Az oktatók pályázati témái alapján – ezekből nincs túl sok, legördülő menüvel megoldható
    public ArrayList<Oktato> palyazatiTemaKereso(String tema) { //Ez ugyanaz, mint az elozo, ha parameterben at tudnam adni a field nevet, akkor egy is eleg lenne
        ArrayList<Oktato> oktatoLista = new ArrayList<>();      //viszont csak olyan megoldast talaltam, ahol public mezot kellene hasznalnom az osztalyban, ezert marad igy
        for (Oktato oktato : oktatoListak("összes")) {
            if (oktato.getPalyazatiTema().contains(tema)) {
                oktatoLista.add(oktato);
            }
        }
        return nevRendezo(oktatoLista);   //Oktato obj-et kuldok vissza, majd ott levalogatom, hogy mit akarok megjeleniteni
    }







    private ArrayList<Oktato> nevRendezo(ArrayList<Oktato> lista) {
        Collections.sort(lista, new Comparator<Oktato>() {
            @Override
            public int compare(Oktato o1, Oktato o2) {
                return o1.getNev().compareTo(o2.getNev());    //nev alapjan sorba rendeyve
            }
        });
        return lista;
    }
}
