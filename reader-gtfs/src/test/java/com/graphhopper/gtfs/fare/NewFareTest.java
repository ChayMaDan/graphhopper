package com.graphhopper.gtfs.fare;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.conveyal.gtfs.GTFSFeed;
import com.conveyal.gtfs.model.Fare;
import com.conveyal.gtfs.model.FareAttribute;
import com.conveyal.gtfs.model.FareRule;
import com.csvreader.CsvReader;
import com.github.javafaker.Faker;

/*
 * Nouveaux Tests pour la classe Fares :
 *      - Amélioration du score de mutation 
 *      - Utilisation de la bibliothèque Java Faker pour générer des données de test réalistes.
 */
public class NewFareTest {

    private final Faker faker = new Faker();

    /*
     * Nous avons repris la m^me méthode parseFares de la classe FareTest
     * pour créer des instances de Fare réalistes.
     * 
     */

    public static Map<String, Fare> parseFares(String feedId, String fareAttributes, String fareRules) {
        GTFSFeed feed = new GTFSFeed();
        feed.feedId = feedId;
        HashMap<String, Fare> fares = new HashMap<>();
        new FareAttribute.Loader(feed, fares) {
            void load(String input) {
                reader = new CsvReader(new StringReader(input));
                reader.setHeaders(new String[]{"fare_id", "price", "currency_type", "payment_method", "transfers", "transfer_duration"});
                try {
                    while (reader.readRecord()) {
                        loadOneRow();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.load(fareAttributes);
        new FareRule.Loader(feed, fares) {
            void load(String input) {
                reader = new CsvReader(new StringReader(input));
                reader.setHeaders(new String[]{"fare_id", "route_id", "origin_id", "destination_id", "contains_id"});
                try {
                    while (reader.readRecord()) {
                        loadOneRow();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.load(fareRules);
        return fares;
    }

    //Test 1 ajouté le 27/10/2025
    /*
     * Nom du test : AppliesReturnTrueWhenFareHasNoRulesTest
     * 
     * Intention : Vérifier que applies() retourne true quanf fare_rules est vide.
     *             Une fare sans règle doit s'appliquer à tous les segments.
     * 
     * Données de Test : 
     *  - fare.fare_rules = []
     *  - un segment quelconque
     * 
     * Oracle : applies() retourne true
     * 
     * Ce test doit tuer le mutant de la ligne 92 : "replaced boolean with false"
     * 
     */
    @Test
    public void AppliesReturnTrueWhenFareHasNoRulesTest() {
        
        //Setup
        Map<String, Fare> fares = parseFares("test_feed", "test_fare,1.00,USD,0\\n" ,"");
        Fare fare = fares.get("test_fare");

        Trip.Segment segment = new Trip.Segment(
            "feed_1",
            "Route_001",
            0,
            "stopA",
            "stopB",
            new HashSet<>()
        );

        //Appel de la méthode à tester
        boolean resultat = Fares.possibleFares(fares, segment).contains(fare);

        //Oracle 
        assertTrue(resultat);
        
    }

    //Test 2 ajouté le 27/10/2025
    /*
     * Nom du test : SanititizeFareRulesCreatesOriginDestinationRuleWhenBothIdsPresentTest
     * 
     * Intention : vérifier que sanititizeFareRules() crée une règle d'origine-destination (OriginDestinationRule)
     *            lorsque les deux IDs (origin_id et destination_id) sont présents dans une FareRule.
     * 
     * Données de Test :
     *      - Une liste de FareRule avec une règle ayant à la fois origin_id et destination_id
     *      - Pas de route_id ou contains_id dans cette règle
     * 
     * Oracle : La liste retournée par sanititizeFareRules() contient une instance d'OriginDestinationRule
     * 
     * Ce test doit tuer les mutants de la ligne 99 : "negated conditional"
     */
    @Test
    public void SanititizeFareRulesCreatesOriginDestinationRuleWhenBothIdsPresentTest() {

        //Setup
        FareRule fareRule = new FareRule();
        fareRule.route_id = null;
        fareRule.origin_id = "zoneA";
        fareRule.destination_id = "zoneB";
        fareRule.contains_id = null;

        List<FareRule> fareRules = Collections.singletonList(fareRule);

        //Appel de la méthode à tester
        List<SanitizedFareRule> resultat = Fares.sanitizeFareRules(fareRules);

        //Oracle
        long count = resultat.stream().filter(rule -> rule instanceof OriginDestinationRule).count();
        assertEquals(1, count);
    }

    //Test 3 ajouté le 27/10/2025
    /*
     * Nom du test : SanitizefareRulesCreatesZoneRuleWhenContainsIdPresentTest
     * 
     * Intention : vérifier que sanititizeFareRules() crée une règle de zone (ZoneRule)
     *           lorsque contains_id est présent dans une FareRule.
     * 
     * Données de Test :
     *     - 2 FareRule, une avec contains_id = "Zone1", "Zone2"
     * 
     * Oracle : La liste retournée par sanititizeFareRules() contient une instance de ZoneRule avec 2 zones
     * 
     * Ce test doit tuer les mutants de la ligne 100 : "negated conditional", "replaced boolean return with true"
     *
     */
    @Test
    public void SanitizefareRulesCreatesZoneRuleWhenContainsIdPresentTest() { 

        //Setup
        FareRule fareRule1 = new FareRule();
        fareRule1.contains_id = "zoneA";

        FareRule fareRule2 = new FareRule();
        fareRule2.contains_id = "zoneB";

        List<FareRule> fareRules = Arrays.asList(fareRule1, fareRule2);

        //Appel de la méthode à tester
        List<SanitizedFareRule> resultat = Fares.sanitizeFareRules(fareRules); 

        //Oracle
        long count = resultat.stream().filter(rule -> rule instanceof ZoneRule).count();
        assertEquals(1, count); //Vérifie qu'une seule instance de ZoneRule
    }
    
    //Test 4 ajouté le 27/10/2025
    /*
     * Nom du test : SanitizeFareRulesCreatesEmptyZoneRuleWhenNoContainsIdTest
     * 
     * Intention : vérifier que sanititizeFareRules() crée une règle de zone (ZoneRule)
     *          même lorsque contains_id est absent dans une FareRule.
     * 
     * Données de Test :
     *   - Des FareRule avec contains_id = null
     * 
     * Oracle : La liste retournée par sanititizeFareRules() contient quand même une instance de ZoneRule avec 0 zone
     * 
     * Ce test doit tuer le mutant de la ligne 100 : "replaced return value with empty string"
     * 
     */
    @Test
    public void SanitizeFareRulesCreatesEmptyZoneRuleWhenNoContainsIdTest() {

        //Setup
        FareRule fareRule = new FareRule();
        fareRule.route_id = "Route_000";
        fareRule.contains_id = null;

        List<FareRule> fareRules = Collections.singletonList(fareRule);

        //Appel de la méthode à tester
        List<SanitizedFareRule> resultat = Fares.sanitizeFareRules(fareRules);

        //Oracle
        long count = resultat.stream().filter(rule -> rule instanceof ZoneRule).count();
        assertEquals(1, count); //Vérifie qu'une seule instance de ZoneRule

    }

    //Test 5 ajouté le 27/10/2025
    /*
     * Nom du test : SanitizeFareRulesNeverReturnsEmptyListTest
     * 
     * Intention : - Vérifier que sanititizeFareRules() ne retourne jamais une liste vide même avec des données aléatoires
     *             - Utiliser Java Faker pour simuler des données GTfS réelles.
     * 
     * Données de Test :
     *  - Des FareRules générés par Java Faker
     * 
     * Oracle : La liste retournée par sanititizeFareRules() n'est jamais vide
     * 
     * Ce test doit tuer le mutant de la ligne 101 : "replaced return value with Collections.emptyList"
     */
    @Test
    public void SanitizeFareRulesNeverReturnsEmptyListTest() {

        //Setup
        FareRule fareRule1 = new FareRule();
        fareRule1.route_id = faker.regexify("Route_[0-9]{3}");

        FareRule fareRule2 = new FareRule();
        fareRule2.origin_id = faker.address().cityName();
        fareRule2.destination_id = faker.address().cityName();

        FareRule fareRule3 = new FareRule();
        fareRule3.contains_id = faker.regexify("zone[A-Z]");

        List<FareRule> fareRules = Arrays.asList(fareRule1, fareRule2, fareRule3);

        //Appel de la méthode à tester
        List<SanitizedFareRule> resultat = Fares.sanitizeFareRules(fareRules);

        //Oracle
        assertFalse(resultat.isEmpty());
        assertTrue(resultat.size() >= 1);
    }

}
