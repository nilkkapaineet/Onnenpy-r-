package com.company;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.exit;
import static java.lang.System.in;

public class Main {
    public static void main(String[] args) {

        // The name of the file to open.
        String fileName = "file.txt";

        // This will reference one line at a time
        String line = "";

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader =
                    new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            List<String> lauseet = new ArrayList<>();

            while ((line = bufferedReader.readLine()) != null) {
                lauseet.add(line);
            }
            // Always close files.
            bufferedReader.close();

            Collections.shuffle(lauseet);

            System.out.println("Tervetuloa pelaamaan Onnenpyörää. Anna pelaajien määrä:");
            Scanner scanner = new Scanner(System.in);
            int maara = scanner.nextInt();
            String[] pelaaja = new String[maara];
            int[] pisteet = new int[maara];
            System.out.println("Anna pelaajien nimet:");
            for (int i=0; i<maara; i++) {
                System.out.print("Pelaaja " + (i+1) + ": ");
                pelaaja[i] = scanner.next();
                pisteet[i] = 0;
            }
            System.out.println("Aloitetaan peli. Pelissä on kuusi kierrosta. Vokaali maksaa 300 pistettä.");

            // toista kunnes lause ratkennut
            // Kysy: osta vokaali, pyöritä tai arvaa
            // jos ohi, rosvo, arvaus väärin tai ei konsonanttia, vuoro vaihtuu
            int vuoro = 0;
            for (int kierros=0; kierros<6; kierros++) {
                int[] kierrosPisteet = new int[maara];
                for (int i=0; i<maara; i++) {
                    kierrosPisteet[i] = 0;
                }
                boolean ratkennut = false;
                String lause = lauseet.get(kierros);
                String nayttoLause = lause.replaceAll("[a-zA-ZäÄöÖ]", "#");
                System.out.println("Kierros " + (1+kierros) );
                List<String> arvatutKirjaimet = new ArrayList<>();
                while (!ratkennut) {
                    System.out.println("Arvaa seuraava lause:");
                    System.out.println(nayttoLause);
                    System.out.println(pelaaja[vuoro] + ", sinun vuorosi. Haluatko [a]rvata, [p]yörittää vai [o]staa vokaalin? Piste[t]ilannetta voi tarkastella myös.");
                    String kasky = scanner.next();
                    if (kasky.equals("a")) {
                        System.out.println("Haluat arvata. Lause on:");
                        String arvaus = scanner.next();
                        arvaus += scanner.nextLine();

                        if (arvaus.equals(lause)) {
                            if (kierrosPisteet[vuoro] < 1000) {
                                kierrosPisteet[vuoro] = 1000;
                            }
                            System.out.println("Oikein meni! " + pelaaja[vuoro] + " saa " + kierrosPisteet[vuoro] + " pistettä.");
                            ratkennut = true;
                            pisteet[vuoro] += kierrosPisteet[vuoro];
                        } else {
                            System.out.println("Väärin meni. Vuoro vaihtuu.");
                            if (vuoro != maara-1) {
                                vuoro++;
                            } else {
                                vuoro = 0;
                            }
                        }
                    } else if (kasky.equals("t") ) {
                        System.out.println("Pistetilanne on:");
                        tulostaPisteet(pelaaja, kierrosPisteet, pisteet);
                    } else if (kasky.equals("p")) {
                        System.out.println("Haluat pyörittää.");
                        String[] palautus = pyorita(scanner, pelaaja[vuoro], pisteet[vuoro], lause, kierrosPisteet[vuoro], nayttoLause);

                        if (palautus[0].equals("ohi")) {
                            System.out.println("Seuraavan vuoro");
                            if (vuoro != maara-1) {
                                vuoro++;
                            } else {
                                vuoro = 0;
                            }
                        } else if (palautus[0].equals("rosvo")) {
                            // rosvo
                            kierrosPisteet[vuoro] = 0;
                            if (vuoro != maara-1) {
                                vuoro++;
                            } else {
                                vuoro = 0;
                            }
                        } else if (palautus[0].equals("sanottu jo")) {
                            // sanottu jo
                            if (vuoro != maara - 1) {
                                vuoro++;
                            } else {
                                vuoro = 0;
                            }
                        } else if (palautus[0].equals("ei osumia")) {
                            if (vuoro != maara - 1) {
                                vuoro++;
                            } else {
                                vuoro = 0;
                            }
                        } else {
                            // jotain pisteitä tuli
                            // näytä lause paljastetuin kirjaimin
                            int pyorityspisteet = Integer.parseInt(palautus[1]);
                            kierrosPisteet[vuoro] += pyorityspisteet;
                            nayttoLause = palautus[0];
                        }
                    } else if (kasky.equals("o")) {
                        if (kierrosPisteet[vuoro] < 300) {
                            System.out.println("Liian vähän pisteitä vokaalin ostamiseen.");
                            continue;
                        }
                        System.out.println("Haluat ostaa vokaalin. Minkä?");
                        String vokaali = scanner.next();
                        if (osta(pelaaja[vuoro], kierrosPisteet[vuoro], lause, vokaali, arvatutKirjaimet) ) {
                            // näytä arvatut vokaalit näyttölauseeseen
                            kierrosPisteet[vuoro] -= 300;
                            int osumia = 0;
                            for (int i=0; i<lause.length(); i++) {
                                if (lause.substring(i, (i+1)).equals(vokaali)) {
                                    nayttoLause = nayttoLause.substring(0, i)+vokaali+nayttoLause.substring((i+1) );
                                    osumia++;
                                }
                            }
                            String isoVokaali = vokaali.toUpperCase();
                            for (int i=0; i<lause.length(); i++) {
                                if (lause.substring(i, (i+1)).equals(isoVokaali)) {
                                    nayttoLause = nayttoLause.substring(0, i)+isoVokaali+nayttoLause.substring((i+1) );
                                    osumia++;
                                }
                            }
                            System.out.println(osumia + " " + vokaali + "-kirjainta.");
                            arvatutKirjaimet.add(vokaali);
                        } else {
                            System.out.println("Ei vokaalia " + vokaali + " tai se oli jo arvattu. Vuoro vaihtuu.");
                            if (vuoro != maara-1) {
                                vuoro++;
                            } else {
                                vuoro = 0;
                            }
                        }
                    } else {
                        System.out.println("Anteeksi, en kuullut. Mitä haluatkaan tehdä?");
                        continue;
                    }
                }
                System.out.println("Se oli kierros nro " + (kierros+1) + ". Tilanne on seuraava:");
                // nollaa kierrospisteet
                for (int i=0; i<kierrosPisteet.length; i++) {
                    kierrosPisteet[i] = 0;
                }
                tulostaPisteet(pelaaja, kierrosPisteet, pisteet);
            }

            System.out.println("Peli loppui.");
            // järjestele pisteiden mukaiseen järjestykseen
            int max = 0;
            int voittaja = 0;
            for (int i=0; i<maara; i++) {
                if (pisteet[i] > max) {
                    voittaja = i;
                }
            }
            System.out.println("Onneksi olkoon " + pelaaja[voittaja] + "! Voitit.");

        } catch (FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        } catch (IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
            // Or we could just do this:
            // ex.printStackTrace();
        }

    }

    public static boolean osta(String pelaaja, int pisteet, String lause, String vokaali, List<String> arvatutKirjaimet) {
        // katsotaan, onko kyseinen vokaali olemassa ja se ei saisi olla jo avattu
        for (String s : arvatutKirjaimet) {
            if (s.equals(vokaali)) {
                return false;
            }
        }
        if (lause.indexOf(vokaali) > 0) {
            return true;
        }
        String isoVokaali = vokaali.toUpperCase();
        if (lause.indexOf(isoVokaali) > 0) {
            return true;
        }
        return false;
    }

    public static void tulostaPisteet(String[] pelaaja, int[] pisteet, int[] kokonaisPisteet) {
        for (int i=0; i<pisteet.length; i++) {
            System.out.println(pelaaja[i] + ": " + pisteet[i] + "/" + kokonaisPisteet[i] + " pistettä.");
        }
    }

    public static String[] pyorita(Scanner scanner, String pelaaja, int pisteet, String lause, int kierrosPisteet, String nayttoLause) {
        int[] sektori = {0, 50, 100, 150, 200, 300, 500, 1001};
        String[] palautusarvo = {"lause", "pisteet"};
        // arvo sektori
        int random = (int)(Math.random() * sektori.length);
        int arvottuSektori = sektori[random];
        if (arvottuSektori == 1001) {
            // rosvo --> pisteet nolliin, seuraavan vuoro
            System.out.println("Rosvosektori. Menetät kierroksen pisteesi. Vuoro vaihtuu.");
            palautusarvo[0] = "rosvo";
            palautusarvo[1] = "0";
            return palautusarvo;
        } else if (arvottuSektori == 0) {
            // ohi --> seuraavan vuoro
            System.out.println("Ohisektori. Ei pisteitä. Vuoro vaihtuu.");
            palautusarvo[0] = "ohi";
            palautusarvo[1] = "0";
            return palautusarvo;
        } else {
            // pistesektori -->
            // sano konsonantti -->
            int osumia = 0;
            System.out.println(arvottuSektori + " pistettä. Anna konsonantti: ");
            String konsonantti = scanner.next();
            // käy läpi lauseen konsonantit, ei saa olla jo tiedetty konsonantti
            int konsonanttiPisteet = 0;
            // tarkasta, ettei konsonanttia ole jo sanottu
            for(int i = 0; i < nayttoLause.length()-1; i ++){
                if (konsonantti.equals(nayttoLause.substring(i, (i+1)))) {
                    System.out.println("Kirjain jo sanottu. Vuoro vaihtuu.");
                    palautusarvo[0] = "sanottu jo";
                    palautusarvo[1] = "0";
                    return palautusarvo;
                }
            }
            konsonantti = konsonantti.toUpperCase();
            for(int i = 0; i < nayttoLause.length()-1; i ++){
                if (konsonantti.equals(nayttoLause.substring(i, (i+1)))) {
                    System.out.println("Kirjain jo sanottu. Vuoro vaihtuu.");
                    palautusarvo[0] = "sanottu jo";
                    palautusarvo[1] = "0";
                    return palautusarvo;
                }
            }
            // varsinainen osumatarkastelu
            konsonantti = konsonantti.toLowerCase();
            for(int i = 0; i < lause.length()-1; i++){
                if (konsonantti.equals(lause.substring(i, (i+1)))) {
                    konsonanttiPisteet += arvottuSektori;
                    osumia++;
                }
            }
            for (int i=0; i<lause.length(); i++) {
                if (lause.substring(i, (i+1)).equals(konsonantti)) {
                    nayttoLause = nayttoLause.substring(0, i)+konsonantti+nayttoLause.substring((i+1) );
                }
            }
            konsonantti = konsonantti.toUpperCase();
            for(int i = 0; i < lause.length()-1; i ++){
                if (konsonantti.equals(lause.substring(i, (i+1)))) {
                    konsonanttiPisteet += arvottuSektori;
                    osumia++;
                }
            }
            String isoKonsonantti = konsonantti.toUpperCase();
            for (int i=0; i<lause.length(); i++) {
                if (lause.substring(i, (i+1)).equals(isoKonsonantti)) {
                    nayttoLause = nayttoLause.substring(0, i)+isoKonsonantti+nayttoLause.substring((i+1) );
                }
            }
            if (osumia == 0) {
                System.out.println("Ei konsonanttia " + konsonantti);
                palautusarvo[0] = "ei osumia";
            } else {
                System.out.println("Löytyi " + osumia + " " + konsonantti + "-kirjainta. " + konsonanttiPisteet + " pistettä.");
                palautusarvo[0] = nayttoLause;
            }
            // korjaa näyttölauseeseen löydetyt konsonantit
            palautusarvo[1] = Integer.toString(konsonanttiPisteet);;
            return palautusarvo;
            // jos konsonantti löytyy -->
            // pisteet * konsonanttien lukumäärä
            // jossei konsonanttia -->
            // seuraavan vuoro
        }
    }
}