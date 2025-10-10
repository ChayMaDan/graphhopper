/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.reader.dem;

import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.RAMDirectory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Karich
 */
public class HeightTileTest {
    @Test
    public void testGetHeight() {
        // data access has same coordinate system as graphical or UI systems have (or the original DEM data has).
        // But HeightTile has lat,lon system ('mathematically')
        int width = 10;
        int height = 20;
        HeightTile instance = new HeightTile(0, 0, width, height, 1e-6, 10, 20);
        DataAccess heights = new RAMDirectory().create("tmp");
        heights.create(2 * width * height);
        instance.setHeights(heights);
        init(heights, width, height, 1);

        // x,y=1,7
        heights.setShort(2 * (17 * width + 1), (short) 70);

        // x,y=2,9
        heights.setShort(2 * (19 * width + 2), (short) 90);

        assertEquals(1, instance.getHeight(5, 5), 1e-3);
        assertEquals(70, instance.getHeight(2.5, 1.5), 1e-3);
        // edge cases for one tile with the boundaries [min,min+degree/width) for lat and lon
        assertEquals(1, instance.getHeight(3, 2), 1e-3);
        assertEquals(70, instance.getHeight(2, 1), 1e-3);

        // edge cases for the whole object
        assertEquals(1, instance.getHeight(+1.0, 2), 1e-3);
        assertEquals(90, instance.getHeight(0.5, 2.5), 1e-3);
        assertEquals(90, instance.getHeight(0.0, 2.5), 1e-3);
        assertEquals(1, instance.getHeight(+0.0, 3), 1e-3);
        assertEquals(1, instance.getHeight(-0.5, 3.5), 1e-3);
        assertEquals(1, instance.getHeight(-0.5, 3.0), 1e-3);
        // fall back to "2,9" if within its boundaries
        assertEquals(90, instance.getHeight(-0.5, 2.5), 1e-3);

        assertEquals(1, instance.getHeight(0, 0), 1e-3);
        assertEquals(1, instance.getHeight(9, 10), 1e-3);
        assertEquals(1, instance.getHeight(10, 9), 1e-3);
        assertEquals(1, instance.getHeight(10, 10), 1e-3);

        // no error
        assertEquals(1, instance.getHeight(10.5, 5), 1e-3);
        assertEquals(1, instance.getHeight(-0.5, 5), 1e-3);
        assertEquals(1, instance.getHeight(1, -0.5), 1e-3);
        assertEquals(1, instance.getHeight(1, 10.5), 1e-3);
    }

    @Test
    public void testGetHeightForNegativeTile() {
        int width = 10;
        HeightTile instance = new HeightTile(-20, -20, width, width, 1e-6, 10, 10);
        DataAccess heights = new RAMDirectory().create("tmp");
        heights.create(2 * 10 * 10);
        instance.setHeights(heights);
        init(heights, width, width, 1);

        // x,y=1,7
        heights.setShort(2 * (7 * width + 1), (short) 70);

        // x,y=2,9
        heights.setShort(2 * (9 * width + 2), (short) 90);

        assertEquals(1, instance.getHeight(-15, -15), 1e-3);
        assertEquals(70, instance.getHeight(-17.5, -18.5), 1e-3);
        // edge cases for one tile with the boundaries [min,min+degree/width) for lat and lon
        assertEquals(1, instance.getHeight(-17, -18), 1e-3);
        assertEquals(70, instance.getHeight(-18, -19), 1e-3);
    }

    @Test
    public void testInterpolate() {
        HeightTile instance = new HeightTile(0, 0, 2, 2, 1e-6, 10, 10).setInterpolate(true);
        DataAccess heights = new RAMDirectory().create("tmp");
        heights.create(2 * 2 * 2);
        instance.setHeights(heights);
        double topLeft = 0;
        double topRight = 1;
        double bottomLeft = 2;
        double bottomRight = 3;
        set(heights, 2, 0, 0, (short) topLeft);
        set(heights, 2, 1, 0, (short) topRight);
        set(heights, 2, 0, 1, (short) bottomLeft);
        set(heights, 2, 1, 1, (short) bottomRight);

        // corners
        assertEquals(bottomLeft, instance.getHeight(0, 0), 1e-3);
        assertEquals(topLeft, instance.getHeight(10, 0), 1e-3);
        assertEquals(bottomRight, instance.getHeight(0, 10), 1e-3);
        assertEquals(topRight, instance.getHeight(10, 10), 1e-3);

        // midpoints
        assertEquals(avg(topLeft, topRight), instance.getHeight(10, 5), 1e-3);
        assertEquals(avg(bottomLeft, bottomRight), instance.getHeight(0, 5), 1e-3);
        assertEquals(avg(topLeft, bottomLeft), instance.getHeight(5, 0), 1e-3);
        assertEquals(avg(topRight, bottomRight, topLeft, bottomLeft), instance.getHeight(5, 5), 1e-3);

        // missing data uses whatever remains
        set(heights, 2, 1, 0, Short.MIN_VALUE);
        set(heights, 2, 0, 1, Short.MIN_VALUE);
        set(heights, 2, 1, 1, Short.MIN_VALUE);
        assertEquals(topLeft, instance.getHeight(0, 0), 1e-3);
        assertEquals(topLeft, instance.getHeight(10, 0), 1e-3);
        assertEquals(topLeft, instance.getHeight(0, 10), 1e-3);
        assertEquals(topLeft, instance.getHeight(10, 10), 1e-3);

        // when all data missing, returns NaN
        set(heights, 2, 0, 0, Short.MIN_VALUE);
        assertEquals(Double.NaN, instance.getHeight(5, 5), 1e-3);
    }

