package com.graphhopper.gtfs;

import com.graphhopper.storage.Graph;
import com.graphhopper.util.details.PathDetail;
import com.graphhopper.util.details.PathDetailsBuilderFactory;

import com.graphhopper.ResponsePath;
import com.graphhopper.Trip;
import com.graphhopper.gtfs.fare.Amount;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PointList;
import com.graphhopper.util.Translation;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.weighting.Weighting;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Geometry;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class TripFromLabelTest {
    
    @Mock 
    private Graph mockGraph;
    @Mock 
    private EncodedValueLookup mockEncodedValueLookup;
    @Mock 
    private GtfsStorage mockGtfsStorage;
    @Mock 
    private RealtimeFeed mockRealtimeFeed;
    @Mock
    private PathDetailsBuilderFactory mockPathDetailsBuilderFactory;
    @Mock
    private Translation mockTranslation;
    @Mock
    private Weighting mockWeighting;

    private TripFromLabel tripFromLabel;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        tripFromLabel = new TripFromLabel(mockGraph, mockEncodedValueLookup, mockGtfsStorage, mockRealtimeFeed, mockPathDetailsBuilderFactory, 5.0);
    }

    //Test 1 ajouté le 27/09/2025
    /*
     * Nom : getCheapestFareEmptyPtTest
     * Intention : tester la méthode getCheapestFare lorsqu'aucun moyen de transport public (Pt) n'est disponible
     *             PAS de trajets à transport public <==> PAS de tarifs à calculer
     * Motivation de données : liste vide => aucun trajet à transport public n'existe
     * Oracle : doit vérifier si la méthode retourne Optional.empty() car aucun moyen de transport public n'est disponible
     */
    @Test
    public void getCheapestFareEmptyPtTest(){
        
        List<Trip.Leg> emptyPtList = new ArrayList<>();

        Optional<Amount> resultat = TripFromLabel.getCheapestFare(mockGtfsStorage, emptyPtList);

        assertFalse(resultat.isPresent());
    }

    //Test 2 ajouté le 27/09/2025
    /*
     * Nom : getCheapestFareWalkLegTest
     * Intention : tester la méthode getCheapestFare lorsque le trajet ne peut se faire qu'à pied
     *            Trajets uniquement à pied <==> PAS de tarifs à calculer
     * Motivation de données : liste avec uniquement des trajets à pied (WalkLeg)
     * Oracle : doit vérifier si la méthode retourne Optional.empty() car aucun moyen de transport public n'est disponible
     */
    @Test
    public void getCheapestFareWalkLegTest(){
    
        Trip.WalkLeg mockWalkLeg1 = mock(Trip.WalkLeg.class);
        Trip.WalkLeg mockWalkLeg2 = mock(Trip.WalkLeg.class);
        List<Trip.Leg> WalkLegList = Arrays.asList(mockWalkLeg1, mockWalkLeg2);
        
        Optional<Amount> resultat = TripFromLabel.getCheapestFare(mockGtfsStorage, WalkLegList);

        assertFalse(resultat.isPresent());
    }

    //Test 3 ajouté le 27/09/2025
    /*
     * Nom : parsePartitionToLegsEmptyPathTest
     * Intention : tester la méthode parsePartitionToLegs lorsque le chemin est vide (longueur =< 1)    
     *             Pas de transitions <==> Pas de trajets à créer
     * Motivation de données : liste vide pour vérifier le comportement avec aucune donnée  
     * Oracle : doit vérifier que la liste retournée est vide
     */
    @Test
    public void parsePartitionToLegsEmptyPathTest(){

        //Setup : liste vide
        List<Label.Transition> emptyPath = new ArrayList<>();
        List<String> requestedPathdetails = new ArrayList<>();

        //Appel de la méthode à tester
        List<Trip.Leg> resultat = tripFromLabel.parsePartitionToLegs(emptyPath, mockGraph, mockEncodedValueLookup, mockWeighting, mockTranslation, requestedPathdetails);

        //Oracle
        assertTrue(resultat.isEmpty());
    }

    //Test 4 ajouté le 27/09/2025
    /*
     * Nom : parsePartitionToLegsOneEltPathTest
     * Intention : tester la méthode parsePartitionToLegs lorsque le chemin contient un seul élément (longueur = 1)
     *           Un seul élément <==> Pas de trajets à créer
     * Motivation de données : liste avec un seul élément pour vérifier le comportement avec une donnée minimale
     * Oracle : doit vérifier que la liste retournée est vide car un seul élément ne permet pas de former un trajet
     */
    @Test
    public void parsePartitionToLegsOneEltPathTest(){
        
        //Setup : liste avec un seul élément
        Label.Transition mockTransition = mock(Label.Transition.class);
        List<Label.Transition> oneEltPath = Arrays.asList(mockTransition);
        List<String> requestedPathdetails = new ArrayList<>();

        //Appel de la méthode à tester 
        List<Trip.Leg> resultat = tripFromLabel.parsePartitionToLegs(oneEltPath, mockGraph, mockEncodedValueLookup, mockWeighting, mockTranslation, requestedPathdetails);

        //Oracle
        assertTrue(resultat.isEmpty());
    }

    //Test 5 ajouté le 05/10/2025
    /*
     * Nom : parsePathToPartitionsTransitionPathTest
     * Intention : tester la méthode parsePathToPartitions avec un chemin de transitions pour vérifier qu'elle créé 
     *            correctement une SEULE partition
     * Motivation de données :  Chemin avec 3 transitions pour tester le partitionnement de base 
     * Oracle : doit vérifier que la liste retournée est une seule partition qui contient 3 transitions dans l'ordre
     */
    @Test
    public void parsePartitionToLegsTransitionPathTest(){
        //Setup : Chemin avec 3 transitions
        Label mockTransition1 = mock(Label.class);
        Label mockTransition2 = mock(Label.class);
        Label mockTransition3 = mock(Label.class);

        GraphExplorer.MultiModalEdge mockEdge1 = mock(GraphExplorer.MultiModalEdge.class);
        GraphExplorer.MultiModalEdge mockEdge2 = mock(GraphExplorer.MultiModalEdge.class);

        when(mockEdge1.getType()).thenReturn(GtfsStorage.EdgeType.HIGHWAY);
        when(mockEdge1.getType()).thenReturn(GtfsStorage.EdgeType.HIGHWAY);

        Label.Transition transition1 = new Label.Transition(mockTransition1, null);
        Label.Transition transition2 = new Label.Transition(mockTransition2, mockEdge1);
        Label.Transition transition3 = new Label.Transition(mockTransition3, mockEdge2);

        List<Label.Transition> transitionPath = Arrays.asList(transition1, transition2, transition3);

        //Appel de la méthode à tester
        List<List<Label.Transition>> resultat = tripFromLabel.parsePathToPartitions(transitionPath);

        //Oracle
        assertNotNull(resultat);
        assertEquals(1, resultat.size()); // Une seule partition
        assertEquals(3, resultat.get(0).size()); // La partition contient 3 transitions
        assertEquals(transition1, resultat.get(0).get(0));
        assertEquals(transition2, resultat.get(0).get(1));
        assertEquals(transition3, resultat.get(0).get(2));
    }

    //Test 6 ajouté le 27/09/2025
    /*
     * Nom : createResponsePathEmptyTest
     * Intention : Vérifier qu'un comportement de la méthode createResponsePath avec une liste vide  
     * Motivation de données : liste vide pour tester le cas limite où aucun trajet n'est fourni
     * Oracle : doit vérifier qu'une IndexOutOfBoundsException est levée en essayant d'accéder à 
     *          un index inexistant legs.get(legs.size() - 1) sans vérifier si la lise est vide
     */
    @Test
    public void createResponsePathEmptyTest(){
        
        //Setup des données de test
        PointList PointList = new PointList();
        PointList.add(45.5017, -73.5673); // Montréal
        PointList.add(45.5087, -73.554); // Montréal, un autre point

        List<Trip.Leg> emptyLegs = new ArrayList<>();

        //Oracle
        assertThrows(IndexOutOfBoundsException.class, () -> {
            TripFromLabel.createResponsePath(mockGtfsStorage, mockTranslation, PointList, emptyLegs); // Accès à un index inexistant pour provoquer l'exception
        });

    }

    //Test 7 ajouté le 27/09/2025
    /*
     * Nom : createResponsePathWalkLegTest
     * Intention : tester la methode createResponsePath avec deux trajets à pied simulés à Montréal
     *             pour vérifier que les propriétés sont correctement transférées
     * Motivation de données : deux WalkLegs avec des propriétés : distance, temps, géométrie, instructions, pathDetails 
     * Oracle : vérifier que les propriétés du ResponsePath et des WalkLegs sont correctes
     */
    @Test
    public void createResponsePathWalkLegTest(){

        //Setup des données de test

        PointList PointList = new PointList();
        PointList.add(45.5017, -73.5673); // Montréal
        PointList.add(45.5087, -73.554); // Montréal, un autre point

        GeometryFactory geometryFactory = new GeometryFactory();

        //Premier Trajet à pied : 1500m, 10 minutes
        Date departureTime1 = new Date(1000L);
        Date arrivalTime1 = new Date(601000L);
        //Géométrie pour le premier trajet
        Coordinate[] coordinates1 = new Coordinate[] {
            new Coordinate(-73.5673, 45.5017),
            new Coordinate(-73.5650, 45.5030),
            new Coordinate(-73.5600, 45.5050)
        };
        Geometry geometry1 = geometryFactory.createLineString(coordinates1);
        //Instructions pour le premier trajet
        InstructionList instructions1 = new InstructionList(mockTranslation);
        PointList instructionPointList1 = new PointList();
        instructionPointList1.add(45.5017, -73.5673);
        instructionPointList1.add(45.5030, -73.5650);
        Instruction inst1 = new Instruction(Instruction.CONTINUE_ON_STREET, "Rue A", instructionPointList1);
        inst1.setDistance(1500.0);
        inst1.setTime(600000); // 10 minutes en ms
        instructions1.add(inst1);
        //Path Deatils pour le premier trajet
        Map<String, List<PathDetail>> pathDetailsMap1 = new HashMap<>();
        List<PathDetail> pathDetails1 = new ArrayList<>();
        PathDetail pd1 = new PathDetail(5.0); //5km/h
        pd1.setFirst(0);
        pd1.setLast(2);
        pathDetails1.add(pd1);
        pathDetailsMap1.put("vitesse normale", pathDetails1);

        Trip.WalkLeg walkLeg1 = new Trip.WalkLeg("Walk 1", departureTime1, geometry1, 1500.0, instructions1, pathDetailsMap1, arrivalTime1);

        //Deuxième Trajet à pied : 2500m, 15 minutes
        Date departureTime2 = new Date(601000L);
        Date arrivalTime2 = new Date(1501000L);
        //Géométrie pour le deuxième trajet
        Coordinate[] coordinates2 = new Coordinate[] {
            new Coordinate(-73.5600, 45.5050),
            new Coordinate(-73.5570, 45.5070),
            new Coordinate(-73.5540, 45.5087),
        };
        Geometry geometry2 = geometryFactory.createLineString(coordinates2);
        //Instructions pour le deuxième trajet
        InstructionList instructions2 = new InstructionList(mockTranslation);
        PointList instructionPointList2 = new PointList();
        instructionPointList2.add(45.5050, -73.5600);
        instructionPointList2.add(45.5070, -73.5570);
        instructionPointList2.add(45.5087, -73.5540);
        Instruction inst2 = new Instruction(Instruction.CONTINUE_ON_STREET, "Rue B", instructionPointList2);
        inst2.setDistance(2500.0);
        inst2.setTime(900000); // 15 minutes en ms
        instructions2.add(inst2);
        //Path Deatils pour le deuxième trajet
        Map<String, List<PathDetail>> pathDetailsMap2 = new HashMap<>();
        List<PathDetail> pathDetails2 = new ArrayList<>();
        PathDetail pd2 = new PathDetail(6.0); //6km/h
        pd2.setFirst(0);
        pd2.setLast(2);
        pathDetails2.add(pd2);
        pathDetailsMap2.put("vitesse normale", pathDetails2);

        Trip.WalkLeg walkLeg2 = new Trip.WalkLeg("Walk 2", departureTime2, geometry2, 2500.0, instructions2, pathDetailsMap2,arrivalTime2);

        //Liste des trajets à pied
        List<Trip.Leg> walkLegs = Arrays.asList(walkLeg1, walkLeg2);

        //Appel de la méthode à tester
        ResponsePath resultat = TripFromLabel.createResponsePath(mockGtfsStorage, mockTranslation, PointList, walkLegs);
        BigDecimal fare = resultat.getFare();
        Trip.WalkLeg firstLeg = (Trip.WalkLeg) resultat.getLegs().get(0);
        Trip.WalkLeg secondLeg = (Trip.WalkLeg) resultat.getLegs().get(1);

        //Oracle : vérifications des propriétés du ResponsePath et des WalkLegs
        assertNotNull(resultat);
        assertNotNull(resultat.getLegs());
        assertEquals(2, resultat.getLegs().size());
        assertTrue(resultat.getLegs().get(0) instanceof Trip.WalkLeg);
        assertTrue(resultat.getLegs().get(1) instanceof Trip.WalkLeg);
        assertEquals(4000.0, resultat.getDistance(), 0.001);
        assertEquals(1500000, resultat.getTime()); // 25 minutes en ms
        assertNotNull(resultat.getInstructions());
        assertTrue(resultat.getInstructions().size() >= 1); // Au moins une instruction (une par trajet)
        assertNotNull(resultat.getPoints());
        assertTrue(resultat.getPoints().size() >= 3); // Trois points dans le PointList
        assertTrue(fare == null || fare.compareTo(BigDecimal.ZERO) == 0); // Pas de tarif pour les trajets à pied
        assertEquals(1500.0, firstLeg.getDistance(), 0.001);
        assertEquals(2500.0, secondLeg.getDistance(), 0.001);

    }
}
