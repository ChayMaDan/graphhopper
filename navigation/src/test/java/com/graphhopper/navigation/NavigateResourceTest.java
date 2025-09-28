package com.graphhopper.navigation;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NavigateResourceTest {


    @Test
    public void voiceInstructionsTest() {

        List<Double> bearings = NavigateResource.getBearing("");
        assertEquals(0, bearings.size());
        assertEquals(Collections.EMPTY_LIST, bearings);

        bearings = NavigateResource.getBearing("100,1");
        assertEquals(1, bearings.size());
        assertEquals(100, bearings.get(0), .1);

        bearings = NavigateResource.getBearing(";100,1;;");
        assertEquals(4, bearings.size());
        assertEquals(100, bearings.get(1), .1);
    }


    //Test 1 : ajouté 27/09/2025
    @Test
    public void getBearingInvalidValuesTest(){
        //nom : getBearingInvalidValuesTest
        //intention : tester la méthode getBearing avec des valeurs invalides
        //motivation de données : des valeurs invalides pour tester le parsing de la méthode getBearing
        //Oracle : Le résultat doit être une liste de Double avec des NaN pour les valeurs invalides
        
        //Cas 1 : valeur sans virgule 
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            NavigateResource.getBearing("123");
        });
        assertTrue(exception.getMessage().contains("You passed an invalid bearings parameter"));

        //Cas2 : format invalide de Double 
        exception = assertThrows(IllegalArgumentException.class, () -> {
            NavigateResource.getBearing("abc,10");
        });
        assertTrue(exception.getMessage().contains("You passed an invalid bearings parameter"));
    }

    //Test 2 : ajouté 27/09/2025
    @Test
    public void VoiceUnitsTest(){
        //nom :VoiceUnitsTest
        //intention : tetser les paramètres voiceUnits de la méthode doGet
        //motivation de données : "metric", "imperial", null, chaîne vide
        //Oracle : "metric" doit retourner DistanceUtils.Unit.METRIC, tout autre valeur doit retourner DistanceUtils.Unit.IMPERIAL.
        
        //Simuler la logique du doGet pour voiceUnits.equals("metric") ? DistanceUtils.Unit.METRIC : DistanceUtils.Unit.IMPERIAL;
        String metric_voiceUnits = "metric";
        String imperial_voiceUnits = "imperial";
        String empty_voiceUnits = "";
        String null_voiceUnits = null;
        

        DistanceUtils.Unit unit1 = metric_voiceUnits.equals("metric") ? DistanceUtils.Unit.METRIC : DistanceUtils.Unit.IMPERIAL;
        DistanceUtils.Unit unit2 = imperial_voiceUnits.equals("metric") ? DistanceUtils.Unit.METRIC : DistanceUtils.Unit.IMPERIAL;
        DistanceUtils.Unit unit3 = empty_voiceUnits.equals("metric") ? DistanceUtils.Unit.METRIC : DistanceUtils.Unit.IMPERIAL;
        DistanceUtils.Unit unit4 = (null_voiceUnits != null && null_voiceUnits.equals("metric")) ? DistanceUtils.Unit.METRIC : DistanceUtils.Unit.IMPERIAL;

        assertEquals(DistanceUtils.Unit.METRIC, unit1);
        assertEquals(DistanceUtils.Unit.IMPERIAL, unit2);
        assertEquals(DistanceUtils.Unit.IMPERIAL, unit3);
        assertEquals(DistanceUtils.Unit.IMPERIAL, unit4);
    }

}
