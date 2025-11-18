package com.graphhopper.routing.util;

import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/*
 * Tests unitaires pour la classe RoadDensityCalculator :
 * 
 * Choix de la classe: RoadDensityCalculator
 * Justification: La classe RoadDensityCalculator dépend fortement de la structure de données Graph
 *                  et de ses itérateurs d'arêtes. En utilisant des mocks, nous pouvons isoler le comportement
 *                  de RoadDensityCalculator et tester ses méthodes sans avoir besoin d'une implémentation complète de Graph.   
 * 
 * Choix des classes mockées: Graph, NodeAccess, EdgeExplorer, EdgeIterator, EdgeIteratorState
 * Justification: Graph : permet de simuler la structure de données du graphe routier.
 *                NodeAccess : permet de simuler l'accès aux coordonnées des nœuds.
 *                EdgeExplorer : permet de simuler la navigation à travers les arêtes du graphe.
 *                EdgeIterator : permet de simuler l'itération sur les arêtes.
 *                EdgeIteratorState : permet de simuler l'état d'une arête.
 * 
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RoadDensityCalculatorTest {

    @Mock 
    private Graph graph;

    @Mock
    private NodeAccess nodeAccess;

    @Mock
    private EdgeExplorer edgeExplorer;

    @Mock
    private EdgeIterator edgeIterator;

    @Mock
    private EdgeIteratorState edgeState;

    private RoadDensityCalculator roadDensityCalculator;

    @BeforeEach
    void setUp(){
        when(graph.getNodeAccess()).thenReturn(nodeAccess);
        when(graph.createEdgeExplorer()).thenReturn(edgeExplorer);

        roadDensityCalculator = new RoadDensityCalculator(graph);
    }

    //Test 1 ajouté le 16/11/2025
    /* 
     * Nom du test :  testCalcRoadDensityWithSimpleGraph
     * Intention : Vérifier le calcul de la densité routière dans un scénario simple avec des arêtes adjacentes.
     * Données du test : 
     *      Classes mockées : 
     *          - Graph : structure de données du graphe routier
     *          - NodeAccess : coordonnées des nœuds
     *          - EdgeExplorer : navigation à travers les arêtes
     *          - EdgeIterator : itération sur les arêtes
     *          - EdgeIteratorState : arête de départ
     *      Valeurs simulées :
     *          - Un graphe avec 3 nœuds (0, 1, 2) ; 1 centre (0-1) et 1 arête (0-2).
     *          - Noeud 0 : (45.5, -73.5)
     *          - Noeud 1 : (45.5009, -73.5)
     *          - Noeud 2 : (45.50065, -73.5)
     *          - Arête entre noeud 0 et noeud 2
     *          - Rayon de recherche de 200 mètres.
     *          - Facteur de pondération constant de 1.0 pour toutes les arêtes.
     * 
     * Oracle : La densité routière calculée doit être égale à 1.0 / 200 / 200 = 2.5e-5
    */
    @Test
    public void testCalcRoadDensityWithSimpleGraph() {

        //Setup des mocks
        // Arête de départ entre le nœud 0 et le nœud 1
        when(edgeState.getBaseNode()).thenReturn(0);
        when(edgeState.getAdjNode()).thenReturn(1);

        // Coordonnées des nœuds

        // Nœud 0 : (45.5, -73.5)
        when(nodeAccess.getLat(0)).thenReturn(45.5);
        when(nodeAccess.getLon(0)).thenReturn(-73.5);

        // Nœud 1 : (45.5009, -73.5)
        when(nodeAccess.getLat(1)).thenReturn(45.5009);
        when(nodeAccess.getLon(1)).thenReturn(-73.5);

        // Centre entre nœud 0 et nœud 1 : (45.50045, -73.5)

        // Nœud 2 : (45.50065, -73.5)
        when(nodeAccess.getLat(2)).thenReturn(45.50065);
        when(nodeAccess.getLon(2)).thenReturn(-73.5);

        //Configuration de l'EdgeExplorer et EdgeIterator pour simuler une arête adjacente
        when(edgeExplorer.setBaseNode(anyInt())).thenReturn(edgeIterator);
        //Simulation de l'itération sur les arêtes adjacentes
        when(edgeIterator.next())
            .thenReturn(true) // Première arête trouvée entre nœud 0 et nœud 2
            .thenReturn(false)  // Plus d'arêtes à explorer pour nœud 0
            .thenReturn(false) // Plus d'arêtes à explorer pour nœud 1
            .thenReturn(false); // Plus d'arêtes à explorer pour nœud 2

        when(edgeIterator.getAdjNode()).thenReturn(2);

        double radius = 200; // 200 mètres
        double roadFactor = 1.0; // Facteur de pondération

        //Appel de la méthode à tester
        double density = roadDensityCalculator.calcRoadDensity(edgeState, radius, e -> roadFactor);

        // La densité routière attendue : Une seule arête prise en compte
        double densityExpected = 1.0 / radius / radius;

        //Oracle
        assertEquals(densityExpected, density, 1e-6);

        // 3 noeuds explorés 
        verify(edgeExplorer, times(3)).setBaseNode(anyInt());
        //next() est appelé 4 fois, 2 fois pour le noeud 0, 1 fois pour chaque neoud 1 et 2
        verify(edgeIterator, times(4)).next();
        //getAdjNode() est appelé 3 fois, une pour chaque arête et 2 fois pour l'exploration des noeuds 1 et 2 
        verify(edgeIterator, times(3)).getAdjNode();
        //le graph nous permet l'accès aux noeuds
        verify(graph, atLeastOnce()).getNodeAccess();
        //au minimum on appelle 2 fois getLat() et getLon()
        verify(nodeAccess, atLeast(2)).getLat(anyInt());
        verify(nodeAccess, atLeast(2)).getLon(anyInt());

    }

    //Test 2 ajouté le 16/11/2025
    /* 
     * Nom du test : testCalcRoadDensityOutsideRadius
     * Intention : Vérifier qque l'algorithme explore les noeuds à l'extérieur du rayon de recherche 
     *             mais ne prend en compte que les arêtes à l'intérieur du rayon dans le calcul de la densité.
     * 
     * Données du test : 
     *      Classes mockées : 
     *          - Graph : structure de données du graphe routier
     *          - NodeAccess : coordonnées des nœuds
     *          - EdgeExplorer : navigation à travers les arêtes
     *          - EdgeIterator : itération sur les arêtes
     *          - EdgeIteratorState : arête de départ
     *      Valeurs simulées :
     *          - Graphe avec 4 noeuds (0, 1, 2, 3)
     *          - Noeud 0 : (45.5, -73.5)
     *          - Noeud 1 : (45.5, -73.5) pour simplifier le calcul du centre
     *          - Noeud 2 : (45.5001, -73.5) à l'intérieur du rayon
     *          - Noeud 3: (45.502, -73.5): à l'extérieur du rayon
     *          - Arête entre noeud 0 et noeud 2 (pris en compte)
     *          - Arête entre noeud 2 et noeud 3 ( exploré mais pas pris en compte)
     *          - Rayon de recherche de 100 mètres.
     *          - Facteur de pondération constant de 1.0 pour toutes les arêtes.
     * 
     * Oracle : la densité routière doit être égale à  1.0 / 100 / 100 = 1.0e-4 
     *          Seul l'arête (route) entre 0 et 2 est prise en compte 
    */
    @Test
    public void testCalcRoadDensityOutsideradius() {

        //Setup des mocks 
        // Arête de départ entre le nœud 0 et le nœud 1
        when(edgeState.getBaseNode()).thenReturn(0);
        when(edgeState.getAdjNode()).thenReturn(1);

        // Coordonnées des nœuds
        // Nœud 0 : (45.5, -73.5)
        when(nodeAccess.getLat(0)).thenReturn(45.5);
        when(nodeAccess.getLon(0)).thenReturn(-73.5);

        //Noeud 1 : (45.5 , -73.5) : on a choisi la même coordonnée que le noeud 0 pour simplifier le calcul du centre
        when(nodeAccess.getLat(1)).thenReturn(45.5);
        when(nodeAccess.getLon(1)).thenReturn(-73.5);

        // Centre entre nœud 0 et nœud 1 : (45.5, -73.5)

        // Noeud 2: (45.5001, -73.5): à l'intérieur du rayon
        when(nodeAccess.getLat(2)).thenReturn(45.5001);
        when(nodeAccess.getLon(2)).thenReturn(-73.5);

        //Noeud 3: (45.502, -73.5): à l'extérieur du rayon
        when(nodeAccess.getLat(3)).thenReturn(45.502);
        when(nodeAccess.getLon(3)).thenReturn(-73.5);

        when(edgeExplorer.setBaseNode(anyInt())).thenReturn(edgeIterator);

        when(edgeIterator.next())
            .thenReturn(true) // Arête trouvée entre noeud 0 et noeud 2
            .thenReturn(false) // Plus d'arêtes à explorer pour nœud 0
            .thenReturn(false) // Plus d'arêtes à explorer pour noeud 1
            .thenReturn(true) // Arête trouvée entre noeud 2 et noeud 3
            .thenReturn(false) // Plus d'arêtes à explorer pour nœud 2
            .thenReturn(false); // Plus d'arêtes à explorer pour noeud 3

        when(edgeIterator.getAdjNode())
            .thenReturn(2)
            .thenReturn(3);
        
        
        double radius = 100; // 100 mètres
        double roadFactor = 1.0; // Facteur de pondération

        //Appel de la méthode à tester
        double density = roadDensityCalculator.calcRoadDensity(edgeState, radius, e -> roadFactor);

        double densityExpected = 1.0 / radius / radius;

        //Oracle
        assertEquals(densityExpected, density, 1e-6);

        //3 noeuds explorés (noeud 3 n'est pas exploré)
        verify(edgeExplorer, times(3)).setBaseNode(anyInt());
        //next() est appelé 5 fois, 2 fois pour le noeud 0, 1 fois pour le noeud 1 et 2 fois pour le neoud 2
        verify(edgeIterator, times(5)).next();
        //getAdjNode() est appelé 4 fois, une pour chaque arête (2) et deux fois pour vérifer si les noeuds ont été visité
        verify(edgeIterator, times(4)).getAdjNode();
        //au minimum on appelle 2 fois getLat() et getLon()
        verify(nodeAccess, atLeast(2)).getLat(anyInt());
        verify(nodeAccess, atLeast(2)).getLon(anyInt());
        
    }

    //Test 3 ajouté le 16/11/2025
        /* 
     * Nom du test :  testCalcRoadDensityNoAdjacentEdges
     * Intention : Vérifier que la méthode calcRoadDensity gère correctement un graphe minimal.
     *             La méthode retourne une densité de 0.0 lorsque aucune arête adjacente 
     *             n'est relié à l'arête de départ (par exemple, une route isolée)
     * Données du test : 
     *      Classes mockées : 
     *          - Graph : structure de données du graphe routier
     *          - NodeAccess : coordonnées des nœuds
     *          - EdgeExplorer : navigation à travers les arêtes
     *          - EdgeIterator : itération sur les arêtes
     *          - EdgeIteratorState : arête de départ
     *      Valeurs simulées :
     *         - Un graphe avec 2 nœuds (0, 1) et une seule arête (0-1).
     *         - Noeud 0 : (45.5, -73.5)
     *         - Noeud 1 : (45.5009, -73.5)
     *         - Rayon de recherche de 200 mètres.
     *         - Facteur de pondération constant de 1.0 pour toutes les arêtes.
     * 
     * Oracle : La densité routière doit être égale exactement à 0.0 lorsque le rayon est zéro.
    */
    @Test 
    public void testCalcRoadDensityNoAdjacentEdges() {

        //Setup des mocks 
        // Arête de départ entre le nœud 0 et le nœud 1
        when(edgeState.getBaseNode()).thenReturn(0);
        when(edgeState.getAdjNode()).thenReturn(1);

        // Coordonnées des nœuds
        // Nœud 0 : (45.5, -73.5)
        when(nodeAccess.getLat(0)).thenReturn(45.5);
        when(nodeAccess.getLon(0)).thenReturn(-73.5);

        //Noeud 1 : (45.5009, -73.5)
        when(nodeAccess.getLat(1)).thenReturn(45.5009);
        when(nodeAccess.getLon(1)).thenReturn(-73.5);

        when(edgeExplorer.setBaseNode(anyInt())).thenReturn(edgeIterator);

        when(edgeIterator.next())
            .thenReturn(false) // Plus d'arêtes à explorer pour nœud 0
            .thenReturn(false); // Plus d'arêtes à explorer pour nœud 1

        
        double radius = 200; // 200 mètres
        double roadFactor = 1.0; // Facteur de pondération

        //Appel de la méthode à tester
        double density = roadDensityCalculator.calcRoadDensity(edgeState, radius, e -> roadFactor);

        double densityExpected = 0.0;

        //Oracle
        assertEquals(densityExpected, density, 1e-6);

        //2 noeuds explorés 
        verify(edgeExplorer, times(2)).setBaseNode(anyInt());
        //next() est appelé 2 fois, une pour chaque noeud
        verify(edgeIterator, times(2)).next();
        //getAdjNode() n'est jamais appelé car aucune arête 
        verify(edgeIterator, never()).getAdjNode();
    } 

    //Test 4 ajouté le 16/11/2025
        /* 
     * Nom du test :  testCalcRoadDensityWithNodeVisted
     * Intention : Vérifier que la méthode calcRoadDensity ne compte pas deux fois les arêtes des noeuds
     *              déjà vistés (éviter de tourner en rond).
     * Données du test : 
     *      Classes mockées : 
     *          - Graph : structure de données du graphe routier
     *          - NodeAccess : coordonnées des nœuds
     *          - EdgeExplorer : navigation à travers les arêtes
     *          - EdgeIterator : itération sur les arêtes
     *          - EdgeIteratorState : arête de départ
     *      Valeurs simulées :
     *         - Un graphe avec 2 nœuds (0, 1) et une seule arête (0-1).
     *         - Noeud 0 : (45.5, -73.5)
     *         - Noeud 1 : (45.5001, -73.5)
     *         - Noeud 2 : (45.5, -73.5)
     *         - Arête entre 0 et 2 (pris en compte car première découverte du noeud 2)
     *         - Arête entre 1 et 2 (2 déjà visité donc ignoré)
     *         - Arête entre 2 et 0 (2 déjà visité donc ignoré)
     *         - Rayon de recherche de 100 mètres.
     *         - Facteur de pondération constant de 1.0 pour toutes les arêtes.
     * 
     * Oracle : la densité routière doit être égale à  1.0 / 100 / 100 = 1.0e-4 
     *          Seul l'arête (route) entre 0 et 2 est prise en compte 
    */
    @Test
    public void testCalcRoadDensityNodeVisited(){

        //Setup des mocks 
        // Arête de départ entre le nœud 0 et le nœud 1
        when(edgeState.getBaseNode()).thenReturn(0);
        when(edgeState.getAdjNode()).thenReturn(1);

        // Coordonnées des nœuds
        // Nœud 0 : (45.5, -73.5)
        when(nodeAccess.getLat(0)).thenReturn(45.5);
        when(nodeAccess.getLon(0)).thenReturn(-73.5);

        //Noeud 1 : (45.5001, -73.5)
        when(nodeAccess.getLat(1)).thenReturn(45.5001);
        when(nodeAccess.getLon(1)).thenReturn(-73.5);

        //Noeud 2 : (45.5, -73.5)
        when(nodeAccess.getLat(2)).thenReturn(45.5);
        when(nodeAccess.getLon(2)).thenReturn(-73.5);

        when(edgeExplorer.setBaseNode(anyInt())).thenReturn(edgeIterator);

        when(edgeIterator.next())
            .thenReturn(true) // Arête trouvée entre noeud 0 et noeud 2
            .thenReturn(false) // Plus d'arêtes à explorer pour nœud 0
            .thenReturn(true) // Arête trouvée entre noeud 1 et noeud 2 (nœud déjà visité)
            .thenReturn(false) // Plus d'arêtes à explorer pour nœud 1
            .thenReturn(true) // Arête trouvée entre noeud 2 et noeud 0 (nœud déjà visité)
            .thenReturn(false); // Plus d'arêtes à explorer pour nœud 1

        when(edgeIterator.getAdjNode())
            .thenReturn(2) // Arête de 0 à 2
            .thenReturn(2) // Arête de 1 à 2 (nœud déjà visité)
            .thenReturn(0); // Arête de 2 à 0 (nœud déjà visité)
        
        
        double radius = 100; // 0 mètres
        double roadFactor = 1.0; // Facteur de pondération

        //Appel de la méthode à tester
        double density = roadDensityCalculator.calcRoadDensity(edgeState, radius, e -> roadFactor);

        // La densité routière attendue : Une seule arête prise en compte
        double densityExpected = 1.0 / radius / radius;

        //Oracle
        assertEquals(densityExpected, density, 1e-6);

        //3 noeuds explorés 
        verify(edgeExplorer, times(3)).setBaseNode(anyInt());
        //getAdjNode() est appelé 6 fois (2 par noeud exploré)
        verify(edgeIterator, times(6)).next();
    }
}