    private void init(DataAccess da, int width, int height, int i) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                set(da, width, x, y, (short) 1);
            }
        }
    }

    private void set(DataAccess da, int width, int x, int y, short height) {
        da.setShort(2 * (y * width + x), height);
    }

    private double avg(double... ns) {
        double sum = 0;
        for (double n : ns) {
            sum += n;
        }
        return sum / ns.length;
    }
    /**
     * testToString
     *
     * but : vérifier que toString() renvoie une chaîne correcte pour HeightTile.
     * idée : utile pour le débogage et les journaux.
     * attendu :
     *  - jamais null ni vide
     *  - format "minLat,minLon"
     *  - les valeurs négatives et positives s’affichent correctement (ex : -15 et 25)
     */
    @Test
    public void testToString() {
        HeightTile heightTile = new HeightTile(-15, 25, 10, 10, 1e-6, 1, 1);
        String result = heightTile.toString();

        assertNotNull(result, "toString() ne doit jamais renvoyer null");
        assertTrue(result.length() > 0, "toString() ne doit pas être vide");

        // vérifie que le format et les valeurs sont corrects
        assertTrue(result.contains("-15"), "Doit contenir la valeur minLat");
        assertTrue(result.contains("25"), "Doit contenir la valeur minLon");
        assertTrue(result.contains(","), "Doit contenir une virgule comme séparateur");
        assertEquals("-15,25", result, "Doit correspondre exactement au format 'minLat,minLon'");
    }

    /**
     * testIsSeaLevel
     *
     * but : vérifier que isSeaLevel() et setSeaLevel() fonctionnent correctement.
     * idée : permet de savoir si une tuile est au niveau de la mer.
     * test :
     *  - état initial false
     *  - passage à true
     *  - retour à false
     * attendu :
     *  - par défaut = false
     *  - après setSeaLevel(true) = true
     *  - setSeaLevel() renvoie la même instance (API fluide)
     *  - après setSeaLevel(false) = false
     */

    @Test
    public void testIsSeaLevel() {
        HeightTile heightTile = new HeightTile(0, 0, 5, 5, 1e-6, 1, 1);
        DataAccess heights = new RAMDirectory().create("tmp");
        heights.create(2 * 5 * 5);
        heightTile.setHeights(heights);

        // état initial : false
        assertFalse(heightTile.isSeaLevel(),
                "Le niveau de la mer doit être faux au départ");

        // passage à true
        HeightTile result = heightTile.setSeaLevel(true);
        assertEquals(heightTile, result,
                "setSeaLevel doit renvoyer la même instance");
        assertTrue(heightTile.isSeaLevel(),
                "Le niveau de la mer doit être vrai après setSeaLevel(true)");

        // retour à false
        heightTile.setSeaLevel(false);
        assertFalse(heightTile.isSeaLevel(),
                "Le niveau de la mer doit redevenir false");
    }

    /**
     * testGetHeightBoundaryExceptions
     *
     * but : vérifier que getHeight() lève une erreur quand les coordonnées dépassent les limites.
     * idée : éviter les accès invalides qui provoqueraient des bugs silencieux.
     * configuration :
     *  - précision = 1.0 = limites simples à calculer (borne inférieure = -1, borne supérieure = 2)
     *  - tuile positionnée à (0, 0) avec 10x10
     * test :
     *  - latitude = 3.0 = hors limites = exception
     *  - longitude = 3.0 = hors limites = exception
     * attendu :
     *  - IllegalStateException dans les deux cas
     *  - message clair indiquant la latitude ou la longitude fautive
     */

    @Test
    public void testGetHeightBoundaryExceptions() {
        // précision simple pour avoir des limites prévisibles
        HeightTile heightTile = new HeightTile(0, 0, 10, 10, 1.0, 1, 1);
        DataAccess heights = new RAMDirectory().create("tmp");
        heights.create(2 * 10 * 10);
        heightTile.setHeights(heights);
        init(heights, 10, 10, 1);

        // latitude hors limites : |3 - 0| = 3 > 2
        Exception latException = assertThrows(IllegalStateException.class, () -> {
            heightTile.getHeight(3.0, 0.5); // deltaLat = 3 > latHigherBound = 2
        }, "Doit lever une exception pour une latitude hors limites");

        assertTrue(latException.getMessage().contains("latitude not in boundary"),
                "Le message doit mentionner la latitude");
        assertTrue(latException.getMessage().contains("3.0"),
                "Le message doit contenir la valeur fautive");

        // longitude hors limites : |3 - 0| = 3 > 2
        Exception lonException = assertThrows(IllegalStateException.class, () -> {
            heightTile.getHeight(0.5, 3.0); // deltaLon = 3 > lonHigherBound = 2
        }, "Doit lever une exception pour une longitude hors limites");

        assertTrue(lonException.getMessage().contains("longitude not in boundary"),
                "Le message doit mentionner la longitude");
        assertTrue(lonException.getMessage().contains("3.0"),
                "Le message doit contenir la valeur fautive");
    }
}
