package com.graphhopper.routing.weighting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.carrotsearch.hppc.IntArrayList;
import com.graphhopper.storage.BaseGraph;

/*
 * Tests unitaires pour la classe QueryGraphWeighting :
 * 
 * Choix de la classe : QueryGraphWeighting
 * Justification : Cette classe permet de faire des calculs de coûts sur les virages 
 *                 pour gérer les noeuds et arêtes virtuels.
 *                 Elle utilise deux dépendances, Basegraph qui gère la structure du graphe 
 *                 routier et Weighting qui gère comment attribuer le coût aux virages. 
 *                 Les mocks nous permettent d'isoler la logique métier ces deux classes et 
 *                 de simuler leurs comportements pour bien tester ce problème complexe.
 *                 
 * 
 * 
 * Choix des classes mockées: BaseGraph, Weighting
 * Justification : BaseGraph : permet de simuler la structure de données du graphe 
 *                             routier avec des noeuds/arêtes réels et virtuels
 *                 Weighting : permet de simuler l'interface de calculs de coûts sur les virages
 *                             Notre classe QueryGraphWeighting délègue à Weighting la logique sur les calculs 
 */
@ExtendWith(MockitoExtension.class)
public class QueryGraphWeightingTest {

    @Mock
    private BaseGraph graph;

    @Mock
    private Weighting weighting;

    private QueryGraphWeighting queryGraphWeighting;
    private IntArrayList closestEdges;

    @BeforeEach
    public void setup(){
        //Setup
        //Graphe simulé avec 10 noeuds réels et 15 arêtes réelles
        //Tout indice de noeud >= 10 et d'arête >= 15 est virtuels
        when(graph.getNodes()).thenReturn(10);
        when(graph.getEdges()).thenReturn(15); 

        closestEdges = new IntArrayList();
        //Chaque arête "originale" est formée par deux arêtes virtuelles
        closestEdges.add(0); // origine de l'arête 15 est l'arête 0
        closestEdges.add(0); // origine de l'arête 16 est l'arête 0

        queryGraphWeighting = new QueryGraphWeighting(graph, weighting, closestEdges);
    }

    //Test ajouté le 16/11/2025
    /* 
     * Nom du test :  testCalcTurnWeightUturnVirtualNode
     * Intention : vérifier que la calsse interdit les UTurn dans les noeuds virtuels en luis assignat un coût infini
     *             Nous cherchons à tester la méthode calcTurnWeight(int inEdge, int viaNode, int outEdge)
     * Données du test : 
     *      Classes mockées : 
     *          - Graph : structure de données du graphe routier avec 10 neuds et 15 arêtes réels
     *          - Weighting : Interface de calculs des coûts sur les virages 
     *      Valeurs simulées :
     *          - viaNode == virtualNode = 10 : le premier noeud virtuel dans le graphe simulé
     *          - virtualEdge = 15 : la première arête virtuelle dans le graphe simulé
     *          - inEdge = outEdge : même arête en entrée et sortie pour simuler un (virage) U Turn
     * 
     * Oracle : Le coût sur ce virage doit être Double.POSITIVE_INFINITY (car le U Turn est interdit)
     *         Aussi la méthode weigthing.calcTurnWeight() ne doit pas être appelé car le code détecte si
     *         un virage est U Turn (interdit) sans consulter l'interface Weighting. 
    */
    @Test
    public void testCalcTurnWeightUturnVirtualNode(){

        //SetUp
        int virtualNode = 10;
        int virtualEdge = 15;

        //Appel de la méthode à tester 
        //Simuler un U Turn
        double turnWeight = queryGraphWeighting.calcTurnWeight(virtualEdge, virtualNode, virtualEdge);

        //Oracle 
        assertEquals(Double.POSITIVE_INFINITY, turnWeight);

        //weighting.calcTurnWeight() ne doit être appelé
        verify(weighting, never()).calcTurnWeight(anyInt(), anyInt(), anyInt()); 
    }

    //Test ajouté le 16/11/2025
    /* 
     * Nom du test :  testCalcTurnWeightTurnRealNode
     * Intention : vérifier que pour des noeud et arêtes réels, la classe délègue correctement 
     *             le calcul des coûts sur les virage à l'objet Weighting puis retourne la valeur trouvée
     * Données du test : 
     *      Classes mockées : 
     *          - Graph : structure de données du graphe routier avec 10 neuds et 15 arêtes réels
     *          - Weighting : Interface de calculs des coûts sur les virages
     *      Valeurs simulées :
     *          - realNode = 5 : noeud réel (< 10) où se passe le virage 
     *          - inEdge = 10 : arête réelle (< 15) entrante
     *          - outEdge = 12 : arête réelle (< 15) sortante
     *          - turnWeightExpected = 20, on ne sait pas le coût sur le virage que doit retourner 
     *                    l'objet Weighting donc on le simule et il doit être retourné par le mock
     * 
     * Oracle : Le coût sur ce virage doit être 20 (valeur du mock)
     *          Aussi la méthode weigthing.calcTurnWeight() doit être appelé une fois pour 1 virage
    */
    @Test
    public void testCalcTurnWeightTurnRealNode(){

        int realNode = 5;
        int inEdge = 10;
        int outEdge = 12;
        double turnWeightExpected = 20; // valeur pris au hasard, n'a aucun sens mathématique

        when(weighting.calcTurnWeight(inEdge, realNode, outEdge)).thenReturn(turnWeightExpected);

        //Appel de la méthode à tester
        double turnWeight = queryGraphWeighting.calcTurnWeight(inEdge, realNode, outEdge);

        //Oracle 
        assertEquals(turnWeightExpected, turnWeight);

        verify(weighting, times(1)).calcTurnWeight(inEdge, realNode, outEdge);
    }

    //Test pour vérifier le Rickroll
    @Test
    public void testFailureRickroll(){
        fail("Déclenchement du Rickroll");
    }
}
