package com.graphhopper.navigation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DistanceUtilsTest {
    
    // Test 3 ajouté 27/09/2025 par Chaimaa Dannane
    // Test de la classe DistanceUtils pour vérifier les conversions d'unités
    @Test
    public void UnitTranslationKeyTest(){
        //nom : UnitTranslationKeyTest
        //intention : tester les clés de traduction des unités dans la classe DistanceUtils
        //motivation de données : vérifier la correspondance correcte entre les unités métriques et impériales
        //Oracle : Les clés de traduction doivent correspondre aux valeurs attendues pour chaque unité

        assertEquals("in_km_singular", DistanceUtils.UnitTranslationKey.IN_HIGHER_DISTANCE_SINGULAR.metric);
        assertEquals("in_mi_singular", DistanceUtils.UnitTranslationKey.IN_HIGHER_DISTANCE_SINGULAR.imperial);
        assertEquals("in_km", DistanceUtils.UnitTranslationKey.IN_HIGHER_DISTANCE_PLURAL.metric);
        assertEquals("in_mi", DistanceUtils.UnitTranslationKey.IN_HIGHER_DISTANCE_PLURAL.imperial);
        assertEquals("in_m", DistanceUtils.UnitTranslationKey.IN_LOWER_DISTANCE_PLURAL.metric);
        assertEquals("in_ft", DistanceUtils.UnitTranslationKey.IN_LOWER_DISTANCE_PLURAL.imperial);
        assertEquals("for_km", DistanceUtils.UnitTranslationKey.FOR_HIGHER_DISTANCE_PLURAL.metric);
        assertEquals("for_mi", DistanceUtils.UnitTranslationKey.FOR_HIGHER_DISTANCE_PLURAL.imperial);

    }

}
